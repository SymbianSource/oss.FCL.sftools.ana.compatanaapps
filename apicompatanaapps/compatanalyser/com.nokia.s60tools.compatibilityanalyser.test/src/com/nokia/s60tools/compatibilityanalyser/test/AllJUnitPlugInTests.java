package com.nokia.s60tools.compatibilityanalyser.test;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllJUnitPlugInTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(
				"Test for com.nokia.s60tools.compatibilityanalyser.test");
		//$JUnit-BEGIN$
		suite.addTestSuite(CompatibilityAnalysisTest.class);
		//$JUnit-END$
		return suite;
	}

}
