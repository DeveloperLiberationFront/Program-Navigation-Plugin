package dataTool.ui;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.eclipse.core.internal.runtime.Activator;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;

import dataTool.EnableNavigationAction;

public class DataLink {
	private String name;
	private String location;
	private IMethod method;
	
	final public static String INVALID = "Invalid";
	final public static String INVALID_DESC = "Function is not in scope project.";
	
	public DataLink(IMethod method, String name, String location) {
		this.method = method;
		this.name = name;
		this.location = location;
	}
	
	public String getName() {
		return name;
	}
	
	public String getLocation() {
		return location;
	}
	
	public String getText() {
		if(!name.equals(INVALID)) {
			return "<a>"+name+"</a>";
		}
		return name;
	}
	
	public void open(int index) {
		IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(method.getPath());
		if (file.exists()) {// && file.isFile()) {
		    String path = file.getLocation().toString();//Path for that to file to open;
		    IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			IEditorDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(file.getName());
		    URI fromString = null;
		    IEditorPart openEditor = null;
			try {
				fromString = org.eclipse.core.runtime.URIUtil.fromString("file://" + path);
			} 
			catch (URISyntaxException e) {
				e.printStackTrace();
			}
		    try {
		        openEditor = IDE.openEditor(page, fromString, desc.getId(), true);
		        IEditorInput editorInput = openEditor.getEditorInput();
		    } 
		    catch (PartInitException e) {
		         e.printStackTrace();
		    }
		    EnableNavigationAction plugin = new EnableNavigationAction();
	        plugin.init(page.getWorkbenchWindow());
	        plugin.run(null);
	        System.out.println(index);
	        if(index > 0)
	        {
	        	//TODO go to the actual line in the new file, refresh plugin to work on new page.
	        }
		}
	}
	
	private static void goToLine(IEditorPart editorPart, int index) {
		if (!(editorPart instanceof ITextEditor) || index <= 0) {
		    return;
		}
		ITextEditor editor = (ITextEditor) editorPart;
		IDocument document = editor.getDocumentProvider().getDocument(editor.getEditorInput());
		if (document != null) {
		    IRegion lineInfo = null;
		    try {
		      lineInfo = document.getLineInformationOfOffset(index);
		    } catch (BadLocationException e) {
		      
		    }
		    if (lineInfo != null) {
		        editor.selectAndReveal(lineInfo.getOffset(), lineInfo.getLength());
		    }
		}
	}
}
