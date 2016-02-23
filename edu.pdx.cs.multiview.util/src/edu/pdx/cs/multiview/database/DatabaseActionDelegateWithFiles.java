package edu.pdx.cs.multiview.database;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPart;

public abstract class DatabaseActionDelegateWithFiles extends
		DatabaseAction {

	private IStructuredSelection selection;

	private void putFilesIn(List<IFile> f, Object resource) {
		if(resource instanceof IFile){
			IFile file = (IFile)resource;
			if( fileOfInterest(file))
				f.add(file);
		}else if(resource instanceof IContainer){
			try {
				for(IResource r : ((IContainer)resource).members()){
					putFilesIn(f,r);
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}else if(resource instanceof IJavaElement){
			try {
				IResource r = ((IJavaElement)resource).getCorrespondingResource();
				putFilesIn(f,r);
			} catch (JavaModelException e) {
				e.printStackTrace();
			}
		}
			
	}

	protected abstract boolean fileOfInterest(IFile file);

	protected List<IFile> getFileList() {
		List<IFile> f = new LinkedList<IFile>();
		
		if(selection!=null){
			Iterator<?> iterator = selection.iterator();
			while(iterator.hasNext()){
				putFilesIn(f, iterator.next());
			}
		}
		return f;
	}

	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = (IStructuredSelection) selection;
	}

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {}

}