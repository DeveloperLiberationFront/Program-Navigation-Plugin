package edu.pdx.cs.multiview.jdt.delta;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;

public class MemberSetTest extends BaseMemberTestCase {
	
	public void testBasicAddAndRemove() throws JavaModelException {
		IMemberSet mset = new MemberSet();
		assertEquals(0,mset.getMembers().size());
		IMethod m = _testType.getMethods()[0];
		mset.add(getMemberInfo(m));
		assertEquals(1,mset.getMembers().size());
		
		//multiple adds of the same member should be ignored
		mset.add(getMemberInfo(m));
		assertEquals(1,mset.getMembers().size());
		
		//test remove
		mset.remove(m.getHandleIdentifier());
		assertEquals(0,mset.getMembers().size());
		
		//add another member
		IMethod[] ms = _testType.getMethods();
		mset.add(getMemberInfo(ms[0]));
		mset.add(getMemberInfo(ms[1]));
		assertEquals(2,mset.getMembers().size());
	}
	

	
}
