package edu.pdx.cs.multiview.jdt.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.compiler.IScanner;
import org.eclipse.jdt.core.compiler.ITerminalSymbols;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.TargetSourceRangeComputer.SourceRange;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.jdt.internal.corext.util.JdtFlags;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.UndoEdit;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.texteditor.AbstractTextEditor;

import edu.pdx.cs.multiview.util.Debug;

/**
 * JDT utils.
 * 
 * @author Phil Quitslund, emerson
 * 
 * @since May 19, 2005
 *
 */
@SuppressWarnings("restriction")
public class JDTUtils {

	/**
	 * Tabulation types used for managing indentation.
	 */
	public static enum Tabulation {
		TAB, SPACE;
		/**
		 * Calculate indent level based on a count #
		 * @param count - the # of occurences
		 * @return the indentLevel
		 */
		public int indentLevel(int count) {
			switch(this) {
				case TAB   : return count;
				case SPACE : return count/getDefinedIndentSize();
			}
			throw new AssertionError("Unknown Tabulation: " + this);
		}
		/**
		 * Build an indentation string buffer based on the type and count
		 * @param level the indent level
		 * @return the StringBuffer representation of the indent
		 */
		public StringBuffer indent(int level) {
			switch(this) {
				case TAB   : return repeat('\t', level);
				case SPACE : return repeat(repeat(' ', getDefinedIndentSize()), level);
			}
			throw new AssertionError("Unknown Tabulation: " + this);
		}

	}
	
	
	/** The system defined line delimeter, lazily initialized */
     private static String _lineDelimeter;

	/**
     * Fetches the java element associated with this element handle.
     * @param spec 
     * @return the java element or null.
     */
    public static IJavaElement getJavaElement(String spec) {
        return spec == null ? null : JavaCore.create(spec);
    }

    /**
     * Open and select the given element in a Java editor
     * @param je
     */
    public static IEditorPart openElementInEditor(IJavaElement je) {
       try {
           IEditorPart editorPart = JavaUI.openInEditor(je);
           JavaUI.revealInEditor(editorPart, je);
           return editorPart;
       } catch (PartInitException e) {
           e.printStackTrace();
       } catch (JavaModelException e) {
           e.printStackTrace();
       }
       return null;
    }

    /**
     * @return the package that contains this element
     */
    public static IPackageFragment getPackage(IJavaElement child) {
        if (child == null) {
            Debug.debug("getPackage called on a null argument");
            return null;
        }
        IJavaElement primary = child.getPrimaryElement();
        if (primary instanceof IType) {
           IType type = (IType)primary;
           return type.getPackageFragment();
        }
        if (primary instanceof IMethod) {
            IMethod m = (IMethod)primary;
            return getPackage(m.getParent());
        }
        return null;
    }

     
//SNIPPED from PrettySignature (and modified):    
    
    public static String getMethodSignature(IMethod method) {
    	return getMethodSignature(method, false);
    }
    
    
	/**
	 * @param method
	 * @param qualified - whether to add type and package name
	 * @return
	 */
	public static String getMethodSignature(IMethod method, boolean qualified) {
		StringBuffer buffer= new StringBuffer();
//!pq	buffer.append(JavaModelUtil.getFullyQualifiedName(method.getDeclaringType()));
		boolean isConstructor= method.getElementName().equals(method.getDeclaringType().getElementName());
//!pq	if (!isConstructor) {
//			buffer.append('.');
//		}
		
		IType type = method.getDeclaringType();
		
		String pkg = type.getPackageFragment().getElementName();
		if (pkg != null)
			buffer.append(pkg).append('.');
		
		buffer.append(type.getElementName()).append('.');
		
		buffer.append(getUnqualifiedMethodSignature(method, !isConstructor));
		
		return buffer.toString();
	}


	public static String getMethodSignature(MethodDeclaration node) {		
		return node.getName().getIdentifier();
	}

	public static String getUnqualifiedTypeSignature(IType type) {
		return type.getElementName();
	}
	
	public static String getUnqualifiedMethodSignature(IMethod method, boolean includeName) {
		StringBuffer buffer= new StringBuffer();
		if (includeName) {
			buffer.append(method.getElementName());
		}
		buffer.append('(');
		
		String[] types= method.getParameterTypes();
		if (types.length > 0)
			buffer.append(Signature.toString(types[0]));
		for (int i= 1; i < types.length; i++) {
			buffer.append(", "); //$NON-NLS-1$
			buffer.append(Signature.toString(types[i]));
		}
		
		buffer.append(')');
		
		return buffer.toString();
	}

	public static String getUnqualifiedMethodSignature(IMethod method) {
		return getUnqualifiedMethodSignature(method, true);
	}

    /**
     * Trim a file extension (e.g., .java or .class) if there is one.
     * @return the trimmed name
     */
    public static String trimExtension(String name) {
        int index = name.lastIndexOf('.');
        if (index == -1)
            return name;
        return name.substring(0, index);
    }
    
    /**
     * Fetches the workbench adapter associated with this element.
     * 
     * @param element
     * @return the workbench adapter or null if there is none.
     */
    public static IWorkbenchAdapter getWorkbenchAdapter(Object element) {
        if (element != null && element instanceof IAdaptable) {
            return (IWorkbenchAdapter) ((IAdaptable) element).getAdapter(IWorkbenchAdapter.class);
		}
		return null;
    }


    /**
     * Returns the method body for the given method.  Note that the returned String
     * includes the braces.
     * @param method
     * @return the method body
     * FIXME: how to handle abstract methods?
     */
    public static String getBody(IMethod method) {
    	MethodDeclaration decl = JavaElementFinder.findMethodDeclaration(method);
    	Block body = decl.getBody(); 
    	return body == null ? "" : body.toString();
    }
   
    /**
     * Returns the Javadoc associated with the given method 
     * @param method
     * @return the method javadoc or null if there is none
     */
    public static String getJavaDoc(IMethod method) {
    	MethodDeclaration decl = JavaElementFinder.findMethodDeclaration(method);
    	Javadoc doc = decl.getJavadoc();
    	if (doc == null)
    		return null;
    	return doc.toString();
    }
    
    
    
    /**
     * @return the System-defined line delimeter
     */
    public static String getLineDelimeter() {
    	if (_lineDelimeter==null)
    		_lineDelimeter = System.getProperty("line.separator");
    	return _lineDelimeter;
    }
   
    
    /**
     * For convenience; equivalent to: 
     * 		format(src,objectClass,indent,CodeFormatter.K_CLASS_BODY_DECLARATIONS,sep)
     * where sep is the separator returned by System.getProperty("line.separator");
     * 
     * EXAMPLE USE: formatting a method to insert:
     * 
     * 		IType type = ... //the type into which to insert
     * 		String newMethod = "void newMethod(){\nint x;\n}";
	 *		IMethod sibling  = type.getMethods()[0]; //or some other method for a sibling
	 *		int siblingIndent = JDTUtils.inferIndentLevel(sibling);
	 *		String newMethodFormatted = JDTUtils.format(newMethod, type, siblingIndent);
	 *		type.createMethod(newMethodFormatted, sibling, true, null);
     *
     * @param source - the source String to format
     * @param objectClass - the Java element that contains the source
     * @param indent - the initial indentation level, used to shift left/right the 
     * 		  entire source fragment.
     * @return the formatted source String
     * @throws BadLocationException 
     * @throws MalformedTreeException 
     */
    public static String format(String source, IJavaElement objectClass, int indent) throws MalformedTreeException, BadLocationException {  	
    	return format(source, objectClass, indent, CodeFormatter.K_CLASS_BODY_DECLARATIONS,getLineDelimeter());
    }
    
    /**
     * Formats this String using the Java formatter.
     * 
     * @param source - the source string to format
     * @param objectClass - the Java element that contains the source
     * @param indent - the initial indentation level, used to shift left/right the 
     * 		  entire source fragment.
     * @param kind - the kind of the code snippet to format. It can be any of these:
	 *        K_EXPRESSION, K_STATEMENTS, K_CLASS_BODY_DECLARATIONS, K_COMPILATION_UNIT, K_UNKNOWN,
	 *        K_SINGLE_LINE_COMMENT, K_MULTI_LINE_COMMENT, K_JAVA_DOC (defined in 
	 *        org.eclipse.jdt.core.formatter.CodeFormatter)
     * @param lineSeparator - the line separator to use in formatted source,
	 *     if set to <code>null</code>, then the platform default one will be used.
     * @return the formated String
     * @throws MalformedTreeException
     * @throws BadLocationException
     * @see org.eclipse.jdt.core.formatter.CodeFormatter
     */
    public static String format(String source, IJavaElement objectClass, 
    		int indent, int kind, String lineSeparator) throws MalformedTreeException, BadLocationException {
    	
    	TextEdit textEdit = ToolFactory.createCodeFormatter(null)
    							.format(kind, source, 0, source.length(), indent,lineSeparator);
    	String formattedContent;
    	if (textEdit != null) {
    		Document document = new Document(source);
    		textEdit.apply(document);
    		formattedContent = document.get();
    	} else {
    		formattedContent = source;
    	}
    	return formattedContent;
    }
    
    /**
     * Replaces a member with some text.  Assumedly, the new text
     * is a valid java member.
     * 
     * @param member
     * @param newSource
     * 
     * @throws JavaModelException
     */
    public static void replaceMember(IMember member, String newSource) 
    							throws JavaModelException{
    	
    	IBuffer buffer = member.getCompilationUnit().getBuffer();
    	
    	int start = member.getSourceRange().getOffset();
    	int len = member.getSourceRange().getLength();
    	
    	buffer.replace(start,len,newSource);
    	
    	buffer.save(null,true);
    	buffer.close();
    }
    
    /**
     * Replaces the body of a method
     * 
     * @param method	a method
     * @param newBody
     * @throws InvalidInputException 
     */
    public static void changeBody(IMethod method, String newBody) throws
    													JavaModelException, 
    													InvalidInputException{
    	assert !Flags.isAbstract(method.getFlags());
    	
    	//set a buffer on the source of the containing compilation unit
    	IBuffer buffer = method.getCompilationUnit().getBuffer();
    	
    	//set up a scanner on the source
	    IScanner scanner = ToolFactory.createScanner(false,false,false,false);
		scanner.setSource(buffer.getCharacters());
		
		//set the scanner to just scan the method
		ISourceRange methodRange = method.getSourceRange();
		int methodEnd = methodRange.getOffset()+methodRange.getLength();
		scanner.resetTo(methodRange.getOffset(), methodEnd);
    	
		//skip uninteresting parts
		skipComments(scanner);
		skipToCurlyBrace(scanner);
		
		//replace source
		int start = scanner.getCurrentTokenStartPosition() + 1;
		buffer.replace(start,methodEnd-start-1,newBody);
		
		//save buffer
		buffer.save(null,true);
		buffer.close();
    }
    

    /**
     * Infer the indent level of a given element by parsing the source.
     * 
     * @param element
     * @return the inferred indent level or -1 in case of error
     * @throws JavaModelException 
     * N.B. no effort is made to guard against pathological cases where tabs and spaces are mixed
     */
    public static int inferIndentLevel(IMember element) throws JavaModelException {
    	ISourceRange range = element.getSourceRange();
    	ICompilationUnit icu = element.getCompilationUnit();
    	if (icu == null)
    		return -1; //ERROR
    	IBuffer buffer = icu.getBuffer();
    	if (buffer == null)
    		return -1; //ERROR
    	
    	//char indentChar = getDefinedIndentCharacter();
    	Tabulation tabulation = getDefinedIndentTabulation();
    	
    	int index = range.getOffset()-1; //start at the character before member starts
    	int count = 0;
    	while (index != 0) {
    		char c = buffer.getChar(index);
    		if (Character.toString(c).equals("\n")) //TODO: lineDelim might be "\r\n"; is it enough to look for
    			return tabulation.indentLevel(count);    //      a trailing '\n'?
    		++count;
    		--index;
    	}
    	return -1; //ERROR
    }
    
    
    /**
     * Queries the Java core preference store for the default indent character
     * (tab or space)
     * @return
     */
    public static char getDefinedIndentCharacter() {
    	Hashtable options = JavaCore.getDefaultOptions();
    	Object option = options.get("org.eclipse.jdt.core.formatter.tabulation.char");
    	return "tab".equals(option) ? '\t' : ' '; 
    }
    

    /**
     * Queries the Java core preference store for the default indent character
     * (tab or space)
     * @return
     */
    public static Tabulation getDefinedIndentTabulation() {
    	Hashtable options = JavaCore.getDefaultOptions();
    	Object option = options.get("org.eclipse.jdt.core.formatter.tabulation.char");
    	return "tab".equals(option) ? Tabulation.TAB : Tabulation.SPACE; 
    }
    
    
    public static int getDefinedIndentSize() {
    	Hashtable options = JavaCore.getDefaultOptions();
    	Object option = options.get("org.eclipse.jdt.core.formatter.tabulation.size");
    	if (!(option instanceof String))
    		return -1;
    	String value = (String)option;
    	return Integer.parseInt(value);
    }
    

    /*
     * Skips all comments and whitespace
     */
    private static void skipComments(IScanner scanner) throws InvalidInputException{
    	
    	int token;		
		do{
		    token = scanner.getNextToken();
		    
		}while(token == ITerminalSymbols.TokenNameCOMMENT_BLOCK || 
				token == ITerminalSymbols.TokenNameCOMMENT_JAVADOC || 
				token == ITerminalSymbols.TokenNameCOMMENT_JAVADOC ||
				token == ITerminalSymbols.TokenNameWHITESPACE);
    	
    }
    
    /*
     * Skips to {
     * 
     * @param scanner
     * @throws InvalidInputException
     */
	private static void skipToCurlyBrace(IScanner scanner) 
										throws InvalidInputException {
	
		int token;		
		do{
		    token = scanner.getNextToken();			    
		}while(token != ITerminalSymbols.TokenNameLBRACE);
	}
	
	/**
	 * Gets the signature of a method, that is, the source minus
	 * javadocs and body.
	 * 
	 * @param memberSource
	 * @return
	 */
	public static String getSignature(String memberSource){		
		//TODO: had test in CVS - perspectivej.tests: rewrite
		return stripBody(stripJavadocs(memberSource));
	}
	
	/**
	 * Returns the source of a method without prepended comments
	 * 
	 * @param memberSource
	 * @return
	 */
    public static String stripJavadocs(String memberSource){
        
        memberSource = memberSource.trim();
        
        int docEnd;
        
        while(memberSource.startsWith("/")){
            if(memberSource.startsWith("/*")){
                docEnd = memberSource.indexOf("*/");
                memberSource = memberSource.substring(docEnd+2);
            }else if(memberSource.startsWith("//")){
                docEnd = memberSource.indexOf("\n");
                memberSource = memberSource.substring(docEnd+1);
            } else {
                System.err.println("Unexpected source");
            }
            memberSource = memberSource.trim();
        }
                
        return memberSource;
    }
	
	private static String stripBody(String memberSource){

		int sourceStart = memberSource.indexOf("{");
		if(sourceStart>0)
			return memberSource.substring(0,sourceStart);
		
		return memberSource;
	}

	/**
	 * @param type
	 * 
	 * @return	the signature of the type
	 * 
	 * @throws JavaModelException 
	 * 
	 * TODO: put in <> for anonymous inner class
	 */
	public static String getTypeSignature(IType type) throws JavaModelException {		

		String signature = getModifiers(type);
		
		if(Flags.isInterface(type.getFlags()))
			signature += "interface ";
		else
			signature += "class ";
		
		signature += type.getElementName() + " ";
		
		String superName = type.getSuperclassName();
		if(superName!=null && !superName.equals("") && !superName.equals("Object"))
			signature += "extends " + superName + " ";
		
		//TODO: 1.5 - show type params?
		
		IType[] types = type.getTypes();
		if(types.length > 0){
			
			signature += "implements ";
			
			for(int i = 0; i<types.length-1; i++)
				signature += types[i].getElementName() + ", ";
			
			signature += types[types.length-1].getElementName();
		}
			
		return signature;
	}
	
	private static String getModifiers(IMember member) throws JavaModelException{
		
		int flags = member.getFlags();
		String result = "";
		
		if(Flags.isPublic(flags))
			result += "public ";
		else if(Flags.isPrivate(flags))
			result += "private ";
		else if(Flags.isProtected(flags))
			result += "protected ";
		
		if(Flags.isAbstract(flags))
			result += "abstract ";
		
		if(Flags.isFinal(flags))
			result += "final ";
		
		if(Flags.isNative(flags))
			result += "native ";
		
		if(Flags.isStatic(flags))
			result += "static ";
		
		if(Flags.isStrictfp(flags))
			result += "strictfp ";
		
		if(Flags.isSynchronized(flags))
			result += "synchronized ";
		
		if(Flags.isTransient(flags))
			result += "transient ";
		
		if(Flags.isVolatile(flags))
			result += "native ";		
		
		return result;
	}

	/**
	 * @param subType
	 * 
	 * @return	the supertype of the parameter
	 */
	public static IType getSuperType(IType subType) {
		ITypeHierarchy hierarchy = null;
		try {
			hierarchy = subType.newSupertypeHierarchy(new NullProgressMonitor());
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
				
		IType superType = hierarchy.getSuperclass(subType);
		return superType;
	}

	/**
	 * 
	 * @param superClass
	 * 
	 * @return	the subtypes of the parameter
	 */
	public static IType[] getSubclasses(IType superClass) {
		
		ITypeHierarchy hierarchy = null;
		try {
			hierarchy = superClass.newTypeHierarchy(new NullProgressMonitor());
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		
		IType[] subTypes = hierarchy.getSubclasses(superClass);
		return subTypes;
	}

	public static IType[] getInterfaces(IType type) {
		
		ITypeHierarchy hierarchy = null;
		try {
			hierarchy = type.newSupertypeHierarchy(new NullProgressMonitor());
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		
		return hierarchy.getSuperInterfaces(type);
	}	
    
	/**
	 * Create a StringBuffer that repeats a given item 'count' times
	 * @param <T> the type of the thing to repeat
	 * @param t the thing
	 * @param count the number of times to repeat
	 * @return a StringBuffer
	 */
	public static <T> StringBuffer repeat(T t, int count) {
		return repeat(t, count, new StringBuffer());
	}
	
	/** A helper function for repeat(T t, int count) that threads a collecting StringBuffer
	 */
	private static <T> StringBuffer repeat(T t, int count, StringBuffer sb) {
		return count == 0 ? sb : repeat(t, count-1, sb.append(t));
	}
	
	/**
	 * @see AST#parseCompilationUnit(org.eclipse.jdt.core.ICompilationUnit, 
	 * 									boolean)
	 * 
	 * @param unit
	 * @param resolveBindings
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static CompilationUnit parseCompilationUnit(
												ICompilationUnit unit,
												boolean resolveBindings) {

		try {
			ASTParser c = ASTParser.newParser(AST.JLS3);
			c.setSource(unit);
			c.setResolveBindings(resolveBindings);
			ASTNode result = c.createAST(null);
			return (CompilationUnit) result;
		} catch (IllegalStateException e) {
			// convert ASTParser's complaints into old form
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * @param 	method
	 * @return	all methods that override the method
	 */
	public static Set<IMethod> getOverridingMethods(IMethod method) {      
		
		SearchEngine engine = new SearchEngine();
		SearchPattern pattern = SearchPattern.createPattern(method, 
				IJavaSearchConstants.DECLARATIONS |
				IJavaSearchConstants.IGNORE_DECLARING_TYPE);        
		
		//TODO: option for scope?  workspace/package/project?
		IJavaSearchScope scope = SearchEngine.
			createJavaSearchScope(new IJavaElement[] {method.getJavaProject()});
		
		final Set<IMethod> found = new HashSet<IMethod>();
		
		SearchRequestor requestor = new SearchRequestor() {        
			@Override
			public void acceptSearchMatch(SearchMatch match) {
				if(match.getElement() instanceof IMethod)
					found.add((IMethod)match.getElement());                
			}        
		};
		
		try {
			engine.search(pattern, 
					new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()}, 
					scope, 
					requestor, 
					null);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		
		return found;
	}

	public static boolean isParentOf(IJavaElement element, IJavaElement possibleParent) {
		
		if(element.getParent()==null)
			return false;
		
		if(element.getParent().equals(possibleParent))
			return true;
			
		
		return isParentOf(element.getParent(),possibleParent);
	}
	//TODO: check out HierarchyRefactoring.getUnindendedText
	
	/*
	 * Copied from:
	 * 
	 * @see org.eclipse.jdt.internal.corext.refactoring.structure.PullUpRefactoring
	 * 		#canBeAccessedFrom(org.eclipse.jdt.core.IMember, 
	 * 			org.eclipse.jdt.core.IType, org.eclipse.jdt.core.ITypeHierarchy)
	 * 
	 * Note that this returns false when if the member "doesn't exist"
	 */
	public static boolean canBeAccessedFrom(IMember member, 
				IType target, IType declarationType, ITypeHierarchy hierarchy) throws JavaModelException {
		
		

		if (member.exists()) {
			
			//true, if the member exists in the type
			if (target.equals(member.getDeclaringType()))
				return true;
			
			//true, if the member is the type
			if (target.equals(member))
				return true;
			
			//true, if the type contains the target
			if (member instanceof IMethod) {
				final IMethod method= (IMethod) member;
				final IMethod stub= target.getMethod(method.getElementName(), method.getParameterTypes());
				if (stub.exists())
					return true;
			}
			
			//if the member is not declared in a type
			if (member.getDeclaringType() == null) {
				
				//false if the member is not a type 
				if (!(member instanceof IType))
					return false;
				
				//true, if the member is public
				if (JdtFlags.isPublic(member))
					return true;
				
				//false, if the member is private
				if (!JdtFlags.isPackageVisible(member))
					return false;
				
				//true, if the type and member are in the same package
				if (JavaModelUtil.isSamePackage(((IType) member).getPackageFragment(), target.getPackageFragment()))
					return true;
				
				
				IType type= member.getDeclaringType();
				//true, if the type is a subclass
				if (type != null)
					return hierarchy.contains(type);
				
				//otherwise, false
				return false;
			}
			
			final IType declaringType= member.getDeclaringType();
			
			//false, when the member's declaring type is inaccessible
			if (!canBeAccessedFrom(declaringType, target, declarationType, hierarchy))
				return false;
			
			//what's this?
			if (declaringType.equals(declarationType))
				return false;
			return true;
		}
		return false;
	}

	/**
	 * @param element	
	 * 
	 * @return	all IJavaElements referenced from inside element
	 * 
	 * @throws JavaModelException
	 */
	public static List<IJavaElement> getReferencesFrom(IMember element) throws JavaModelException {
		
		final List<IJavaElement> references = new ArrayList<IJavaElement>();
		
		SearchRequestor requestor = new SearchRequestor() {		
			@Override
			public void acceptSearchMatch(SearchMatch match) {
				if(match.getElement() instanceof IJavaElement)
					references.add((IJavaElement)match.getElement());
		
			}		
		};
		
		IProgressMonitor monitor = new NullProgressMonitor();
		
		SearchEngine engine = new SearchEngine();
		engine.searchDeclarationsOfSentMessages(element,requestor,monitor);		
		engine.searchDeclarationsOfAccessedFields(element,requestor,monitor);
		engine.searchDeclarationsOfReferencedTypes(element,requestor,monitor);
		
		return references;
	}

	/**
	 * @param member
	 * @param setter	if this is a setter - if false, then this is a getter
	 * 
	 * @return	given an IField will return a valid name for a setter or getter
	 * 			that is not already a sibling method of the IField
	 */
	public static String selectorFor(IField member, boolean setter){
		
		return selectorFor(member,setter,0);
	}

	private static String selectorFor(IField member, boolean setter, int iterationCount){
		
		String primes = "";
		for(int i = 0; i<iterationCount; i++)
			primes += "Prime";
	
		String prefix = setter ? "set" : "get";
		String name = member.getElementName();
		name = name.replaceFirst(name.substring(0,1),name.substring(0,1).toUpperCase());
		
		name = prefix + name + primes;
		
		try {
			for(IMethod method : member.getDeclaringType().getMethods())
				if(method.getElementName().equals(name))
					return selectorFor(member,setter,iterationCount+1);
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		
		return name;
	}

	public static IMethod createAccessor(IField field, boolean isSetter) 
		throws JavaModelException{		
		
		String type =  Signature.toString(field.getTypeSignature());
		
		String source = null;
		
		if(isSetter)
		
			source = "protected void " + selectorFor(field,true) + 
				"(" + type + " arg){\n" +
				"\t this." + field.getElementName() + " = arg;\n}";	
			
		else

			source = "protected "+type+" " + selectorFor(field,false) + 
						"(){\n" +
						"\t return " + field.getElementName() + ";\n}";
		
		
		return field.getDeclaringType().createMethod(	source,
														field,
														true,
														new NullProgressMonitor());	
	}

	/**
	 * @param inner
	 * @param outer
	 * @return whether inner is within outer
	 */
	public static boolean isWithin(SourceRange inner, ISourceRange outer) {
				
		if((outer.getOffset() > inner.getStartPosition()))
			return false;
		
		if(outer.getOffset() + outer.getLength() < 
				inner.getStartPosition() + inner.getLength())
			return false;
		
		return true;
	}
	

	public static UndoEdit rewriteAST(ASTRewrite rewrite, IBuffer source) 
						throws MalformedTreeException, BadLocationException {
	    
	    IDocument doc = new Document(source.getContents());
	    TextEdit edits = rewrite.rewriteAST(doc,null);
	    
	    UndoEdit edit = edits.apply(doc);
		source.setContents(doc.get());
	    
	    return edit;
	}

	/**
	 * Changes the super type of type to newSuper
	 * 
	 * @param t
	 * @param parent
	 */
	public static void setSuperType(final IType type, final IType newSuper) {
		
		try {
			ICompilationUnit cu = type.getCompilationUnit();
			String source = cu.getBuffer().getContents();
			Document document= new Document(source);
		
			// creation of DOM/AST from a ICompilationUnit
			ASTParser parser = ASTParser.newParser(AST.JLS3);
			parser.setSource(cu);
			final CompilationUnit astRoot = (CompilationUnit) parser.createAST(null);
		
			// start record of the modifications
			astRoot.recordModifications();

			ASTVisitor visitor = new ASTVisitor(){
				public boolean visit(TypeDeclaration decl){
					
					if(!decl.getName().getIdentifier().equals(type.getElementName()))
						return false;
					
					AST ast = astRoot.getAST();
					Type sType = newSuper!=null ?
						//TODO: comment back in after fix in Eclipse bug I reported
						//ast.newSimpleType(ast.newName(newSuper.getFullyQualifiedName())) :
						ast.newSimpleType(ast.newName(newSuper.getElementName())) :
						ast.newSimpleType(ast.newName("Object"));
					decl.setSuperclassType(sType);
					return false;
				}
			};
			
			astRoot.accept(visitor);
			
			TextEdit edits = astRoot.rewrite(document,cu.getJavaProject().getOptions(true));
			edits.apply(document);
			cu.getBuffer().setContents(document.get());
			cu.getBuffer().save(new NullProgressMonitor(),true);
			
		} catch (JavaModelException e) {
			e.printStackTrace();
		} catch (MalformedTreeException e) {
			e.printStackTrace();
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	//TODO: refactor into JDTUtil
	public static Collection<IMethod> overridersOf(final IMethod method){      
	    
	    SearchEngine engine = new SearchEngine();
	    SearchPattern pattern = SearchPattern.createPattern(method,
	                                IJavaSearchConstants.DECLARATIONS);
	    
	    IType[] subclasses = getSubclasses(method.getDeclaringType());
	    
	    final Set<IMethod> found = new HashSet<IMethod>();
	    
	    if(pattern==null)
	        return found;
	    
	    IJavaSearchScope scope = SearchEngine.
	        createJavaSearchScope(subclasses);
	    
	    SearchRequestor requestor = new SearchRequestor() {        
	        public void acceptSearchMatch(SearchMatch match) {
	            if(match.getElement() instanceof IMethod)
	            	found.add((IMethod)match.getElement());                
	        }        
	    };
	    
	    try {
	        engine.search(pattern, 
	                            new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()}, 
	                            scope, 
	                            requestor, 
	                            null);
	    } catch (CoreException e) {
	        e.printStackTrace();
	    }
	    
	    return found;
	}
	
    /**
     * A null pointer exception will be thrown if the {@link MethodDeclaration}
     * was not parsed with BindingsResolved
     * 
     * @param methodDecl
     * @param method
     * 
     * @return		whether the two arguments are the same, assuming they
     * 				are in the same class (same name and parameter types)
     */
	public static boolean isSameMethod(MethodDeclaration methodDecl,IMethod method) {

		if (!methodDecl.getName().getIdentifier().equals(
				method.getElementName()))
			return false;

		List parameters1 = methodDecl.parameters();
		String[] parameters2 = method.getParameterTypes();

		if (parameters1.size() != parameters2.length)
			return false;

		Iterator params1 = parameters1.iterator();

		int param2Count = 0;
		while (params1.hasNext()) {
			SingleVariableDeclaration varDecl = (SingleVariableDeclaration) params1
					.next();

			String typeName = varDecl.resolveBinding().getType().getName();

			if (!typeName.equals(Signature.toString(parameters2[param2Count])))
				return false;

			param2Count++;
		}

		return true;
	}
	
	/**
	 * Tries two methods for getting the source in an editor
	 * 
	 * @param editor	an editor that contains java source
	 * 
	 * @return			the contents of that editor
	 */
	public static String getCUSource(AbstractTextEditor editor){
		
		ICompilationUnit compilationUnit = getCompilationUnit(editor);
		
		try {
			return compilationUnit.getSource();
		} catch (Exception e) {
			// ignore - we'll try again...
		}
		
		return editor.getDocumentProvider().getDocument(editor.getEditorInput()).get();	
	}

	/**
	 * @param editor	an editor
	 * 
	 * @return	the compilation unit that the editor contains, or null if it doesn't
	 * 			contain one
	 */
	public static ICompilationUnit getCompilationUnit(AbstractTextEditor editor) {
		return (ICompilationUnit)editor.getEditorInput().
												getAdapter(IJavaElement.class);
	}

	public static int whiteSpaceBefore(ASTNode node, String source) {		
		
		int whiteSpaceCount = 0;
		for(int i = node.getStartPosition()-1; i > 0; i--){
			
			char lastChar = source.charAt(i);
			if(Character.isWhitespace(lastChar))
				whiteSpaceCount++;
			else
				i = -1;//break early...
		}
		
		return whiteSpaceCount;
	}
	
	public static int whiteSpaceAfter(ASTNode node, String source) {	
		
		int whiteSpaceCount = 0;
		for(int i = node.getStartPosition()+node.getLength(); i < source.length(); i++){
			
			char lastChar = source.charAt(i);
			if(Character.isWhitespace(lastChar))
				whiteSpaceCount++;
			else
				i = source.length();//break early...
		}
		
		return whiteSpaceCount;
	}

	public static String getContents(InputStream is) {
		if (is == null)
			return null;
		BufferedReader reader= null;
		try {
			StringBuffer buffer= new StringBuffer();
			char[] part= new char[2048];
			int read= 0;
			reader= new BufferedReader(new InputStreamReader(is));
	
			while ((read= reader.read(part)) != -1)
				buffer.append(part, 0, read);
			
			return buffer.toString();
			
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException ex) {
					// silently ignored
				}
			}
		}
		return null;
	}

	public static boolean isExtractableExpression(ASTNode node) {
		if (!(node instanceof Expression))
			return false;
		
		if(node.getParent().getNodeType()==ASTNode.ASSIGNMENT)
			if(((Assignment)node.getParent()).getLeftHandSide()==node)
				return false;
			
		if(node.getParent() instanceof VariableDeclaration)
			if(((VariableDeclaration)node.getParent()).getName()==node)
				return false;
		
		if(node instanceof Annotation)
			return false;
		
		if (node instanceof Name) {
			IBinding binding= ((Name) node).resolveBinding();
			return !(binding instanceof ITypeBinding ||
					binding instanceof IMethodBinding);
		}
		return true;
	}
}