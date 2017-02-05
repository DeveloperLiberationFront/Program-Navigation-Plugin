/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.corext.dom;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;


public class CodeScopeBuilder extends ASTVisitor {

	public static class Scope {
		private Scope fParent;
		private int fStart;
		private int fLength;
		private List fNames;
		private List fChildren;
		private int fCursorOffset;
		Scope(Scope parent, int start, int length) {
			fParent= parent;
			fStart= start;
			fLength= length;
			if (fParent != null)
				fParent.addChild(this);
		}
		public void setCursor(int offset) {
			fCursorOffset= offset;
		}
		private void addChild(Scope child) {
			if (fChildren == null)
				fChildren= new ArrayList(2);
			fChildren.add(child);
		}
		private void addName(String name) {
			if (fNames == null)
				fNames= new ArrayList(2);
			fNames.add(name);
		}
		public Scope findScope(int start, int length) {
			if (fStart <= start && start + length <= fStart + fLength) {
				if (fChildren == null)
					return this;
				for (Iterator iter= fChildren.iterator(); iter.hasNext();) {
					Scope scope= ((Scope)iter.next()).findScope(start, length);
					if (scope != null)
						return scope;
				}
				return this;
			}
			return null;
		}
		public String createName(String candidate, boolean add) {
			int i= 1;
			String result= candidate;
			while(isInUse(result)) {
				result= candidate + i++;
			}
			if (add)
				addName(result);
			return result;
		}
		public boolean isInUse(String name) {
			if (internalIsInUse(name))
				return true;
			if (fChildren != null) {
				for (Iterator iter= fChildren.iterator(); iter.hasNext();) {
					Scope child= (Scope) iter.next();
					if (fCursorOffset < child.fStart && child.isInUseDown(name)) {
						return true;
					}
				}
			}
			return false;
		}
		private boolean internalIsInUse(String name) {
			if (fNames != null && fNames.contains(name))
				return true;
			if (fParent != null)
				return fParent.internalIsInUse(name);
			return false;

		}
		private boolean isInUseDown(String name) {
			if (fNames != null && fNames.contains(name))
				return true;
			if (fChildren == null)
				return false;
			for (Iterator iter= fChildren.iterator(); iter.hasNext();) {
				Scope scope= (Scope) iter.next();
				if (scope.isInUseDown(name))
					return true;
			}
			return false;
		}
	}

	private IBinding fIgnoreBinding;
	private Selection fIgnoreRange;
	private Scope fScope;
	private List fScopes;

	public static Scope perform(BodyDeclaration node, IBinding ignore) {
		CodeScopeBuilder collector= new CodeScopeBuilder(node, ignore);
		node.accept(collector);
		return collector.fScope;
	}

	public static Scope perform(BodyDeclaration node, Selection ignore) {
		CodeScopeBuilder collector= new CodeScopeBuilder(node, ignore);
		node.accept(collector);
		return collector.fScope;
	}

	private CodeScopeBuilder(ASTNode node, IBinding ignore) {
		fScope= new Scope(null, node.getStartPosition(), node.getLength());
		fScopes= new ArrayList();
		fIgnoreBinding= ignore;
	}

	private CodeScopeBuilder(ASTNode node, Selection ignore) {
		fScope= new Scope(null, node.getStartPosition(), node.getLength());
		fScopes= new ArrayList();
		fIgnoreRange= ignore;
	}

	public boolean visit(CatchClause node) {
		// open a new scope for the exception declaration.
		fScopes.add(fScope);
		fScope= new Scope(fScope, node.getStartPosition(), node.getLength());
		return true;
	}

	public void endVisit(CatchClause node) {
		fScope= (Scope)fScopes.remove(fScopes.size() - 1);
	}

	public boolean visit(SimpleName node) {
		if (fIgnoreBinding != null && Bindings.equals(fIgnoreBinding, node.resolveBinding()))
			return false;
		if (fIgnoreRange != null && fIgnoreRange.covers(node))
			return false;
		fScope.addName(node.getIdentifier());
		return false;
	}

	public boolean visit(QualifiedName node) {
		// only consider the left most identifier.
		node.getQualifier().accept(this);
		return false;
	}

	public boolean visit(MethodInvocation node) {
		Expression receiver= node.getExpression();
		if (receiver == null) {
			SimpleName name= node.getName();
			if (fIgnoreBinding == null || !Bindings.equals(fIgnoreBinding, name.resolveBinding()))
				node.getName().accept(this);
		} else {
			receiver.accept(this);
		}
		accept(node.arguments());
		return false;
	}

	public boolean visit(TypeDeclarationStatement node) {
		if (node.getAST().apiLevel() == AST.JLS2) {
			fScope.addName(node.getTypeDeclaration().getName().getIdentifier());
		} else {
			fScope.addName(node.getDeclaration().getName().getIdentifier());
		}
		return false;
	}

	public boolean visit(Block node) {
		fScopes.add(fScope);
		fScope= new Scope(fScope, node.getStartPosition(), node.getLength());
		return true;
	}

	public void endVisit(Block node) {
		fScope= (Scope)fScopes.remove(fScopes.size() - 1);
	}

	public boolean visit(ForStatement node) {
		fScopes.add(fScope);
		fScope= new Scope(fScope, node.getStartPosition(), node.getLength());
		return true;
	}

	public void endVisit(ForStatement node) {
		fScope= (Scope)fScopes.remove(fScopes.size() - 1);
	}

	private void accept(List list) {
		int size;
		if (list == null || (size= list.size()) == 0)
			return;
		for (int i= 0; i < size; i++) {
			((ASTNode)list.get(i)).accept(this);
		}
	}
}
