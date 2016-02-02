package dataTool;

import org.eclipse.jdt.core.dom.ASTNode;
import org.junit.Before;
import org.junit.Test;

import junit.framework.TestCase;

public class DataNodeTest extends TestCase {
	
	private Finder finder;
	
	@Before
	public void setUp() {
		finder = finder.getInstance();
		finder.setFlowDirection(Finder.UP);
	}
	
	@Test
	public void testIsParameter() {
		DataNode node = new DataNode("Test", 0, DataNode.VAR, "");
		assertFalse(node.isParameter(0));
		
		DataNode node2 = new DataNode("TEST", 5, DataNode.PARAM_UP, "test");
		assertTrue(node2.isParameter(5));
		assertFalse(node2.isParameter(1));
		
		Finder f = Finder.getInstance();
		f.setFlowDirection(Finder.DOWN);
		DataNode node3 = new DataNode("test", 10, DataNode.PARAM_DOWN, "");
		assertTrue(node3.isParameter(12));
		assertFalse(node3.isParameter(15));
	}
	
	/*
	 * Tests for all of the DataNode getters, need to add checks for ASTNode constructor as well
	 */
	
	@Test
	public void testGetValue() {
		DataNode node = new DataNode("TEST", 0, "type", "method");
		assertEquals(node.getValue(),"TEST");
	}
	
	@Test
	public void testGetStartPosition() {
		DataNode node = new DataNode("TEST", 23, "type", null);
		assertEquals(node.getStartPosition(),23);
	}
	
	@Test
	public void testGetType() {
		DataNode node = new DataNode("TEST", 100, "type", null);
		assertEquals(node.getType(),"type");
		DataNode node2 = new DataNode("TEST", 3, DataNode.CLASS_VAR, null);
		assertEquals(node2.getType(),DataNode.CLASS_VAR);
	}
	
	@Test
	public void testGetLength() {
		DataNode node = new DataNode("TEST", 123, DataNode.FOR_VAR, "test_func");
		assertEquals(node.getLength(), 4);
		DataNode node2 = new DataNode("String test", 0, DataNode.VAR_DECL, "");
		assertEquals(node2.getLength(), 11);
	}
	
	@Test
	public void testGetMethod() {
		DataNode node = new DataNode("TEST", 1, DataNode.VAR, "method");
		assertEquals(node.getMethod(), null);
		DataNode node2 = new DataNode("test", 103, DataNode.PARAM_UP, "test_method");
		assertEquals(node2.getMethod(), "test_method");
	}

}
