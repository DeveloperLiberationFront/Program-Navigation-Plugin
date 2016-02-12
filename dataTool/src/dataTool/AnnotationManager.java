package dataTool;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;
import org.eclipse.ui.texteditor.AbstractTextEditor;

import dataTool.annotations.ProgramNavigationPainter;
import dataTool.annotations.SuggestedSelectionAnnotation;
import dataTool.ui.NavigationDownBox;
import dataTool.ui.NavigationUpBox;
import edu.pdx.cs.multiview.jdt.util.JDTUtils;
import edu.pdx.cs.multiview.jface.annotation.AnnTransaction;
import edu.pdx.cs.multiview.jface.annotation.AnnotationPainter;
import edu.pdx.cs.multiview.util.eclipse.EclipseHacks;

public class AnnotationManager implements ISelectionChangedListener {

	private SuggestedSelectionAnnotation currentAnnotation = new SuggestedSelectionAnnotation();
	private SourceViewer sourceViewer;
	private AnnotationPainter painter;
	private boolean isActive;

	// the visitor for the editor
	private Visitor visitor;

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

	public void selectionChanged(ITextSelection selection) {
		painter.removeAllAnnotations();
		try {
			ASTNode one = getNode(selection.getOffset());
			if(one != null) {
				addAnnotation(one);
				if(!isActive) {
					NavigationUpBox.createInstance(sourceViewer.getTextWidget(), one.getStartPosition());
					NavigationDownBox.createInstance(sourceViewer.getTextWidget(), one.getStartPosition());
				}
				//TODO Perform project search here instead of in ProgramNavigationPainter
				NavigationUpBox.getInstance().setText(one.toString()+" up");
				NavigationDownBox.getInstance().setText(one.toString()+" down");
			}
			else {
				removeAnnotations();
			}
		} catch (Exception e) {
			Activator.logError(e);
			if(isActive) {
				NavigationUpBox.getInstance().setText("");
				NavigationDownBox.getInstance().setText("");
			}
			removeAnnotations();
		}
	}

	/**
	 * Function that returns the method the current node is located in
	 * @param node: Current node selected by the user
	 * @returns String method name
	 */
	private String getMethod(ASTNode node) {
		// TODO Function that could come in handy later to get current method mouse is clicked in
		ASTNode temp = node;
		while(!(temp instanceof MethodDeclaration) && temp != null) {
			temp = temp.getParent();
		}
		if(temp == null) {
			return null;
		}
		return ((MethodDeclaration) temp).getName().getIdentifier();
	}
	
	private void addAnnotation(ASTNode one) {
		int start = one.getStartPosition();
		int end = one.getStartPosition() + one.getLength();

		if (!isAlreadyAnnotated(start, end))
			addAnnotationsAt(start, end - start);
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

		Position headPosition = painter.getPosition(currentAnnotation);

		if (headPosition != null)
			return headPosition.getOffset() == start && headPosition.getOffset() + headPosition.getLength() == end;

		return false;
	}

	private ASTNode getNode(int position) {
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

	private void addAnnotationsAt(int statementStart, int length) {

		AnnTransaction anns = new AnnTransaction();
		anns.remove(currentAnnotation);
		anns.add(currentAnnotation, new Position(statementStart, length));

		painter.replaceAnnotations(anns);
	}

	public void removeAnnotations() {
		try {
			if (currentAnnotation != null) {
				// painter.removeAnnotation(currentAnnotation);
				painter.removeAllAnnotations();
			}
		} catch (Exception ignore) {
		}
	}

	public void dispose() {
		painter.dispose();
		NavigationUpBox.dispose();
		NavigationDownBox.dispose();
		isActive = false;
	}

	public void selectionChanged(SelectionChangedEvent event) {
		painter.removeAllAnnotations();
		selectionChanged((ITextSelection) event.getSelection());
	}
}
