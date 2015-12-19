package edu.pdx.cs.multiview.jdt.delta;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(
				"Test for edu.pdx.cs.multiview.jdt.delta");
		//$JUnit-BEGIN$
		suite.addTestSuite(MemberSetTest.class);
		suite.addTestSuite(ListenerTest.class);
		suite.addTestSuite(MultipleListenerTest.class);
		suite.addTestSuite(MemberStoreTest.class);
		//$JUnit-END$
		return suite;
	}

}
