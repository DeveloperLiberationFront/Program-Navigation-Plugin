package dataTool.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import dataTool.Finder;

public class NavigationBox {
	final public static int WIDTH = 90;
	final public static int HEIGHT = 180;
	
	private Shell shell;
	private StyledText widget;
	private int offset;
	private NavigationBox currentBox;
	
	public NavigationBox(StyledText text, int start) {
		widget = text;
		offset = start;
	}
	
	/**
	 * Displays NavigationBox based on the user's current direction
	 */
	public void showLabel() {
		Finder finder = Finder.getInstance();
		if(finder.getFlowDirection().equals(Finder.UP)) {
			currentBox = NavigationUpBox.getInstance(widget, offset);
			currentBox.showLabel();
		}
		else if(finder.getFlowDirection().equals(Finder.DOWN)) {
			currentBox = NavigationDownBox.getInstance(widget, offset);
			currentBox.showLabel();
		}
		else {
			//Something went very wrong
		}
	}
	
	public void dispose() {
		if(currentBox != null) {
			currentBox.dispose();
		}
	}
}
