/** ***************************************************
 * File:  Record.java
 *
 *
 * Copyright (c) 2002-2003 Advanced Applications Total Applications Works.
 * (AATAW)  All Rights Reserved.
 *
 * AATAW grants you ("Licensee") a non-exclusive, royalty free, license to use,
 * modify and redistribute this software in source and binary code form,
 * provided that i) this copyright notice and license appear on all copies of
 * the software; and ii) Licensee does not utilize the software in a manner
 * which is disparaging to AATAW.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
 * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. AATAW AND ITS LICENSORS SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
 * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL AATAW OR ITS
 * LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,
 * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF
 * OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 *
 * This software is not designed or intended for use in on-line control of
 * aircraft, air traffic, aircraft navigation or aircraft communications; or in
 * the design, construction, operation or maintenance of any nuclear
 * facility. Licensee represents and warrants that it will not use or
 * redistribute the Software for such purposes.
 ***************************************************   */
package test_flower;

import javax.swing.JOptionPane;
import java.io.*;
import javax.swing.*;
import java.util.*;
import test_flower.LinkedList;


/** *******************************************************************
 * class: The Record class' purpose is to read and write records to
 *        a randomaccess file.
 * *******************************************************************/
public class Record  {
   private int recID ;
   private int next ;
   private int quantity ;
   private String toolType ;
   private String brandName ;
   private String toolDesc ;
   private String partNum ;
   private String cost ;


   public Record() {
   }

   public Record( Record rec ) {
      this.setRecID( rec.getRecID()  )  ;
      this.setNext( rec.getNext() )  ;
      this.setQuantity( rec.getQuantity() )  ;
      this.setToolType( rec.getToolType() )  ;
      this.setBrandName( rec.getBrandName() )  ;
      this.setToolDesc( rec.getToolDesc() )  ;
      this.setPartNumber(rec.getPartNumber() ) ;
      this.setCost( rec.getCost() ) ;
   }


   /** *******************************************************************
    *  Method: ReadRec() Reads a record from the specified RandomAccessFile.
    * 1- Read the first integer
    * 2- Read the second integer
    * 3- Read characters one at a time until we reach a string of
    *    ';;;'. This indicates that we have reached the end of the
    *    character string for this particular record.
    * 4- Load the resulting string into a StringTokenizer object.
    * 5- We are looking for 7 tokens, so if the token count is
    *    greater than 4, we will tokenize the string.
    * 6- The tokens are loaded into a string array and then into the
    *    class variables.
    ********************************************************** */
   public void ReadRec( RandomAccessFile file ) throws IOException
   {
      char f[] = new char[ 45 ];
      StringTokenizer tokens ;
      StringBuffer buf1  = new StringBuffer("");

      recID = file.readInt();

      next  = file.readInt();

      quantity = file.readInt();

      for ( int i = 0; i < f.length; i++ ) {
         f[ i ] = file.readChar();
         if ( !Character.isLetterOrDigit( f[ i ] ) )
             f[ i ] = ' ' ;
      }
      toolType = new String( f );
      toolType = toolType.trim() ;

      for ( int i = 0; i < f.length; i++ ) {
         f[ i ] = file.readChar();
         if ( !Character.isLetterOrDigit( f[ i ] ) )
             f[ i ] = ' ' ;
      }
      brandName = new String( f );
      brandName = brandName.trim() ;

      for ( int i = 0; i < f.length; i++ ) {
         f[ i ] = file.readChar();
         if ( !Character.isLetterOrDigit( f[ i ] ) )
             f[ i ] = ' ' ;
      }
      toolDesc = new String( f );
      toolDesc = toolDesc.trim() ;
      tokens =  new StringTokenizer( toolDesc, "�?") ;
      while (tokens.hasMoreTokens()) {
         buf1.append( tokens.nextToken() ) ;
      }
      toolDesc = buf1.toString() ;

      for ( int i = 0; i < f.length; i++ ) {
         f[ i ] = file.readChar();
         if ( !Character.isLetterOrDigit( f[ i ] ) )
             f[ i ] = ' ' ;
      }
      partNum = new String( f );
      partNum = partNum.trim() ;

      for ( int i = 0; i < f.length; i++ ) {
         f[ i ] = file.readChar();
         if ( !Character.isLetterOrDigit( f[ i ] ) ) {
            if ( f[ i ] != '.' )
               f[ i ] = ' ' ;
         }
      }
      cost = new String( f );
      cost = cost.trim() ;
   }

   /** ***************************************************************
    *  The fill() method is used to fill in the passed string with
    *  blanks.
    ***************************************************************** */
    public StringBuffer fill ( String str, StringBuffer buf ) {
       String strTwo = new String( "                     "  +
           "                                             " ) ;

       if ( str != null )
         buf = new StringBuffer( str + strTwo );
      else
         buf = new StringBuffer( strTwo );

      buf.setLength( 45 );

      return buf ;
   }


   /** ************************************************************
    *  write() Writes a record to the specified RandomAccessFile.
    *          1- First it writes a int (recid) to the output file
    *          2- Next it writes the quantity as an int.
    *          3- Then it writes the remaing record as a string.
    ************************************************************** */
   public void write( RandomAccessFile file ) throws IOException
   {
      StringBuffer buf  = new StringBuffer( " " );
      String str = "" ;

      file.writeInt( recID );
      str = str + recID + " " ;

      file.writeInt( next );
      str = str + next + " "  ;

      file.writeInt( quantity );
      str = str + quantity + " "  ;
      //System.out.println( "The value of quantity on write is "
       //              + quantity );

      buf = fill ( toolType, buf ) ;
      file.writeChars( buf.toString() );
      str = str + buf.toString() ;

      buf = fill ( brandName, buf.delete(0, 44) ) ;
      file.writeChars( buf.toString() );
      str = str + buf.toString() ;

      buf = fill ( toolDesc, buf.delete(0, 44) ) ;
      file.writeChars( buf.toString() );
      str = str + buf.toString() ;


      buf = fill ( partNum, buf.delete(0, 44) ) ;
      file.writeChars( buf.toString() );
      str = str + buf.toString() ;

      buf = fill ( cost, buf.delete(0, 44) ) ;
      file.writeChars( buf.toString() );
      str = str + buf.toString() ;

   }

   /** ********************************************************
    * Method: getRecID() is used to obtain the record ID.
    ********************************************************/
   public int getRecID() { return recID; }

   /** ********************************************************
    * Method: getToolType() is used to obtain the tool type.
    ********************************************************/
   public String getToolType() { return toolType.trim(); }

   /** ********************************************************
    * Method: getToolDesc() is used to obtain the description of
    *         the tool.
    ********************************************************/
   public String getToolDesc() { return toolDesc.trim(); }

   /** ********************************************************
    * Method: getPartNumber() is used to obtain the Part Number
    *         value currently in the record.
    ********************************************************/
   public String getPartNumber() { return partNum.trim(); }

   /** ********************************************************
    * Method: getQuantity() is used to obtain the value of the
    *         quantity currently in the record.
    ********************************************************/
   public int getQuantity() { return quantity; }

   /** ********************************************************
    * Method: getBrandName() is used to obtain the value of the
    *         current value of Brand Name from the record.
    ********************************************************/
   public String getBrandName() { return brandName.trim(); }

   /** ********************************************************
    * Method: getCost() is used to obtain the the value of the
    *         current value of cost  from the record.
    ********************************************************/
   public String getCost() { return cost.trim(); }

   public int getNext() { return next ; }

   /** ********************************************************
    * Method: setToolType() is used to set the value of the
    *         current value of Tool Type from the record.
    ********************************************************/
   public void setToolType( String f ) { toolType = f; }

   /** ********************************************************
    * Method: setRecID() is used to set the value of the of the
    *         record ID in the record.
    ********************************************************/
   public void setRecID( int p ) { recID = p ; }

   public void setNext( int p ) { next = p ; }

   /** ********************************************************
    * Method: setCost() is used to set the value of the of the
    *         cost in the record.
    ********************************************************/
   public void setCost( String f ) { cost = f ; }

   /** ********************************************************
    * Method: setBrandName() is used to set the value of the
    *         brand name in the record.
    ********************************************************/
   public void setBrandName( String f ) { brandName = f; }

   /** *************************************************************
    * Method: setToolDesc() is used to set the value of the tool
    *         description in the record.
    ********************************************************/
   public void setToolDesc( String f ) { toolDesc = f; }

   /** ********************************************************
    * Method: setPartNumber() is used to set the part number
    *         in the record.
    ********************************************************/
   public void setPartNumber( String f ) { partNum = f; }

   /** ********************************************************
    * Method: setQuantity() is used to set the value of the
    *         quantity in the record.
    ********************************************************/
   public void setQuantity( int q ) { quantity = q; }

   /** NOTE: This method contains a hard coded value for the
    * size of a record of information. The value is arrived at
    * by adding up the size Java allocates to each data type
    * writeChars(String s)
    *   Writes every character in the string s, to the output stream, in
    *   order, two bytes per character.
    * The data Record is five strings of 45 characters each and two int
    * data types.
    *
    */
   public static int getSize() { return 462; }
}
