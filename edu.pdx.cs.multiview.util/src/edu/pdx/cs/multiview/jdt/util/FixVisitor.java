/*
 * Created on Jul 27, 2004
 */
package edu.pdx.cs.multiview.jdt.util;

import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEditGroup;


/**
 * @author Emerson Murphy-Hill
 *
 * Represents an ASTVisitor that "fixes" an AST.  Subclasses should use the 
 * ASTRewrite and TextEdit group during visitation of the AST, and call
 * doReplacement to do the replacement when complete.
 * 
 * <p> 
 * Note: this class is intended to be extended by clients.
 * </p>
 */
public abstract class FixVisitor extends ASTVisitor{

    private ASTRewrite rewrite;
    private TextEditGroup group;
    
    /**
     * Constructor
     * 
     * @param anAST	the AST this visitor will be working over
     */
    public FixVisitor(AST anAST){
        
		rewrite = ASTRewrite.create(anAST);
        group = new TextEditGroup("");
    }
    
    /**
     * Commits all changes made in the rewriter. All exceptions are caught and
     * printed here.If the changes are acceptable, the modified argument should
     * be saved.
     * 
     * @param source
     *            a buffer to the source of the file being modified
     */
    public void doReplacement(IBuffer source) {
        
        try {
            JDTUtils.rewriteAST(getRewrite(),source);
        } catch (MalformedTreeException e) {
            e.printStackTrace();
        } catch (BadLocationException e) {
            e.printStackTrace();
        }  
    }
    
    /**
     * @return	the rewriter to be used to modify the AST
     */
    public ASTRewrite getRewrite(){
        
        return rewrite;
    }
    
    /**
     * @return	the group to be used to track the changes 
     */
    public TextEditGroup getTextEditGroup(){
        
        return group;
    }
}
