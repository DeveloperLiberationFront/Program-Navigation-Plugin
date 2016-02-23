package edu.pdx.cs.multiview.jdt.delta;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaCore;

import edu.pdx.cs.multiview.jdt.delta.IMemberDelta.DeltaKind;
import edu.pdx.cs.multiview.jdt.delta.MemberModel.IModelListener;
import edu.pdx.cs.multiview.jdt.util.JDTUtils;
import edu.pdx.cs.multiview.test.WorkspaceTestUtil;

public class MultipleListenerTest extends BaseMemberTestCase {
		

	public void testSingleNotify() throws Exception {
		IMethod m = _testType.getMethods()[0];
		ModelListener listener = new ModelListener(m);
		MemberModel.addListener(m, listener);
		
		
		//change once
		JDTUtils.changeBody(m,"boolean x;");
		
		
		WorkspaceTestUtil.waitForIndexer();
		assertTrue(listener.notificationCount==1);
		
		//change again
		JDTUtils.changeBody(m,"int x;");
		
		WorkspaceTestUtil.waitForIndexer();
		assertTrue(listener.notificationCount==2);
		
		IMethod other = _testType.getMethods()[1];
		
		//change a different method
		JDTUtils.changeBody(other,"String s;");
		
		//make sure that listener is not notified
		WorkspaceTestUtil.waitForIndexer();
		assertTrue(listener.notificationCount==2);
		
		//remove listener
		MemberModel.removeListener(m, listener);
		
		//change once more
		JDTUtils.changeBody(m, "double d;");
		
		//make sure that listener is not notified
		WorkspaceTestUtil.waitForIndexer();
		assertTrue(listener.notificationCount==2);
	}
	
		
	static class ModelListener implements IModelListener {
		public int notificationCount = 0;
		IJavaElement _javaElement;
		
		public ModelListener(IJavaElement element) {
			_javaElement = element;
		}

		public void notify(IMemberDelta delta) {
			String handle = delta.getHandleId();
			IJavaElement je = JavaCore.create(handle);
			assertEquals(je, _javaElement);
			if (!delta.getKind().equals(DeltaKind.UNCHANGED)) {
				notificationCount++;
				System.out.println("x");
			}
		}		
	}
		
}
