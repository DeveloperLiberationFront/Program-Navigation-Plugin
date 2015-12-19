package edu.pdx.cs.multiview.util.editor;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

public class AnnotationUtils {

	public static void drawOutline(GC gc, StyledText textWidget, List<SimpleName> names) {
	
		for(SimpleName name : names)
			drawOutline(gc, textWidget, name);
	}

	public static Rectangle drawOutline(GC gc, StyledText textWidget, ASTNode node) {
		return drawOutline(gc,textWidget,node.getStartPosition(),node.getLength());
	}
	
	public static Rectangle drawOutline(GC gc, StyledText textWidget, int start, int length) {
		Rectangle rec = getBounds(textWidget, start, length);
		gc.drawRectangle(rec.x,rec.y,rec.width,rec.height);
		return rec;
	}

	public static Rectangle getBounds(StyledText textWidget, int offset, int length) {
		//TODO: GETTEXTBOUNDS??? - what I've been looking for!
		Rectangle bounds;
		if (length > 0)
			bounds= textWidget.getTextBounds(offset, offset + length - 1);
		else {
			Point loc= textWidget.getLocationAtOffset(offset);
			bounds= new Rectangle(loc.x, loc.y, 1, textWidget.getLineHeight(offset));
		}
		
		//this is necessary to fudge because the rectangle won't be in the bounding box
		//when drawn by IDrawingStrategy
		bounds.height -= 1;
		
		return bounds;
	}
	
	

	public static void fill(GC gc, StyledText textWidget, List<SimpleName> nodes){
		for(SimpleName name : nodes)
			fill(gc, textWidget, name);
	}

	public static void fill(GC gc, StyledText textWidget, ASTNode node) {
		fill(gc, textWidget, node.getStartPosition(), node.getLength());
	}

	public static void fill(GC gc, StyledText textWidget, int startPosition, int length) {
		Rectangle rec = getBounds(textWidget, 	startPosition, 
												length);
		gc.fillRectangle(rec);
	}

	public static void drawText(GC gc, StyledText textWidget, List<SimpleName> nodes) {
		for(SimpleName node : nodes)
			drawText(gc, textWidget, node);
	}

	public static void drawText(GC gc, StyledText textWidget, SimpleName node) {
		drawText(gc, textWidget, node.getIdentifier(), node.getStartPosition(), node.getLength());
	}

	public static void drawText(GC gc, StyledText textWidget, String text, int startPosition, int length) {
		Rectangle rec = getBounds(textWidget, 	startPosition, 
												length);
		gc.drawString(text, rec.x, rec.y+1, true);
	}

	public static void drawTacha(GC gc, Point startPoint, Point endPoint) {
	
		Point tachaCenter = new Point(	(startPoint.x + endPoint.x)/2 , 
										(startPoint.y + endPoint.y)/2);
		drawTacha(gc,tachaCenter);
	}
	
	public static void drawTacha(GC gc, Point point) {
		
		int oldWidth = gc.getLineWidth();
		gc.setLineWidth(2);
		int size = 10;
		gc.drawLine(point.x - size, point.y - size, point.x + size, point.y + size);
		gc.drawLine(point.x - size, point.y + size, point.x + size, point.y - size);
		
		gc.setLineWidth(oldWidth);
	}

	public static Point anchorOf(ASTNode node, StyledText textWidget, int flags){
		return anchorOf(textWidget, flags, node.getStartPosition(), node.getLength());
	}

	public static Point anchorOf(StyledText textWidget, int flags, int startPosition, int length) {
		
		Rectangle r = textWidget.getTextBounds(startPosition, startPosition+length);
		return anchorOf(flags, r);
	}

	public static Point anchorOf(int flags, Rectangle r) {
		
		Point p = new Point(r.x, r.y); 
		
		if((flags & SWT.CENTER) == SWT.CENTER){
			p.x = r.x + r.width/2;
			p.y = r.y + r.height/2;
		}
		
		if((flags & SWT.BOTTOM) == SWT.BOTTOM)
			p.y = r.y + r.height;
		
		if((flags & SWT.TOP) == SWT.TOP)
			p.y = r.y;
		
		if((flags & SWT.LEFT) == SWT.LEFT)
			p.x = r.x;
		
		if((flags & SWT.RIGHT) == SWT.RIGHT)
			p.x = r.x + r.width;
		
		return p;
	}
	
	

}
