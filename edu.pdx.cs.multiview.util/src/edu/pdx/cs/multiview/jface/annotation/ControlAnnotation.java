package edu.pdx.cs.multiview.jface.annotation;

import org.eclipse.jface.text.source.Annotation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;


/**
 * I am an  annotation that has an attached {@link Control}
 * 
 * @author emerson
 *
 */
public abstract class ControlAnnotation extends Annotation implements ISelfDrawingAnnotation{
	
	private Control control;
	protected StyledText textWidget;
		
	private VisibilityListener offscreenListener = new VisibilityListener();

	private int offset, length;
	
	/**
	 * Sets the text widget I am drawn on
	 * 
	 * @param widget
	 */
	private void setStyledText(StyledText widget){
		if(textWidget!=widget){
			if(textWidget!=null && !textWidget.isDisposed())
				textWidget.removeListener(SWT.Paint, offscreenListener);
			textWidget = widget;
			textWidget.addListener(SWT.Paint, offscreenListener);
			textWidget.addListener(SWT.Hide, offscreenListener);
			textWidget.addListener(SWT.Dispose, new Listener(){
				public void handleEvent(Event event) {
					textWidget.removeListener(SWT.Dispose, this);
					dispose();
				}
			});
			init();
		}
	}
	
	protected abstract Control initControl();
	
	protected abstract void disposeControl(Control c);
	
	private void init() {
		if(control!=null)
			disposeControl(control);
		
		control = initControl();
		
		//TODO: we have a serious issue on how to detect when a text widget is no longer visible
		//		in the general case...
	}
	
	public void draw(StyledText widget, Rectangle bounds, int offset, int length) {

		setStyledText(widget);

		if(control.getBounds().equals(bounds))
			return;
		
		control.setBounds(bounds);	
		this.offset = offset;
		this.length = length;
	}
	
	public void draw(StyledText widget, Point upperRight, int offset, int length){
		
		setStyledText(widget);
		
		Rectangle labelBounds = control.getBounds();
		labelBounds.x = upperRight.x - labelBounds.width;
		labelBounds.y = upperRight.y;
		draw(widget,labelBounds,offset,length);
	}
	
	/*
	 * Dispose of my shell and region 
	 */
	public void dispose(){
		
		if(control!=null){
			disposeControl(control);
		}
		
		if(textWidget!=null){
			textWidget.removeListener(SWT.Paint, offscreenListener);
			textWidget.addListener(SWT.Hide, offscreenListener);
		}
	}

	public void deactivate() {

		if(isActive()){
			isActive = false;
			setVisibleLater(isActive);
		}
	}

	public void activate() {
		if(!isActive()){
			isActive = true;
			setVisibleLater(isActive);
		}
	}
	
	private boolean isActive = false;
	
	public boolean isActive(){
		return isActive;
	}
	
	private void setVisibleLater(final boolean visible) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
					if(control!=null && !control.isDisposed())
						control.setVisible(visible);//don't call open -  it'll change focus!
				}
			});		
	}
	
	/*
	 * Assures that the shell disappears if the annotation has
	 * scrolled off page
	 */
	private final class VisibilityListener implements Listener {
		public void handleEvent(Event event) {
			try {

				if(textWidget==null){
					dispose();
					return;
				}
				
				if(textWidget.isDisposed() || !textWidget.isVisible()){
					control.setVisible(false);
					return;
				}
				
				Rectangle bounds = textWidget.getTextBounds(offset, offset+length);
				
				//TODO: I don't think client area is quite right
				if(textWidget.getClientArea().intersects(bounds) && isActive())
					control.setVisible(true);
				else
					control.setVisible(false);
				
			} catch (Exception e) {
				System.err.println(e.getMessage());
			}
		}
	}
}