/**
 * 
 */
package dataTool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Statement;

import edu.pdx.cs.multiview.jdt.util.JDTUtils;

class StatementVisitor extends ASTVisitor{
	
	private StackMap<Position,ASTNode> statements = 
		new StackMap<Position,ASTNode>();
	
	private String source;
	
	public StatementVisitor(String someSource) {
		this.source = someSource;
	}

	public void preVisit(ASTNode node){
		if(shouldVisit(node))
			add(node);
	}
	
	private boolean shouldVisit(ASTNode node) {
		return (node instanceof Statement)/* ||
				(node instanceof Expression)*/;
	}

	private void add(ASTNode node) {
		// TODO: we don't just want whitespace, we want comments too
		int whiteSpaceBefore = JDTUtils.whiteSpaceBefore(node,source);
		int whiteSpaceAfter = JDTUtils.whiteSpaceAfter(node,source);
		int start = node.getStartPosition();
		int length = node.getLength();
		statements.put(new Position(start,length,whiteSpaceBefore,whiteSpaceAfter),node);
	}
	
	public ASTNode statementAt(int index, boolean trueBeforeFalseAfter){
		
		for(Position p : statements.keyStack()){			
			boolean isContained = trueBeforeFalseAfter ? 
									p.includesWithBefore(index) : 
									p.includesWithAfter(index)	;								
			if(isContained){
				return statements.get(p);
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
	
	private class Position extends org.eclipse.jface.text.Position{
		
		private int whitespaceBefore, whitespaceAfter;
		
		public Position(int offset, int length, int whiteSpaceBefore, int whiteSpaceAfter){
			super(offset,length);
			this.whitespaceBefore = whiteSpaceBefore;
			this.whitespaceAfter = whiteSpaceAfter;
		}
		
		public boolean includesWithBefore(int index){
			return (this.offset-whitespaceBefore <= index) && (index < this.offset + length + whitespaceBefore);
		}
		
		public boolean includesWithAfter(int index){
			return (this.offset <= index) && (index < this.offset + length + whitespaceAfter);
		}
	}
}