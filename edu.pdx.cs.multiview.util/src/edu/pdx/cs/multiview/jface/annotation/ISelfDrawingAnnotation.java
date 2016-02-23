package edu.pdx.cs.multiview.jface.annotation;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.GC;

/**
 * I represent an annotation that knows how to draw itself.
 * 
 * @author emerson
 *
 */
public interface ISelfDrawingAnnotation {

	/**
	 * Draw this annotation.  This method is not intended to be called by clients; 
	 * it automatically called by the {@link AnnotationPainter} automatically.
	 * 
	 * Note that you should use offset and length for positional information only.
	 * You should not use out outside positional information, such as the {@link ASTNode}s,
	 * because their in-file positions may differ from their {@link StyledText} positions,
	 * due to folding and code editing.
	 * 
	 * @param gc
	 * @param textWidget
	 * @param offset
	 * @param length
	 */
	public void draw(GC gc, StyledText textWidget, int offset, int length);
}
