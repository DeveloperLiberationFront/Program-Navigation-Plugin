package dataTool;

import java.util.ArrayList;
import java.util.HashSet;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.swt.graphics.Color;

import dataTool.annotations.SuggestedSelectionAnnotation;

public class Finder {
	
	final public static String UP = "up";
	final public static String DOWN = "down";
	
	private static String findDirection = UP; //default direction is up
	private static Finder currentFinder;
	private static Color currentColor;
	
	public Finder () {
		//Do nothing
	}
	
	public Finder (String s) {
		if (s.equals(UP) || s.equals(DOWN))
			setFlowDirection(s);
	}
	
	public void initialize(HashSet<String> list, String sourceCode) {
		DownFinder down = DownFinder.getInstance();
		UpFinder up = UpFinder.getInstance();
		//up.searchClassUp(sourceCode);
		down.searchClassDown(list, sourceCode);
	}
	
	public ArrayList<DataNode> getOccurrences(String data) {
		if(findDirection.equals(UP)) {
			UpFinder finder = UpFinder.getInstance();
			return finder.getUpOccurrences(data);
		}
		else if(findDirection.equals(DOWN)) {
			DownFinder finder = DownFinder.getInstance();
			return finder.getDownOccurrences(data);
		}
		return null;
	}
	
	/**
	 * Controls which flow the tool will navigate to show data flow
	 * @param s: Direction for flow display, required to be UP or DOWN
	 */
	public void setFlowDirection(String s) {
		if(s.equals(UP)) {
			findDirection = s;
			currentFinder = UpFinder.getInstance();
			SuggestedSelectionAnnotation.color = new Color(null, 0, 0, 255);
		}
		else if(s.equals(DOWN)) {
			findDirection = s;
			currentFinder = DownFinder.getInstance();
			SuggestedSelectionAnnotation.color = new Color(null, 255, 0, 0);
		}
		//else something went very wrong...
	}
	
	/**
	 * Function to return the current direction of the flow navigation.
	 */
	public String getFlowDirection() {
		return findDirection;
	}
	
	public boolean contains(String str) {
		return currentFinder.contains(str);
	}
	
	/**
	 * Function to get the finder instance
	 * @return- Returns the current finder instance searching in the appropriate direction.
	 */
	public static Finder getInstance() {
		if(findDirection.equals(UP)) {
			return UpFinder.getInstance();
		}
		else {
			return DownFinder.getInstance();
		}	
	}
}
