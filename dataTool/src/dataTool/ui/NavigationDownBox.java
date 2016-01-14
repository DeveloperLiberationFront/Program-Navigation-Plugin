package dataTool.ui;

import java.awt.MouseInfo;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import dataTool.Finder;

public class NavigationDownBox extends NavigationBox {
	private Shell shell;
	private StyledText widget;
	private int offset;
	private static NavigationDownBox instance;
	
	private NavigationDownBox(StyledText text, int start) {
		super(text, SWT.BORDER);
		offset = start;
		widget = text;
	}
	
	public static NavigationDownBox getInstance(StyledText text, int start) {
		if(instance == null) {
			instance = new NavigationDownBox(text, start);
		}
		return instance;
	}
	
	public void showLabel() {
		if(shell != null) {
			dispose();
		}
		Finder finder = Finder.getInstance();
		finder.setFlowDirection(Finder.DOWN);
		Display display = Display.getDefault();
	    shell = new Shell();
	    shell.setLayout(new FillLayout());
	    Composite composite = new Composite(shell, SWT.NULL);
	    composite.setLayout(new RowLayout());
	    Button upButton = new Button(composite, SWT.RADIO);
	    upButton.setText("Up");
	    upButton.setSelection(false);
	    Button downButton = new Button(composite, SWT.RADIO);
	    downButton.setText("Down");
	    downButton.setSelection(true);
	    upButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				downButton.setSelection(false);
				NavigationUpBox up = NavigationUpBox.getInstance(widget, offset);
				dispose();
				up.showLabel();
			}	
	    });
	    
	    Label label = new Label(composite, SWT.NONE);
	    label.setText("test down");
	    shell.setSize(super.WIDTH, super.HEIGHT);
	    shell.setLocation((int)MouseInfo.getPointerInfo().getLocation().getX()-(super.WIDTH/2),(int)MouseInfo.getPointerInfo().getLocation().getY());
	    shell.open();
	}
	
	public void dispose() {
		if(shell != null) {
			shell.dispose();
		}
	}
}
