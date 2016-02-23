package edu.pdx.cs.multiview.jdt.delta;

import java.util.Set;

import junit.framework.TestCase;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;

import edu.pdx.cs.multiview.jdt.delta.ListenerTest.ModelListener;
import edu.pdx.cs.multiview.jdt.delta.MemberModel.IModelListener;
import edu.pdx.cs.multiview.test.JavaTestProject;

//FIXME:: this entire test suite should be cleaned up and refactored...
public class MemberStoreTest extends /* BaseMember */ TestCase {

	/** A test project to be reused by all tests run in this class*/
	protected static JavaTestProject _testProject;
		
	/** A counter for generating unique packages to ensure distinct test types */
	protected static int counter;
	
	/** A test source buffer */
	private static final String TEST_CLASS_SRC = "public class TestClass {\n" +
				"void m(){}\n" +
				"void n(){}\n" +
				"int x;\n}";
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		if (_testProject == null)
			_testProject = new JavaTestProject(getClass().getName());
	}
	
	
	public void testBasicAddAndRemove() throws CoreException {
		MemberStore store = new MemberStore();
		IType testType = createFreshTestType();
		IMethod m = testType.getMethods()[0];
		String key = m.getHandleIdentifier();
		IMemberInfo stored =  getMemberInfo(m);
		store.put(key,stored);
		
		IMemberInfo retrieved = store.get(m);
		assertEquals(stored, retrieved);
		
		//confirm its there, delete it and check again
		assertTrue(store.contains(m));
		store.remove(key);
		assertFalse(store.contains(m));
	}
	
	protected IMemberInfo getMemberInfo(IMember m) {
		return MemberInfo.createInfo(m);
	}

	public IType createFreshTestType() throws CoreException {
		IPackageFragment pkg = _testProject.createPackage("test" + counter++);//N.B. increment for uniqueness
		IType testType = _testProject.createType(pkg, "TestClass.java", TEST_CLASS_SRC);
		assert testType != null  : "failed to create type";
		assert testType.exists() : "type does not exist";
		IMethod m = testType.getMethod("m", new String[]{});
		assert m.exists() : "type does not have expected method";
		//Need to yield here because createCompilationUnit(..) in createType(..) creates an IWorkspaceRunnable
		//that may not have run yet...
		Thread.yield();
		return testType;
	}
	
	public void testAddAndRemoveListener() throws Exception {
		IType testType = createFreshTestType();
		IMethod m = testType.getMethods()[0];
		ModelListener listener = new ModelListener(m);
		MemberStore store = new MemberStore();
		store.addListener(m, listener);
		IMemberInfo info = store.get(m);
		Set<IModelListener> listeners = info.getListeners();
		assertTrue(listeners.contains(listener));
		store.removeListener(m,listener);
		assertFalse(listeners.contains(listener));
	}	
	
	
}
