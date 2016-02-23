package edu.pdx.cs.multiview.jdt.util;

import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IInitializer;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

/**
 * @author Phil Quitslund
 * 
 * @since May 16, 2005
 *
 * See ASTNodeFinder for the inspiration...
 *
 */
public class JavaElementFinder {


    /**
     * @return the IType associated with this node.
     */
    public static IType findType(TypeDeclaration node, ICompilationUnit parent) {
        try {
            IType[] types = parent.getAllTypes();
            String nodeName = node.getName().toString();
            for (int i = 0; i < types.length; i++) {
                IType type = types[i];
                if (type.getElementName().equals(nodeName))
                    return type;
            }
        } catch (JavaModelException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } 
        return null;
    }
    
	/**
	 * 
	 * @param method			a method
	 * @param enclosingType		the type the method resides in (or null)
	 * 
	 * @return			the method following the method parameter, or null if it's the last
	 * 
	 * @throws JavaModelException
	 */	
	public static IJavaElement findSibling(IMember member, IType parent) 
					throws JavaModelException{
		if(parent==null)
			parent = (IType)member.getParent();
		
		boolean returnNext = false;
		for(IJavaElement candidate: parent.getChildren()){
			if(returnNext)
				return candidate;
			else if(candidate.equals(member))
				returnNext = true;				
		}
		
		return null;
	}    
    
    
    public static IField findField(FieldDeclaration node, ICompilationUnit parent) {

        ASTNode owner = node.getParent();
        if (!(owner instanceof TypeDeclaration))
            return null;
        IType type = findType((TypeDeclaration)owner, parent);
        List vardecls = node.fragments();
        for (Iterator iter = vardecls.iterator(); iter.hasNext();) {
            VariableDeclarationFragment var = (VariableDeclarationFragment) iter.next();
            //the first should do TODO: sanity check this!
            String name = var.getName().toString();
            return type.getField(name);
        }
        
        return null;
    }
    
    //for now does not parse params (just their # -- and name of selector)
    //consider just checking for correspondence?
    public static IMethod findMethod(MethodDeclaration node, ICompilationUnit parent) {

        ASTNode owner = node.getParent();
        if (!(owner instanceof TypeDeclaration))
            return null;
        IType type = findType((TypeDeclaration)owner, parent);
        
        String methodName = node.getName().toString();
        List params = node.parameters();
        String[] pStrings = new String[params.size()];
        for (Iterator iter = params.iterator(); iter.hasNext();) {
            SingleVariableDeclaration var = (SingleVariableDeclaration) iter.next();
            //var.getType().
        }
        try {
            IMethod[] methods = type.getMethods();
            for (int i = 0; i < methods.length; i++) {
                IMethod method = methods[i];
                if (method.getElementName().equals(methodName))
                    return method;
            }
        } catch (JavaModelException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return null;
    }


    public static IInitializer findInitializer(Initializer node, ICompilationUnit parent) {
        
        ASTNode owner = node.getParent();
        if (!(owner instanceof TypeDeclaration))
            return null;
        IType type = findType((TypeDeclaration)owner, parent);
        
        try {
            IInitializer[] inits = type.getInitializers();
            for (int i = 0; i < inits.length; i++) {
                IInitializer init = inits[i];
                if (areCorresponding(node, init))
                    return init;
            }
        } catch (JavaModelException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public static IJavaElement findPackage(ASTNode node, ICompilationUnit parent) {
        ASTNode owner = node.getParent();
        if (!(owner instanceof TypeDeclaration))
            return null;
        IType type = findType((TypeDeclaration)owner, parent);
        
        return type == null ? null : type.getPackageFragment();
    }
    
    
    /**
     * @param node
     * @param member
     * 
     * @return		whether node and member occupy the same source range
     */
    public static boolean areCorresponding(ASTNode node, IMember member) {
        
        try {
            ISourceRange sourceRange= member.getSourceRange();
            int memberStart = sourceRange.getOffset();
            int memberLength = sourceRange.getLength();
            
    		int nodeStart= node.getStartPosition();
    		int nodeLength= node.getLength();
    		
    		return (memberStart == nodeStart && memberLength == nodeLength);
		
        } catch(JavaModelException e) {
            e.printStackTrace();
        }
		
        return false;
    }
    
    /**
     * @param node
     * @param member
     * 
     * @return	whether node and member end at the same source position
     */
    public static boolean overlap(ASTNode node, IMember member) {
    	
        try {
            ISourceRange sourceRange= member.getSourceRange();
            int memberStart = sourceRange.getOffset();
            int memberLength = sourceRange.getLength();
        
    		int nodeStart= node.getStartPosition();
    		int nodeLength= node.getLength();
    		
    		return isAscending(memberStart,nodeStart,memberStart+memberLength) ||
    				isAscending(memberStart,nodeStart+nodeLength,memberStart+memberLength);
		
        } catch(JavaModelException e) {
            e.printStackTrace();
        }
		
        return false;    	
    }


    private static boolean isAscending(int a, int b, int c) {
		return a <= b && b <= c;
	}

	public static MethodDeclaration findMethodDeclaration(IMethod method) {	
    	ICompilationUnit icu = method.getCompilationUnit();
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(icu);
		parser.setResolveBindings(true);
		CompilationUnit root = (CompilationUnit) parser.createAST(null);
		return findMethodDeclaration(method, root);
    }

	public static MethodDeclaration findMethodDeclaration(IMethod method,
			CompilationUnit root) {
		MethodDeclarationFinder finder = new MethodDeclarationFinder(method);
		root.accept(finder);
		return finder.getDecl();
	}
    
    static class MethodDeclarationFinder extends ASTVisitor {
    	MethodDeclaration _decl;
		private IMethod _method;
		public MethodDeclarationFinder(IMethod method) {
			_method = method;
		}
		@Override
		public boolean visit(MethodDeclaration node) {
			
			if (isMethod(node)) {
				_decl = node;
				return false;
			}
			return true;
		}
		
		public MethodDeclaration getDecl() {
			return _decl;
		}
		
		public boolean isMethod(MethodDeclaration node){
			try {
				SimpleName name = node.getName();
				int nodeStart = name.getStartPosition();
				
				return _method.getNameRange().getOffset() == nodeStart;
			} catch (JavaModelException e) {
				e.printStackTrace();
			}
			
			return false;
		}
    }
    
    
}
