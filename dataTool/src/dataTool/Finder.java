package dataTool;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jface.text.Position;
import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;

import dataTool.annotations.SuggestedSelectionAnnotation;

public class Finder {
	protected static Map<String, ArrayList<DataNode>> map; // contains name and first node for all data
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
	public ArrayList<DataNode> getOccurrences(String data, Position position) {
		ArrayList<DataNode> occurrences = new ArrayList<DataNode>();
		ArrayList<DataNode> upOccurrences = UpFinder.getInstance().getUpOccurrences(data, position);
		ArrayList<DataNode> downOccurrences = DownFinder.getInstance().getDownOccurrences(data, position);
		
		if( upOccurrences != null ) {
			occurrences.addAll(upOccurrences);
		}
		if( downOccurrences != null ) {
			occurrences.addAll( downOccurrences);
		}
		
		return occurrences;
	}
	public static void add( DataNode dn ) {
		System.out.println(dn.getSignature() + " " + dn.getStartPosition());
		ArrayList<DataNode> list;
		String key = dn.getSignature();
		if (!map.containsKey(key)) {
			list = new ArrayList<DataNode>();
			list.add(dn);
			map.put(key, list);
		}
		else {
			list = map.get(key);
			list.add(dn);
			map.put(key, list);
		}
	}
//	/**
//	 * Function that checks if selected text is a variable
//	 * @param str: String value of current text
//	 * @returns true if current variable is a DataNode, else false
//	 */
//	public boolean contains(String str) {
//		System.out.println(str);
//		return (UpFinder.getInstance().contains(str) || DownFinder.getInstance().contains(str));
//	}
}
