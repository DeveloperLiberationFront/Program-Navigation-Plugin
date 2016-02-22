/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package dataTool.ui;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchPage;

import dataTool.DataCallHierarchy;
import dataTool.DataNode;
import dataTool.Finder;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditorBreadcrumb;
import org.eclipse.jdt.internal.ui.javaeditor.ShowInBreadcrumbAction;
import org.eclipse.jdt.internal.ui.javaeditor.breadcrumb.IBreadcrumb;


/**
 * Action to set the focus into the editor breadcrumb.
 * The breadcrumb is made visible if it is hidden.
 *
 * @since 3.4
 */
public class ShowDataInBreadcrumbAction extends ShowInBreadcrumbAction {

	private final JavaEditor fEditor;
	private IWorkbenchPage page;

	public ShowDataInBreadcrumbAction(JavaEditor editor, IWorkbenchPage wbpage) {
		super(editor);
		fEditor = editor;
		page = wbpage;
		setEnabled(true);
	}

	/*
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public void run() {
		IBreadcrumb breadcrumb = fEditor.getBreadcrumb();
		JavaEditorBreadcrumb j = (JavaEditorBreadcrumb)breadcrumb;
		if (breadcrumb == null)
			return;

		IPreferenceStore store= JavaPlugin.getDefault().getPreferenceStore();
		store.setValue(getPreferenceKey(), true);
		breadcrumb.activate();
	}

	/**
	 * Returns the preference key for the breadcrumb. The
	 * value depends on the current perspective.
	 *
	 * @return the preference key or <code>null</code> if there's no perspective
	 */
	private String getPreferenceKey() {
		IPerspectiveDescriptor perspective = page.getPerspective();
		if (perspective == null)
			return null;
		return JavaEditor.EDITOR_SHOW_BREADCRUMB + "." + perspective.getId(); //$NON-NLS-1$
	}

}