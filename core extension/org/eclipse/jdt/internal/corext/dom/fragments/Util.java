/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.corext.dom.fragments;

import org.eclipse.core.runtime.Assert;

import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.compiler.IScanner;
import org.eclipse.jdt.core.compiler.ITerminalSymbols;
import org.eclipse.jdt.core.compiler.InvalidInputException;

import org.eclipse.jdt.internal.corext.SourceRange;

/**
 * This class houses a collection of static methods which do not refer to,
 * or otherwise depend on, other classes in this package.  Each
 * package-visible method is called by more than one other class in this
 * package.  Since they do not depend on other classes in this package,
 * they could be moved to some less specialized package.
 */
class Util {
	static boolean rangeIncludesNonWhitespaceOutsideRange(SourceRange selection, SourceRange nodes, IBuffer buffer) {
		if(!selection.covers(nodes))
			return false;

		//TODO: skip leading comments. Consider that leading line comment must be followed by newline!
		if(!isJustWhitespace(selection.getOffset(), nodes.getOffset(), buffer))
			return true;
		if(!isJustWhitespaceOrComment(nodes.getOffset() + nodes.getLength(), selection.getOffset() + selection.getLength(), buffer))
			return true;
		return false;
	}
	private static boolean isJustWhitespace(int start, int end, IBuffer buffer) {
		if (start == end)
			return true;
		Assert.isTrue(start <= end);
		return 0 == buffer.getText(start, end - start).trim().length();
	}
	private static boolean isJustWhitespaceOrComment(int start, int end, IBuffer buffer) {
		if (start == end)
			return true;
		Assert.isTrue(start <= end);
		String trimmedText= buffer.getText(start, end - start).trim();
		if (0 == trimmedText.length()) {
			return true;
		} else {
			IScanner scanner= ToolFactory.createScanner(false, false, false, null);
			scanner.setSource(trimmedText.toCharArray());
			try {
				return scanner.getNextToken() == ITerminalSymbols.TokenNameEOF;
			} catch (InvalidInputException e) {
				return false;
			}
		}
	}
}
