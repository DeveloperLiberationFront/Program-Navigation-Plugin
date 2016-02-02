package dataTool.ui;

import java.awt.MouseInfo;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;

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

public class NavigationUpBox extends NavigationBox {
	
	private Shell shell;
	private StyledText widget;
	private int offset;
	private static NavigationUpBox instance;
	private static boolean resize = false;
	private ArrayList<String> links = new ArrayList<String>();
	
	private NavigationUpBox(StyledText text, int start) {
		super(text, SWT.BORDER);
		widget = text;
		offset = start;
	}
	
	public static NavigationUpBox getInstance(StyledText text, int start) {
		if(instance == null) {
			instance = new NavigationUpBox(text, start);
		}
		resize = false;
		return instance;
	}
	
	public void showLabel() {
		if(shell != null) {
			dispose();
		}
		Display display = Display.getDefault();
	    shell = new Shell(display, SWT.ON_TOP);
	    boolean mode = false;
	    shell.setLayout(new GridLayout());
	    setSize();
	    shell.open();
	  }
	
	private void setSize() {
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
    	
    	shell.setSize(comp.getClientArea().width-30, comp.getClientArea().height/5);
	    shell.setLocation(comp.toDisplay(comp.getLocation()).x,comp.toDisplay(comp.getLocation()).y);
	}

	public void setText(ArrayList<DataLink> text) {
		Finder finder = Finder.getInstance();
		Composite composite = new Composite(shell, SWT.NULL);
	    composite.setLayout(new RowLayout());
	    if(text != null) {
	    	for(DataLink l: text) {
	    		System.out.println(shell.getText());
		    	if(!links.contains(l.getName())) {
		    		links.add(l.getName());
		    		Link link = new Link(composite, SWT.WRAP);
			    	link.setText(l.getText());
			    	link.addListener(SWT.Selection, new Listener() {
			    		@Override
						public void handleEvent(Event arg0) {
							l.open(finder.getGoToIndex());
						}			    	
			    	});
			    	Label blank = new Label(composite, SWT.NULL);
				    shell.pack();
				    setSize();
		    	}
	    	}
	    }
	}

	public void dispose() {
		if(shell != null) {
			shell.dispose();
		}
	}
}