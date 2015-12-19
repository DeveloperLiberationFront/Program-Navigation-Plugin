package edu.pdx.cs.multiview.jdt.util;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

/**
 * @author Phil Quitslund
 * 
 * @since May 13, 2005
 *
 */
public class JavaElementAnalyzer {

    /** delegate for sub-iterations */
    JavaElementAnalyzer _delegate = this;
    
    /** the root element */
    private IJavaElement _root;

    /** a pointer to the current element */
    private IJavaElement _current;
    
    /** a pointer the underlying ICompilationUnit */
    private ICompilationUnit _icu;
    
    
    /** a pointer the current IType */
    private IType _type;
    
    /** Have the fields in the current type been analyzed? */
    private boolean fieldsAnalyzed;

    /** the collection of types to analyze */
    private IType[] _types;

    /** the index of the current type */
    private int _typeIndex;

    private IField[] _fields;

    private int _fieldIndex;

    private IMethod[] _methods;

    private int _methodIndex;
    
    private boolean singletonVisited;
    
    
    /**
     * @param element
     */
    public JavaElementAnalyzer(IJavaElement element) {
        _root = element;
        _current = element;
    }

    
    public boolean hasNext() throws JavaModelException {
        if (_delegate == null)
            return false; //done
        boolean hasNext = _delegate.doHasNext();
        if (!hasNext) {
            _delegate = getNextDelegate();
            return hasNext();
        }
        return true;
    }
    

    private JavaElementAnalyzer getNextDelegate() throws JavaModelException {
        if (singletonVisited)
            return null;
        IType type = getNextType();
        if (type == null)
            return null;
        return new JavaElementAnalyzer(type);
    }


    private boolean doHasNext() throws JavaModelException {
        _current = getNext();
        return _current != null;
    }



    private IJavaElement getNext() throws JavaModelException {
        if (singletonVisited)
            return null;
        return _delegate.doGetNext();
    }

    private IJavaElement doGetNext() throws JavaModelException {
        if (_current == null)
            return null;
        if (_current instanceof ICompilationUnit) {
            _icu = (ICompilationUnit)_current;
            _type = getNextType();
        } else if (_current instanceof IType) {
            IType type = (IType)_current;
            //TODO: this is where it's broken....
            
        } else if (_icu == null) {
            if (_current instanceof IMember) {
                singletonVisited = true;
                return _current;
            }
        }
        return getNextMember();
    }


    /**
     * @return
     * @throws JavaModelException
     */
    private IType getNextType() throws JavaModelException {
        if (_types == null) {
            _types = _icu.getAllTypes();
            _typeIndex = 0;
        }
        if (_typeIndex < _types.length)
            return _types[_typeIndex++];
        return null;
    }


    /**
     * @return
     * @throws JavaModelException
     */
    private IJavaElement getNextMember() throws JavaModelException {
        IJavaElement next = getNextField();
        if (next == null)
            next = getNextMethod();
        if (next == null)
            fieldsAnalyzed = false; //reset for next pass
        return next;
    }


    /**
     * @return
     * @throws JavaModelException
     */
    private IJavaElement getNextMethod() throws JavaModelException {
        if (_methods == null) {	 
            _methods = _type.getMethods();
            _methodIndex = 0;
        }
        if (_methodIndex < _methods.length)
            return _methods[_methodIndex++];
        return null;
    }


    /**
     * @return
     * @throws JavaModelException
     */
    private IJavaElement getNextField() throws JavaModelException {
        if (fieldsAnalyzed)
            return null;
        if (_fields == null) {	 
            _fields = _type.getFields();
            _fieldIndex = 0;
        }
        if (_fieldIndex < _fields.length)
            return _fields[_fieldIndex++];
        fieldsAnalyzed = true; //done
        return null;
    }


 


    public IJavaElement next() {
        return _current;  
    }
 
}
