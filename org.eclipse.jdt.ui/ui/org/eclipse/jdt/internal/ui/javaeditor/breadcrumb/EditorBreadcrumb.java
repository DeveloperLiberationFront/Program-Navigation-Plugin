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
package org.eclipse.jdt.internal.ui.javaeditor.breadcrumb;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;

import org.eclipse.core.runtime.Assert;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.ITextEditor;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;

import org.eclipse.jdt.ui.JavaUI;

/**
 * The editor breadcrumb shows the parent chain of the active editor item inside a
 * {@link BreadcrumbViewer}.
 *
 * <p>
 * Clients must implement the abstract methods.
 * </p>
 *
 * @since 3.4
 */
public abstract class EditorBreadcrumb implements IBreadcrumb {

	private static final String ACTIVE_TAB_BG_END= "org.eclipse.ui.workbench.ACTIVE_TAB_BG_END"; //$NON-NLS-1$

	private ITextEditor fTextEditor;
	private ITextViewer fTextViewer;

	private BreadcrumbViewer fBreadcrumbViewer;

	private boolean fHasFocus;
	private boolean fIsActive;

	private Composite fComposite;

	private Listener fDisplayFocusListener;
	private Listener fDisplayKeyListener;

	private IPropertyChangeListener fPropertyChangeListener;

	private ISelection fOldTextSelection;

	private IPartListener fPartListener;
	
	private Shell shell;
	
	private static IMethod searchMethod = null;
	
	private static int searchParamIndex = -1;
		
	/**
	 * The editor inside which this breadcrumb is shown.
	 *
	 * @param editor the editor
	 */
	public EditorBreadcrumb(ITextEditor editor) {
		setTextEditor(editor);
	}

	/**
	 * The active element of the editor.
	 *
	 * @return the active element of the editor, or <b>null</b> if none
	 */
	protected abstract Object getCurrentInput();

	/**
	 * Create and configure the viewer used to display the parent chain.
	 *
	 * @param parent the parent composite
	 * @return the viewer
	 */
	protected abstract BreadcrumbViewer createViewer(Composite parent);

	/**
	 * Reveal the given element in the editor if possible.
	 *
	 * @param element the element to reveal
	 * @return true if the element could be revealed
	 */
	protected abstract boolean reveal(Object element);

	/**
	 * Open the element in a new editor if possible.
	 *
	 * @param element the element to open
	 * @return true if the element could be opened
	 */
	protected abstract boolean open(Object element);

	/**
	 * Create an action group for the context menu shown for the selection of the given selection
	 * provider or <code>null</code> if no context menu should be shown.
	 *
	 * @param selectionProvider the provider of the context selection
	 * @return action group to use to fill the context menu or <code>null</code>
	 *
	protected abstract ActionGroup createContextMenuActionGroup(ISelectionProvider selectionProvider);
	*/
	
	/**
	 * The breadcrumb has been activated. Implementors must retarget the editor actions to the
	 * breadcrumb aware actions.
	 */
	protected abstract void activateBreadcrumb();

	/**
	 * The breadcrumb has been deactivated. Implementors must retarget the breadcrumb actions to the
	 * editor actions.
	 */
	protected abstract void deactivateBreadcrumb();

	public ISelectionProvider getSelectionProvider() {
		return fBreadcrumbViewer;
	}

	protected void setTextViewer(ITextViewer viewer) {
		fTextViewer= viewer;
	}

	/*
	 * @see org.eclipse.jdt.internal.ui.javaeditor.IBreadcrumb#setInput(java.lang.Object)
	 */
	public void setInput(Object element) {
		if (element == null)
			return;

		Object input= fBreadcrumbViewer.getInput();
		if (input == element || element.equals(input))
			return;
		fBreadcrumbViewer.setInput(element);
	}

	/*
	 * @see org.eclipse.jdt.internal.ui.javaeditor.IBreadcrumb#setFocus()
	 */
	public void activate() {
		fBreadcrumbViewer.setFocus();
	}

	/*
	 * @see org.eclipse.jdt.internal.ui.javaeditor.breadcrumb.IBreadcrumb#isActive()
	 */
	public boolean isActive() {
		return fIsActive;
	}
	
	/**
	 * Adds links for program navigation in the breadcrumbs
	 * TODO refresh breadcrumb2, top breadcrumb go to line #
	 */
	public void setText(ArrayList<Object> items) {
		if(fComposite == null) {
			return;
		}
		for(Control c: fComposite.getChildren()) {
			c.dispose();
		}
		if(items != null) {
			GridLayout gridLayout= new GridLayout(items.size(), false);
			gridLayout.marginWidth= 0;
			gridLayout.marginHeight= 0;
			gridLayout.verticalSpacing= 0;
			gridLayout.horizontalSpacing= 0;
			fComposite.setLayout(gridLayout);
			fComposite.update();
			for(Object o: items) {
				if(o instanceof IMethod) {
					final IMethod i = (IMethod)o;
					Link link = new Link(fComposite, SWT.NULL);
					link.setText("<a>"+i.getElementName()+"</a> ");
					link.addListener(SWT.Selection, new Listener() {
						public void handleEvent(Event arg0) {
						//Go to specific line
						IEditorPart editor = null;
						try {
							if(i.getParameters().length > 0) {
								editor = JavaUI.openInEditor(i.getParameters()[searchParamIndex], true, true);
							}
							else {
								editor = JavaUI.openInEditor(i, true, true);
								if(searchMethod != null && editor != null) {
									String code = ((AbstractTextEditor)editor).getDocumentProvider().getDocument(editor.getEditorInput()).get();
									lineSearch(code.toCharArray(), i);
									goToLine(editor);
								}
							}
						} catch (JavaModelException e) {
							// Auto-generated catch block
							e.printStackTrace();
						} catch (PartInitException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						}
					});
				}
				else if(o instanceof int[]) {
					final int[] list = (int[])o;
					final Link link = new Link(fComposite, SWT.NULL);
					link.setText("<a>line "+list[0]+"</a> ");
					link.addListener(SWT.Selection, new Listener(){
						public void handleEvent(Event arg0) {
							link.setForeground(new Color(null, 128,0,128));
							IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
					    	((ITextEditor) editor).selectAndReveal(list[1], list[2]);
						}
					});
				}
			}
		}
		fComposite.redraw();
		fComposite.layout();
		fComposite.getShell().layout();
	}
	
	private static ASTNode searchResult = null;

	private void lineSearch(char[] source, IMethod method) {
		final IMethod m = method;
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(source);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
		cu.accept(new ASTVisitor(){
			public boolean visit(MethodDeclaration md) {
				final String methodName = md.getName().getIdentifier();
				md.accept(new ASTVisitor() {
				public boolean visit(MethodInvocation mi) {
					if(m.getElementName().equals(methodName)) {
						if(mi.getName().getIdentifier().equals(searchMethod.getElementName())) {
							searchResult = mi;
						}
					}
					return true;
				}
				public boolean visit(ClassInstanceCreation c) {
					//TODO Class instance
					return true;
				}
			});
				return true;
		}
		});
	}
		
	private static void goToLine(IEditorPart editorPart) {
		if (!(editorPart instanceof ITextEditor)) {
		    return;
		}
		ITextEditor editor = (ITextEditor) editorPart;
		IDocument document = editor.getDocumentProvider().getDocument(editor.getEditorInput());
		if (document != null && searchResult != null) {
		    editor.selectAndReveal(searchResult.getStartPosition(), searchResult.getLength());
		}
	}
	
	/**
	 * Sets the current method we're searching for, needed because we don't have 
	 * access to DataNode
	 * @param iMethod: Current method to search for
	 */
	public void setSearchMethod(IMethod iMethod) {
		searchMethod = iMethod;
	}
	
	/**
	 * Sets the parameter index of the current data node to link directly to it
	 * @param i: int index of parameter in method arguments
	 */
	public void setSearchIndex(int i) {
		searchParamIndex = i;
	}
	/*
	 * @see org.eclipse.jdt.internal.ui.javaeditor.IBreadcrumb#createContent(org.eclipse.swt.widgets.Composite)
	 */
	public Control createContent(Composite parent) {
		Assert.isTrue(fComposite == null, "Content must only be created once."); //$NON-NLS-1$
		boolean rtl= (getTextEditor().getSite().getShell().getStyle() & SWT.RIGHT_TO_LEFT) != 0;

		fComposite= new Composite(parent, rtl ? SWT.RIGHT_TO_LEFT : SWT.NONE);
		GridData data= new GridData(SWT.FILL, SWT.TOP, true, false);
		fComposite.setLayoutData(data);
		GridLayout gridLayout= new GridLayout(1, false);
		gridLayout.marginWidth= 0;
		gridLayout.marginHeight= 0;
		gridLayout.verticalSpacing= 0;
		gridLayout.horizontalSpacing= 0;
		fComposite.setLayout(gridLayout);
		Link l = new Link(fComposite, SWT.NULL);
		fDisplayFocusListener= new Listener() {
			public void handleEvent(Event event) {
				if (isBreadcrumbEvent(event)) {
					if (fHasFocus)
						return;

					fIsActive= true;

					focusGained();
				} else {
					if (!fIsActive)
						return;

					boolean hasTextFocus= fTextViewer.getTextWidget().isFocusControl();
					if (hasTextFocus) {
						fIsActive= false;
					}

					if (!fHasFocus)
						return;

					focusLost();
				}
			}
		};
		Display.getCurrent().addFilter(SWT.FocusIn, fDisplayFocusListener);
		fBreadcrumbViewer= createViewer(fComposite);
		fBreadcrumbViewer.getControl().setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		
		return fComposite;
	}

	/*
	 * @see org.eclipse.jdt.internal.ui.javaeditor.IEditorViewPart#dispose()
	 */
	public void dispose() {
		System.out.println("dispose editor");
		if (fPropertyChangeListener != null) {
			JFaceResources.getColorRegistry().removeListener(fPropertyChangeListener);
		}
		if (fDisplayFocusListener != null) {
			Display.getDefault().removeFilter(SWT.FocusIn, fDisplayFocusListener);
		}
		deinstallDisplayListeners();
		if (fPartListener != null) {
			getTextEditor().getSite().getPage().removePartListener(fPartListener);
		}

		setTextEditor(null);
	}

	/**
	 * Either reveal the selection in the editor or open the selection in a new editor. If both fail
	 * open the child pop up of the selected element.
	 *
	 * @param selection the selection to open
	 */
	private void doRevealOrOpen(ISelection selection) {
		if (doReveal(selection)) {
			fTextViewer.getTextWidget().setFocus();
		} else if (doOpen(selection)) {
			fIsActive= false;
			focusLost();
			fBreadcrumbViewer.setInput(getCurrentInput());
		}
	}

	private boolean doOpen(ISelection selection) {
		if (!(selection instanceof StructuredSelection))
			return false;

		StructuredSelection structuredSelection= (StructuredSelection) selection;
		if (structuredSelection.isEmpty())
			return false;

		return open(structuredSelection.getFirstElement());
	}

	private boolean doReveal(ISelection selection) {
		if (!(selection instanceof StructuredSelection))
			return false;

		StructuredSelection structuredSelection= (StructuredSelection) selection;
		if (structuredSelection.isEmpty())
			return false;

		if (fOldTextSelection != null) {
			getTextEditor().getSelectionProvider().setSelection(fOldTextSelection);

			boolean result= reveal(structuredSelection.getFirstElement());

			fOldTextSelection= getTextEditor().getSelectionProvider().getSelection();
			getTextEditor().getSelectionProvider().setSelection(new StructuredSelection(this));
			return result;
		} else {
			return reveal(structuredSelection.getFirstElement());
		}
	}

	/**
	 * Focus has been transfered into the breadcrumb.
	 */
	private void focusGained() {
		if (fHasFocus)
			focusLost();

		fComposite.setBackground(JFaceResources.getColorRegistry().get(ACTIVE_TAB_BG_END));
		fHasFocus= true;

		installDisplayListeners();

		activateBreadcrumb();

		getTextEditor().getEditorSite().getActionBars().updateActionBars();

		fOldTextSelection= getTextEditor().getSelectionProvider().getSelection();

		getTextEditor().getSelectionProvider().setSelection(new StructuredSelection(this));
	}

	/**
	 * Focus has been revoked from the breadcrumb.
	 */
	private void focusLost() {
		fComposite.setBackground(null);
		fHasFocus= false;

		deinstallDisplayListeners();

		deactivateBreadcrumb();

		getTextEditor().getEditorSite().getActionBars().updateActionBars();

		getTextEditor().getSelectionProvider().setSelection(fOldTextSelection);
		fOldTextSelection= null;
	}

	/**
	 * Installs all display listeners.
	 */
	private void installDisplayListeners() {
		//Sanity check
		deinstallDisplayListeners();

		fDisplayKeyListener= new Listener() {
			public void handleEvent(Event event) {
				if (event.keyCode != SWT.ESC)
					return;

				if (!isBreadcrumbEvent(event))
					return;

				fTextViewer.getTextWidget().setFocus();
			}
		};
		Display.getDefault().addFilter(SWT.KeyDown, fDisplayKeyListener);
	}

	/**
	 * Removes all previously installed display listeners.
	 */
	private void deinstallDisplayListeners() {
		if (fDisplayKeyListener != null) {
			Display.getDefault().removeFilter(SWT.KeyDown, fDisplayKeyListener);
			fDisplayKeyListener= null;
		}
	}

	/**
	 * Tells whether the given event was issued inside the breadcrumb viewer's control.
	 *
	 * @param event the event to inspect
	 * @return <code>true</code> if event was generated by a breadcrumb child
	 */
	private boolean isBreadcrumbEvent(Event event) {
		if (fBreadcrumbViewer == null)
			return false;

		Widget item= event.widget;
		if (!(item instanceof Control))
			return false;

		return isChild((Control) item, fBreadcrumbViewer.getControl());
	}

	private boolean isChild(Control child, Control parent) {
		if (child == null)
			return false;

		if (child == parent)
			return true;

		return isChild(child.getParent(), parent);
	}

	/**
	 * Sets the text editor for which this breadcrumb is.
	 *
	 * @param textEditor the text editor to be used
	 */
	protected void setTextEditor(ITextEditor textEditor) {
		fTextEditor= textEditor;

		if (fTextEditor == null)
			return;

		fPartListener= new IPartListener() {

			public void partActivated(IWorkbenchPart part) {
				if (part == fTextEditor && fHasFocus) {
					//focus-in event comes before part activation and the
					//workbench activates the editor -> reactivate the breadcrumb
					//if it is the active part.
					focusGained();
				}
			}

			public void partBroughtToTop(IWorkbenchPart part) {
			}

			public void partClosed(IWorkbenchPart part) {

			}

			public void partDeactivated(IWorkbenchPart part) {
				if (part == fTextEditor && fHasFocus) {
					focusLost();
				}
			}

			public void partOpened(IWorkbenchPart part) {
			}

		};
		fTextEditor.getSite().getPage().addPartListener(fPartListener);
	}

	/**
	 * This breadcrumb's text editor.
	 *
	 * @return the text editor
	 */
	protected ITextEditor getTextEditor() {
		return fTextEditor;
	}

}