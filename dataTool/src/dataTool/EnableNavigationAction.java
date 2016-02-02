package dataTool;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;

import dataTool.ui.NavigationBox;

/**
 * An action that enables the statement helper
 * 
 * @author emerson
 */
public class EnableNavigationAction implements IWorkbenchWindowActionDelegate {

	//TODO: have little roll-overs so you can select arbitarary expressions
	//	aka - mouse-hovers that replace hot-keys for selecting nodes
	
	//the currently open page
	private IWorkbenchPage page;
	
	//the listener to the current page
	private AnnotationManager annotationManager;
	
	//whether the listener is currently enabled
	private boolean isEnabled = false;
	
	public static String project;
	public static String file;
	public static String path;
	
	public void dispose() {
		if(annotationManager!=null)
			annotationManager.dispose();
	}
	
	/**
	 * Enables the annotation manager
	 * 
	 * @param activeEditor
	 */
	private void enable(IEditorPart activeEditor) {
		String[] path = activeEditor.getTitleToolTip().split("/");
		project = path[0];
		file = activeEditor.getTitle();
		this.path = activeEditor.getEditorInput().toString().replace("org.eclipse.ui.part.FileEditorInput(", "").replace(")","");
		if(page.getActiveEditor()!=null) {
			annotationManager = new AnnotationManager((AbstractDecoratedTextEditor)activeEditor);
		}
	}

	public void init(IWorkbenchWindow window) {
		this.page = window.getActivePage();
		window.addPageListener(new IPageListener() {

			@Override
			public void pageActivated(IWorkbenchPage arg0) {
				// TODO Auto-generated method stub
				init(arg0.getWorkbenchWindow());
			}

			@Override
			public void pageClosed(IWorkbenchPage arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void pageOpened(IWorkbenchPage arg0) {
				// TODO Auto-generated method stub
				init(arg0.getWorkbenchWindow());
			}
			
		});
	}

	public void run(IAction action) {
		//JavaCore.cre
		try {
			if(!isEnabled )
				enable(page.getActiveEditor());	
				
			else
				dispose();

		} catch (Exception e) {
			Activator.logError(e);
		} finally {
			isEnabled = !isEnabled;
		}
	}
	
	public boolean isEnabled() {
		return isEnabled;
	}
	
	public void selectionChanged(IAction action, ISelection selection) {
		//do nothing
	}

}
