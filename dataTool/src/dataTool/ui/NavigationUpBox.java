package dataTool.ui;

import java.awt.MouseInfo;
import java.awt.Toolkit;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.ITextEditor;

import dataTool.AnnotationManager;
import dataTool.EnableNavigationAction;
import dataTool.Finder;
import edu.pdx.cs.multiview.jdt.util.JDTUtils;
import sun.misc.IOUtils;

public class NavigationUpBox {
	
	private static Display display;
	private static Shell shell;
	private static StyledText widget;
	private int offset;
	private static NavigationUpBox instance;
	private static boolean resize = false;
	private static ASTNode searchResult = null;
	public String searchMethod = "";
	
	private NavigationUpBox(StyledText text, int start) {
		widget = text;
		offset = start;
	}
	
	/**
	 * Creates singleton instance of the top UI box
	 * @param st: currently selected StyledText
	 * @param start: Offset of the current node
	 */
	public static void createInstance(StyledText st, int start) {
		if(instance == null) {
			instance = new NavigationUpBox(st, start);
			showLabel();
		}
	}
	
	/**
	 * Returns current instance of the top box
	 * @returns NavigationUpBox instance
	 */
	public static NavigationUpBox getInstance() {
		resize = false;
		return instance;
	}
	
	public static void showLabel() {
		if(shell != null) {
			dispose();
		}
		display = Display.getDefault();
	    shell = new Shell(display, SWT.ON_TOP);
	    shell.setLayout(new RowLayout());
	    setSize();
	    shell.open();
	  }
	
	/**
	 * Method to set the size and location of the shell
	 */
	private static void setSize() {
		Canvas c = (Canvas) widget.getParent();
    	Composite comp = c.getParent();
    	comp.addListener(SWT.Resize, new Listener() {

			@Override
			public void handleEvent(Event arg0) {
				if(!resize) {
					resize = true;
					showLabel();
				}
			}
    	});
    	
    	shell.setSize(comp.getClientArea().width-30, 35);
	    shell.setLocation(comp.toDisplay(comp.getLocation()).x,comp.toDisplay(comp.getLocation()).y);
	}
	
	/**
	 * Sets the text for the top box of the user interface
	 * @param set: Set of methods calling current node
	 */
	public void setText(Set<IMethod> set) throws JavaModelException {
		for(Control c: shell.getChildren()) {
			c.dispose();
		}
		if(set != null) {
	    	for(IMethod i: set) {
	    		DataLink l = new DataLink(i, i.getElementName(), i.getPath().toString());
	    		Link link = new Link(shell, SWT.NULL);
		    	link.setText(l.getText());
		    	link.addListener(SWT.Selection, new Listener() {
		    		@Override
					public void handleEvent(Event arg0) {
		    			try {
							if(i.getSource() == null) {
		    					JOptionPane.showMessageDialog(null, DataLink.INVALID, "Error",JOptionPane.ERROR_MESSAGE);
		    				}
		    				else {
		    					searchMethod = AnnotationManager.currentSearch;
								openLink(i);
								//EnableNavigationAction plugin = new EnableNavigationAction();
				    			//plugin.reset();
		    				}
						} catch (JavaModelException e1) {
							// Auto-generated catch block
							e1.printStackTrace();
						}		    			
					}			    	
		    	});
		    }
	    }
		shell.pack();
		setSize();
	}
	
	/**
	 * Opens invocation of new method in the editor and clears navigation box links
	 * @param i: IMethod to open
	 */
	public void openLink(IMethod i) {
		IEditorPart editor = null;
		try {
			setText(null);
			NavigationDownBox.getInstance().setText(null);
			editor = JavaUI.openInEditor(i, true, true);
		} catch (JavaModelException | PartInitException e) {
			// Auto-generated catch block
			e.printStackTrace();
		}
		if(editor != null) {
			String code = JDTUtils.getCUSource((AbstractTextEditor) editor);
			lineSearch(code.toCharArray(), i);
			goToLine(editor);
		}
	}
	
	private void lineSearch(char[] source, IMethod method) {
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(source);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
		cu.accept(new ASTVisitor(){
			public boolean visit(MethodDeclaration md) {
				String methodName = md.getName().getIdentifier();
				md.accept(new ASTVisitor() {
				public boolean visit(MethodInvocation m) {
					if(method.getElementName().equals(methodName)) {
						if(m.getName().getIdentifier().equals(searchMethod)) {
							searchResult = m;
						}
					}
					return true;
				}
				public boolean visit(ClassInstanceCreation c) {
					//System.out.println("  "+c.getType().toString() +" " + AnnotationManager.currentSearch);
					return true;
				}
			});
				return true;
		}
		});
	}	
	/**
	 * Opens the new class at the specific line
	 * http://stackoverflow.com/questions/2873879/eclipe-pde-jump-to-line-x-and-highlight-it
	 * @param editorPart: current editor
	 * @param lineNumber: line number of method invocation
	 */
	private static void goToLine(IEditorPart editorPart) {
		  if (!(editorPart instanceof ITextEditor)) {
		    return;
		  }
		  ITextEditor editor = (ITextEditor) editorPart;
		  IDocument document = editor.getDocumentProvider().getDocument(editor.getEditorInput());
		  System.out.println("goto "+searchResult.toString()+" "+editor);
		  if (document != null) {
		    	editor.selectAndReveal(searchResult.getStartPosition(), searchResult.getLength());
		  }
		}

	/**
	 * Removes the top navigation box from view.
	 */
	public static void dispose() {
		if(shell != null) {
			shell.dispose();
			instance = null;
		}
	}
}