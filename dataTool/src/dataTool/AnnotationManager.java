package dataTool;


import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;
import org.eclipse.ui.texteditor.AbstractTextEditor;

import dataTool.annotations.SuggestedSelectionAnnotation;
import edu.pdx.cs.multiview.jdt.util.JDTUtils;
import edu.pdx.cs.multiview.jface.annotation.AnnTransaction;
import edu.pdx.cs.multiview.jface.annotation.AnnotationPainter;
import edu.pdx.cs.multiview.util.eclipse.EclipseHacks;

public class AnnotationManager implements ISelectionChangedListener {
	
	private SuggestedSelectionAnnotation currentAnnotation = new SuggestedSelectionAnnotation();
	
	private AnnotationPainter painter;
	
	//the visitor for the editor 
	private StatementVisitor visitor;
	
	/**
	 * Creates an  annotation manager given an editor, containing a
	 * compilation unit, assumedly
	 * 
	 * @param anEditor
	 */
	public AnnotationManager(AbstractDecoratedTextEditor anEditor) {
		
		parseCU(anEditor);
		
		SourceViewer sourceViewer = EclipseHacks.getSourceViewer(anEditor);
		painter = new AnnotationPainter(sourceViewer);
		painter.addSelectionChangedListener(this);
		sourceViewer.addPainter(painter);
		
		selectionChanged((ITextSelection)painter.getSelection());
	}

	public void selectionChanged(ITextSelection selection) {
		//TODO: Don't allow all types of ASTNodes, only want vars and params!
		try {								
	
			ASTNode one = getNode(selection.getOffset(),true);
			
			if(selection.getLength()==0){
				addAnnotation(one, one);
				return;
			}
			
			ASTNode two = getNode(selection.getOffset()+selection.getLength(),false);
			
			if(one!=null){
				if(!areSiblings(one,two))			
					two = meet(one,two);
			
				if(two!=null)
					addAnnotation(one, two);
				
			}else{
				removeAnnotations();
			}
			
		} catch (Exception e) {
			Activator.logError(e);
			removeAnnotations();
		} 
	}

	private void addAnnotation(ASTNode one, ASTNode two) {
		int start = one.getStartPosition();
		int end = two.getStartPosition()+two.getLength();
		System.out.println(one.toString());
		System.out.println(one.getNodeType() + " type " + two.getNodeType());
		if(!isAlreadyAnnotated(start, end))
			addAnnotationsAt(start, end-start);
	}

	private boolean areSiblings(ASTNode one, ASTNode two) {
		
		return one.getParent().equals(two.getParent());
	}

	private ASTNode meet(ASTNode from, ASTNode to) {

		if(areSiblings(from,to))
			return to;
		
		if(to.getParent()==null)
			return null;
		
		return meet(from,to.getParent());
	}

	private boolean isAlreadyAnnotated(int start, int end) {
		
		Position headPosition = painter.getPosition(currentAnnotation);
		
		if(headPosition!=null)
			return headPosition.getOffset()==start && 
					headPosition.getOffset()+headPosition.getLength()==end;
		
		return false;
	}


	private ASTNode getNode(int position, boolean includeWhitespace) {
		return visitor.statementAt(position,includeWhitespace);
	}
	
	private void parseCU(AbstractTextEditor editor){
		try {
			visitor = parse(JDTUtils.getCUSource(editor));
		} catch (JavaModelException e) {
			Activator.logError(e);
		}
	}
	
	private static StatementVisitor parse(String source) throws JavaModelException{
		
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(source.toCharArray());
		CompilationUnit astRoot = (CompilationUnit) parser.createAST(null);
		
		StatementVisitor visitor = new StatementVisitor(source);			
		astRoot.accept(visitor);
		
		return visitor;
	}

	private void addAnnotationsAt(int statementStart, int length) {
		
		AnnTransaction anns = new AnnTransaction();
		anns.remove(currentAnnotation);
		anns.add(currentAnnotation, new Position(statementStart,length));
		
		painter.replaceAnnotations(anns);
	}
	
	public void removeAnnotations(){
		try{
			if(currentAnnotation!=null){
				painter.removeAnnotation(currentAnnotation);
			}
		}catch(Exception ignore){}
	}
	
	public void dispose() {
		painter.dispose();
	}

	public void selectionChanged(SelectionChangedEvent event) {
		selectionChanged((ITextSelection)event.getSelection());
	}
}
