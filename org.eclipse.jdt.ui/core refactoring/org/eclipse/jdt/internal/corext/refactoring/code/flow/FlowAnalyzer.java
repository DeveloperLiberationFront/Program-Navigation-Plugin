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
package org.eclipse.jdt.internal.corext.refactoring.code.flow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.core.dom.WildcardType;

import org.eclipse.jdt.internal.corext.dom.GenericVisitor;

/**
 * Special flow analyzer to determine the return value of the extracted method
 * and the variables which have to be passed to the method.
 *
 * Note: This analyzer doesn't do a full flow analysis. For example it doesn't
 * do dead code analysis or variable initialization analysis. It analyses the
 * the first access to a variable (read or write) and if all execution paths
 * return a value.
 */
abstract class FlowAnalyzer extends GenericVisitor {

	static protected class SwitchData {
		private boolean fHasDefaultCase;
		private List fRanges= new ArrayList(4);
		private List fInfos= new ArrayList(4);
		public void setHasDefaultCase() {
			fHasDefaultCase= true;
		}
		public boolean hasDefaultCase() {
			return fHasDefaultCase;
		}
		public void add(IRegion range, FlowInfo info) {
			fRanges.add(range);
			fInfos.add(info);
		}
		public IRegion[] getRanges() {
			return (IRegion[]) fRanges.toArray(new IRegion[fRanges.size()]);
		}
		public FlowInfo[] getInfos() {
			return (FlowInfo[]) fInfos.toArray(new FlowInfo[fInfos.size()]);
		}
		public FlowInfo getInfo(int index) {
			return (FlowInfo)fInfos.get(index);
		}
	}

	private HashMap fData = new HashMap(100);
	/* package */ FlowContext fFlowContext= null;

	public FlowAnalyzer(FlowContext context) {
		fFlowContext= context;
	}

	protected abstract boolean createReturnFlowInfo(ReturnStatement node);

	protected abstract boolean traverseNode(ASTNode node);

	protected boolean skipNode(ASTNode node) {
		return !traverseNode(node);
	}

	protected final boolean visitNode(ASTNode node) {
		return traverseNode(node);
	}

	//---- Hooks to create Flow info objects. User may introduce their own infos.

	protected ReturnFlowInfo createReturn(ReturnStatement statement) {
		return new ReturnFlowInfo(statement);
	}

	protected ThrowFlowInfo createThrow() {
		return new ThrowFlowInfo();
	}

	protected BranchFlowInfo createBranch(SimpleName label) {
		return new BranchFlowInfo(label, fFlowContext);
	}

	protected GenericSequentialFlowInfo createSequential() {
		return new GenericSequentialFlowInfo();
	}

	protected ConditionalFlowInfo createConditional() {
		return new ConditionalFlowInfo();
	}

	protected EnhancedForFlowInfo createEnhancedFor() {
		return new EnhancedForFlowInfo();
	}

	protected ForFlowInfo createFor() {
		return new ForFlowInfo();
	}

	protected TryFlowInfo createTry() {
		return new TryFlowInfo();
	}

	protected WhileFlowInfo createWhile() {
		return new WhileFlowInfo();
	}

	protected IfFlowInfo createIf() {
		return new IfFlowInfo();
	}

	protected DoWhileFlowInfo createDoWhile() {
		return new DoWhileFlowInfo();
	}

	protected SwitchFlowInfo createSwitch() {
		return new SwitchFlowInfo();
	}

	protected BlockFlowInfo createBlock() {
		return new BlockFlowInfo();
	}

	protected MessageSendFlowInfo createMessageSendFlowInfo() {
		return new MessageSendFlowInfo();
	}

	protected FlowContext getFlowContext() {
		return fFlowContext;
	}

	//---- Helpers to access flow analysis objects ----------------------------------------

	protected FlowInfo getFlowInfo(ASTNode node) {
		return (FlowInfo)fData.remove(node);
	}

	protected void setFlowInfo(ASTNode node, FlowInfo info) {
		fData.put(node, info);
	}

	protected FlowInfo assignFlowInfo(ASTNode target, ASTNode source) {
		FlowInfo result= getFlowInfo(source);
		setFlowInfo(target, result);
		return result;
	}

	protected FlowInfo accessFlowInfo(ASTNode node) {
		return (FlowInfo)fData.get(node);
	}

	//---- Helpers to process sequential flow infos -------------------------------------

	protected GenericSequentialFlowInfo processSequential(ASTNode parent, List nodes) {
		GenericSequentialFlowInfo result= createSequential(parent);
		process(result, nodes);
		return result;
	}

	protected GenericSequentialFlowInfo processSequential(ASTNode parent, ASTNode node1) {
		GenericSequentialFlowInfo result= createSequential(parent);
		if (node1 != null)
			result.merge(getFlowInfo(node1), fFlowContext);
		return result;
	}

	protected GenericSequentialFlowInfo processSequential(ASTNode parent, ASTNode node1, ASTNode node2) {
		GenericSequentialFlowInfo result= createSequential(parent);
		if (node1 != null)
			result.merge(getFlowInfo(node1), fFlowContext);
		if (node2 != null)
			result.merge(getFlowInfo(node2), fFlowContext);
		return result;
	}

	protected GenericSequentialFlowInfo createSequential(ASTNode parent) {
		GenericSequentialFlowInfo result= createSequential();
		setFlowInfo(parent, result);
		return result;
	}

	protected GenericSequentialFlowInfo createSequential(List nodes) {
		GenericSequentialFlowInfo result= createSequential();
		process(result, nodes);
		return result;
	}

	//---- Generic merge methods --------------------------------------------------------

	protected void process(GenericSequentialFlowInfo info, List nodes) {
		if (nodes == null)
			return;
		for (Iterator iter= nodes.iterator(); iter.hasNext();) {
			info.merge(getFlowInfo((ASTNode)iter.next()), fFlowContext);
		}
	}

	protected void process(GenericSequentialFlowInfo info, ASTNode node) {
		if (node != null)
			info.merge(getFlowInfo(node), fFlowContext);
	}

	protected void process(GenericSequentialFlowInfo info, ASTNode node1, ASTNode node2) {
		if (node1 != null)
			info.merge(getFlowInfo(node1), fFlowContext);
		if (node2 != null)
			info.merge(getFlowInfo(node2), fFlowContext);
	}

	//---- special visit methods -------------------------------------------------------

	public boolean visit(EmptyStatement node) {
		// Empty statements aren't of any interest.
		return false;
	}

	public boolean visit(TryStatement node) {
		if (traverseNode(node)) {
			fFlowContext.pushExcptions(node);
			node.getBody().accept(this);
			fFlowContext.popExceptions();
			List catchClauses= node.catchClauses();
			for (Iterator iter= catchClauses.iterator(); iter.hasNext();) {
				((CatchClause)iter.next()).accept(this);
			}
			Block finallyBlock= node.getFinally();
			if (finallyBlock != null) {
				finallyBlock.accept(this);
			}
		}
		return false;
	}

	//---- Helper to process switch statement ----------------------------------------

	protected SwitchData createSwitchData(SwitchStatement node) {
		SwitchData result= new SwitchData();
		List statements= node.statements();
		if (statements.isEmpty())
			return result;

		int start= -1, end= -1;
		GenericSequentialFlowInfo info= null;

		for (Iterator iter= statements.iterator(); iter.hasNext(); ) {
			Statement statement= (Statement)iter.next();
			if (statement instanceof SwitchCase) {
				SwitchCase switchCase= (SwitchCase)statement;
				if (switchCase.isDefault()) {
					result.setHasDefaultCase();
				}
				if (info == null) {
					info= createSequential();
					start= statement.getStartPosition();
				} else {
					if (info.isReturn() || info.isPartialReturn() || info.branches()) {
						result.add(new Region(start, end - start + 1), info);
						info= createSequential();
						start= statement.getStartPosition();
					}
				}
			} else {
				info.merge(getFlowInfo(statement), fFlowContext);
			}
			end= statement.getStartPosition() + statement.getLength() - 1;
		}
		result.add(new Region(start, end - start + 1), info);
		return result;
	}

	protected void endVisit(SwitchStatement node, SwitchData data) {
		SwitchFlowInfo switchFlowInfo= createSwitch();
		setFlowInfo(node, switchFlowInfo);
		switchFlowInfo.mergeTest(getFlowInfo(node.getExpression()), fFlowContext);
		FlowInfo[] cases= data.getInfos();
		for (int i= 0; i < cases.length; i++)
			switchFlowInfo.mergeCase(cases[i], fFlowContext);
		switchFlowInfo.mergeDefault(data.hasDefaultCase(), fFlowContext);
		switchFlowInfo.removeLabel(null);
	}

	//---- concret endVisit methods ---------------------------------------------------

	public void endVisit(AnnotationTypeDeclaration node) {
		if (skipNode(node))
			return;
		GenericSequentialFlowInfo info= processSequential(node, node.bodyDeclarations());
		info.setNoReturn();
	}

	public void endVisit(AnnotationTypeMemberDeclaration node) {
		if (skipNode(node))
			return;
		GenericSequentialFlowInfo info= processSequential(node, node.getType(), node.getDefault());
		info.setNoReturn();
	}

	public void endVisit(AnonymousClassDeclaration node) {
		if (skipNode(node))
			return;
		FlowInfo info= processSequential(node, node.bodyDeclarations());
		info.setNoReturn();
	}

	public void endVisit(ArrayAccess node) {
		if (skipNode(node))
			return;
		processSequential(node, node.getArray(), node.getIndex());
	}

	public void endVisit(ArrayCreation node) {
		if (skipNode(node))
			return;
		GenericSequentialFlowInfo info= processSequential(node, node.getType());
		process(info, node.dimensions());
		process(info, node.getInitializer());
	}

	public void endVisit(ArrayInitializer node) {
		if (skipNode(node))
			return;
		processSequential(node, node.expressions());
	}

	public void endVisit(ArrayType node) {
		if (skipNode(node))
			return;
		processSequential(node, node.getElementType());
	}

	public void endVisit(AssertStatement node) {
		if (skipNode(node))
			return;
		IfFlowInfo info= new IfFlowInfo();
		setFlowInfo(node, info);
		info.mergeCondition(getFlowInfo(node.getExpression()), fFlowContext);
		info.merge(getFlowInfo(node.getMessage()), null, fFlowContext);
	}

	public void endVisit(Assignment node) {
		if (skipNode(node))
			return;
		FlowInfo lhs= getFlowInfo(node.getLeftHandSide());
		FlowInfo rhs= getFlowInfo(node.getRightHandSide());
		if (lhs instanceof LocalFlowInfo) {
			LocalFlowInfo llhs= (LocalFlowInfo)lhs;
			llhs.setWriteAccess(fFlowContext);
			if (node.getOperator() != Assignment.Operator.ASSIGN) {
				GenericSequentialFlowInfo tmp= createSequential();
				tmp.merge(new LocalFlowInfo(llhs, FlowInfo.READ, fFlowContext), fFlowContext);
				tmp.merge(rhs, fFlowContext);
				rhs= tmp;
			}
		}
		GenericSequentialFlowInfo info= createSequential(node);
		// first process right and side and then left hand side.
		info.merge(rhs, fFlowContext);
		info.merge(lhs, fFlowContext);
	}

	public void endVisit(Block node) {
		if (skipNode(node))
			return;
		BlockFlowInfo info= createBlock();
		setFlowInfo(node, info);
		process(info, node.statements());
	}

	public void endVisit(BooleanLiteral node) {
		// Leaf node.
	}

	public void endVisit(BreakStatement node) {
		if (skipNode(node))
			return;
		setFlowInfo(node, createBranch(node.getLabel()));
	}

	public void endVisit(CastExpression node) {
		if (skipNode(node))
			return;
		processSequential(node, node.getType(), node.getExpression());
	}

	public void endVisit(CatchClause node) {
		if (skipNode(node))
			return;
		processSequential(node, node.getException(), node.getBody());
	}

	public void endVisit(CharacterLiteral node) {
		// Leaf node.
	}

	public void endVisit(ClassInstanceCreation node) {
		if (skipNode(node))
			return;
		GenericSequentialFlowInfo info= processSequential(node, node.getExpression());
		process(info, node.getType());
		process(info, node.arguments());
		process(info, node.getAnonymousClassDeclaration());
	}

	public void endVisit(CompilationUnit node) {
		if (skipNode(node))
			return;
		GenericSequentialFlowInfo info= processSequential(node, node.imports());
		process(info, node.types());
	}

	public void endVisit(ConditionalExpression node) {
		if (skipNode(node))
			return;
		ConditionalFlowInfo info= createConditional();
		setFlowInfo(node, info);
		info.mergeCondition(getFlowInfo(node.getExpression()), fFlowContext);
		info.merge(
			getFlowInfo(node.getThenExpression()),
			getFlowInfo(node.getElseExpression()),
			fFlowContext);
	}

	public void endVisit(ConstructorInvocation node) {
		if (skipNode(node))
			return;
		processSequential(node, node.arguments());
	}

	public void endVisit(ContinueStatement node) {
		if (skipNode(node))
			return;
		setFlowInfo(node, createBranch(node.getLabel()));
	}

	public void endVisit(DoStatement node) {
		if (skipNode(node))
			return;
		DoWhileFlowInfo info= createDoWhile();
		setFlowInfo(node, info);
		info.mergeAction(getFlowInfo(node.getBody()), fFlowContext);
		info.mergeCondition(getFlowInfo(node.getExpression()), fFlowContext);
		info.removeLabel(null);
	}

	public void endVisit(EmptyStatement node) {
		// Leaf node.
	}

	public void endVisit(EnhancedForStatement node) {
		if (skipNode(node))
			return;
		EnhancedForFlowInfo forInfo= createEnhancedFor();
		setFlowInfo(node, forInfo);
		forInfo.mergeParameter(getFlowInfo(node.getParameter()), fFlowContext);
		forInfo.mergeExpression(getFlowInfo(node.getExpression()), fFlowContext);
		forInfo.mergeAction(getFlowInfo(node.getBody()), fFlowContext);
		forInfo.removeLabel(null);
	}

	public void endVisit(EnumConstantDeclaration node) {
		if (skipNode(node))
			return;
		GenericSequentialFlowInfo info= processSequential(node, node.arguments());
		process(info, node.getAnonymousClassDeclaration());
	}

	public void endVisit(EnumDeclaration node) {
		if (skipNode(node))
			return;
		GenericSequentialFlowInfo info= processSequential(node, node.superInterfaceTypes());
		process(info, node.enumConstants());
		process(info, node.bodyDeclarations());
		info.setNoReturn();
	}

	public void endVisit(ExpressionStatement node) {
		if (skipNode(node))
			return;
		assignFlowInfo(node, node.getExpression());
	}

	public void endVisit(FieldAccess node) {
		if (skipNode(node))
			return;
		processSequential(node, node.getExpression(), node.getName());
	}

	public void endVisit(FieldDeclaration node) {
		if (skipNode(node))
			return;
		GenericSequentialFlowInfo info= processSequential(node, node.getType());
		process(info, node.fragments());
	}

	public void endVisit(ForStatement node) {
		if (skipNode(node))
			return;
		ForFlowInfo forInfo= createFor();
		setFlowInfo(node, forInfo);
		forInfo.mergeInitializer(createSequential(node.initializers()), fFlowContext);
		forInfo.mergeCondition(getFlowInfo(node.getExpression()), fFlowContext);
		forInfo.mergeAction(getFlowInfo(node.getBody()), fFlowContext);
		// Increments are executed after the action.
		forInfo.mergeIncrement(createSequential(node.updaters()), fFlowContext);
		forInfo.removeLabel(null);
	}

	public void endVisit(IfStatement node) {
		if (skipNode(node))
			return;
		IfFlowInfo info= createIf();
		setFlowInfo(node, info);
		info.mergeCondition(getFlowInfo(node.getExpression()), fFlowContext);
		info.merge(getFlowInfo(node.getThenStatement()), getFlowInfo(node.getElseStatement()), fFlowContext);
	}

	public void endVisit(ImportDeclaration node) {
		if (skipNode(node))
			return;
		assignFlowInfo(node, node.getName());
	}

	public void endVisit(InfixExpression node) {
		if (skipNode(node))
			return;
		GenericSequentialFlowInfo info= processSequential(node, node.getLeftOperand(), node.getRightOperand());
		process(info, node.extendedOperands());
	}

	public void endVisit(InstanceofExpression node) {
		if (skipNode(node))
			return;
		processSequential(node, node.getLeftOperand(), node.getRightOperand());
	}

	public void endVisit(Initializer node) {
		if (skipNode(node))
			return;
		assignFlowInfo(node, node.getBody());
	}

	public void endVisit(Javadoc node) {
		// no influence on flow analysis
	}

	public void endVisit(LabeledStatement node) {
		if (skipNode(node))
			return;
		FlowInfo info= assignFlowInfo(node, node.getBody());
		if (info != null)
			info.removeLabel(node.getLabel());
	}

	public void endVisit(MarkerAnnotation node) {
		// nothing to do for marker annotations;
	}

	public void endVisit(MemberValuePair node) {
		if (skipNode(node))
			return;

		FlowInfo name= getFlowInfo(node.getName());
		FlowInfo value= getFlowInfo(node.getValue());
		if (name instanceof LocalFlowInfo) {
			LocalFlowInfo llhs= (LocalFlowInfo)name;
			llhs.setWriteAccess(fFlowContext);
		}
		GenericSequentialFlowInfo info= createSequential(node);
		// first process value and then name.
		info.merge(value, fFlowContext);
		info.merge(name, fFlowContext);

	}

	public void endVisit(MethodDeclaration node) {
		if (skipNode(node))
			return;
		GenericSequentialFlowInfo info= processSequential(node, node.getReturnType2());
		process(info, node.parameters());
		process(info, node.thrownExceptions());
		process(info, node.getBody());
	}

	public void endVisit(MethodInvocation node) {
		endVisitMethodInvocation(node, node.getExpression(), node.arguments(), getMethodBinding(node.getName()));
	}

	public void endVisit(NormalAnnotation node) {
		if (skipNode(node))
			return;
		GenericSequentialFlowInfo info= processSequential(node, node.getTypeName());
		process(info, node.values());
	}

	public void endVisit(NullLiteral node) {
		// Leaf node.
	}

	public void endVisit(NumberLiteral node) {
		// Leaf node.
	}

	public void endVisit(PackageDeclaration node) {
		if (skipNode(node))
			return;
		assignFlowInfo(node, node.getName());
	}

	public void endVisit(ParameterizedType node) {
		if (skipNode(node))
			return;
		GenericSequentialFlowInfo info= processSequential(node, node.getType());
		process(info, node.typeArguments());
	}

	public void endVisit(ParenthesizedExpression node) {
		if (skipNode(node))
			return;
		assignFlowInfo(node, node.getExpression());
	}

	public void endVisit(PostfixExpression node) {
		endVisitIncDecOperation(node, node.getOperand());
	}

	public void endVisit(PrefixExpression node) {
		PrefixExpression.Operator op= node.getOperator();
		if (PrefixExpression.Operator.INCREMENT.equals(op) || PrefixExpression.Operator.DECREMENT.equals(op)) {
			endVisitIncDecOperation(node, node.getOperand());
		} else {
			assignFlowInfo(node, node.getOperand());
		}
	}

	public void endVisit(PrimitiveType node) {
		// Leaf node
	}

	public void endVisit(QualifiedName node) {
		if (skipNode(node))
			return;
		processSequential(node, node.getQualifier(), node.getName());
	}

	public void endVisit(QualifiedType node) {
		if (skipNode(node))
			return;
		processSequential(node, node.getQualifier(), node.getName());
	}

	public void endVisit(ReturnStatement node) {
		if (skipNode(node))
			return;

		if (createReturnFlowInfo(node)) {
			ReturnFlowInfo info= createReturn(node);
			setFlowInfo(node, info);
			info.merge(getFlowInfo(node.getExpression()), fFlowContext);
		} else {
			assignFlowInfo(node, node.getExpression());
		}
	}

	public void endVisit(SimpleName node) {
		if (skipNode(node) || node.isDeclaration())
			return;
		IBinding binding= node.resolveBinding();
		if (binding instanceof IVariableBinding) {
			IVariableBinding variable= (IVariableBinding)binding;
			if (!variable.isField()) {
				setFlowInfo(node, new LocalFlowInfo(
					variable,
					FlowInfo.READ,
					fFlowContext));
			}
		} else if (binding instanceof ITypeBinding) {
			ITypeBinding type= (ITypeBinding)binding;
			if (type.isTypeVariable()) {
				setFlowInfo(node, new TypeVariableFlowInfo(type, fFlowContext));
			}
		}
	}

	public void endVisit(SimpleType node) {
		if (skipNode(node))
			return;
		assignFlowInfo(node, node.getName());
	}

	public void endVisit(SingleMemberAnnotation node) {
		if (skipNode(node))
			return;
		assignFlowInfo(node, node.getValue());
	}

	public void endVisit(SingleVariableDeclaration node) {
		if (skipNode(node))
			return;

		IVariableBinding binding= node.resolveBinding();
		LocalFlowInfo nameInfo= null;
		Expression initializer= node.getInitializer();
		if (binding != null && !binding.isField() && initializer != null) {
			nameInfo= new LocalFlowInfo(binding, FlowInfo.WRITE, fFlowContext);
		}
		GenericSequentialFlowInfo info= processSequential(node, node.getType(), initializer);
		info.merge(nameInfo, fFlowContext);
	}

	public void endVisit(StringLiteral node) {
		// Leaf node
	}

	public void endVisit(SuperConstructorInvocation node) {
		endVisitMethodInvocation(node, node.getExpression(), node.arguments(), node.resolveConstructorBinding());
	}

	public void endVisit(SuperFieldAccess node) {
		if (skipNode(node))
			return;
		processSequential(node, node.getQualifier(), node.getName());
	}

	public void endVisit(SuperMethodInvocation node) {
		endVisitMethodInvocation(node, node.getQualifier(), node.arguments(), getMethodBinding(node.getName()));
	}

	public void endVisit(SwitchCase node) {
		endVisitNode(node);
	}

	public void endVisit(SwitchStatement node) {
		if (skipNode(node))
			return;
		endVisit(node, createSwitchData(node));
	}

	public void endVisit(SynchronizedStatement node) {
		if (skipNode(node))
			return;
		GenericSequentialFlowInfo info= processSequential(node, node.getExpression());
		process(info, node.getBody());
	}

	public void endVisit(ThisExpression node) {
		if (skipNode(node))
			return;
		assignFlowInfo(node, node.getQualifier());
	}

	public void endVisit(ThrowStatement node) {
		if (skipNode(node))
			return;
		ThrowFlowInfo info= createThrow();
		setFlowInfo(node, info);
		Expression expression= node.getExpression();
		info.merge(getFlowInfo(expression), fFlowContext);
		info.mergeException(expression.resolveTypeBinding(), fFlowContext);
	}

	public void endVisit(TryStatement node) {
		if (skipNode(node))
			return;
		TryFlowInfo info= createTry();
		setFlowInfo(node, info);
		info.mergeTry(getFlowInfo(node.getBody()), fFlowContext);
		info.removeExceptions(node);
		for (Iterator iter= node.catchClauses().iterator(); iter.hasNext();) {
			CatchClause element= (CatchClause)iter.next();
			info.mergeCatch(getFlowInfo(element), fFlowContext);
		}
		info.mergeFinally(getFlowInfo(node.getFinally()), fFlowContext);
	}

	// TODO account for enums and annotations

	public void endVisit(TypeDeclaration node) {
		if (skipNode(node))
			return;
		GenericSequentialFlowInfo info= processSequential(node, node.getSuperclassType());
		process(info, node.superInterfaceTypes());
		process(info, node.bodyDeclarations());
		info.setNoReturn();
	}

	public void endVisit(TypeDeclarationStatement node) {
		if (skipNode(node))
			return;
		assignFlowInfo(node, node.getDeclaration());
	}

	public void endVisit(TypeLiteral node) {
		if (skipNode(node))
			return;
		assignFlowInfo(node, node.getType());
	}

	public void endVisit(TypeParameter node) {
		if (skipNode(node))
			return;
		GenericSequentialFlowInfo info= processSequential(node, node.getName());
		process(info, node.typeBounds());
	}

	public void endVisit(VariableDeclarationExpression node) {
		if (skipNode(node))
			return;
		GenericSequentialFlowInfo info= processSequential(node, node.getType());
		process(info, node.fragments());
	}

	public void endVisit(VariableDeclarationStatement node) {
		if (skipNode(node))
			return;
		GenericSequentialFlowInfo info= processSequential(node, node.getType());
		process(info, node.fragments());
	}

	public void endVisit(VariableDeclarationFragment node) {
		if (skipNode(node))
			return;

		IVariableBinding binding= node.resolveBinding();
		LocalFlowInfo nameInfo= null;
		Expression initializer= node.getInitializer();
		if (binding != null && !binding.isField() && initializer != null) {
			nameInfo= new LocalFlowInfo(binding, FlowInfo.WRITE, fFlowContext);
		}
		GenericSequentialFlowInfo info= processSequential(node, initializer);
		info.merge(nameInfo, fFlowContext);
	}

	public void endVisit(WhileStatement node) {
		if (skipNode(node))
			return;
		WhileFlowInfo info= createWhile();
		setFlowInfo(node, info);
		info.mergeCondition(getFlowInfo(node.getExpression()), fFlowContext);
		info.mergeAction(getFlowInfo(node.getBody()), fFlowContext);
		info.removeLabel(null);
	}

	public void endVisit(WildcardType node) {
		if (skipNode(node))
			return;
		assignFlowInfo(node, node.getBound());
	}

	private void endVisitMethodInvocation(ASTNode node, ASTNode receiver, List arguments, IMethodBinding binding) {
		if (skipNode(node))
			return;
		MessageSendFlowInfo info= createMessageSendFlowInfo();
		setFlowInfo(node, info);
		for (Iterator iter= arguments.iterator(); iter.hasNext();) {
			Expression arg= (Expression) iter.next();
			info.mergeArgument(getFlowInfo(arg), fFlowContext);
		}
		info.mergeReceiver(getFlowInfo(receiver), fFlowContext);
		info.mergeExceptions(binding, fFlowContext);
	}

	private void endVisitIncDecOperation(Expression node, Expression operand) {
		if (skipNode(node))
			return;
		FlowInfo info= getFlowInfo(operand);
		if (info instanceof LocalFlowInfo) {
			// Normally we should do this in the parent node since the write access take place later.
			// But I couldn't come up with a case where this influences the flow analysis. So I kept
			// it here to simplify the code.
			GenericSequentialFlowInfo result= createSequential(node);
			result.merge(info, fFlowContext);
			result.merge(
				new LocalFlowInfo((LocalFlowInfo)info, FlowInfo.WRITE, fFlowContext),
				fFlowContext);
		} else {
			setFlowInfo(node, info);
		}
	}

	private IMethodBinding getMethodBinding(Name name) {
		if (name == null)
			return null;
		IBinding binding= name.resolveBinding();
		if (binding instanceof IMethodBinding)
			return (IMethodBinding)binding;
		return null;
	}
}
