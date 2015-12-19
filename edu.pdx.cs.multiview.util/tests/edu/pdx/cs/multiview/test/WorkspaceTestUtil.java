package edu.pdx.cs.multiview.test;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.TypeNameRequestor;

public class WorkspaceTestUtil {

	/**
	 * @throws JavaModelException
	 */
	public static void waitForIndexer() throws JavaModelException {
		SearchEngine searchEngine = new SearchEngine();
		TypeNameRequestor typeNameRequestor = new TypeNameRequestor()
		{
			@SuppressWarnings("unused")
			public void acceptClass(char[] packageName, char[] simpleTypeName,
				char[][] enclosingTypeNames, String path)
			{
				// nothing to do
			}

			@SuppressWarnings("unused")
			public void acceptInterface(char[] packageName,
				char[] simpleTypeName, char[][] enclosingTypeNames, String path)
			{
				// nothing to do
			}
		};
		searchEngine.searchAllTypeNames(null, null, SearchPattern.R_EXACT_MATCH
			| SearchPattern.R_CASE_SENSITIVE, IJavaSearchConstants.CLASS,
			SearchEngine.createJavaSearchScope(new IJavaElement[0]),
			typeNameRequestor, IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
			null);
	}
	
	
	
	//public static copyDirectory(File from, File to) {
	
	
	
}
