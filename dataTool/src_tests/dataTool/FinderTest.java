package dataTool;

import java.util.ArrayList;

import org.eclipse.jdt.core.dom.ASTNode;
import org.junit.After;
import org.junit.Test;

import junit.framework.TestCase;

public class FinderTest extends TestCase {
	
	public void setUp() {
		
	}
	
	public void testGetInstance() {
		Finder f = new Finder(Finder.UP);
		assert(f instanceof UpFinder);
		
		f.setFlowDirection(Finder.DOWN);
		f = Finder.getInstance();
		assert(f instanceof DownFinder);
		
		f.setFlowDirection("middle");
		try {
			f = Finder.getInstance();
		}
		catch (NullPointerException e) {
			
		}
		assert(f == null);
		reset(f);
	}
	
	public void testFlowDirection() {
		Finder f = Finder.getInstance();
		assertEquals(f.getFlowDirection(), Finder.UP);
		
		f.setFlowDirection(Finder.DOWN);
		assertEquals(f.getFlowDirection(), Finder.DOWN);
		f.setFlowDirection(Finder.DOWN);
		assertEquals(f.getFlowDirection(), Finder.DOWN);
		f.setFlowDirection("tester");
		assertEquals(f.getFlowDirection(), null);
		
		reset(f);
	}
	
	public void testContains() {
		DownFinder down = DownFinder.getInstance();
		down.add("abc", 123, "TEST_TYPE", "TEST_METHOD");
		Finder f = new Finder(Finder.DOWN);
		assert(f.contains("abc"));
		assertFalse(f.contains("xyz"));
		
		reset(f);
	}
	
	public void testGetOccurrences() {
		DownFinder down = DownFinder.getInstance();
		down.add("test", 0, "type", "");
		down.add("test", 1, "type", "method");
		down.add("test", 2, "new_type", "new_method");
		down.add("data", 20, DataNode.VAR, null);
		Finder f = new Finder(Finder.DOWN);
		ArrayList<DataNode> testList = f.getOccurrences("test");
		ArrayList<DataNode> dataList = f.getOccurrences("data");
		assertEquals(testList.size(), 3);
		assertEquals(dataList.size(), 1);
		
		reset(f);
	}
	/**
	 * To fix errors with order tests are run and singleton pattern
	 */
	private void reset(Finder f) {
		f.setFlowDirection(Finder.UP);
		
	}
}
