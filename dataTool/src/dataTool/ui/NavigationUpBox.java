package dataTool.ui;

import java.awt.MouseInfo;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import dataTool.Finder;

public class NavigationUpBox extends NavigationBox {
	
	private Shell shell;
	private StyledText widget;
	private int offset;
	private static NavigationUpBox instance;
	
	private NavigationUpBox(StyledText text, int start) {
		super(text, SWT.BORDER);
		widget = text;
		offset = start;
	}
	
	public static NavigationUpBox getInstance(StyledText text, int start) {
		if(instance == null) {
			instance = new NavigationUpBox(text, start);
		}
		return instance;
	}
	public void showLabel(String text) {
		if(shell != null) {
			dispose();
		}
		Finder finder = Finder.getInstance();
		finder.setFlowDirection(Finder.UP);
		//Display display = Display.getDefault();
	    shell = new Shell();
	    boolean mode = false;
	    shell.setLayout(new FillLayout());
	    Composite composite = new Composite(shell, SWT.NULL);
	    composite.setLayout(new RowLayout());
	    Button upButton = new Button(composite, SWT.RADIO);
	    upButton.setText("Up");
	    upButton.setSelection(true);
	    Button downButton = new Button(composite, SWT.RADIO);
	    downButton.setText("Down");
	    downButton.setSelection(false);
	    downButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				upButton.setSelection(false);
				NavigationDownBox down = NavigationDownBox.getInstance(widget, offset);
				dispose();
				down.showLabel(text);
			}
	    });
	    Label label = new Label(composite, SWT.NONE);
	    label.setText(text);
	    shell.setSize(super.WIDTH, super.HEIGHT);
	    shell.setLocation((int)MouseInfo.getPointerInfo().getLocation().getX()-(super.WIDTH/2),(int)MouseInfo.getPointerInfo().getLocation().getY()-super.HEIGHT);
	    shell.open();
	  }
	
	public void dispose() {
		if(shell != null) {
			shell.dispose();
		}
	}
}