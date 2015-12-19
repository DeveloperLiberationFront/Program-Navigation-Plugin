package edu.pdx.cs.multiview.jface.annotation;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

import edu.pdx.cs.multiview.util.editor.AnnotationUtils;

public class Highlight implements ISelfDrawingAnnotation {
	
	public static final int ALPHA_LEVEL = 50;
	
	private final Color c;
	private boolean emphasize;
	private static final Color BLACK = Display.getCurrent().getSystemColor(SWT.COLOR_BLACK);

	public Highlight(Color c) {
		this.c = c;
	}

	public void draw(GC gc, StyledText textWidget, int offset, int length) {
		Rectangle bounds = AnnotationUtils.getBounds(textWidget, offset, length);
		gc.setAlpha(ALPHA_LEVEL);
		gc.setBackground(c);
		gc.fillRectangle(bounds);
		
		if(emphasize){
			gc.setAlpha(255);
			gc.setForeground(BLACK);
			gc.setLineWidth(2);
			gc.drawRectangle(bounds);
		}
	}
	
	public boolean isEmphasized(){
		return emphasize;
	}
	
	public void setEmphasized(boolean b){
		emphasize = b;
	}
}
