package edu.pdx.cs.multiview.jface.dialog;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;

/**
 * 
 * This class was designed to show a progress monitor on top of an 
 * editor.  This class is only PARTIALLY IMPLEMENTED.
 * 
 * @author emerson
 */
public class EditorProgressDialog{

	private int size = 100;
	private AbstractDecoratedTextEditor editor;
	private Shell window;
	private Drawer d;
	
	private Color blue = new Color(null,0,0,255);
	private Color green = new Color(null,0,255,0);

	public EditorProgressDialog(AbstractDecoratedTextEditor editor) {
		this.editor = editor;		
	}
	
	public void open(){
		
		StyledText st = (StyledText)editor.getAdapter(Control.class);		
		window = new Shell(st.getDisplay(),SWT.NO_TRIM | SWT.ON_TOP);
		
		window.setLayout(new FillLayout());
		
		d = new Drawer(window,SWT.NONE);		
		d.setBackground(green);
		
		Point pt = st.toDisplay(st.getSize().x, st.getSize().y);
				
		window.setSize(size,size);
		window.setLocation(pt.x-size,pt.y-size);
		
		d.addPaintListener(d);
		window.addPaintListener(d);
		window.setVisible(true);
	}

	public void close() {
		window.dispose();
	}	
	
	public int getProgress(){
		return (int)(Math.random()*size);
	}
	
	private class Drawer extends Canvas implements PaintListener{

		public Drawer(Composite parent, int style) {
			super(parent, style);
		}
		
		public void paintControl(PaintEvent e) {
			GC gc = e.gc;
			gc.setForeground(green);
			System.out.println("bloo");
			gc.drawRectangle(getProgress(), getProgress(), getProgress(), getProgress());
		}
	}
	
	public IProgressMonitor getProgressMonitor(){
		return new IProgressMonitor(){

			public void beginTask(String name, int totalWork) {}

			public void done() {}

			public void internalWorked(double work) {
				System.out.println("w");
//				window.redraw();
//				d.redraw();
				window.setVisible(true);
				d.setVisible(true);
			}

			public boolean isCanceled() {
				return false;
			}

			public void setCanceled(boolean value) {}

			public void setTaskName(String name) {}

			public void subTask(String name) {}

			public void worked(int work) {
				window.pack();
			}};
	}
}
