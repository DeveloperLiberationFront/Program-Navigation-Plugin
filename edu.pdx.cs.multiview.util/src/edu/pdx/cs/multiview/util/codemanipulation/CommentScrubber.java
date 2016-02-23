package edu.pdx.cs.multiview.util.codemanipulation;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StreamTokenizer;
import java.io.StringReader;

import edu.pdx.cs.multiview.util.Debug;


/**
 * Phil's Attic
 * 
 * @author pq
 */
public class CommentScrubber {
	
	public static void main(String[] args) {	
		System.out.println(strip("/* foo */ \n\nvoid foo() { int x; // a comment \n}"));
		System.out.println(strip("/** some docs that might break alias of foo() */ public void foo() { System.err.println(\"foo\"); foo(3); }"));
		System.out.println(strip("static void /*final*/ bar() { System.err.println('x'); }"));
	}
	
	public static String strip(String s) {
		StreamTokenizer st = new StreamTokenizer (new BufferedReader (new StringReader(s)));    
		PrintStream out = System.out;
		StringBuffer sb = new StringBuffer();
		
		st.eolIsSignificant (true);    	
		st.ordinaryChar('/'); //-- recognizes the division operator '/'    	
		st.ordinaryChar('.'); //-- if '.' is not an ordinary char, a single '.' is taken as the numeric constant 0.0    	
		st.ordinaryChars('0','9');//-- let your C or java compiler take care of numerical constants...    	
		st.wordChars('0','9'); //-- let your C or java compiler take care of numerical constants...    	
		st.wordChars('_','_'); //-- includes the '_' as a word constituent    	
		st.slashStarComments (true); //-- recognizes /*    	
		st.slashSlashComments (true); //-- recognizes //  
		st.ordinaryChar('"');
		st.ordinaryChar('\'');
		st.whitespaceChars(Character.SPACE_SEPARATOR, Character.PARAGRAPH_SEPARATOR);
		try {
			while (st.nextToken() != StreamTokenizer.TT_EOF) {	    
				switch (st.ttype) {	    
					case StreamTokenizer.TT_EOL: break;	    
					case StreamTokenizer.TT_NUMBER: sb.append(st.nval); break;	    
					case StreamTokenizer.TT_WORD: sb.append(st.sval); break;	    	    
					default: sb.append((char)st.ttype); break;	    }    	
			}
		} catch (IOException e) {
			Debug.debug("CommentScrubber encountered unexpected IOException");
			e.printStackTrace();
			return "";
		}
		return sb.toString();
	}
	
	public static boolean equals(String s1, String s2) {
        
		if (s1 == null)
			return (s2 == null);
		if (s2 == null)
			return (s1 == null);
		return strip(s1).equals(strip(s2));
	}
	

}


