package edu.pdx.cs.multiview.util.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.texteditor.AbstractTextEditor;

/**
 * I keep track of the currently activated {@link AbstractTextEditor}
 * and send any selection events to the listeners.
 * 
 * @author emerson
 */
public class EditorSelectionManager implements IPartListener, ISelectionListener{

	private List<ISelectionChangedListener> listeners = 
		new ArrayList<ISelectionChangedListener>(10);
	
	//the editor I'm currently listening to
	private AbstractTextEditor editor;

	//the page I'm listening to
	private IWorkbenchPage page;
	
	public void addSelectionChangedListener(ISelectionChangedListener listener){
		listeners.add(listener);
	}
	
	public void removeSelectionChangedListener(ISelectionChangedListener listener){
		listeners.remove(listener);
	}
	
	public void partActivated(IWorkbenchPart part) {
		if(part instanceof AbstractTextEditor){
			removeListenerFromEditor();
			editor = (AbstractTextEditor)part;
			addListenerToEditor();
		}
	}
	
	/**
	 *	Adds a listener to when an editor is opened.
	 */
	protected void addListenerToEditor(){
		if(getEditor()!=null)
			getEditor().getEditorSite().getWorkbenchWindow().getSelectionService().addPostSelectionListener(this);
	}
	
	/**
	 *	Removes a listener to when an editor is opened.
	 */
	protected void removeListenerFromEditor(){
		if(getEditor()!=null)
			getEditor().getEditorSite().getWorkbenchWindow().getSelectionService().removePostSelectionListener(this);
	}
	

	public void partClosed(IWorkbenchPart part) {/*nada*/}
	public void partBroughtToTop(IWorkbenchPart part) {/*nada*/}
	public void partDeactivated(IWorkbenchPart part) {/*nada*/}
	public void partOpened(IWorkbenchPart part) {/*nada*/}
	
	protected boolean noOpenEditor(IWorkbenchWindow workbenchWindow) {
		
		for(IWorkbenchPage page : workbenchWindow.getPages()){			
			IEditorPart part = page.getActiveEditor();
			if(part!=null && part.getEditorInput().getAdapter(IJavaElement.class)!=null)
				return false;
		}
		
		return true;
	}
		
	/**
	 * Adds self as a listener to the workbench at some
	 * time in the near future.
	 * 
	 * @param site
	 */
	public void listenToLater(final IViewSite site) {
		Display.getCurrent().asyncExec(
				new Runnable(){
					public void run() {
						listenTo(site.getWorkbenchWindow().getActivePage());
					}
				});
	}
	
	/**
	 * Adds self as a listener to hte workbench at some 
	 * time in the near future
	 * 
	 * @param activePage
	 */
	public void listenToLater(final IWorkbenchPage activePage) {
		//run this later, as the activate page may not be loaded yet
		
		Display.getCurrent().asyncExec(
				new Runnable(){
					public void run() {
						listenTo(activePage);
					}
				});
	}
	
	/**
	 * Listens to the editor in this active page
	 * 
	 * @param activePage
	 */
	public void listenTo(IWorkbenchPage activePage) {
		activePage.addPartListener(this);
		this.page = activePage;
		
		IEditorPart activeEditor = activePage.getActiveEditor();
		if(activeEditor!=null)
			partActivated(activeEditor);
	}

	/**
	 *	Removes everything I am listening to
	 */
	public void dispose() {
		removeListenerFromEditor();
		if(page!=null)
			page.removePartListener(this);
		listeners = listeners.subList(0, 0);
	}
	
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if(editor!=null && editor.getSelectionProvider()!=null)
			for(ISelectionChangedListener listener : listeners)
				listener.selectionChanged(new SelectionChangedEvent
							(editor.getSelectionProvider(),selection));
	}
	

	/**
	 * Set the text selection inside of the editor
	 * 
	 * @param startPosition
	 * @param length
	 */
	public void setSelection(int startPosition, int length) {
		ITextSelection selection = new TextSelection(startPosition,length);
		editor.getSelectionProvider().setSelection(selection);	
		//force an event - this doesn't happen naturally, for some reason
		this.selectionChanged(editor.getEditorSite().getPart(), selection);
	}
	
	public AbstractTextEditor getEditor(){
		return editor;
	}

	public void setEditor(AbstractTextEditor editor2) {
		editor = editor2;
	}

	public StyledText getStyledText() {
		AbstractTextEditor part = getEditor();
		Control control = (Control) part.getAdapter(Control.class);
		if(control instanceof StyledText)
			return (StyledText)control;
		else
			return null;
	}

}
