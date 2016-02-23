package edu.pdx.cs.multiview.util;

import java.io.PrintStream;


/**
 * @author Phil Quitslund
 */
public class Debug {

	public static boolean TRACE = true;
	public static boolean DEBUG = true;
	
	private static final PrintStream OUT = System.out;
	
	
	//private static Collection listeners = new Vector();

	public static void trace(String msg) {
		if (TRACE) {
			println("[trace]: " +  msg);
			//inform("[trace]: " +  msg);
		}
	}

	public static void debug(String msg) {
		if (DEBUG) {
			println("[debug]: " + msg);
			//inform("[debug]: " +  msg);
		}
	}

	public static void error(String msg) {
		println("[error]: " + msg);
		//inform("[error]: " +  msg);
	}
	
	
	
	public static void report(Exception e) {
		if (DEBUG || TRACE) {
			println("[exception caught]: " + e.getMessage());
			e.printStackTrace(System.out);
			//inform("[exception caught]: " + e.getMessage());
		}
	}


	public static void print(String msg) {
		OUT.print(msg);
		
	}	
	
	public static void print(Object o) {
		print(o.toString());
	}


	public static void trace(Object o) {
		trace(o == null ? "" : o.toString());
	}


	public static void trace(int i) {
		trace(new Integer(i));		
	}
	
	/**
	 * @param label
	 * @param msg
	 */
	public static void trace(String label, String msg) {
		if (TRACE)
			inform("[" + label + "]: " + msg);
	}
	
	private static void inform(String msg) {
//		for (Iterator iter = listeners.iterator(); iter.hasNext(); )
//			((LogListener)iter.next()).log(msg+"\n");
		println(msg);
	}

	private static void println(String msg) {
		print(msg+"\n");
	}

	/**
	 * @param view
	 */
//	public static void addLogListener(LogListener listener) {
//		listeners.add(listener);
//	}

}
