package dataTool.annotations;

import java.util.ArrayList;
import java.util.Set;

import org.eclipse.jface.text.source.Annotation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;

import dataTool.DataCallHierarchy;
import dataTool.DataNode;
import dataTool.EnableNavigationAction;
import dataTool.Finder;
import dataTool.ui.NavigationDownBox;
import dataTool.ui.NavigationUpBox;
import edu.pdx.cs.multiview.jface.annotation.ISelfDrawingAnnotation;

public class LinkAnnotation extends Annotation implements ISelfDrawingAnnotation {
	
	private DataNode linkNode;
	
	public LinkAnnotation(DataNode node) {
		super();
		linkNode = node;
	}

	@Override
	public void draw(GC gc, StyledText textWidget, int offset, int length) {		
		NavigationStyleRange style = new NavigationStyleRange();
		style.start = offset;
		style.length = length;
		style.underline = true;
		style.underlineStyle = SWT.UNDERLINE_LINK;
		style.direction = linkNode.getType();
		textWidget.setStyleRange(style);
		textWidget.addMouseListener(new MouseListener(){

			@Override
			public void mouseDoubleClick(MouseEvent arg0) {
				mouseDown(arg0);
			}

			@Override
			public void mouseDown(MouseEvent arg0) {
				int click = textWidget.getOffsetAtLocation(new Point(arg0.x,arg0.y));
				if(click >= style.start && click <= style.start+style.length){
					DataCallHierarchy call = new DataCallHierarchy();
					Object[] search;
					IMethod im;
					if(linkNode.getType().equals(DataNode.PARAM_UP)) {
						try {
							search = call.searchProject(linkNode, DataNode.PARAM_UP).toArray();
							im = (IMethod)search[0];
							NavigationUpBox up = NavigationUpBox.getInstance();
							up.searchMethod = linkNode.getMethod().getName().getIdentifier();
							up.openLink(im);
						} catch (Exception e) {
							// Auto-generated catch block
							e.printStackTrace();
						}
					}
					else if(linkNode.getType().equals(DataNode.PARAM_DOWN)) {
						try {
							search = call.searchProject(linkNode, DataNode.PARAM_DOWN).toArray();
							for(Object o: search) {
								im = (IMethod) o;
								if(im.getElementName().equals(linkNode.getParameterMethod().getName().getIdentifier())) {
									NavigationDownBox.getInstance().openLink(im);
								}
							}
						} catch (Exception e) {
							// Auto-generated catch block
							e.printStackTrace();
						}
					}
					//EnableNavigationAction plugin = new EnableNavigationAction();
	    			//plugin.reset();
				}
			}

			@Override
			public void mouseUp(MouseEvent arg0) {
				// Do nothing
				
			}
			
		});
	}
	
	/**
	 * Removes the link annotation from the editor
	 * @param old: StyleRange for a link annotation to a method
	 * @returns StyleRange removing the link from method
	 */
	public static StyleRange removeAnnotation(StyleRange old) {
		NavigationStyleRange clear = new NavigationStyleRange();
		clear.start = old.start;
		clear.length = old.length;
		clear.underline = false;
		clear.direction = null;
		return clear;
	}

}
