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
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditorBreadcrumb;
import org.eclipse.jdt.internal.ui.javaeditor.breadcrumb.EditorBreadcrumb;
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
import dataTool.ui.ShowDataInBreadcrumbAction;
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
	private IBreadcrumb upBreadcrumb;
	private IBreadcrumb downBreadcrumb;

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
		selectionChanged((ITextSelection) painter.getSelection());
	}
	
	/**
	 * Handles updating the annotations and searching when user clicks a variable
	 * @param selection: current text selected
	 */
	public void selectionChanged(ITextSelection selection) {
		painter.removeAllAnnotations();
		try {
			DataNode one = getNode(selection.getOffset());
			Finder finder = Finder.getInstance();
			if(one != null) {
				addAnnotation(one);
				currentSearch = one.getBinding();
				IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				IEditorPart activeEditor = activePage.getActiveEditor();
				JavaEditor j = (JavaEditor) activeEditor;
				upBreadcrumb = j.getBreadcrumb();
				downBreadcrumb = j.getBreadcrumb2();
				if(!isActive) {
					isActive = true;
					upBreadcrumb.setText(null);
					downBreadcrumb.setText(null);
					ShowDataInBreadcrumbAction crumbs = new ShowDataInBreadcrumbAction(j, activePage);
					crumbs.run();	
				}
				DataCallHierarchy call = new DataCallHierarchy();
				Set<IMethod> searchUp = null;
				Set<IMethod> searchDown = null;
				if((finder.upSearch(one) != null || finder.downSearch(one) != null || one.getInvocationMethod() != null) && currentSearch != null) {
					searchUp = call.searchProject(one, Finder.UP);
					searchDown = call.searchProject(one, Finder.DOWN);
					if(one.isParameterSelected(selection.getOffset())) {
						linkAnnotation.searchResultsDown = searchDown;
						linkAnnotation.searchResultsUp = searchUp;
						linkAnnotation.setDataNode(one);
						addLinkAnnotation(one);
					}
				}

				//Adds all occurrences of data node off screen
				ArrayList<Object> textUp = new ArrayList<Object>();
				ArrayList<Object> textDown = new ArrayList<Object>();
				for(DataNode dn: finder.getOccurrences(one.getValue(), new Position(one.getStartPosition(), one.getLength()))) {
					int[] offScreen = new int[3];
					int line = sourceViewer.widgetLineOfWidgetOffset(dn.getStartPosition())+1;
					offScreen[0] = line;
					offScreen[1] = dn.getStartPosition();
					offScreen[2] = dn.getLength();
					if(dn.getStartPosition() < sourceViewer.getTopIndexStartOffset()) {
						textUp.add(offScreen);
					}
					else if(dn.getStartPosition() > sourceViewer.getBottomIndexEndOffset()) {
						textDown.add(offScreen);
					}
				}
				if(searchUp != null) {
					textUp.addAll(searchUp);
					((EditorBreadcrumb)upBreadcrumb).setSearchMethod(call.getCurrentMethod(one.getStartPosition()));
					((EditorBreadcrumb)upBreadcrumb).setSearchIndex(one.getParameterIndex());
				}
				if(searchDown != null) {
					textDown.addAll(searchDown);
					((EditorBreadcrumb)downBreadcrumb).setSearchIndex(one.getParameterIndex());
				}
				upBreadcrumb.setText(textUp);
				downBreadcrumb.setText(textDown);
			}
			else {
				removeAnnotations();
			}
		} catch (Exception e) {
			Activator.logError(e);
			removeAnnotations();
		}
	}
	
	private void addAnnotation(DataNode node) {
		int start = node.getStartPosition();
		int end = node.getStartPosition() + node.getLength();

		if (!isAlreadyAnnotated(start, end))
			addAnnotationsAt(start, end - start, true);
	}
	
	private void addLinkAnnotation(DataNode node) {
		SimpleName method;
		if(node.getInvocationMethod() != null) {
			method = node.getInvocationMethod().getName();
		}
		else {
			method = node.getDeclarationMethod().getName();
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
			if (highlightAnnotation != null || linkAnnotation != null) {
				painter.removeAllAnnotations();
			}
		} catch (Exception ignore) {
		}
	}

	public void dispose() {
		painter.dispose();
		currentSearch = null;
	}
	
	public void deactivate() {
		isActive = false;
		upBreadcrumb.dispose();
		downBreadcrumb.dispose();
	}

	public void selectionChanged(SelectionChangedEvent event) {
		selectionChanged((ITextSelection) event.getSelection());
	}
}
