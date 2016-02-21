package dataTool;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.internal.ui.javaeditor.ShowDataInBreadcrumbAction;
import org.eclipse.jdt.internal.ui.javaeditor.breadcrumb.IBreadcrumb;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;
import org.eclipse.ui.texteditor.AbstractTextEditor;

import dataTool.annotations.LinkAnnotation;
import dataTool.annotations.ProgramNavigationPainter;
import dataTool.annotations.SuggestedSelectionAnnotation;
import edu.pdx.cs.multiview.jdt.util.JDTUtils;
import edu.pdx.cs.multiview.jface.annotation.AnnTransaction;
import edu.pdx.cs.multiview.jface.annotation.AnnotationPainter;
import edu.pdx.cs.multiview.jface.annotation.ISelfDrawingAnnotation;
import edu.pdx.cs.multiview.util.eclipse.EclipseHacks;

public class AnnotationManager implements ISelectionChangedListener {

	private SuggestedSelectionAnnotation highlightAnnotation = new SuggestedSelectionAnnotation();
	private LinkAnnotation linkAnnotation = new LinkAnnotation();
	private SourceViewer sourceViewer;
	private ProgramNavigationPainter painter;
	private static boolean isActive = false;
	private IBreadcrumb dataBreadcrumb;

	// the visitor for the editor
	private Visitor visitor;
	
	public static String currentSearch = null;

	/**
	 * Creates an annotation manager given an editor, containing a compilation
	 * unit, assumedly
	 * 
	 * @param anEditor
	 */
	public AnnotationManager(AbstractDecoratedTextEditor anEditor) {

		parseCU(anEditor);

		sourceViewer = EclipseHacks.getSourceViewer(anEditor);
		painter = new ProgramNavigationPainter(sourceViewer);
		painter.addSelectionChangedListener(this);
		sourceViewer.addPainter(painter);
		//isActive = false;
		selectionChanged((ITextSelection) painter.getSelection());
	}

	public void selectionChanged(ITextSelection selection) {
		painter.removeAllAnnotations();
		try {
			DataNode one = getNode(selection.getOffset());
			System.out.println(one);
			Finder finder = Finder.getInstance();
			if(one != null) {
				addAnnotation(one);
				currentSearch = one.getValue();
				System.out.println(isActive+" isActive");
				IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				IEditorPart activeEditor = activePage.getActiveEditor();
				System.out.println(activeEditor.getTitle()+" tidal");
				JavaEditor j = (JavaEditor) activeEditor;
				dataBreadcrumb = j.getBreadcrumb();
				if(!isActive) {
					isActive = true;
					ShowDataInBreadcrumbAction crumbs = new ShowDataInBreadcrumbAction(j, activePage);
					crumbs.run();	
				}
				DataCallHierarchy call = new DataCallHierarchy();
				Set<IMethod> searchUp = null;
				//Set<IMethod> searchDown = null;
				//System.out.println(one.getBinding()+" "+one.getDeclarationMethod() +" "+one.getInvocationMethod());
				if((finder.upSearch(one) != null || finder.downSearch(one) != null) && currentSearch != null) {
					searchUp = call.searchProject(one, Finder.UP);
					//searchDown = call.searchProject(one, Finder.DOWN);
					//System.out.println("search "+one.getValue()+" "+searchUp+" "+one.getDeclarationMethod());
					if(one.isParameterSelected(selection.getOffset())) {
						//linkAnnotation.searchResultsDown = searchDown;
						linkAnnotation.searchResultsUp = searchUp;
						linkAnnotation.setDataNode(one);
						addLinkAnnotation(one);
					}
				}
				Set<String> test = new HashSet<String>();
				test.add(one.getValue());
				dataBreadcrumb.setText(searchUp);
				//Adds all occurrences of data node off screen
				for(DataNode dn: finder.getOccurrences(one.getValue(), new Position(one.getStartPosition(), one.getLength()))) {
					if(dn.getStartPosition() < sourceViewer.getTopIndexStartOffset()) {
						int line = sourceViewer.widgetLineOfWidgetOffset(dn.getStartPosition())+1;
						//NavigationUpBox.getInstance().addOffScreen(dn, line);
					}
					else if(dn.getStartPosition() > sourceViewer.getBottomIndexEndOffset()) {
						int line = sourceViewer.widgetLineOfWidgetOffset(dn.getStartPosition())+1;
						//NavigationDownBox.getInstance().addOffScreen(dn, line);
					}
				}
			}
			else {
				removeAnnotations();
			}
		} catch (Exception e) {
			Activator.logError(e);
			removeAnnotations();
		}
	}

	/**
	 * Function that returns the method the current node is located in
	 * @param node: Current node selected by the user
	 * @returns String method name
	 */
	private String getMethod(DataNode one) {
		
		//TODO Function that could come in handy later to get current method mouse is clicked in
		/*ASTNode temp = one.;
		while(!(temp instanceof MethodDeclaration) && temp != null) {
			temp = temp.getParent();
		}
		if(temp == null) {
			return null;
		}
		return ((MethodDeclaration) temp).getName().getIdentifier();*/
		//System.out.println(one.getValue()+" get "+one.getMethod().getName());
		//System.out.println(one.getBinding());
		//if(one.getMethod() != null) {
			//return one.getMethod().getName().toString();
		//}
		return null;
	}
	
	private void addAnnotation(DataNode node) {
		int start = node.getStartPosition();
		int end = node.getStartPosition() + node.getLength();

		if (!isAlreadyAnnotated(start, end))
			addAnnotationsAt(start, end - start, true);
	}
	
	private void addLinkAnnotation(DataNode node) {
		SimpleName method;
		if(node.getDeclarationMethod() != null) {
			method = node.getDeclarationMethod().getName();
		}
		else {
			method = node.getInvocationMethod().getName();
		}
		int start = method.getStartPosition();
		int end = method.getStartPosition() + method.getLength();
		if(!isAlreadyAnnotated(start, end)) {
			addAnnotationsAt(start, end - start, false);
		}
	}

	private boolean areSiblings(ASTNode one, ASTNode two) {

		return one.getParent().equals(two.getParent());
	}

	private ASTNode meet(ASTNode from, ASTNode to) {

		if (areSiblings(from, to))
			return to;

		if (to.getParent() == null)
			return null;

		return meet(from, to.getParent());
	}

	private boolean isAlreadyAnnotated(int start, int end) {

		Position headPosition = painter.getPosition(highlightAnnotation);

		if (headPosition != null)
			return headPosition.getOffset() == start && headPosition.getOffset() + headPosition.getLength() == end;

		return false;
	}

	private DataNode getNode(int position) {
		return visitor.statementAt(position);
	}

	private void parseCU(AbstractTextEditor editor) {
		try {
			visitor = parse(JDTUtils.getCUSource(editor));
		} catch (JavaModelException e) {
			Activator.logError(e);
		}
	}

	private static Visitor parse(String source) throws JavaModelException {

		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(source.toCharArray());
		CompilationUnit astRoot = (CompilationUnit) parser.createAST(null);

		Visitor visitor = new Visitor(source);
		astRoot.accept(visitor);

		return visitor;
	}

	private void addAnnotationsAt(int statementStart, int length, boolean isHighlight) {

		AnnTransaction anns = new AnnTransaction();
		if(isHighlight) {
			anns.remove(highlightAnnotation);
			anns.add(highlightAnnotation, new Position(statementStart, length));
		}
		else {
			anns.remove(linkAnnotation);
			anns.add(linkAnnotation, new Position(statementStart, length));
		}
		painter.replaceAnnotations(anns);
	}

	public void removeAnnotations() {
		try {
			if (highlightAnnotation != null) {
				// painter.removeAnnotation(highlightAnnotation);
				painter.removeAllAnnotations();
			}
		} catch (Exception ignore) {
		}
	}

	public void dispose() {
		painter.dispose();
		//dataBreadcrumb.dispose();
		//isActive = false;
		currentSearch = null;
	}

	public void selectionChanged(SelectionChangedEvent event) {
		painter.removeAllAnnotations();
		selectionChanged((ITextSelection) event.getSelection());
	}
}
