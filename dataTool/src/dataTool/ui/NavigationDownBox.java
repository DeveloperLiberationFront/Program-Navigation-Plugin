package dataTool.ui;

import java.awt.MouseInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
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
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;

import dataTool.DataNode;
import dataTool.EnableNavigationAction;
import dataTool.Finder;
import dataTool.annotations.LinkAnnotation;

@Deprecated
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
	 * @throws JavaModelException 
	 */
	public void setText(Set<IMethod> set) throws JavaModelException {
		for(Control c: shell.getChildren()) {
			c.dispose();
		}
		if(set != null) {
	    	for(IMethod i: set) {
	    		Link link = new Link(shell, SWT.NULL);
		    	link.setText("<a>null</a>");
		    	link.addListener(SWT.Selection, new Listener() {
		    		@Override
					public void handleEvent(Event arg0) {
						link.setForeground(new Color(null, 128,0,128));
		    			try {
		    				if(i.getSource() == null) {
		    					JOptionPane.showMessageDialog(null, LinkAnnotation.INVALID, "Error",JOptionPane.ERROR_MESSAGE);
		    				}
		    				else {
								openLink(i);
								EnableNavigationAction plugin = new EnableNavigationAction();
				    			plugin.reset(null);
		    				}
						} catch (JavaModelException e1) {
							// Auto-generated catch block
							e1.printStackTrace();
						}
					}			    	
		    	});
		    }
	    }
		shell.pack();
		setSize();
	}
	
	/**
	 * Method to add text to navigation box for instances of data off-screen
	 * @param node: DataNode currently out of view
	 */
	public void addOffScreen(DataNode node, int line) {
		Link link = new Link(shell, SWT.NULL);
		link.setText("<a>line "+line+"</a>");
		link.addListener(SWT.Selection, new Listener(){

			@Override
			public void handleEvent(Event arg0) {
				link.setForeground(new Color(null, 128,0,128));
				IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		    	((ITextEditor) editor).selectAndReveal(node.getStartPosition(), node.getLength());
			}
			
		});
		shell.pack();
		setSize();
	}
	
	/**
	 * Opens declaration of new method in the editor and clears navigation box links
	 * @param i: IMethod to open
	 */
	public void openLink(IMethod i) {
		try {
			this.setText(null);
			NavigationUpBox.getInstance().setText(null);
			JavaUI.openInEditor(i, true, true);
		} catch (JavaModelException | PartInitException e) {
			// Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Removes the bottom navigation box from view.
	 */
	public static void dispose() {
		if(shell != null) {
			shell.dispose();
			instance = null;
		}
	}
}