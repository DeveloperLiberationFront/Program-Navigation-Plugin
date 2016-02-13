package dataTool.ui;

import java.awt.MouseInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import dataTool.Finder;

public class NavigationDownBox {
	
	private static Display display;
	private static Shell shell;
	private static StyledText widget;
	private int offset;
	private static NavigationDownBox instance;
	private static boolean resize = false;
	
	private NavigationDownBox(StyledText text, int start) {
		offset = start;
		widget = text;
	}
	
	/**
	 * Creates singleton instance of the bottom UI box
	 * @param st: currently selected StyledText
	 * @param start: Offset of the current node
	 */
	public static void createInstance(StyledText st, int start) {
		if(instance == null) {
			instance = new NavigationDownBox(st, start);
			showLabel();
		}
	}
	
	/**
	 * Returns current instance of the top box
	 * @returns NavigationDownBox instance
	 */
	public static NavigationDownBox getInstance() {
		resize = false;
		return instance;
	}
	
	public static void showLabel() {
		if(shell != null) {
			dispose();
		}
		display = Display.getDefault();
	    shell = new Shell(display, SWT.ON_TOP);
	    shell.setLayout(new RowLayout());
	    setSize();
    	shell.open();
	}
	
	/**
	 * Method to set the size and position of the shell
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
	    shell.setLocation(comp.toDisplay(comp.getLocation()).x,comp.toDisplay(comp.getLocation()).y+(c.getClientArea().height-35)-15);
	}
	
	/**
	 * Sets the text for the bottom box of the user interface
	 * @param set: Set of methods called by current node
	 */
	public void setText(Set<IMethod> set) {
		for(Control c: shell.getChildren()) {
			c.dispose();
		}
		if(set != null) {
	    	for(IMethod i: set) {
	    		DataLink l = new DataLink(i, i.getElementName(), i.getPath().toString());
	    		Link link = new Link(shell, SWT.NULL);
		    	link.setText(l.getText());
		    	link.addListener(SWT.Selection, new Listener() {
		    		@Override
					public void handleEvent(Event arg0) {
		    			setText(null);
		    			NavigationUpBox.getInstance().setText(null);
						l.open(50);
					}			    	
		    	});
		    }
	    }
		shell.pack();
		setSize();
	}
	
	public static void dispose() {
		if(shell != null) {
			shell.dispose();
			instance = null;
		}
	}
}