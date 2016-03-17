/**
 * Class to statically analyze the source code and handle the visited nodes.
 * 
 * @author emerson, Chris
 */
package dataTool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Stack;

import javax.xml.transform.Source;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BlockComment;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.internal.compiler.ast.Assignment;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.MethodRefParameter;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jface.text.Position;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;

import edu.pdx.cs.multiview.jdt.util.JDTUtils;

class Visitor extends ASTVisitor {

	private StackMap<Position, DataNode> nodes = new StackMap<Position, DataNode>();
	/** Maps a method's declaration to the list of Methods invoked inside of its body. */
	private HashMap<Method, ArrayList<Method>> declarationToInvocationMapDown = new HashMap<Method, ArrayList<Method>>();
	/** 
	 * Maps a method's invocation to the list of methods it is invoked in. 
	 * 
	 * Maps the method's SimpleName.resolveBinding().toString() to the list of Method Objects where that
	 * method is invoked.
	 */
	private HashMap<String, ArrayList<Method>> invocationToDeclarationMapUp = new HashMap<String, ArrayList<Method>>();
	private HashMap<SimpleName, DataNode> variableNameToNode = new HashMap<SimpleName, DataNode>();
	private HashSet<String> localVariableSet = new HashSet<String>();
	private Finder finder;

	private static String source;

	public Visitor(String someSource) {
		this.source = someSource;
		parseData();
	}

	/**
	 * Function that returns the source code
	 * 
	 * @return- source code as a str
	 */
	public String getSource() {
		return source;
	}

	/**
	 * Searches through code to find instances of the current variable
	 * 
	 * @param currentData:
	 *            String of current data selected
	 */
	private void addOccurrences(DataNode dn) {
		nodes.put(dn.getPosition(), dn);
		finder.add(dn);
	}

	/**
	 * Method that parses the source statically to get the data we want for the
	 * plugin. This function searches for all parameters and declared variables
	 * and adds them to the data list.
	 * 
	 * http://stackoverflow.com/questions/15308080/how-to-get-all-visible-
	 * variables-for-a-certain-method-in-jdt
	 * 
	 * @param str
	 */
	public void parseData() {
		IProject [] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		IJavaProject thisProject = JavaCore.create(projects[0]);
		IEditorPart activeEditor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		String[] pathArray = activeEditor.getTitleToolTip().split("/");
		String projectName = pathArray[0];
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
	    IProject project = root.getProject(projectName);
	    IJavaProject javaProject = JavaCore.create(project);
		char[] code = source.toCharArray();
		finder = Finder.getInstance();
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(code);
		parser.setResolveBindings(true);
		parser.setProject(javaProject);
		parser.setUnitName(projectName);
		
		parser.setUnitName(thisProject.getElementName());
		
		final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
		
		cu.accept(new ASTVisitor() {
			DataNode addedNode = null;
			SimpleName methodName = null;
			public boolean visit( SimpleName sn ) {
				if( sn.resolveTypeBinding() != null ) {
					// Add the node to the list
					String binding = sn.resolveBinding().toString();
					if( !binding.contains("(") && !binding.contains("class") && !binding.contains("interface")){
						getNodeFromName( sn );
					}
				}
				return true;
			}

			public boolean visit(MethodDeclaration md) {
				localVariableSet = new HashSet<String>();
				methodName = md.getName();
				String methodBinding = methodName.resolveBinding().toString();
				Method methodDeclaration = new Method( methodName);
				
				List<DataNode> params = new ArrayList<DataNode>();
				int i = -1;
				for (SingleVariableDeclaration svd : (List<SingleVariableDeclaration>)md.parameters()) {
					i++;
					SimpleName paramName = svd.getName();
					addedNode = getNodeFromName(paramName);
					if( addedNode != null ) {
						addedNode.setDeclarationMethod(methodDeclaration);
						addedNode.setParameterIndex(i);
						params.add(addedNode);
					}
				}
				methodDeclaration.setArgs(params);
				
				declarationToInvocationMapDown.put( methodDeclaration, new ArrayList<Method>());
				md.accept(new ASTVisitor() {
					public boolean visit( SimpleName sn ) {
						addedNode = getNodeFromName(sn);
						if( addedNode != null ) {
							for( DataNode dn : params ) {
								if( addedNode.getKey().equals(dn.getKey() ) ) {
									addedNode.setDeclarationMethod(methodDeclaration);
									addedNode.setParameterIndex(dn.getParameterIndex());
								}
							}
						}
						return true;
					}
					
					public boolean visit(MethodInvocation mi) {
						
						SimpleName invokedName = mi.getName();
						if( !invocationToDeclarationMapUp.containsKey(invokedName.resolveBinding().toString()) ) {
							invocationToDeclarationMapUp.put(invokedName.resolveBinding().toString(), new ArrayList<Method>());
						}
						List<Expression> args = mi.arguments();
						Method methodInvocation = new Method( mi.getName());
						List<DataNode> nodeArgs = new ArrayList<DataNode>();
						int i = -1;
						for( Expression e : args ) {
							if( e.getNodeType() == ASTNode.SIMPLE_NAME ) {
								i++;
								SimpleName n = (SimpleName) e;
								addedNode = getNodeFromName(n);
								// TODO shouldn't need a check here
								if( addedNode != null ) {
									addedNode.setInvocationMethod(methodInvocation);
									addedNode.setParameterIndex(i);
									nodeArgs.add( addedNode );
								}
							} else {
								nodeArgs.add(null);
							}
						}
						methodInvocation.setArgs(nodeArgs);
						invocationToDeclarationMapUp.get(invokedName.resolveBinding().toString()).add(methodInvocation);
						declarationToInvocationMapDown.get(methodDeclaration).add(methodInvocation);
						
						return true;
					}
				});
				
				
				//Set to null so that class variables can have a null method name
				return true;
			}
			private DataNode getNodeFromName( SimpleName sn ) {
				if( sn.resolveBinding() != null ) {
					String binding = sn.resolveBinding().toString();
					if( !binding.contains("(") && !binding.contains("class" ) ) {	
						if( variableNameToNode.containsKey(sn) ) {
							return variableNameToNode.get(sn);
						} else {
							DataNode addedNode = new DataNode( sn );
							addedNode.setStartPosition(cu.getExtendedStartPosition(sn) );
							variableNameToNode.put(sn, addedNode);
							return addedNode;
						}
					}
				}
				return null;
			}
		});
		synchronizeNodes();
		
	}
	
	private void synchronizeNodes() {
		
		for( Entry e: variableNameToNode.entrySet() ) {
			addOccurrences(( DataNode ) e.getValue());
		}
		finder.setInvocationToDeclarationMap(invocationToDeclarationMapUp);
		finder.setDeclarationToInvocationMap(declarationToInvocationMapDown);
	}

	public DataNode statementAt(int index) {
		for (Position p : nodes.keyStack()) {
			boolean isContained = p.offset <= index && index < p.offset + p.length;
			if (isContained) {
				return nodes.get(p);
			}
		}
		return null;
	}

	private class StackMap<K, V> extends HashMap<K, V> {

		private static final long serialVersionUID = -266310554828357936L;
		private Stack<K> stack = new BackwardStack<K>();

		@Override
		public V put(K arg0, V arg1) {
			stack.push(arg0);
			return super.put(arg0, arg1);
		}

		public Stack<K> keyStack() {
			return stack;
		}
	}

	/**
	 * A stack that you can iterate backwards through
	 * 
	 * @author emerson
	 *
	 * @param <X>
	 */
	static class BackwardStack<X> extends Stack<X> {

		private static final long serialVersionUID = -8981676925135756869L;

		@Override
		public Iterator<X> iterator() {

			List<X> list = new ArrayList<X>(this.size());
			for (int i = size() - 1; i >= 0; i--)
				list.add(get(i));

			return list.iterator();
		}
	}
}