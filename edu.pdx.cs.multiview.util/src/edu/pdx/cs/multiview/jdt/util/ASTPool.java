package edu.pdx.cs.multiview.jdt.util;


import java.util.Date;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.PackageDeclaration;

/**
 * Builds and returns ASTs for clients.  Limits the total number of ASTs
 * in memory by caching ASTs and evicting least-recently-used ASTs.
 * 
 * @author emerson
 */
public abstract class ASTPool <IndexType>{
	
	protected class Entry{
		private CompilationUnit unit;
		private Date date;
		
		public Entry(CompilationUnit unit, Date date) {
			this.unit = unit;
			this.date = date;
		}
		
		public CompilationUnit getUnit() {
			return unit;
		}
	
		public Date getDate() {
			return date;
		}
		
		
	}

	private LRUCache<String, Entry> cache;
	private int maxSize = 3;

	protected ASTPool(int size){
		cache = new LRUCache<String, Entry>(maxSize);
		maxSize = size;
	}
	
	public CompilationUnit getAST(IndexType file){
		
		return getEntry(file).unit;
	}

	public void removeEntry(String path){
		cache.remove(path);
	}

	
	private Entry getEntry(IndexType file) {
		String path = getPath(file);
		Entry e = cache.get(path);
		if(e==null){
			fillCache(file);
			e = cache.get(path);
		}
		return e;
	}

	private void fillCache(IndexType file) {
		CompilationUnit compilationUnit = parse(file);
		Entry e = new Entry(compilationUnit, new Date());
		cache.put(getPath(file), e);
	}
	
	

	public PackageDeclaration getPackage(IndexType file) {
		return getAST(file).getPackage();
	}
	
	public void release(IndexType file) {
		cache.remove(getPath(file));
	}

	abstract String getPath(IndexType file) ;
	
	
	protected abstract CompilationUnit parse(IndexType file);
	
	private static ASTParser parser;
	
	protected ASTParser getParser() {

		if(parser==null){
			parser = ASTParser.newParser(AST.JLS3);
		}
		
		return parser;
	}

	private static  ASTPool<IFile> fileDefaultPool;
	private static	ASTPool<ICompilationUnit> cuDefaultPool;
	
	public static ASTPool<IFile> getDefault(){
		if(fileDefaultPool==null){
			fileDefaultPool = new ASTFilePool(3);
		}
		return fileDefaultPool;
	}
	
	public static ASTPool<ICompilationUnit> getDefaultCU(){
		if(cuDefaultPool==null){
			cuDefaultPool = new ASTCUPool(3);
		}
		return cuDefaultPool;
	}
}

class ASTFilePool extends ASTPool<IFile>{

	public ASTFilePool(int size) {super(size);}

	protected CompilationUnit parse(IFile file){
			
			try {
				String s = JDTUtils.getContents(file.getContents());
				getParser().setSource(s.toCharArray());
			} catch (CoreException ex) {
				ex.printStackTrace();
			}
			
			return (CompilationUnit)getParser().createAST(null);
		}
	
	protected String getPath(IFile file) {
		return file.getFullPath().toString();
	}
}

class ASTCUPool extends ASTPool<ICompilationUnit>{

	public ASTCUPool(int size) {
		super(size);
	}

	@Override
	protected CompilationUnit parse(ICompilationUnit source) {
		
		getParser().setSource(source);
		getParser().setResolveBindings(true);
		
		return  (CompilationUnit)getParser().createAST(null);
	}
	
	protected String getPath(ICompilationUnit file) {
		return file.getPath().toString();
	}
}
