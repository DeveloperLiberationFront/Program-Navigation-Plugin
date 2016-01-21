package dataTool.ui;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import dataTool.Finder;

public class NavigationBox {
	public static int WIDTH = 60;
	public static int HEIGHT = 80;
	
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
	public void showLabel(Object text, boolean parameter) {
		Finder finder = Finder.getInstance();
		if(parameter) {
			HEIGHT = HEIGHT + (((ArrayList)text).size()*20);
			WIDTH = WIDTH*3;
		}
		if(finder.getFlowDirection().equals(Finder.UP)) {
			NavigationUpBox up = NavigationUpBox.getInstance(widget, offset);
			up.showLabel(text, parameter);
			currentBox = up;
		}
		else if(finder.getFlowDirection().equals(Finder.DOWN)) {
			NavigationDownBox down = NavigationDownBox.getInstance(widget, offset);
			down.showLabel(text, parameter);
			currentBox = down;
		}
		resetSize();
	}
	
	private void resetSize() {
		HEIGHT = 80;
		WIDTH = 60;
	}
	
	public void dispose() {
		if(currentBox != null) {
			currentBox.dispose();
		}
	}
}