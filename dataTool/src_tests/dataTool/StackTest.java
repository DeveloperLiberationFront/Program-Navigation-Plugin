package dataTool;

import java.util.Stack;

import dataTool.Visitor.BackwardStack;
import junit.framework.TestCase;

public class StackTest extends TestCase{
	
	/**
	 * Assures that the loop construct in java iterates
	 * through a stack top-to-bottom
	 */
	public void testBackwardStack(){
		
		String a = "a",
				b = "b";
		
		Stack<String> s = new BackwardStack<String>();
		s.push(a);
		s.push(b);
		
		
		boolean isFirst = true;
		for(String i : s)
			if(isFirst){
				assertEquals(i,b);
				isFirst = false;
			}
			else
				assertEquals(i,a);
	}

}
