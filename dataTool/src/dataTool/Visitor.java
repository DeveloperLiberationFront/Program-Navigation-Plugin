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

import edu.pdx.cs.multiview.jdt.util.JDTUtils;

class Visitor extends ASTVisitor{
	
	private StackMap<Position,ASTNode> nodes = new StackMap<Position,ASTNode>();
	private static HashSet<String> data = new HashSet<String>();
	private static ArrayList<SimpleName> seenMethod = new ArrayList<SimpleName>();
	private static UpFinder upFinder;
	private static DownFinder downFinder;
	
	private static String source;
	
	public Visitor(String someSource) {
		this.source = someSource;
		parseData();
	}

	public void preVisit(ASTNode node){
		if(shouldVisit(node)) {
			add(node);
		}
	}
	
	/**
	 *  Function that returns the source code
	 * @return- source code as a str
	 */
	public String getSource() {
		return source;
	}
	
	/**
	 * Method to determine if the current node should be highlighted
	 * @param node
	 * @return boolean: True if selected node should be highlighted, else false
	 */
	private boolean shouldVisit(ASTNode node) {
		if (node instanceof SimpleName) {
			String name = ((SimpleName) node).getIdentifier();
			return data.contains(name);
		}
		return false;
	}
	
	/**
	 * Searches through code to find instances of the current variable
	 * @param currentData: String of current data selected
	 */
	private static void findOccurrences(String currentData) {
		int index = source.indexOf(currentData);
		while(index >= 0) {
			if(downFinder.variableCheck(currentData, index, source)) {
				downFinder.add(currentData, index, DataNode.VAR);
			}
			index = source.indexOf(currentData, index+1);
		}
	}
	
	/**
	 * Method that parses the source statically to get the data we want for the 
	 * plugin. This function searches for all parameters and declared variables
	 * and adds them to the data list.
	 * 
	 * http://stackoverflow.com/questions/15308080/how-to-get-all-visible-variables-for-a-certain-method-in-jdt
	 * @param str
	 */
	public static void parseData() {	
		char[] code = source.toCharArray();
		upFinder = UpFinder.getInstance();
		downFinder = DownFinder.getInstance();
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(code);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);

		final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
		cu.accept(new ASTVisitor() {

		    public boolean visit(VariableDeclarationFragment vdf) {
		    	String var = vdf.getName().getIdentifier();
		        if (!data.contains(var)) {
		        	data.add(var);
		        	upFinder.add(var, vdf.getName(), DataNode.CLASS_VAR);
		        }
		        return true;
		    }

		    public boolean visit(MethodDeclaration md) {
		    	if (!seenMethod.contains(md.getName())) {
		    		seenMethod.add(md.getName());
		    		String param = "";
		    		for (Object o: md.parameters()) {
		    			param = ((SingleVariableDeclaration) o).getName().getIdentifier();
		    			data.add(param);
		    			upFinder.add(param, (SingleVariableDeclaration) o, DataNode.PARAM_UP);
		    			findOccurrences(param);
		    		}
		    		
	               	md.accept(new ASTVisitor() {
			            public boolean visit(VariableDeclarationFragment vdf) {
			            	String var = vdf.getName().getIdentifier();
			               	if (!data.contains(var)) {
			               		data.add(var);
			               		upFinder.add(var, vdf.getName(), DataNode.VAR_DECL);
			               	}			
			               	findOccurrences(var);
			               	return true;
			            }
			            
			            public boolean visit(EnhancedForStatement efs) {
			            	SimpleName forThis = efs.getParameter().getName();
			            	String forStr = forThis.getIdentifier();
			            	data.add(forThis.getIdentifier());
			            	upFinder.add(forThis.getIdentifier(), forThis, DataNode.FOR_VAR);
			            	Statement body = efs.getBody();
			            	findOccurrences(forStr);
			            	return true;
			            }
			            public boolean visit(ForStatement fs) {
			            	List<Expression> conds = fs.initializers();
			            	for(ASTNode e: conds) {
			            		VariableDeclarationExpression temp = (VariableDeclarationExpression)e;
			            		for(Object frag: temp.fragments()) {
			            			SimpleName forThis = ((VariableDeclarationFragment) frag).getName();
			            			String forStr = forThis.getIdentifier();
			            			data.add(forStr);
			            			upFinder.add(forStr, forThis, DataNode.FOR_VAR);
			            			Statement body = fs.getBody();
			            			findOccurrences(forStr);
			            		}
			            	}
			            	return true;
			            }
			            public boolean visit(WhileStatement ws) {
			            	String cond = ws.getExpression().toString();
			            	for(String found: data) {
			            		if(cond.contains(found)){
			            			downFinder.add(found, ws.getExpression().getStartPosition() + cond.indexOf(found), DataNode.VAR);
			            		}
			            		Statement body = ws.getBody();
			            		findOccurrences(found);
			            	}
			            	return true;
			            }
			            
			            public boolean visit(TryStatement ts) {
			            	List<CatchClause> catches = ts.catchClauses();
			            	SimpleName e;
			            	for(CatchClause error: catches) {
			            		e = error.getException().getName();
			            		data.add(e.getIdentifier());
			            		upFinder.add(e.getIdentifier(), e, DataNode.VAR);
			            		Statement errorCode = error.getBody();
			            		findOccurrences(e.getIdentifier());
			            	}
			            	return true;
			            }
			            
			            public boolean visit(MethodInvocation mi) {
			            	List<ASTNode> args = mi.arguments();
			            	for(ASTNode arg: args) {
			            		if(data.contains(arg.toString())) {
			            			downFinder.add(arg, DataNode.PARAM_DOWN);
			            		}
				            }
			            	return true;
			            }
			            public boolean visit(Expression expr) {
			            	//Other expressions
			            	return true;
			            }
			        });
		    	}
			    return true;
		    }
		});
	}

	/**
	 * Function that returns the data to be highlighted
	 * @returns HashSet of data found during parsing
	 */
	public HashSet<String> getData() {
		return data;
	}
	/**
	 * Adds the current node to list of nodes to be highlighted
	 * @param node
	 */
	private void add(ASTNode node) {
		int start = node.getStartPosition();
		int length = node.getLength();
		nodes.put(new Position(start,length),node);
	}
	
	public ASTNode statementAt(int index){
		for(Position p : nodes.keyStack()){			
			boolean isContained = p.offset <= index && index < p.offset + p.length;								
			if(isContained){
				return nodes.get(p);
			}
		}
		return null;
	}
	
	private class StackMap<K,V> extends HashMap<K, V>{
		
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
	static class BackwardStack<X> extends Stack<X>{

		private static final long serialVersionUID = -8981676925135756869L;

		@Override
		public Iterator<X> iterator(){

			List<X> list = new ArrayList<X>(this.size());
			for(int i = size()-1; i>=0; i--)
				list.add(get(i));
			
			return list.iterator();
		}
	}
}