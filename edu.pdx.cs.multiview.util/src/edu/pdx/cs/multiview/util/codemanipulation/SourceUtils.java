package edu.pdx.cs.multiview.util.codemanipulation;


/**
 * A utility class for source manipulation routines.
 *
 */
public class SourceUtils {

	// Source manipulation routines snipped from 
	//http://www.rgagnon.com/javadetails/java-0352.html
	
    /* remove leading whitespace 
     * @see http://www.rgagnon.com/javadetails/java-0352.html
     */
    public static String trimLeadingWS(String source) {
        return source.replaceAll("^\\s+", "");
    }

    /* remove trailing whitespace
    * @see http://www.rgagnon.com/javadetails/java-0352.html
    */
    public static String trimTrailingWS(String source) {
        return source.replaceAll("\\s+$", "");
    }

    /* replace multiple whitespaces between words with single blank
     * @see http://www.rgagnon.com/javadetails/java-0352.html
     */
    public static String itrim(String source) {
        return source.replaceAll("\\b\\s{2,}\\b", " ");
    }

    /* remove all superfluous whitespaces in source string
     * @see http://www.rgagnon.com/javadetails/java-0352.html
     */
    public static String trim(String source) {
        return itrim(trimLeadingWS(trimTrailingWS(source)));
    }

    /*
     * @see http://www.rgagnon.com/javadetails/java-0352.html
     */
    public static String lrtrim(String source){
        return trimLeadingWS(trimTrailingWS(source));
    }
	
	
}
