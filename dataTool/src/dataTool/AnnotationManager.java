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
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;
import org.eclipse.ui.texteditor.AbstractTextEditor;

import dataTool.annotations.LinkAnnotation;
import dataTool.annotations.ProgramNavigationPainter;
import dataTool.annotations.SuggestedSelectionAnnotation;
import dataTool.ui.NavigationDownBox;
import dataTool.ui.NavigationUpBox;
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
	private boolean isActive;

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
		isActive = false;
		selectionChanged((ITextSelection) painter.getSelection());
	}

	public void selectionChanged(ITextSelection selection) {
		painter.removeAllAnnotations();
		try {
			DataNode currentNode = getNode(selection.getOffset());
			//System.out.println(selection.getOffset());
			if(currentNode != null) {
				addAnnotation(currentNode);
				currentSearch = getMethod(currentNode);
				if(!isActive) {
					isActive = true;
					NavigationUpBox.createInstance(sourceViewer.getTextWidget(), currentNode.getStartPosition());
					NavigationDownBox.createInstance(sourceViewer.getTextWidget(), currentNode.getStartPosition());
				}
				DataCallHierarchy call = new DataCallHierarchy();
				Set<IMethod> searchUp = null;
				Set<IMethod> searchDown = null;
				if(Finder.param_map.containsKey(currentNode.getValue()) && currentSearch != null) {
					searchUp = call.searchProject(currentNode, DataNode.PARAM_UP);
					searchDown = call.searchProject(currentNode, DataNode.PARAM_DOWN);
					System.out.println(searchUp);
					System.out.println(searchDown);
				}
				if(NavigationDownBox.getInstance() != null && NavigationUpBox.getInstance() != null) {
					NavigationUpBox.getInstance().setText(searchUp);
					NavigationDownBox.getInstance().setText(searchDown);
				}
				// TODO Add all occurrences of data node off screen
				Finder finder = Finder.getInstance();
				for(DataNode dn: finder.getOccurrences(currentNode.getValue(), new Position(currentNode.getStartPosition(), currentNode.getLength()))) {
					if(dn.getStartPosition() < sourceViewer.getTopIndexStartOffset()) {
						int line = sourceViewer.widgetLineOfWidgetOffset(dn.getStartPosition())+1;
						NavigationUpBox.getInstance().addOffScreen(dn, line);
					}
					else if(dn.getStartPosition() > sourceViewer.getBottomIndexEndOffset()) {
						int line = sourceViewer.widgetLineOfWidgetOffset(dn.getStartPosition())+1;
						NavigationDownBox.getInstance().addOffScreen(dn, line);
					}
				}
				if (currentNode.isParameterSelected(selection.getOffset()) && currentNode.getMethod() != null) {
					linkAnnotation.searchResultsDown = searchDown;
					linkAnnotation.searchResultsUp = searchUp;
					linkAnnotation.setDataNode(currentNode);
					addLinkAnnotation(currentNode);
				}
			}
			else {
				removeAnnotations();
			}
		} catch (Exception e) {
			Activator.logError(e);
			if(isActive) {
				try {
					NavigationUpBox.getInstance().setText(null);
					NavigationDownBox.getInstance().setText(null);
				} catch (Exception e1) {
					// Auto-generated catch block
					//e.printStackTrace();
				}
			}
			removeAnnotations();
		}
	}

	/**
	 * Function that returns the method the current node is located in
	 * @param node: Current node selected by the user
	 * @returns String method name
	 */
	private String getMethod(DataNode one) {
		if(one.getMethod() != null) {
			return one.getMethod().toString();
		}
		return null;
	}
	
	private void addAnnotation(DataNode node) {
		int start = node.getStartPosition();
		int end = node.getStartPosition() + node.getLength();

		if (!isAlreadyAnnotated(start, end))
			addAnnotationsAt(start, end - start, true);
	}
	
	private void addLinkAnnotation(DataNode node) {
		SimpleName method = node.getMethod();
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
		//NavigationUpBox.dispose();
		//NavigationDownBox.dispose();
		isActive = false;
		currentSearch = null;
	}

	public void selectionChanged(SelectionChangedEvent event) {
		painter.removeAllAnnotations();
		selectionChanged((ITextSelection) event.getSelection());
	}
}
