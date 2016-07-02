package dataTool;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.State;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.internal.ui.javaeditor.ToggleBreadcrumbAction;
import org.eclipse.jdt.internal.ui.javaeditor.breadcrumb.IBreadcrumb;
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
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;
import dataTool.ui.ShowDataInBreadcrumbAction;

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
	private String previous = "";
	private String toggleId = "org.eclipse.example.command.toggleState";
	private ShowDataInBreadcrumbAction crumbs;
	
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
		if(page.getActiveEditor()!=null) {
			annotationManager = new AnnotationManager((AbstractDecoratedTextEditor)activeEditor);
		}
	}
	
	/**
	 * Disables the plugin
	 */
	private void disable() {
		if(annotationManager!=null) {
			annotationManager.setEnabled(false);
			annotationManager.deactivate();
			annotationManager.dispose();
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
				if(!previous.equals(arg0.getTitle())) {
					previous = arg0.getTitle();
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
				// Auto-generated method stub
				
			}
			
		});
	}

	public void run(IAction action) {
		//JavaCore.cre
		try {
			ICommandService service = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
			Command command = service.getCommand("org.eclipse.example.command.toggle");
			State state = command.getState("org.eclipse.example.command.toggleState");
			if (state == null) {
				state = new State();
				state.setValue(true);
				state.setId("org.eclipse.example.command.toggleState");
				command.addState("org.eclipse.example.command.toggleState", state);
			}
			else {
				state.setValue(!(Boolean) state.getValue());
			}
			if((boolean) state.getValue()) {
				IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				IEditorPart activeEditor = activePage.getActiveEditor();
				JavaEditor j = (JavaEditor) activeEditor;
				crumbs = new ShowDataInBreadcrumbAction(j, activePage);
				crumbs.run();
				enable(page.getActiveEditor());
				isEnabled = true;
			}
			else {
				crumbs.stop();
				isEnabled = false;
				disable();
			}
		} catch (Exception e) {
			Activator.logError(e);
		}
	}
	
	public void selectionChanged(IAction action, ISelection selection) {
		//do nothing
	}
	
	/**
	 * Resets the plugin when user opens a new page
	 * @param workbench
	 * @throws JavaModelException
	 */
	public void reset(IWorkbenchPage workbench) throws JavaModelException {
		IWorkbenchPage newPage;
		if(workbench == null) {
			newPage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		}
		else {
			newPage = workbench;
		}
		JavaEditor editor = (JavaEditor)newPage.getActiveEditor();
        init(newPage.getWorkbenchWindow());
        enable(page.getActiveEditor());
		isEnabled = true;
	}
}
