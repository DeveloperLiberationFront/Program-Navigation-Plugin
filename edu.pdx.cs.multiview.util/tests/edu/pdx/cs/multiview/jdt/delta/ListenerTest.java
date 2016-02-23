package edu.pdx.cs.multiview.jdt.delta;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaCore;

import edu.pdx.cs.multiview.jdt.delta.IMemberDelta.DeltaKind;
import edu.pdx.cs.multiview.jdt.delta.MemberModel.IModelListener;
import edu.pdx.cs.multiview.test.WorkspaceTestUtil;
import edu.pdx.cs.multiview.util.Debug;

public class ListenerTest extends BaseMemberTestCase {
	
	
	public void testListenToMethod() throws Exception {
		IMethod m = _testType.getMethods()[0];
		ModelListener listener = new ModelListener(m);
		MemberModel.addListener(m, listener);
		//Thread.yield();
		m.delete(true, null);
		Debug.trace("here");
		//Thread.sleep(8000);
		WorkspaceTestUtil.waitForIndexer();
		Debug.trace("there");
		assertTrue(listener.notified);
	}
		
	static class ModelListener implements IModelListener {
		public boolean notified = false;
		IJavaElement _javaElement;
		
		public ModelListener(IJavaElement element) {
			_javaElement = element;
		}

		public void notify(IMemberDelta delta) {
			String handle = delta.getHandleId();
			IJavaElement je = JavaCore.create(handle);
			assertEquals(je, _javaElement);
			if (!delta.getKind().equals(DeltaKind.UNCHANGED)) {
				notified = true;
				Debug.trace("done");
			}
		}		
	}
		
}
