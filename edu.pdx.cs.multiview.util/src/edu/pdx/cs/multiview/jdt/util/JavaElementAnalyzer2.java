package edu.pdx.cs.multiview.jdt.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IInitializer;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
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
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import edu.pdx.cs.multiview.util.Debug;

/**
 * 
 * 
 * @author Phil Quitslund
 * 
 * @since May 13, 2005
 *
 */
public class JavaElementAnalyzer2 {

    /** The root element */
    private IJavaElement _element;

    /** A pointer to the current element */
    private int _current;

    /** The array of children over whihc we iterate */
    private IJavaElement[] _children;
    
    /**
     * @param element
     */
    public JavaElementAnalyzer2(IJavaElement element) {
        _element = element;
        _children = buildList();
    }

    
    //strategy: in beginVisit: check to see if the element corresponds ? flag inScope -> true
    //			in endVisit: check to see if the element corresponds ? flag inScope -> false
    
    //wrench in the works: inner classes...
    //how to indicate ownership of methods?
    //e.g.:
    //class Foo
    //class InnerFoo
    //method inInnerFoo
    //method inFoo
    
    //can't be a simple iterator?
    //OR: when importing, check if current parent is element parent; if not, pop up
    
    

    private IJavaElement[] buildList() {
        List<IJavaElement> elements = new ArrayList<IJavaElement>();
        
        if (_element instanceof IPackageFragment)
            elements = buildListFromPackage((IPackageFragment)_element);
        else if (_element instanceof IMember) 
            elements = buildListFromMember((IMember)_element);
        
        return elements.toArray(new IJavaElement[]{});
    }
    
    
    private List<IJavaElement> buildListFromMember(IMember member) {
        
		final ICompilationUnit icu = member.getCompilationUnit();
		ASTParser parser = ASTParser.newParser(AST.JLS2);
		parser.setSource(icu);
		parser.setResolveBindings(true);
		CompilationUnit node = (CompilationUnit) parser.createAST(null);
		
		//a container for our collected results
		final List<IJavaElement> elements = new ArrayList<IJavaElement>();
		
		node.accept(new ASTVisitor() {
		    
		    boolean inScope;
		    
		    /* (non-Javadoc)
             * @see org.eclipse.jdt.core.dom.ASTVisitor#preVisit(org.eclipse.jdt.core.dom.ASTNode)
             */
            public void preVisit(ASTNode node) {
                //only works for members
                if (_element instanceof IMember)
                    if (areCorresponding(node, (IMember)_element)){
                        Debug.trace("*** entering scope at: " + node);
                        inScope = true;
                    }
            }
            
            /* (non-Javadoc)
             * @see org.eclipse.jdt.core.dom.ASTVisitor#postVisit(org.eclipse.jdt.core.dom.ASTNode)
             */
            public void postVisit(ASTNode node) {
                //only works for members
                if (_element instanceof IMember)
                    if (areCorresponding(node, (IMember)_element)) {
                        Debug.trace("*** leaving scope at: " + node);
                        inScope = false;
                    }
                
            }
		    
		   /*
            * (non-Javadoc)
            * 
            * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.FieldDeclaration)
            */
            public boolean visit(FieldDeclaration node) {
                if (inScope) {
                    Debug.trace(node);
                	IField field = JavaElementFinder.findField(node, icu);
                	Debug.trace("got field: " + field);
                	elements.add(field);
            	}
                return true;
            } 

            
            /* (non-Javadoc)
             * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.MethodDeclaration)
             */
            public boolean visit(MethodDeclaration node) {
                if (inScope) {
                    Debug.trace(node);
                    IMethod method = JavaElementFinder.findMethod(node, icu);
                    Debug.trace("got method: " + method);
                    elements.add(method);
                }
                return true;
            }

            
            /* (non-Javadoc)
             * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.TypeDeclaration)
             */
            public boolean visit(TypeDeclaration node) {
                if (inScope) {
                    Debug.trace(node);
                    IType type = JavaElementFinder.findType(node, icu);
                    Debug.trace("got type: " + type);
                    elements.add(type);
                }
                return true;
            }

            /* (non-Javadoc)
             * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.Initializer)
             */
            public boolean visit(Initializer node) {
                if (inScope) {
                    Debug.trace(node);
                    IInitializer initializer = JavaElementFinder.findInitializer(node, icu);
                    Debug.trace("got initializer: " + initializer);   
                    elements.add(initializer);
                }
                return true;
            }
            /* (non-Javadoc)
             * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.PackageDeclaration)
             */
            public boolean visit(PackageDeclaration node) {
                if (inScope) {
                    Debug.trace(node);
                    IJavaElement pkg = JavaElementFinder.findPackage(node, icu);
                    Debug.trace("got package: " + pkg);   
                    elements.add(pkg);
                }
                return true;
            }
            
		});
		
        return elements;

    }


    private List<IJavaElement> buildListFromPackage(IPackageFragment fragment) {
        List<IJavaElement> elements = new ArrayList<IJavaElement>();
        try {
            elements.add(fragment);
            ICompilationUnit[] icu = fragment.getCompilationUnits();
            for (int i = 0; i < icu.length; i++) {
                elements.addAll(buildListFromIcu(icu[i]));
            }
        } catch (JavaModelException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return  elements;
    }

    private List<IMember> buildListFromIcu(final ICompilationUnit icu) {
		ASTParser parser = ASTParser.newParser(AST.JLS2);
		parser.setSource(icu);
		parser.setResolveBindings(true);
		CompilationUnit node = (CompilationUnit) parser.createAST(null);
		
		//a container for our collected results
		final List<IMember> elements = new ArrayList<IMember>();
		
		node.accept(new ASTVisitor() {
			   /*
	            * (non-Javadoc)
	            * 
	            * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.FieldDeclaration)
	            */
	            public boolean visit(FieldDeclaration node) {
	                IField field = JavaElementFinder.findField(node, icu);
	                Debug.trace("got field: " + field);
	                elements.add(field);
	                return true;
	            } 

	            
	            /* (non-Javadoc)
	             * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.MethodDeclaration)
	             */
	            public boolean visit(MethodDeclaration node) {
	                IMethod method = JavaElementFinder.findMethod(node, icu);
	                Debug.trace("got method: " + method);
	                elements.add(method);
	                return true;
	            }

	            
	            /* (non-Javadoc)
	             * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.TypeDeclaration)
	             */
	            public boolean visit(TypeDeclaration node) {
	                IType type = JavaElementFinder.findType(node, icu);
	                Debug.trace("got type: " + type);
	                elements.add(type);
	                return true;
	            }

	            /* (non-Javadoc)
	             * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.Initializer)
	             */
	            public boolean visit(Initializer node) {
	                IInitializer initializer = JavaElementFinder.findInitializer(node, icu);
                    Debug.trace("got initializer: " + initializer);   
                    elements.add(initializer);
	                return true;
	            }
		});
		
		return elements;
    }


    private boolean areCorresponding(ASTNode node, IMember member) {
        
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
     * @return whether there is a next element
     */
    public boolean hasNext() {
        return _current < _children.length;
    }
    
    /**
     * @return the next element
     */
    public IJavaElement next() {
        //return *then* increment
        return _children[_current++];
    }
 
}
