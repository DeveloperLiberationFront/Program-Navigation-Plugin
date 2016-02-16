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

/**
 * Probably don't need this class anymore going forward
 * @author Chris
 *
 */
public class DataLink {
	private String name;
	private String location;
	private IMethod method;
	
	final public static String INVALID = "Method does not exist in project source.";
	
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
}
