package dataTool.annotations;

import org.eclipse.jface.text.source.Annotation;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

import edu.pdx.cs.multiview.jface.annotation.ISelfDrawingAnnotation;

/**
 * I am an annotation that suggests a bunch of lines 
 * should be selected
 * 
 * @author Chris
 */
public class SuggestedSelectionAnnotation extends Annotation implements ISelfDrawingAnnotation{

	public static final Color color = new Color(null,0,0,255);
	
	public void draw(GC gc, StyledText textWidget, int offset, int length) {	
		
		gc.setBackground(color);
		gc.setAlpha(50);

		Point lineStart = textWidget.getLocationAtOffset(offset);
		int nextLineOffset = textWidget.getOffsetAtLine(textWidget.getLineAtOffset(offset)+1);
		Point nextLineStart = textWidget.getLocationAtOffset(nextLineOffset);
		Point lastLineEnd = textWidget.getLocationAtOffset(offset+length);
		
		//a single line is highlighted...
		if(nextLineOffset>offset+length){
			gc.fillRectangle(lineStart.x,lineStart.y,lastLineEnd.x-lineStart.x,textWidget.getLineHeight());
			return;
		}
		
		//the first line is highlighted, and then some 
		gc.fillRectangle(lineStart.x,lineStart.y,textWidget.getBounds().width,textWidget.getLineHeight());
		
		//the last line is highlighted
		gc.fillRectangle(0,lastLineEnd.y,lastLineEnd.x,textWidget.getLineHeight());
		
		if(nextLineStart.y>=lastLineEnd.y)
			return;
		
		//the first, last, and some in between is highlighted 
		
		gc.fillRectangle(0,nextLineStart.y,textWidget.getBounds().width,lastLineEnd.y-nextLineStart.y);
	}
}