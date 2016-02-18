package dataTool;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.internal.ui.javaeditor.breadcrumb.IBreadcrumb;
import org.eclipse.jdt.internal.ui.javaeditor.ShowDataInBreadcrumbAction;
import org.eclipse.jdt.internal.ui.javaeditor.ShowInBreadcrumbAction;
import org.eclipse.jdt.internal.ui.javaeditor.ToggleBreadcrumbAction;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;

import dataTool.ui.NavigationDownBox;
import dataTool.ui.NavigationUpBox;

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
	private static IBreadcrumb dataBreadcrumb;
	
	public static String project;
	public static String file;
	public static String path;

	public void dispose() {
		if(annotationManager!=null)
			annotationManager.dispose();
	}
	
	public static IBreadcrumb getBreadcrumb() {
		return dataBreadcrumb;
	}
	/**
	 * Enables the annotation manager
	 * 
	 * @param activeEditor
	 */
	private void enable(IEditorPart activeEditor) {
		if(page.getActiveEditor()!=null) {
			annotationManager = new AnnotationManager((AbstractDecoratedTextEditor)activeEditor);
		}
	}

	public void init(IWorkbenchWindow window) {
		this.page = window.getActivePage();
		page.addPartListener(new IPartListener(){

			@Override
			public void partActivated(IWorkbenchPart arg0) {
				// Auto-generated method stub
			}

			@Override
			public void partBroughtToTop(IWorkbenchPart arg0) {
				// Auto-generated method stub

			}

			@Override
			public void partClosed(IWorkbenchPart arg0) {
				// Auto-generated method stub

			}

			@Override
			public void partDeactivated(IWorkbenchPart arg0) {
				// Auto-generated method stub
				
			}

			@Override
			public void partOpened(IWorkbenchPart arg0) {
				// TODO Auto-generated method stub
				arg0.getSite().getPage().activate(arg0);
				try {
					dispose();
					isEnabled = false;
					reset(arg0.getSite().getPage());
				} catch (JavaModelException e) {
					// Auto-generated catch block
					e.printStackTrace();
				}
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
	
	public void reset(IWorkbenchPage workbench) throws JavaModelException {
		try {
			//NavigationUpBox.getInstance().setText(null);
			//NavigationDownBox.getInstance().setText(null);
		}
		catch (NullPointerException e) {
			// Navigation boxes aren't active
		}
		IWorkbenchPage newPage;
		if(workbench == null) {
			newPage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		}
		else {
			newPage = workbench;
		}
        init(newPage.getWorkbenchWindow());
        run(null);
	}
}
