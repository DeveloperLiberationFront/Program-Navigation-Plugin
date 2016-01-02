package dataTool;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;

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
		if(page.getActiveEditor()!=null)
			annotationManager = new AnnotationManager((AbstractDecoratedTextEditor)activeEditor);
	}

	public void init(IWorkbenchWindow window) {
		this.page = window.getActivePage();
	}

	public void run(IAction action) {
		
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
	
	public void selectionChanged(IAction action, ISelection selection) {
		//do nothing
	}

}
