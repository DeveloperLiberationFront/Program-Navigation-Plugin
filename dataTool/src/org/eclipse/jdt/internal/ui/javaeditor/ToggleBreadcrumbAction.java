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
package org.eclipse.jdt.internal.ui.javaeditor;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import org.eclipse.ui.texteditor.ResourceAction;

import org.eclipse.jdt.internal.ui.IJavaHelpContextIds;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.JavaPluginImages;


/**
 * Action to hide and show the editor breadcrumb.

 * @since 3.4
 */
public class ToggleBreadcrumbAction extends ResourceAction implements IPropertyChangeListener, IPerspectiveListener {

	private IPreferenceStore fStore;
	private IWorkbenchPage fPage;

	/**
	 * Constructs and updates the action.
	 *
	 * @param page the workbench page
	 */
	public ToggleBreadcrumbAction(IWorkbenchPage page) {
		
		super(JavaEditorMessages.getBundleForConstructedKeys(), "ToggleBreadcrumbAction.", IAction.AS_CHECK_BOX); //$NON-NLS-1$
		JavaPluginImages.setToolImageDescriptors(this, "toggle_breadcrumb.gif"); //$NON-NLS-1$
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IJavaHelpContextIds.TOGGLE_BREADCRUMB_ACTION);
		fPage= page;
		fPage.getWorkbenchWindow().addPerspectiveListener(this);
		update();
	}

	/*
	 * @see IAction#actionPerformed
	 */
	public void run() {
		fStore.setValue(getPreferenceKey(), isChecked());
	}

	/*
	 * @see TextEditorAction#update
	 */
	public void update() {
		if (fStore == null) {
			fStore= JavaPlugin.getDefault().getPreferenceStore();
			fStore.addPropertyChangeListener(this);
		}
		String key= getPreferenceKey();
		setChecked(key != null && fStore.getBoolean(key));
		setEnabled(true);
	}

	/*
	 * @see IPropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		if (event.getProperty().equals(getPreferenceKey()))
			setChecked(Boolean.valueOf(event.getNewValue().toString()).booleanValue());
	}

	/**
	 * Dispose this action
	 */
	public void dispose() {
		if (fStore != null) {
			fStore.removePropertyChangeListener(this);
			fStore= null;
		}
		if (fPage != null) {
			fPage.getWorkbenchWindow().removePerspectiveListener(this);
			fPage= null;
		}
	}

	/**
	 * Returns the preference key for the breadcrumb. The
	 * value depends on the current perspective.
	 *
	 * @return the preference key or <code>null</code> if there's no perspective
	 */
	private String getPreferenceKey() {
		IPerspectiveDescriptor perspective= fPage.getPerspective();
		if (perspective == null)
			return null;
		return JavaEditor.EDITOR_SHOW_BREADCRUMB + "." + perspective.getId(); //$NON-NLS-1$
	}

	/*
	 * @see org.eclipse.ui.IPerspectiveListener#perspectiveActivated(org.eclipse.ui.IWorkbenchPage, org.eclipse.ui.IPerspectiveDescriptor)
	 * @since 3.4
	 */
	public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
		boolean isChecked= fStore.getBoolean(getPreferenceKey());
		if (isChecked != isChecked()) {
			Boolean value= Boolean.valueOf(isChecked);
			fStore.firePropertyChangeEvent(getPreferenceKey(), value, value);
		}
	}

	/*
	 * @see org.eclipse.ui.IPerspectiveListener#perspectiveChanged(org.eclipse.ui.IWorkbenchPage, org.eclipse.ui.IPerspectiveDescriptor, java.lang.String)
	 */
	public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, String changeId) {
	}
}
