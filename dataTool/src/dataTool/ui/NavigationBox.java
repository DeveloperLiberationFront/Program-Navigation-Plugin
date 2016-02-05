package dataTool.ui;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import dataTool.Finder;

public class NavigationBox {
	public static int WIDTH = 600;
	public static int HEIGHT = 150;
	
	private Shell shell;
	private StyledText widget;
	private int offset;
	private NavigationBox currentBox;
	
	public NavigationBox(StyledText text, int start) {
		widget = text;
		offset = start;
	}
	
	public NavigationBox() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Displays NavigationBox based on the user's current direction
	 */
	public void showLabels() {
		NavigationUpBox up = NavigationUpBox.getInstance(widget, offset);
		up.showLabel();
		NavigationDownBox down = NavigationDownBox.getInstance(widget, offset);
		down.showLabel();
	}
	
	public void dispose() {
		if(currentBox != null) {
			currentBox.dispose();
		}
	}

	public void setText(HashMap<String, ArrayList<DataLink>> map) {
		NavigationUpBox up = NavigationUpBox.getInstance(null, -1);
		NavigationDownBox down = NavigationDownBox.getInstance(null, -1);
		up.setText(map.get(Finder.UP));
		down.setText(map.get(Finder.DOWN));
		System.out.println(map.get(Finder.DOWN));		
	}
}