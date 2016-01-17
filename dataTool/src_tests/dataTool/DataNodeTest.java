package dataTool;

import org.eclipse.jdt.core.dom.ASTNode;

import junit.framework.TestCase;

public class DataNodeTest extends TestCase {
	
	public void setUp() {
		
	}
	
	public void testIsParameter() {
		DataNode node = new DataNode("Test", 0, DataNode.VAR);
		assert(!node.isParameter());
		
		DataNode node2 = new DataNode("TEST", 5, DataNode.PARAM_UP);
		assert(node2.isParameter());
		
		DataNode node3 = new DataNode("test", 10, DataNode.PARAM_DOWN);
		assert(node3.isParameter());
	}
	
	/*
	 * Tests for all of the DataNode getters, need to add checks for ASTNode constructor as well
	 */
	public void testGetValue() {
		DataNode node = new DataNode("TEST", 0, "type");
		assertEquals(node.getValue(),"TEST");
	}
	
	public void testGetStartPosition() {
		DataNode node = new DataNode("TEST", 23, "type");
		assertEquals(node.getStartPosition(),23);
	}
	
	public void testGetType() {
		DataNode node = new DataNode("TEST", 100, "type");
		assertEquals(node.getType(),"type");
		DataNode node2 = new DataNode("TEST", 3, DataNode.CLASS_VAR);
		assertEquals(node2.getType(),DataNode.CLASS_VAR);
	}
	
	public void testGetLength() {
		DataNode node = new DataNode("TEST", 123, DataNode.FOR_VAR);
		assertEquals(node.getLength(), 4);
		DataNode node2 = new DataNode("String test", 0, DataNode.VAR_DECL);
		assertEquals(node2.getLength(), 11);
	}
	public void testGetMethod() {
		DataNode node = new DataNode("TEST", 1, DataNode.VAR);
		assertEquals(node.getMethod(), null);
	}

}
