package dataTool;

import java.util.ArrayList;
import java.util.HashSet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;

import dataTool.annotations.SuggestedSelectionAnnotation;

public class Finder {
	
	final public static String UP = "up";
	final public static String DOWN = "down";
	
	private static Finder currentFinder;
	private static Color currentColor;
	private String goToName = null;
	private int goToOffset = -1;
	
	public Finder () {
		//Do nothing
	}
	
	public void setGoToIndex(int offset) {
		goToOffset = offset;
	}
	
	public void setGoToFunc(String name) {
		goToName = name;
	}
	
	public int getGoToIndex() {
		if(goToOffset > 0) {
			return goToOffset;
		}
		return -1;
	}
		
	public String getGoToFunc() {
		if(goToName != null) {
			return goToName;
		}
		return null;
	}
	
	/**
	 * Returns the occurrences of the selected string in the class
	 * @param data: current String value
	 * @returns ArrayList of DataNodes for string
	 */
	public ArrayList<DataNode> getOccurrences(String data) {
		ArrayList<DataNode> occurrences = new ArrayList<DataNode>();
		occurrences.addAll(UpFinder.getInstance().getUpOccurrences(data));
		occurrences.addAll(DownFinder.getInstance().getDownOccurrences(data));
		return occurrences;
	}
	
	/**
	 * Function that checks if selected text is a variable
	 * @param str: String value of current text
	 * @returns true if current variable is a DataNode, else false
	 */
	public boolean contains(String str) {
		return (UpFinder.getInstance().contains(str) || DownFinder.getInstance().contains(str));
	}
}
