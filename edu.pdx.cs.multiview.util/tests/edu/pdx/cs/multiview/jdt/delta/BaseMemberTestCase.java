package edu.pdx.cs.multiview.jdt.delta;

import junit.framework.TestCase;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;

import edu.pdx.cs.multiview.test.JavaTestProject;


public class BaseMemberTestCase extends TestCase {

	/** A test source buffer */
	private static final String TEST_CLASS_SRC = "public class TestClass {\n" +
				"void m(){boolean value;}\n" +
				"void n(){}\n" +
				"int x;\n}";

	/** A test type */
	protected IType _testType;
	/** A test project */
	protected JavaTestProject _testProject;

	{
		try {
			if (_testProject == null || _testProject.getJavaProject().exists())
				_testProject = new JavaTestProject(getClass().getName());
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		assertNotNull(_testProject);
		_testType = createTestType();
	}

	@Override
	protected void tearDown() throws Exception {
		_testProject.dispose();
	}
	
	protected IMemberInfo getMemberInfo(IMember m) {
		return MemberInfo.createInfo(m);
	}

	public IType createTestType() throws CoreException {
		IPackageFragment pkg = _testProject.createPackage("test");
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

}
