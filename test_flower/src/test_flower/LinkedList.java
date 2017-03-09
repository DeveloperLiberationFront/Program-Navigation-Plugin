/**************************************************************
 * File: LinkedList.java
 *
 * Methods:
 *    -  public LinkedList()
 *    -  public LinkedList(int n, LinkedList ln)
 *    -  public void add( int num )
 *    -  void addLast( int num)
 *    -  public void addFirst( int num )
 *    -  public void DisplayLL()
 *    -  void Remove(int num)
 *    -  public boolean isEmpty()
 *    -  public boolean contains( int num )
 *    -  public void toArray(int a[])
 *    -  public int getLast()
 *    -  public int size()
 *    -  public static void main(String args[])
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
 ***********************************************************************/
package test_flower;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.awt.event.*;
//import Record;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import java.util.*;
import test_flower.Record ;

public class LinkedList {
   public int value;              // value of element
   public boolean myDebug = false ;
   public LinkedList next;        // reference to next
   private int head ,
               minusOne = -1 ,
               ONE = 1 ,
               ZERO = 0 ;

   /** **********************************************
    *  constructor
    *
    ************************************************ */
   public LinkedList() {
      // initialize list head
      head = -1;
   }

   /** **********************************************
    *  constructor
    *
    ************************************************ */
   public LinkedList(int n, LinkedList ln) {
      value = n;
      next = ln;
   }


   /** *************************************************
    *  sysPrint() - is used for debugging
    *               - set myDebug = true to turn on debugging
    *
    ************************************************ */
   public void sysPrint( String str  )   {
      if ( myDebug ) {
         System.out.println( str );
      }
   }
   /** ****************************************************************
    *  Method: add( RandomAccessFile lFile, RandomAccessFile hFile,
                        Record NewNode )
    *
    *  Purpose:  Adds a record to the list in sorted
    *  order.
    *
    ********************************************************************** */
   public void add( RandomAccessFile lFile, RandomAccessFile hFile,
                        Record NewNode ) {
      Record data, NodeRef  = new Record() , PreviousNode  = new Record() ,
                   HeadRef  = new Record() , secPreviousNode  = new Record()   ;
      int filesize = 0 , comp1 = 0 , loopCtl = 0 , ii = 0 ;
      boolean greaterThan = false  ;

      sysPrint("add(): 1a - starting the add processing with NewNode "
                     + NewNode.getRecID() + " "
                     + NewNode.getNext() + " "
                     + NewNode.getBrandName()
      ) ;
      /** ***********************************************
       * If there are no nodes in the list
       * make NewNode the first node
       ************************************************** */

      try {
         if ( lFile.length() <= ZERO )  {
            sysPrint("add(): 1b - adding first node to linked list") ;
            hFile.seek( 0 ) ;
            NodeRef.ReadRec( hFile );
            NodeRef.setNext( 0 ) ;
            NodeRef.write( hFile ) ;
            lFile.seek( 0 ) ;
            NewNode.setRecID( 0 );
            NewNode.setNext( -1 );
            NewNode.write( lFile );
            sysPrint("add(): 1ab - adding the first node "
                     + NewNode.getRecID() + " "
                     + NewNode.getNext() + " "
                     + NewNode.getBrandName() + " "
            ) ;
            sysPrint("add(): 1ac - the head node "
                     + NodeRef.getRecID() + " "
                     + NodeRef.getNext() + " "
            ) ;
         }
         else	{  /** Otherwise, check if a new head is required */

            sysPrint("add(): 1bb - there is a node in the headfile.") ;

            try {
               hFile.seek( 0 ) ;
               NodeRef.ReadRec( hFile ) ;
               hFile.seek( 0 ) ;
               HeadRef.ReadRec( hFile ) ;
               PreviousNode = new Record( NodeRef ) ;
               sysPrint("add(): 1bc - read headfile.") ;
               lFile.seek( NodeRef.getNext() * Record.getSize()  ) ;
               NodeRef.ReadRec( lFile ) ;

                 /** ***********************************************************
                  * Check to see if the head should be replaced by the
                  *************************************************************/
                  // NewNode Brand name.
                           /*
               sysPrint("add(): 1bc -  A new head has been inserted at " + NewNode.getRecID()
                  + " with a Brand name of " + NewNode.getBrandName() );
               sysPrint("add(): 1bd - The old head was at " + NodeRef.getRecID()
                  + " with a Brand name of " + NodeRef.getBrandName() );  */

               comp1 = NodeRef.getBrandName().toUpperCase().compareTo( NewNode.getBrandName().toUpperCase())  ;
               sysPrint("add(): 1be - The value of the compare is "  + comp1
                             + "   " + NodeRef.getBrandName() + " vs " + NewNode.getBrandName() ) ;

               if ( comp1 >= ZERO ) {
                  sysPrint("add(): 1bf - Processing  if(comp1) code - need a new head." ) ;
                  // The node that the head points to has a Brand name >
                  // the one in the NewNode
                  NewNode.setNext( HeadRef.getNext() );
                  filesize = (int) lFile.length() / Record.getSize() ;
                  sysPrint("add(): 1bg - The value of lFile.length() is " + lFile.length() ) ;
                  sysPrint("add(): 1bga - The value of filesize is " + filesize ) ;
                  NewNode.setRecID( filesize  ) ;
                  lFile.seek( filesize * Record.getSize()) ;
                  NewNode.write( lFile );
                  hFile.seek( 0 ) ;
                  HeadRef.setNext( NewNode.getRecID() );
                  HeadRef.setBrandName( NewNode.getBrandName() );
                  HeadRef.write( hFile );

                  sysPrint("add(): 1c -  A new head has been inserted " + NewNode.getRecID()
                  + " with a Brand name of " + NewNode.getBrandName() + " located at " + HeadRef.getNext() );
                  sysPrint("add(): 1d - The old head was  " + HeadRef.getNext()
                  + " with a Brand name of " + NodeRef.getBrandName() );
               }
               else {
                  // Skip all nodes whose Brand member is less
                  // than NewNode Brand name.

                  loopCtl = (int) lFile.length() / Record.getSize()  ;
                  secPreviousNode = new Record( PreviousNode ) ;
                  PreviousNode    = new Record( NodeRef ) ;

                  while ( ii < loopCtl ) {
                     if ( NodeRef.getNext() !=  minusOne ) {
                        secPreviousNode = new Record( PreviousNode ) ;
                        PreviousNode = new Record( NodeRef ) ;
                        sysPrint("add() 1db: secPreviousNode is " + secPreviousNode.getBrandName() ) ;
                        sysPrint("add() 1dc: PreviousNode is "    +  PreviousNode.getBrandName() ) ;
                        sysPrint("add() 1dd: NodeRef is "         + NodeRef.getBrandName() ) ;
                        lFile.seek( NodeRef.getNext() * Record.getSize() ) ;
                        NodeRef.ReadRec( lFile );
                        comp1 = NodeRef.getBrandName().toUpperCase().compareTo( NewNode.getBrandName().toUpperCase())  ;
                        if ( comp1 >= ZERO ) {
                           greaterThan = true ;
                           break ;
                        }
                     }
                     else {
                        break ;
                     }

                     ii++ ;
                  }

 	          /** **********************************************
                   * Insert the node after the one pointed to
	           * by PreviousNode and before the one pointed to
	           * by NewNode.
                   **********************************************  */

                  if( greaterThan )  {
                     sysPrint("add() 1fa >: The value of PreviousNode.getBrandName is " + PreviousNode.getBrandName());
                     sysPrint("add() 1fb >: The value of secPreviousNode.getBrandName is " + secPreviousNode.getBrandName());
                     sysPrint("add() 1fc >: The value of NodeRef.getBrandName is " + NodeRef.getBrandName());
                     sysPrint("add() 1fc >: The value of NewNode.getBrandName is " + NewNode.getBrandName());

                     NewNode.setNext ( NodeRef.getRecID() ) ;
                     filesize = (int) lFile.length() / Record.getSize() ;
                     NewNode.setRecID( filesize ) ;
                     lFile.seek( filesize * Record.getSize() ) ;
                     NewNode.write( lFile ) ;
                     lFile.seek( PreviousNode.getRecID() * Record.getSize() ) ;
                     PreviousNode.ReadRec( lFile ) ;
                     PreviousNode.setNext( NewNode.getRecID() );
                     lFile.seek( PreviousNode.getRecID() * Record.getSize() ) ;
                     PreviousNode.write( lFile );
                  }
                  else {
                     sysPrint("add() 1fd <: The value of PreviousNode.getBrandName is " + PreviousNode.getBrandName());
                     sysPrint("add() 1fe <: The value of secPreviousNode.getBrandName is " + secPreviousNode.getBrandName());
                     sysPrint("add() 1fc >: The value of NodeRef.getBrandName is " + NodeRef.getBrandName());
                     sysPrint("add() 1ff <: The value of NewNode.getBrandName is " + NewNode.getBrandName());
                     if ( HeadRef.getNext() == PreviousNode.getRecID() ) {
                        sysPrint("add() 1fg <: HeadRef points to PreviousNode. Need a new head. " );
                     }

                     NewNode.setNext ( NodeRef.getRecID() ) ;
                     filesize = (int) lFile.length() / Record.getSize() ;
                     NewNode.setRecID( filesize ) ;
                     NewNode.setNext( -1 );
                     lFile.seek( lFile.length() ) ;
                     NewNode.write( lFile ) ;
                     lFile.seek( NodeRef.getRecID() * Record.getSize() ) ;
                     NodeRef.ReadRec( lFile );
                     NodeRef.setNext( NewNode.getRecID() ) ;
                     lFile.seek( NodeRef.getRecID() * Record.getSize() ) ;
                     NodeRef.write( lFile ) ;
                  }
                  //secPreviousNode.setNext( NewNode.getRecID() ) ;
                  //lFile.seek( secPreviousNode.getRecID() * Record.getSize() ) ;
                 //PreviousNode.write( lFile ) ;

                  //sysPrint("add() 1f: The value of secPreviousNode.getBrandName is " + secPreviousNode.getBrandName());
                  sysPrint("add() 1f: The value of NewNode.getBrandName is " + NewNode.getBrandName());
                  // lFile.seek( NewNode.getRecID() * Record.getSize() ) ;
                  // NewNode.write( lFile ) ;
               }
            }
            catch ( IOException ex ) {
                sysPrint(  "Error reading file" );
            }
         }
      }
      catch ( IOException ex ) {
            sysPrint(    "Error reading file" );
      }
   }

   /** ***************************************************************************
    *  Method: addLast( RandomAccessFile lFile, RandomAccessFile hFile,
                        Record NewNode
    *
    *  Purpose:  Adds a node containing the value passed
    *  in num, to the end of the list.
    *
    ******************************************************************************* */
   void addLast( RandomAccessFile lFile, RandomAccessFile hFile,
                        Record NewNode ) {
      Record NodeRef = null;

      try {
         NodeRef.ReadRec( hFile );


         /** **********************************************
          * Allocate a new node & store Record
          *
          ************************************************ */
         NewNode.setRecID( (int)(lFile.length() / Record.getSize() ) ) ;
         NewNode.setNext( -1 ) ;

         /** *************************************************
          * If there are no nodes in the list
          * make NewNode the first node
          ************************************************* */
         if ( NodeRef.getNext() <= ZERO ) {
            NewNode.setRecID( 0 ) ;
            NewNode.setNext( -1 ) ;
            lFile.seek( 0 );
            NewNode.write( lFile ) ;
            NodeRef = NewNode ;
            NodeRef.setNext( 0 ) ;
            NodeRef.write( hFile ) ;
         }
         else	{  // Otherwise, insert NewNode at end

              // Find the last node in the list
            while ( NodeRef.getNext() >= ZERO ) {
               lFile.seek( NodeRef.getNext() * Record.getSize() );
               NodeRef.ReadRec( lFile );
            }

              // Insert NewNode as the last node

            lFile.seek( lFile.length() / Record.getSize() ) ;
            NodeRef.setRecID( (int)(lFile.length() / Record.getSize() ) );
            NodeRef.setNext( -1 );
            NodeRef.write( lFile );

            //NodeRef.next = NewNode;
         }
      }
      catch ( IOException ex ) {
            //part.setText( "Error reading file" );
      }
   }

   /** ***********************************************************************
    *  Method: addFirst( RandomAccessFile lFile, RandomAccessFile hFile,
    *                   Record NewNode )
    *
    *  Purpose:  Adds a record to the beginning of the
    *  list.
    *
    ************************************************************************* */
   public void addFirst( RandomAccessFile lFile, RandomAccessFile hFile,
                        Record NewNode ) {

      Record NodeRef = null , PreviousNode = null   ;

      try {
         NodeRef.ReadRec( hFile );
         int fEnd ;

         /** *************************************************
          * If there are no nodes in the list
          * make NewNode the first node
          ************************************************* */
         if ( NodeRef.getNext() <= ZERO ) {
            NewNode.setRecID( 0 ) ;
            NewNode.setNext( -1 ) ;
            lFile.seek( 0 ) ;
            NewNode.write( lFile ) ;
            NodeRef = NewNode ;
            NodeRef.setNext( 0 ) ;
            hFile.seek( 0 ) ;
            NodeRef.write( hFile ) ;
         }
         else	{
            fEnd = (int) (lFile.length() / Record.getSize() ) ;
            NewNode.setRecID( fEnd ) ;
            NewNode.setNext( NodeRef.getRecID() ) ;
            lFile.seek( fEnd ) ;
            NewNode.write( lFile ) ;
         }
      }
      catch ( IOException ex ) {
            //part.setText( "Error reading file" );
      }

   }

   /** ************************************************************************
    * Method: DisplayLL( RandomAccessFile lFile, RandomAccessFile hFile )
    *
    * Purpose: Iterate through a non-empty linked list
    *          until all of the entries are displayed on
    *          the screen.
    *
    *          This method is not used in the carStore application
    *          becuase of the use of JTable and the toArray() method.
    ************************************************************************** */
   public void DisplayLL( RandomAccessFile lFile, RandomAccessFile hFile ) {
      Record NodeRef = null ;

      try {
         NodeRef.ReadRec( hFile ) ;

         // list all entries
         while ( NodeRef.getNext() >= ZERO ) {
            lFile.seek( NodeRef.getRecID() * Record.getSize() ) ;
            NodeRef.ReadRec( lFile ) ;
            // Change the following line to JTable code
         //sysPrint(NodeRef.value);
         }
      }
      catch ( IOException ex ) {
       //part.setText( "Error reading file" );
      }
   }

   /** *************************************************************************
    * Method: Remove( RandomAccessFile lFile, RandomAccessFile hFile,
                         int numID )
    *
    * The Remove function searches for a node
    * with Num as its value. The node, if found, is
    * deleted from the list and from memory.
    * 1. Check to see if the Linked list has entries.
    *    - If the Linked list is empty, then there is no
    *      further processing required.
    * 2. Check if the value we are seeking is in the first
    *    node in the Linked list.
    *    - Delete that node by making the value of next the
    *      new head.
    * 3. Start iterating through the Linked list until the
    *    value we are seeking is found.
    *    - Delete that node by removing its reference from
    *      the link.
    *
    ************************************************************************* */
   public void Remove( RandomAccessFile lFile, RandomAccessFile hFile,
                         int numID) {
      Record NodeRef = null , HeadRef , PreviousNode = null  , secPreviousNode = null  ;

      try {
         HeadRef = new Record() ;
         NodeRef = new Record() ;
         HeadRef.ReadRec( hFile ) ;

         /** If the list is empty, do nothing.  */
         if ( HeadRef.getNext() >= ZERO  ) {

            lFile.seek( HeadRef.getNext() * Record.getSize() ) ;
            NodeRef.ReadRec( lFile ) ;

	    /** *******************************************************
             * Determine if the first node is the one.
             *  1- If the first node's recID matches the number passed
             *     - Get the record ID of the next node
             *     - Store the ID of the next node in the head node
             *     - Write a new head node
             **********************************************************/
            if ( NodeRef.getRecID() == numID ) {
               PreviousNode = new Record( NodeRef ) ;
               NodeRef.setRecID( 0 ) ;
               NodeRef.setNext( NodeRef.getNext() ) ;
               NodeRef.setCost( " " ) ;
               NodeRef.setBrandName( " " ) ;
               NodeRef.setPartNumber( " " ) ;
               NodeRef.setToolDesc( " " ) ;
               hFile.seek( 0 ) ;
               NodeRef.write( hFile ) ;
            }
            else {
               sysPrint("Remove() 1: The value of NodeRef.getNext() is "
                     + NodeRef.getNext() ) ;
               secPreviousNode = new Record( NodeRef ) ;
               PreviousNode = new Record( NodeRef ) ;
               lFile.seek( NodeRef.getNext() * Record.getSize() ) ;
               NodeRef.ReadRec( lFile );

               /** *************************************
                * Skip all nodes whose value member is
                * not equal to num.
                * *************************************** */

               while ( NodeRef.getRecID()  >= ZERO ) {
                  sysPrint("Remove() 2: The value of NodeRef.getRecID() is "
                     + NodeRef.getRecID() ) ;
                  secPreviousNode = new Record( PreviousNode ) ;
                  PreviousNode = new Record( NodeRef ) ;
                  sysPrint("Remove() 3: The value of PreviousNode.getRecID() is "
                     + PreviousNode.getRecID() + " and brand name is " + PreviousNode.getBrandName()) ;
                  if ( NodeRef.getRecID() != numID  ) {
                     lFile.seek( NodeRef.getNext() * Record.getSize() ) ;
                     NodeRef.ReadRec( lFile ) ;
                  }
                  else {

                     /** *********************************************
                      * Link the previous node to the node after
                      * NodeRef, then delete NodeRef.
                      *********************************************** */

                     secPreviousNode.setNext( NodeRef.getNext() ) ;
                     //secPreviousNode.setRecID( -1 );

                     sysPrint( "Remove() 4: The record " + NodeRef.getRecID() + " has been found." );

                     lFile.seek( 0 ) ;
                     sysPrint( "Remove() 4a: secPreviousNode.getRecID() is"
                              + secPreviousNode.getRecID() ) ;
                     lFile.seek( secPreviousNode.getRecID() * Record.getSize() ) ;
                     sysPrint( "Remove() 4b: " ) ;
                     secPreviousNode.write( lFile );

                     sysPrint( "Remove() 5: The PreviousNode.getRecID() is " + PreviousNode.getRecID() +
                           " with a brand name of " + PreviousNode.getBrandName() );
                     sysPrint( "Remove() 6: The NodeRef.getNext() is "
                                     + NodeRef.getNext() );
                     sysPrint( "Remove() 7: The PreviousNode.getNext() is "
                                     + PreviousNode.getNext() );
                     sysPrint( "Remove() 8: The secPreviousNode.getRecID() is "
                                     + secPreviousNode.getRecID() + " and brand name of " +
                                     secPreviousNode.getBrandName() );
                     sysPrint( "Remove() 9: The secPreviousNode.getNext() is "
                                     + secPreviousNode.getNext() );

                     /** quit while loop - node found   */
                     break ;

                  } /** End of inner if-then-else   */
               }  /** End of while loop   */
            }  /** End of outer if-then-else   */
         }  /** End of outer if   */
      }
      catch ( IOException ex ) {
       sysPrint( "Remove() 10: Error reading file" );
      }
   }

   /** **********************************************
    * Method: isEmpty()
    *
    * Purpose: Determine if the linked list is empty.
    *          Return:
    *              - true if linked list is not empty
    *              - false if linked list is empty
    *
    ************************************************ */
   public boolean isEmpty( RandomAccessFile hFile ) {
      boolean ret = false  ;
      Record NodeRef = null , PreviousNode = null  ;

      try {
         NodeRef.ReadRec( hFile ) ;

         if ( NodeRef.getNext() <= ZERO ) {
            ret = false ;
         }
         else {
            ret = true ;
         }
      }
      catch ( IOException ex ) {
       //part.setText( "Error reading file" );
      }

      return ret ;
   }

   /** ******************************************************************
    * Method: contains( RandomAccessFile lFile, RandomAccessFile hFile,
                       int num )
    *
    * Purpose: Determine if the linked list contains the passed recid
    *          parameter.
    *          Return:
    *              - true if linked list is not empty
    *              - false if linked list is empty
    *
    *********************************************************************** */
   public boolean contains( RandomAccessFile lFile, RandomAccessFile hFile,
                       int num ) {

      Record NodeRef = null , PreviousNode = null  ;
      boolean ret = false ;

      try {
         NodeRef.ReadRec( hFile ) ;
         int a ;

         /** If the list is empty, do nothing.  */
         if ( NodeRef.getNext() >= ZERO  ) {

            lFile.seek( NodeRef.getNext() * Record.getSize() ) ;
            NodeRef.ReadRec( lFile );
            PreviousNode = NodeRef;

	    /** *************************************
             * Skip all nodes whose value member is
             * not equal to num.
             * *************************************** */

             while ( NodeRef.getRecID() != num  ) {
                if ( NodeRef.getNext() >= ZERO ) {
                   PreviousNode = NodeRef;
                   lFile.seek( NodeRef.getNext() * Record.getSize() ) ;
                   NodeRef.ReadRec( lFile );
                }
                else if ( NodeRef.getNext() == num ) {

                   /** *********************************************
                    * Link the previous node to the node after
                    * NodeRef, then delete NodeRef.
                    *********************************************** */

                   ret = true ;
                   //sysPrint("\nThe number " + num +
                   //            " sought has been found.\n") ;

                   /** quit while loop - node found   */
                   break ;

               } /** End of inner if-then-else   */
            }  /** End of while loop   */
         }  /** End of outer if   */
      }
      catch ( IOException ex ) {
       //part.setText( "Error reading file" );
      }

      return ret ;
   }

   /** **********************************************
    * Method: toArray(int a[])
    *
    * Purpose: Returns an array containing all of the
    *          elements in this list in the correct
    *          order.
    *
    ************************************************ */
   public int toArray( RandomAccessFile lFile, RandomAccessFile hFile,
                         String a[][] ) {

      Record NodeRef = new Record() ,
             PreviousNode  = null ;
      int ii = 0 , fileSize , numEntries = a.length , numCol = 8 ;

      try {
         hFile.seek( 0 );
         NodeRef.ReadRec( hFile ) ;
         fileSize = (int) lFile.length() / Record.getSize() ;
         /** If the list is empty, do nothing.  */
         if (  NodeRef.getNext() > minusOne  ) {

	    /** *************************************
             *
             * *************************************** */
             for ( int iii = 0 ; iii < numEntries ; iii++ ) {
                for ( int iv = 0 ; iv < numCol ; iv++ ) {
                   a[ iii ] [ iv ] = "" ;
                }
             }

             do {
                sysPrint( "toArray(): 1 - input data file.NodeRef.getNext() is "
                      + NodeRef.getNext() );
                sysPrint( "toArray(): 2 - NodeRef.getRecID is "
                                      + NodeRef.getRecID() );

                lFile.seek( 0 ) ;
                lFile.seek( NodeRef.getNext() * Record.getSize() ) ;
                sysPrint( "toArray(): 3 - input data file." );
                NodeRef.ReadRec( lFile );
                sysPrint( "toArray(): 4 - input data file. NodeRef.getNext() is "
                  + NodeRef.getNext() );
                String str2 = a[ ii ] [ 0 ]  ;
                sysPrint( "toArray(): 5 - the value of a[ ii ] [ 0 ] is " +
                                      a[ 0 ] [ 0 ] );

                a[ ii ] [ 0 ]  =  String.valueOf( NodeRef.getRecID() ) ;
                a[ ii ] [ 1 ]  =  String.valueOf( NodeRef.getNext() )  ;
                a[ ii ] [ 2 ]  =  String.valueOf( NodeRef.getQuantity() )  ;
                a[ ii ] [ 3 ]  =  NodeRef.getToolType().trim()  ;
                a[ ii ] [ 4 ]  =  NodeRef.getBrandName().trim() ;
                a[ ii ] [ 5 ]  =  NodeRef.getToolDesc().trim()  ;
                a[ ii ] [ 6 ]  =  NodeRef.getPartNumber().trim() ;
                a[ ii ] [ 7 ]  =  NodeRef.getCost().trim() ;

                sysPrint( "0 " + a[ ii ] [ 0 ] +
                                    "1 " + a[ ii ] [ 1 ] +
                                    "2 " + a[ ii ] [ 2 ] +
                                    "3 " + a[ ii ] [ 3 ] +
                                    "4 " + a[ ii ] [ 4 ] +
                                    "5 " + a[ ii ] [ 5 ] +
                                    "6 " + a[ ii ] [ 6 ] +
                                    "7 " + a[ ii ] [ 7 ] );


                ii++;

            /* } while (NodeRef.getNext() != minusOne  ) ;  End of do-while loop   */
            } while ( ii < fileSize ) ; /** End of do-while loop   */
         }  /** End of outer if   */
      }
      catch ( IOException ex ) {
                sysPrint( "toArray(): 6 - error on input data file." );
      }

      return ii ;

   }

   /** **********************************************
    * Method: getLast( RandomAccessFile lFile, RandomAccessFile hFile,
    *                       Record node )
    *
    * Purpose: Return the last logical number in the linked
    *          list
    *
    ************************************************ */
   public Record getLast( RandomAccessFile lFile, RandomAccessFile hFile,
                            Record node ) {

      Record NodeRef = null , PreviousNode = null  ;

      try {
         hFile.seek( 0 );
         NodeRef.ReadRec( hFile );

         /** If the list is empty, do nothing.  */
         if ( NodeRef.getNext() != minusOne )  {

	    /** *************************************
             * Skip all nodes whose value member is
             * not equal to num.
             * *************************************** */

           do {
              PreviousNode = NodeRef;
              lFile.seek( NodeRef.getNext() * Record.getSize() );
              NodeRef.ReadRec( lFile )  ;
           } while ( NodeRef.getNext() != minusOne  )  ;

         }
      }
      catch ( IOException ex ) {
       //part.setText( "Error reading file" );
      }

      return NodeRef ;
   }

   /** *************************************************************************
    * Method: size( RandomAccessFile lFile, RandomAccessFile hFile )
    *
    * Purpose: Returns the size of the linked list.
    *          Return:
    *              - number in linked list if not empty
    *              - 0 if linked list is empty
    *
    ************************************************************************** */
   public int size( RandomAccessFile lFile, RandomAccessFile hFile ) {

      Record NodeRef = null , PreviousNode = null  ;
      int ret = 0 ;

      try {
            /** If the list is empty, do nothing.  */
         hFile.seek( 0 );
         NodeRef.ReadRec( hFile );
         if ( NodeRef.getNext() != minusOne ) {

            ret = 1 ;

            while (NodeRef.getNext() != minusOne  ) {
               lFile.seek( NodeRef.getNext() * Record.getSize() );
               NodeRef.ReadRec( lFile )  ;

               ret++;

            }  /** End of while loop   */
         }  /** End of outer if   */
      }
      catch ( IOException ex ) {
       //part.setText( "Error reading file" );
      }

      return ret ;
   }
}
