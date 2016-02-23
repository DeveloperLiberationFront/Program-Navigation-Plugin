package edu.pdx.cs.multiview.jface.annotation;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;

import edu.pdx.cs.multiview.util.eclipse.EclipseHacks;

/**
 * I do just a bit of stuff - perhaps I am no longerneeded?
 * 
 * @author emerson
 *
 */
public class ShellAnnotationManager{

	private ControlAnnotation onlyPopup;
	private AbstractDecoratedTextEditor editor;
	private AnnotationPainter painter;
	
	private Thread painterGetter;

	
	public void initEditor(AbstractDecoratedTextEditor editor){
		this.editor = editor;
		painterGetter = new Thread(){
			public void run(){
				painter = new AnnotationPainter(EclipseHacks.getSourceViewer(getEditor()));
			}
		};
		Display.getDefault().asyncExec(painterGetter);
	}
	
	public AbstractDecoratedTextEditor getEditor(){
		return this.editor;
	}
	
	protected AnnotationPainter getAnnotationModel() {
		
		if(painterGetter.isAlive())
			try {
				painterGetter.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		
		return painter;
	}
	
	public void setPopup(ControlAnnotation annotation){
		if(onlyPopup!=null)
			onlyPopup.deactivate();
		onlyPopup = annotation;
		onlyPopup.activate();
	}
}
