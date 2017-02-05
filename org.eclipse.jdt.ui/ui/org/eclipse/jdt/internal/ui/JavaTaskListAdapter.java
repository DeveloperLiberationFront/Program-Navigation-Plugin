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
package org.eclipse.jdt.internal.ui;

import org.eclipse.core.runtime.IAdaptable;

import org.eclipse.core.resources.IResource;

import org.eclipse.ui.views.tasklist.ITaskListResourceAdapter;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;

import org.eclipse.jdt.internal.ui.javaeditor.InternalClassFileEditorInput;

public class JavaTaskListAdapter implements ITaskListResourceAdapter {

	/*
	 * @see ITaskListResourceAdapter#getAffectedResource(IAdaptable)
	 */
	public IResource getAffectedResource(IAdaptable element) {
		IJavaElement java = null;
		if(!(element instanceof InternalClassFileEditorInput)) {
			java = (IJavaElement) element;
		}
		else {
			java = element.getAdapter(IJavaElement.class);
		}
		IResource resource= java.getResource();
		if (resource != null)
			return resource;

		ICompilationUnit cu= (ICompilationUnit) java.getAncestor(IJavaElement.COMPILATION_UNIT);
		if (cu != null) {
			return cu.getPrimary().getResource();
		}
		return null;
	 }
}
