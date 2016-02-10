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
import java.util.Stack;

import javax.xml.transform.Source;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BlockComment;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.MethodRefParameter;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jface.text.Position;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;

import edu.pdx.cs.multiview.jdt.util.JDTUtils;

class Visitor extends ASTVisitor {

	private StackMap<Position, ASTNode> nodes = new StackMap<Position, ASTNode>();
	private static ArrayList<SimpleName> seenMethod = new ArrayList<SimpleName>();
	private static Finder finder;

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
	private static void addOccurrences(DataNode dn) {
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
	public static void parseData() {
		char[] code = source.toCharArray();
		finder = Finder.getInstance();
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(code);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);

		final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
		cu.accept(new ASTVisitor() {
			DataNode addedNode = null;
			SimpleName methodName = null;
			public boolean visit(VariableDeclarationFragment vdf) {
				String var = vdf.toString();
				String left = var.substring( 0, var.indexOf("=") ).trim();
				String right = var.substring( var.indexOf("=") + 1 ).trim();
				int offset;
				
				if( methodName != null ) {
					//If not a class variable.
					if( isVariable(left) ) {
						offset = vdf.getStartPosition();
						addedNode = new DataNode(left, offset, DataNode.CLASS_VAR, methodName.toString());
						addOccurrences(addedNode);
					}
					if( isVariable(right) ) {
						offset = vdf.getStartPosition() + vdf.toString().indexOf(right);
						addedNode = new DataNode(right, offset, DataNode.CLASS_VAR, methodName.toString());
						addOccurrences(addedNode);
					}
				} else {
					if( isVariable(left) ) {
						offset = vdf.getStartPosition();
						addedNode = new DataNode(left, offset, DataNode.CLASS_VAR, null);
						addOccurrences(addedNode);
					}
					if( isVariable(right) ) {
						offset = vdf.getStartPosition() + vdf.toString().indexOf(right);
						addedNode = new DataNode(right, offset, DataNode.CLASS_VAR, null);
						addOccurrences(addedNode);
					}
				}
				
				return true;
			}

			private boolean isVariable(String varName) {
				return varName.matches( "^[_a-z]\\w*$" );
			}

			public boolean visit(MethodDeclaration md) {
				methodName = md.getName();
				if (!seenMethod.contains(methodName)) {
					seenMethod.add(methodName);
					String param = "";
					String[] array = new String[md.parameters().size()];
					int i = 0;
					for (Object o : md.parameters()) {
						SingleVariableDeclaration svd = (SingleVariableDeclaration) o;
						array[i] = svd.toString();
						i++;
						param = svd.getName().getIdentifier();
						addedNode = new DataNode(param, svd.getStartPosition(), DataNode.PARAM_UP,
								methodName.toString());
						addOccurrences(addedNode );
					}

					md.accept(new ASTVisitor() {
						public boolean visit(VariableDeclarationFragment vdf) {
							String var = vdf.getName().getIdentifier();
							System.out.println("---" + var);
							addedNode = new DataNode(var.toString(), 
														vdf.getStartPosition(), 
														DataNode.PARAM_DOWN,
														methodName.toString());
							addOccurrences(addedNode );
							return true;
						}

						public boolean visit(EnhancedForStatement efs) {
							SingleVariableDeclaration svd = efs.getParameter();

							SimpleName forThis = svd.getName();
							int startPosition = svd.getStartPosition();
							String forStr = forThis.getIdentifier();

							
							addedNode = new DataNode(forThis.getIdentifier(), 
													 	startPosition, 
													 	DataNode.FOR_VAR,
													 	methodName.toString());
							Statement body = efs.getBody();
							addOccurrences(addedNode );
							return true;
						}

						public boolean visit(ForStatement fs) {
							List<Expression> conds = fs.initializers();
							for (ASTNode e : conds) {
								VariableDeclarationExpression temp = (VariableDeclarationExpression) e;
								for (Object frag : temp.fragments()) {
									SimpleName forThis = ((VariableDeclarationFragment) frag).getName();
									String forStr = forThis.getIdentifier();
									int startPosition = ((VariableDeclarationFragment) frag).getStartPosition();
									
									addedNode = new DataNode(forStr, 
																startPosition, 
																DataNode.FOR_VAR, 
																methodName.toString());
									Statement body = fs.getBody();
									addOccurrences(addedNode );
								}
							}
							return true;
						}

						public boolean visit(WhileStatement ws) {
							String cond = ws.getExpression().toString();
							//TODO look into while loop parsing syntax
//							for (String found : data) {
//								if (cond.contains(found)) {
//									int startPosition = ws.getExpression().getStartPosition() + cond.indexOf(found);
//									addedNode = new DataNode( found, 
//																startPosition, 
//																DataNode.VAR, 
//																methodName.toString());
//									downFinder.add(addedNode);
//								}
//								Statement body = ws.getBody();
//								addOccurrences(addedNode );
//							}
							return true;
						}

						public boolean visit(TryStatement ts) {
							List<CatchClause> catches = ts.catchClauses();
							SimpleName e;
							for (CatchClause error : catches) {
								e = error.getException().getName();
								int startPosition = error.getException().getStartPosition();
								addedNode = new DataNode( e.toString(), 
										startPosition, 
										DataNode.VAR, 
										methodName.toString());
								
								Statement errorCode = error.getBody();
								addOccurrences(addedNode );
							}
							return true;
						}

						public boolean visit(MethodInvocation mi) {
							List<ASTNode> args = mi.arguments();
							for (ASTNode arg : args) {
				
									int startPosition = arg.getStartPosition();
									addedNode = new DataNode(arg.toString(), 
											startPosition,
											DataNode.PARAM_DOWN, 
											mi.getName().toString());
									addOccurrences(addedNode);
							}
							return true;
						}
					});
				}
				//Set to null so that class variables can have a null method name
				//methodName = null;
				return true;
			}
		});
	}


	/**
	 * Adds the current node to list of nodes to be highlighted
	 * 
	 * @param node
	 */
	private void add(ASTNode node) {
		int start = node.getStartPosition();
		int length = node.getLength();
		nodes.put(new Position(start, length), node);
	}

	public ASTNode statementAt(int index) {
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