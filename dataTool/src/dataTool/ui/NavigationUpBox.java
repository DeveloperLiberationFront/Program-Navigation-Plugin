package dataTool.ui;

import java.awt.MouseInfo;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import dataTool.Finder;

public class NavigationUpBox {
	
	private static Display display;
	private static Shell shell;
	private static StyledText widget;
	private static Label label;
	private static Composite composite;
	private int offset;
	private static NavigationUpBox instance;
	private static boolean resize = false;
	private ArrayList<String> links = new ArrayList<String>();
	
	private NavigationUpBox(StyledText text, int start) {
		widget = text;
		offset = start;
	}
	
	/**
	 * Creates singleton instance of the top UI box
	 * @param st: currently selected StyledText
	 * @param start: Offset of the current node
	 */
	public static void createInstance(StyledText st, int start) {
		if(instance == null) {
			instance = new NavigationUpBox(st, start);
			showLabel();
		}
	}
	
	/**
	 * Returns current instance of the top box
	 * @returns NavigationUpBox instance
	 */
	public static NavigationUpBox getInstance() {
		resize = false;
		return instance;
	}
	
	public static void showLabel() {
		if(shell != null) {
			dispose();
		}
		display = Display.getDefault();
	    shell = new Shell(display, SWT.ON_TOP);
	    label = new Label(shell, SWT.NULL);
	    composite = new Composite(shell, SWT.NULL);
	    shell.setLayout(new GridLayout());
	    setSize();
	    shell.open();
	  }
	
	/**
	 * Method to set the size and location of the shell
	 */
	private static void setSize() {
		Canvas c = (Canvas) widget.getParent();
    	Composite comp = c.getParent();
    	comp.addListener(SWT.Resize, new Listener() {

			@Override
			public void handleEvent(Event arg0) {
				if(!resize) {
					resize = true;
					showLabel();
				}
			}
    	});
    	
    	shell.setSize(comp.getClientArea().width-30, 35);
	    shell.setLocation(comp.toDisplay(comp.getLocation()).x,comp.toDisplay(comp.getLocation()).y);
	}

	public void setText(Set<IMethod> set) {
		if(set != null) {
	    	for(IMethod i: set) {
	    		DataLink l = new DataLink(i, i.getElementName(), i.getPath().toString());
		    	if(!links.contains(l.getName())) {
		    		links.add(l.getName());
		    		Link link = new Link(composite, SWT.NULL);
			    	link.setText(l.getText());
			    	link.addListener(SWT.Selection, new Listener() {
			    		@Override
						public void handleEvent(Event arg0) {
							l.open(10);
						}			    	
			    	});
		    	}
	    	}
	    }
		composite.redraw();
		composite.update();
		shell.layout();
	}
	
	public void setText(String text) {
		if(text != null) {
	    	label.setText(text);
	    }
	    else {
	    	label.setText("");
	    }
	    label.redraw();
	    label.update();
	    shell.layout();
	}

	public static void dispose() {
		if(shell != null) {
			shell.dispose();
			instance = null;
		}
	}
}