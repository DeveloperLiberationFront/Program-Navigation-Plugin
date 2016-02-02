package dataTool;

import java.util.ArrayList;

import org.eclipse.jdt.core.dom.ASTNode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import junit.framework.TestCase;

public class FinderTest extends TestCase {
	
	private Finder finder;
	
	@Before
	public void setUp() {
		finder = finder.getInstance();
		finder.setFlowDirection(Finder.UP);
	}
	
	@Test
	public void testGetInstance() {
		assert(finder instanceof UpFinder);
		
		finder.setFlowDirection(Finder.DOWN);
		finder = Finder.getInstance();
		assert(finder instanceof DownFinder);
		
		finder.setFlowDirection("middle");
		try {
			finder = Finder.getInstance();
		}
		catch (NullPointerException e) {
			
		}
		assert(finder == null);
	}
	
	@Test
	public void testFlowDirection() {
		assertEquals(finder.getFlowDirection(), Finder.UP);
		
		finder.setFlowDirection(Finder.DOWN);
		assertEquals(finder.getFlowDirection(), Finder.DOWN);
		
		finder.setFlowDirection(Finder.DOWN);
		assertEquals(finder.getFlowDirection(), Finder.DOWN);
		
		finder.setFlowDirection("tester");
		assertEquals(finder.getFlowDirection(), null);
	}
	
	@Test
	public void testContains() {
		DownFinder down = DownFinder.getInstance();
		down.add("abc", 123, "TEST_TYPE", "TEST_METHOD");
		Finder f = new Finder(Finder.DOWN);
		assert(f.contains("abc"));
		assertFalse(f.contains("xyz"));
	}
	
	@Test
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
	}
}
