/**************************************************************
 * File: CarStore.java
 *
 * Methods:
 *    - CarStore() - constructor: is used to initialize the class
 *    - close() - is used to exit the application
 *    - display() - is used to display entries in a table
 *    - initRecord() - is used to initialize the data tables
 *    - SetUpMenu() - is used to declare any menus/items
 *    - SetUpButtons() - is used to declare any buttons
 *    - SetUpMisc() - is used to setup any misc items
 *    - sysPrint() - is used for debugging
 *                 - set myDebug to true to turn on debugging
 *    - etUpBTable()
 *    - MenuHandler - is the class that responds to GUI events such as
 *                    the pressing of a menu item
 *
 * Copyright (c) 2002-2005 Advanced Applications Total Applications Works.
 * (AATAW)  All Rights Reserved.
 *
 * AATAW grants you ("Licensee") a non-exclusive, royalty free, license to use,
 * modify and redistribute this software in source and binary code form,
 * provided that i) this copyright notice and license appear on all copies of
 * the software; and ii) Licensee does not utilize the software in a manner
 * which is disparaging to AATAW.
 *
 * This software is provided "AS IS," without a warranty of any kind. All
 * express or implied conditions, representations and warranties, including any
 * implied warranty of merchantability, fitness for a particular purpose or
 * non-infringement, are hereby excluded. AATAW and its licensors shall not be
 * liable for any damages suffered by licensee as a result of using, modifying
 * or distributing the software or its derivatives. in no event will AATAW or its
 * licensors be liable for any lost revenue, profit or data, or for direct,
 * indirect, special, consequential, incidental or punitive damages, however
 * caused and regardless of the theory of liability, arising out of the use of
 * or inAbility to use software, even if AATAW has been advised of the
 * possibility of such damages.
 *
 * This software is not designed or intended for use in on-line control of
 * aircraft, air traffic, aircraft navigation or aircraft communications; or in
 * the design, construction, operation or maintenance of any nuclear
 * facility. Licensee represents and warrants that it will not use or
 * redistribute the Software for such purposes nor will it use this code for
 * commercial purposes without the express consent of AATAW.
 ***********************************************************************/

package test_flower;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.awt.event.*;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import java.util.*;
import test_flower.Record ;
import test_flower.LinkedList;

/** ***************************************************************
 *  The CarStore class is used to maintain  the inventory for a Car
 *  parts storre. It allows the user to add, update, nd/or delete
 *  car parts to an overall car parts inventory.
 *************************************************************** */
public class CarStore extends JFrame {
   private boolean myDebug = false ;
   private Container c ;

   /** Declare menus */
   private JMenuBar menuBar ;
   private JMenu fileMenu, viewMenu, optionsMenu, filtersMenu, toolsMenu, helpMenu, aboutMenu ;
   /** File Menu Items */
   private JMenuItem eMI ;
   /** View Menu Items */
   private JMenuItem viewTiresMI, viewBatteriesMI, oilFiltersMI, airFiltersMI, gasFiltersMI ;
   /** Options Menu Items */
   private JMenuItem deleteMI, addMI, updateMI, listAllMI, shopMI ;
   /** Tools Menu Items */
   private JMenuItem debugON, debugOFF ;
   /** Help Menu Items */
   private JMenuItem   helpHWMI ;
   /** About Menu Items */
   private JMenuItem   aboutHWMI ;
   private MenuHandler menuHandler = new MenuHandler();
   private JTable table;
   private LinkedList pp = new LinkedList() ;

   private Record data = new Record() ;
   private File linkFile , headFile;
   private RandomAccessFile lFile , hFile;
   private String pData[] []  = new String [ 250 ] [ 8 ];
   private String columnNames[] = {"Record ID", "Next RECID", "Quantity",
        "Type of tool", "Brand Name",  "Tool Description", "partNum",
       "Price"} ;
   private String tireRecord[][]  = {
                  {"0", "0", "15", "16 in SUV-LT P225/70R16 101S", "Uniroyal", "Uni Laredo AWP", "UN02122706OL", "89.99"},
                  {"1", "-1", "22", "16 in SUV-LT P225/70R16 101S", "Goodyear", " Goodyear Integrity", "GY12122706", "92.99"},
                  {"2", "-1", "9", "16 in SUV-LT P235/70R-16 104T OWL", "Continental", "Continental ContiTrac SUV", "CG03722706OL", "104.99"},
                  {"3", "-1", "8", "16 in SUV-LT P225/70R16 101S", "Sigma", "Sigma Stampede Rad SUV", "SG01922706O",  "106.99"},
                  {"4", "-1", "8", "16 in SUV-LT P225/70R-16 101S LEX", "Goodyear", "Goodyear Integrity", "BF04922706TOL", "114.99"},
                  {"5", "-1", "8", "18 in PAS P235/45R-18 94V NI B", "Michelin", "Michelin Pilot HX MXM4", "MC34494", "225.00"},
                  {"6", "-1", "8", "18 in PAS 245/45ZR-18 96Y DIR", "Michelin", "Michelin Pilot Sport A/S", "MC34313x", "264.00"},
                  {"7", "-1", "8", "16 in SUV-LT P225/70R-16 101S", "Michelin", "Mich LTX M/S", "MC07122706OL", "142.99"},
                  {"8", "-1", "8", "16 in SUV-LT P245/65R-17 105S ORB", "Michelin", "Mich Cross Terrain SUV", "MC06522706OL", "144.99"},
                  {"9", "-1", "8", "16 in SUV-LT P245/70R-16 106T OWL", "Goodyear", "Goodyear Fortera Silent Armor", "GY10122706OL", "123.99"},
                  {"10", "-1", "8", "16 in SUV-LT P235/70R-16 104T OWL", "Goodyear", "BFG All Terrain KO", "BF07722706CRL", "128.99"},
                  {"11", "-1", "8", "16 in SUV-LT P225/70R16 101S", "Goodyear", "Goodyear Eagle LS", "GY11322706O", "131.99"},
                  {"12", "-1", "8", "16 in PAS 225/60R-16 97H VSB", "Goodyear", "Goodyear Eagle GT-HR", "GY31439", "89.99"},
                  {"13", "-1",  "8", "17 in PAS 215/55R-17 94V BSW", "Cooper", "Cooper Lifeliner Touring SLE", "COOP27322", "140.00"},
                  {"14", "-1", "8", "15 in PAS P205/65R15", "Dunlop", "SP Winter Sport M3", "DUN06522906OL", "159.95"},
                  {"15", "-1", "8", "14 in PAS P185/65R14", "Dunlop", "SP40 A/S", "DUN07112806OL", "69.95"},
                  {"16", "-1", "8", "14 in PAS P185/65R14", "Dunlop", "SP20", "DUN06332906OL", "64.95"},
                  {"17", "-1", "8", "17 in SUV-LT P245/65R-17 105T OWL", "Goodyear", "Goodyear Fortera Silent Armor", "GY31614", "155.00"},
                  {"18", "-1", "10", "17 in SUV-LT P255/55R-17 102H B", "Dunlop", "Dunlop Sport Rover GTX", "DUN28363", "166.00"},
                  {"19", "-1", "8", "15 in PAS P205/65R15", "Dunlop", "SP Sport A2 Plus", "DUN06522806OL", "144.99"},
                  {"20", "-1", "8", "16 in SUV-LT P245/65R-17 105S ORB", "Dunlop", "Rover A/T", "DUN06522706OL", "121.95"},
                  {"21", "-1", "-1", " ", " ", " ", " ", "0.00"}
             } ;

   private String batteryRecord[][]  = {
                  {"0", "0", "15", "12V Starting Battery", "Optima", "Optima Red Top Automotive 12V Starting Battery with 720 Cold Cranking Amps", "75/35-OR", "109.88"},
                  {"1", "-1", "22", "12V Starting Battery", "Optima", "Optima Red Top Automotive 12V Starting Battery with 720 Cold Cranking Ampsy", "25-OR", "134.99"},
                  {"2", "-1", "9", "12V Starting Battery", "Optima", "Optima Red Top Automotive 12V Starting Battery with 720 Cold Cranking Amps", "35-OR", "134.99"},
                  {"3", "-1", "8", "12V Starting Battery", "Optima", "Optima Red Top Automotive 12V Starting Battery with 800 Cold Cranking Amps", "34R-OR",  "144.99"},
                  {"4", "-1", "8", "12V Starting Battery", "Optima", "Optima Red Top Automotive 12V Starting Battery with 800 Cold Cranking Amps", "34-OR", "144.99"},
                  {"5", "-1", "8", "12V Starting Battery", "Autocraft", "AUTOCRAFT SILVER", "26R3 ", "54.93"},
                  {"6", "-1", "8", "12V Starting Battery", "Autocraft", "Autocraft 352 AUTOCRAFT TITANIUM", "352", "74.94"},
                  {"7", "-1", "8", "12V Starting Battery", "AC DELCO", "5 Year-Battery  35; BCI; 0360; CCA", "24R-5YR", "43.28"},
                  {"8", "-1", "8", "12V Starting Battery", "AC DELCO", "7 Year-Battery] Cold Climate; 24F; BCI; 0585; CCA", "24R-7YR", "64.49"},
                  {"9", "-1", "8", "12V Starting Battery", "AC DELCO", "Professional - 7 Yr-Battery] Cold Climate; Top Post; 24F; BCI; 0585; CCA", "24R7YR", "64.49"},
                  {"10", "-1", "8", "12V Starting Battery", "AC DELCO", "Professional - 5 Yr-Battery] Top Post; 35; BCI; 0360; CCA", "24R5YR", "43.28"},
                  {"11", "-1", "8", "12V Starting Battery", "AC DELCO", "[Professional - 6 Yr-Battery] Top Post; 35; BCI; 0360; CC", "35-6YR", "46.22"},
                  {"12", "-1", "8", "12V Starting Battery", "AC DELCO", "60 Series - 5 Yr-Battery] Opt.; Dual Terminal; 24F; BCI; 0585; CCA", "ACD3478", "43.49"},
                  {"13", "-1", "8", "12V Starting Battery", "AC DELCO", "60 Series - 5 Yr-Battery] Top Post; 35; BCI; 0360; CCA", "ACD35-60", "69.95"},
                  {"14", "-1", "8", "12V Starting Battery", "AC DELCO", "60 Series - 5 Yr-Battery] Opt.; Top Post; 24F; BCI; 0585; CCA", "ACD24F60", "34.00"},
                  {"15", "-1", "-1", " ", " ", " ", " ", "0.00"},
                  {"16", "-1", "-1", " ", " ", " ", " ", "0.00"},
                  {"17", "-1", "-1", " ", " ", " ", " ", "0.00"}
             } ;

   private String oilFilterRecord[][]  = {
                  {"0", "0", "15", "Oil Filter", "SURE DRAIN", "", "SD3", "7.39"},
                  {"1", "-1", "22", "Oil Filter", "ExtraGuard", "",  "PH3614", "8.33"},
                  {"2", "-1", "9", "Oil Filter", "ToughGuard", "", "TG3614", "6.59"},
                  {"3", "-1", "8", "Oil Filter", "DoubleGuard", "", "TG3614",  "8.88"},
                  {"4", "-1", "8", "Oil Filter", "ExtraGuard", "", "PH4967", "5.89"},
                  {"5", "-1", "8", "Oil Filter", "ToughGuard", "", "TG4967", "8.99"},
                  {"6", "-1", "8", "Oil Filter", "DoubleGuard", "", "DG4967", "4.94"},
                  {"7", "-1", "8", "Oil Filter", "SURE DRAIN", "", "SD3", "4.28"},
                  {"8", "-1", "8", "Oil Filter", "ExtraGuard", "", "PH4386", "6.49"},
                  {"9", "-1", "8", "Oil Filter", "SURE DRAIN", "", "SD2R", "6.49"},
                  {"10", "-1", "8", "Oil Filter", "ExtraGuard", "", "PH3593A", "4.28"},
                  {"11", "-1", "8", "Oil Filter", "ToughGuard", "", "TG3593A", "6.22"},
                  {"12", "-1", "8", "Oil Filter", "DoubleGuard", "", "DG3593A", "4.49"},
                  {"13", "-1", "8", "Oil Filter", "SURE DRAIN", "", "SD2", "9.95"},
                  {"14", "-1", "8", "Oil Filter", "ExtraGuard", "", "PH7317", "6.17"},
                  {"15", "-1", "9", "Oil Filter", "ToughGuard", " ", "TG7317", "6.71"},
                  {"16", "-1", "15", "Oil Filter", "DoubleGuard", " ", "DG7317", "8.99"},
                  {"17", "-1", "25", "Oil Filter", "FRAM", "LUBE Full-Flow Lube Spin-on Oil Filter", "PH3593A", "6.71"},
                  {"18", "-1", "9", "Oil Filter", "BOSCH", "Oil Filter", "72168", "4.71"},
                  {"19", "-1", "15", "Oil Filter", "FRAM", "Tough Guard� LUBE Full-Flow Lube Spin-on Oil Filter", "TG3593A", "6.17"},
                  {"20", "-1", "15", "Oil Filter", "FRAM", "Tough Guard� LUBE Full-Flow Lube Spin-on Oil Filter", "XG3593A", "9.29"}
             } ;

   private String airFilterRecord[][]  = {
                  {"0", "0", "15", "Air Filter", "WIX", "WIX Air Filter}", "46444", "11.29"},
                  {"1", "-1", "22", "Air Filter", "FRAM", "Type S {AIR Rigid Panel Air}", "CA9502", "11.54"},
                  {"2", "-1", "9", "Air Filter", "FRAM", "exc. Type S {AIR Rigid Panel Air", "CA8475", "11.68"},
                  {"3", "-1", "8", "Air Filter", "BECK/ARNLEY", "Air Filter", "0421590",  "11.75"},
                  {"4", "-1", "8", "Air Filter", "ACDELCO", "ACDELCO Air Filter", "A1612C", "14.93"},
                  {"5", "-1", "8", "Air Filter", "ACDELCO ", "ACDELCO Air Filter", "A3031C", "14.53"},
                  {"6", "-1", "8", "Air Filter", "FRAM", "AIR Rigid Panel Air", "CA7351", "8.92"},
                  {"7", "-1", "8", "Air Filter", "BECK/ARNLEY", "BECK/ARNLEY Air Filter ", "0421523", "10.35"},
                  {"8", "-1", "8", "Air Filter", "WIX", "WIX Air Filter", "46017", "11.29"},
                  {"9", "-1", "8", "Air Filter", "BOSCH", "BOSCH Air Filter", "73299", "12.95"},
                  {"10", "-1", "8", "Air Filter", "AC DELCO", "AC DELCO Air Filter", "A1286C", "14.93"},
                  {"11", "-1", "8", "Air Filter", "FRAM", "FRAM Air Filter", "PPA7351", "52.79"},
                  {"12", "-1", "8", "Air Filter", "KFM", "KFM Air Filter", "1780107020A", "6.88"},
                  {"13", "-1", "8", "Air Filter", "BECK/ARNLEY", "BECK/ARNLEY Air Filter", "0421535", "9.13"},
                  {"14", "-1", "8", "BECK/ARNLEY", "FRAM", "FRAM Air Filter", "CA7626", "12.55"},
                  {"15", "-1", "-1", " ", " ", " ", " ", "0.00"},
                  {"16", "-1", "-1", " ", " ", " ", " ", "0.00"},
                  {"17", "-1", "-1", " ", " ", " ", " ", "0.00"}
             } ;

   private String fuelFilterRecord[][]  = {
                  {"0", "0", "15", "Fuel Filter", "BECK/ARNLEY", "BECK/ARNLEY Air Filter", "0431048", "15.95"},
                  {"1", "-1", "22", "Fuel Filter", "WIX", "WIX Fuel Filter", "33319", "14.30"},
                  {"2", "-1", "9", "Fuel Filter", "FRAM", "FRAM Fuel Filter", "G6680", "18.00"},
                  {"3", "-1", "8", "Fuel Filter", "ACDELCO", "ACDELCO FILTER,FUEL 2 WHEEL DRIVE", "GF656",  "18.90"},
                  {"4", "-1", "8", "Fuel Filter", "ACDELCO", "ACDELCO FILTER,FUEL 4 WHEEL DRIVE", "25175534", "18.90"},
                  {"5", "-1", "8", "Fuel Filter", "BECK/ARNLEY", "BECK/ARNLEY Fuel Filter", "0430920", "20.89"},
                  {"6", "-1", "8", "Fuel Filter", "BOSCH", "BOSCH Fuel Filter", "71514", "22.79"},
                  {"7", "-1", "8", "Fuel Filter", "BECK/ARNLEY", "BECK/ARNLEY Fuel Filter", "0431038", "15.22"},
                  {"8", "-1", "8", "Fuel Filter", "WIX", "WIX Fuel Filter", "33567", "17.61"},
                  {"9", "-1", "8", "Fuel Filter", "FRAM", "FRAM Fuel Filter", "G8016", "21.99"},
                  {"10", "-1", "8", "Fuel Filter", "ACDELCO", "ACDELCO Fuel Filter", "GF743", "23.79$"},
                  {"11", "-1", "8", "Fuel Filter", "BOSCH", "BOSCH Fuel Filter", "71618", "17.58"},
                  {"12", "-1", "8", "Fuel Filter", "BECK/ARNLEY", "BECK/ARNLEY Fuel Filter", "0430840", "8.99"},
                  {"13", "-1", "8", "Fuel Filter", "FRAM", "FRAM Fuel Filter", "G4777", "11.70"},
                  {"14", "-1", "8", "Fuel Filter", "WIX", "WIX Fuel Filter", "33477", "12.3"},
                  {"15", "-1", "-1", " ", " ", " ", " ", "0.00"},
                  {"16", "-1", "-1", " ", " ", " ", " ", "0.00"},
                  {"17", "-1", "-1", " ", " ", " ", " ", "0.00"}
             } ;

   private int numEntries = 0 ;
   private JPanel buttonPanel ;
   private JButton  refresh , exit ;
   private CarStore cs ;
   private UpdateRec update;     /** dialog box for record update */
   private NewRec    newRec;     /** dialog box for new record */
   private DeleteRec deleteRec;  /** dialog box for delete record */

   /** ***************************************************************
    *  constructor - is used to initialize the CarStore class through
    *                the following methods:
    *                1- SetUpMenu() is called to setup the menu bar and
    *                   menu items
    *                2- SetUpButtons() is used to declare buttons
    *                3- InitRecord( is called to initialize the various
    *                   data files.
    *                4- display()
    *
    *                This constructor is called when the CarStore object
    *                is created in the main() method.
    ********************************************************************* */
   public CarStore() {
      super ("Car Parts Store") ;

      c = getContentPane() ;

      SetUpMenu() ;

      SetUpButtons() ;

      InitRecord( "tire.dat" , "headTires.dat" , tireRecord , 21 ) ;

      InitRecord( "battery.dat" , "headBattery.dat" , batteryRecord , 15 ) ;

      InitRecord( "oil.dat" , "headOil.dat" , oilFilterRecord , 21 ) ;

      InitRecord( "air.dat" , "headAir.dat" , airFilterRecord , 15 ) ;

      InitRecord( "gas.dat" , "headGas.dat" , fuelFilterRecord , 15 ) ;

      cs = this ;

      display( "Tires" ) ;

      setSize( 700, 400 );
      setVisible( true );
   }

   /** **********************************************************
    *  SetUpMenu() - is used to declare any menus/items
    *                and is called from the CarStore()
    *                constructor.
    *
    *                The SetUpMenu() method is called from the
    *                CarStore constructor.
    ************************************************************ */
   public void SetUpMenu()   {
      /** Create the menubar */
      menuBar = new JMenuBar();

      /** Add the menubar to the frame */
      setJMenuBar(menuBar);

      /** Create the File menu and add it to the menubar  */
      fileMenu = new JMenu("File");

      menuBar.add(fileMenu);
      /** Add the Exit menu items */
      eMI = new JMenuItem("Exit") ;
      fileMenu.add( eMI );
      eMI.addActionListener( menuHandler );

      /** Create the View menu and add it to the menubar  */
      viewMenu = new JMenu("Views");

      menuBar.add(viewMenu);
      /** Add the Views' menu items */
      viewTiresMI = new JMenuItem("Tires") ;

      viewMenu.add( viewTiresMI ) ;
      viewTiresMI.addActionListener( menuHandler ) ;

      viewBatteriesMI = new JMenuItem("Batteries") ;
      viewMenu.add( viewBatteriesMI ) ;
      viewBatteriesMI.addActionListener( menuHandler ) ;

      filtersMenu  = new JMenu("Filters") ;
      oilFiltersMI = new JMenuItem("Oil Filters") ;
      airFiltersMI = new JMenuItem("Air Filters") ;
      gasFiltersMI = new JMenuItem("Gas Filters") ;
      filtersMenu.add( oilFiltersMI ) ;
      filtersMenu.add( airFiltersMI ) ;
      filtersMenu.add( gasFiltersMI ) ;
      oilFiltersMI.addActionListener( menuHandler ) ;
      airFiltersMI.addActionListener( menuHandler ) ;
      gasFiltersMI.addActionListener( menuHandler ) ;
      viewMenu.add( filtersMenu ) ;


      /** Create the Option menu and add it to the menubar  */
      optionsMenu = new JMenu( "Options" ) ;
      menuBar.add( optionsMenu );

      /** Add the Add menuitems */
      addMI = new JMenuItem("Add") ;
      optionsMenu.add( addMI );
      addMI.addActionListener( menuHandler );

      /** Add the Update menuitems */
      updateMI = new JMenuItem("Update") ;
      optionsMenu.add( updateMI );
      updateMI.addActionListener( menuHandler );
      optionsMenu.addSeparator();

      /** Add the Delete menuitems */
      deleteMI = new JMenuItem("Delete") ;
      optionsMenu.add( deleteMI );
      deleteMI.addActionListener( menuHandler );

      /** Create the Tools menu and add it to the menubar  */
      toolsMenu = new JMenu("Tools") ;
      menuBar.add(toolsMenu);
      /** Add the Tools menu items */
      debugON = new JMenuItem("Debug On") ;
      debugOFF = new JMenuItem("Debug Off") ;
      toolsMenu.add( debugON );
      toolsMenu.add( debugOFF );
      debugON.addActionListener( menuHandler );
      debugOFF.addActionListener( menuHandler );

      /** Create the Help menu and add it to the menubar  */
      helpMenu = new JMenu("Help") ;

      /** Add the Help HW Store menuitems */
      helpHWMI = new JMenuItem("Help on Car Store") ;
      helpMenu.add( helpHWMI );
      helpHWMI.addActionListener( menuHandler );

      menuBar.add(helpMenu);

      /** Create the About menu and add it to the menubar  */
      aboutMenu = new JMenu("About") ;

      /** Add the About Store menuitems */
      aboutHWMI = new JMenuItem("About Car Store") ;
      aboutMenu.add( aboutHWMI );
      aboutHWMI.addActionListener( menuHandler );

      menuBar.add(aboutMenu);


   }

   /** *************************************************************
    *  SetUpButtons() - is used to declare any buttons. The only
    *                   button declared in this application is the
    *                   exit button.
    *
    *                   The SetUpButtons() method is called from the
    *                   CarStore constructor.
    **************************************************************** */
   public void SetUpButtons()   {
      buttonPanel = new JPanel() ;
      exit = new JButton( "Exit" ) ;
      buttonPanel.add( exit ) ;
      c.add( buttonPanel , BorderLayout.SOUTH) ;
      exit.addActionListener( menuHandler );
   }


   /** ************************************************************
    *  Redisplay() - is used to refresh the contents of the JTable
    *                on the main frame window.
    *
    *                The Redisplay() method is called from the
    *                CarStore constructor.
    ************************************************************ */
   public void Redisplay( RandomAccessFile lFile, RandomAccessFile hFile,
                         String a[][] ) {
      LinkedList ll = new LinkedList() ;
      ll.toArray( lFile , hFile , pData ) ;
      c.remove( table ) ;
      table = new JTable( pData, columnNames ) ;
      table.setEnabled( true );
      c.add( table , BorderLayout.CENTER) ;
      c.add( new JScrollPane ( table ) );
      c.validate();
   }

   /** *************************************************
    *  display() - is used to display the contents of
    *              data file using a JTable.
    *
    *              The display() method is called from the
    *              1- CarStore constructor
    *              2- MenuHandler class
    ************************************************ */
   public void display( String str ) {
      pp = new LinkedList();
      String hf = null , df = null ,  title = null ;

      if ( str.equals( "Tires" ) ) {
         hf = new String("headTires.dat" ) ;
         df = new String("tire.dat"  ) ;
         linkFile = new File( "tire.dat" ) ;
         headFile = new File( "headTires.dat" ) ;
         title = new String( "Car Parts Store: Tires" ) ;
      }
      else if ( str.equals( "Batteries" )  ) {
         hf = new String("headBattery.dat" ) ;
         df = new String("battery.dat"  ) ;
         linkFile = new File( "battery.dat" ) ;
         headFile = new File( "headBattery.dat" ) ;
         title = new String( "Car Parts Store: Batteries" ) ;
      }
      else if ( str.equals( "Oil Filters" )  ) {
         hf = new String("headOil.dat" ) ;
         df = new String("oil.dat"  ) ;
         linkFile = new File( "oil.dat" ) ;
         headFile = new File( "headOil.dat" ) ;
         title = new String( "Car Parts Store: Oil Filters" ) ;
      }
      else if ( str.equals( "Air Filters" )  ) {
         hf = new String("headAir.dat" ) ;
         df = new String("air.dat"  ) ;
         linkFile = new File( "air.dat" ) ;
         headFile = new File( "headAir.dat" ) ;
         title = new String( "Car Parts Store: Air Filters" ) ;
      }
      else if ( str.equals( "Gas Filters" )  ) {
         hf = new String("headGas.dat" ) ;
         df = new String("gas.dat"  ) ;
         linkFile = new File( "gas.dat" ) ;
         headFile = new File( "headGas.dat" ) ;
         title = new String( "Car Parts Store: Fuel Filters" ) ;
      }

      try {
         /** Open the headFile in RW mode.
          *  If the file does not exist, create it
          *  and initialize it to 250 empty records.
          */

         if ( !headFile.exists() ) {
         }
         else {
            hFile = new RandomAccessFile( hf , "rw" );
         }
      }
      catch ( IOException e ) {
            System.err.println( e.toString() );
            System.err.println( "Failed in opening " + hf );
            System.exit( 1 );
      }

      try {
         /** Open the .dat file in RW mode.
          *  If the file does not exist, create it
          *  and initialize it to 250 empty records.
          */

         sysPrint("display(): 1a - checking to see if " + df + " exists." );
         if ( !linkFile.exists() ) {

            sysPrint("display(): 1b - " + df + " does not exist." );

         }
         else {
            lFile = new RandomAccessFile( df , "rw" );
            int entries = pp.toArray( lFile , hFile , pData );
            setEntries( entries ) ;

            this.setTitle( title );

            table = new JTable( pData, columnNames ) ;
            table.setEnabled( true );
            c.add( table , BorderLayout.CENTER) ;
            c.add( new JScrollPane ( table ) );
            c.validate();
         }

         lFile.close();
         hFile.close();
      }
      catch ( IOException e ) {
            System.err.println( e.toString() );
            System.err.println( "Failed in opening " + hf );
            System.exit( 1 );
      }

   }

   /** *****************************************************
    *  checkpData() - is used to ensure that the recordID
    *  entered is a number that exists inthe array.
    *
    *  checkpData() is called from the UpdateRec class.
    ******************************************************* */
   public boolean  checkpData( int recID , String pData[][] ) {

      int pDataLength = getEntries() , aa = 0 ;
      boolean valIndex = false;

      sysPrint("checkpData(): 1a - The value of pDataLength is " + pDataLength );
      for (int ii = 0 ; ii < pDataLength; ii++) {
         sysPrint("index is " + ii + " checkpData(): 1b - The value of Integer.parseInt(pData[ ii ] [ 0 ] is " + Integer.parseInt(pData[ ii ] [ 0 ] ) ) ;
         sysPrint("checkpData(): 1c - The value of ii is " + ii ) ;
         aa = Integer.parseInt(pData[ ii ] [ 0 ]) ;
         if ( aa == recID ) {
            valIndex = true ;

            break;
         }
      }

      return valIndex ;
   }

   /** *****************************************************************
    *  setEntries() - is called to to set the number of
    *                 entries in the current data file.
    *
    *                 The setEntries() method is called from
    *                 1- display() method
    *                 2- actionPerformed() method of the NewRec class
    *                 3- actionPerformed() method of the DeleteRec class
    ******************************************************************* */
   public void setEntries( int ent )   {
      numEntries = ent ;
   }

   /** ******************************************************************
    *  getEntries() - is used to obtain the number of entries in the
    *                 current data file. The getEntries() method is called
    *                 from
    *                 1- checkpData( method
    *                 2- actionPerformed() method UpdateRec class
    *                 3- actionPerformed() method NewRec class
    ***************************************************************** */
   public int getEntries()   {
      return numEntries  ;
   }

   /** ****************************************************
    * Method: IsInPData() is used to ensure  that the data
    * entered is in the pData array
    *****************************************************/
   public boolean  IsInPData( String strVal ) {

      int strLength = 0 , loopCTL = getEntries() ;
      boolean ret = false;

      for ( int ii = 0 ; ii < loopCTL ; ii++ ) {
         if ( (pData[ ii ] [ 0 ]).equals( strVal ) ) {
            ret = true ;
            break ;
         }
      }

      return ret ;
   }

   /** ****************************************************
    * Method: checkDigit() is used to ensure  that the data
    * entered is a digit
    *****************************************************/
   public boolean  checkDigit(String strVal) {

      int strLength = 0;
      boolean notDig = true;

      strLength = strVal.length();

      for (int ii = 0; ii < strLength; ii++) {
         if (!Character.isDigit(strVal.charAt(ii)) ) {
            notDig = false;
            break;
         }
      }

      sysPrint( "The data is a digit " + notDig ) ;
      return notDig ;
   }

   /** *************************************************
    *  InitRecord() - is used to initialze a data file.
    *                 The InitRecord() method is called
    *                 from the CarStore constructor.
    ************************************************ */
   public void InitRecord( String linkDat , String headDat ,
            String tireRecord[][] , int loopCTL  ) {


      pp = new LinkedList();
      linkFile = new File( linkDat ) ;
      headFile = new File( headDat ) ;

         sysPrint("initRecord(): 1a - the value of linkDat is " + linkDat + " \nand the value of headDat is " + headDat);
      try {
         /** Open the headFile in RW mode.
          *  If the file does not exist, create it
          *  and initialize it to 250 empty records.
          */

         if ( !headFile.exists() ) {

            hFile = new RandomAccessFile( headDat , "rw" );

            data.setRecID( -1 ) ;
            data.setQuantity( -1 ) ;
            data.setNext( 0 ) ;
            data.setToolType( " " ) ;
            data.setBrandName( "zzzzzzzzzzzzzzzzzz" ) ;
            data.setToolDesc( " " ) ;
            data.setPartNumber( " " ) ;
            data.setCost( " " ) ;

            hFile.seek( 0 * data.getSize() );
            data.write( hFile ) ;
         }
         else {
            hFile = new RandomAccessFile( headDat , "rw" );
         }
      }
      catch ( IOException e ) {
            System.err.println( e.toString() );
            System.err.println( "Failed in opening headTires.dat."  );
            System.exit( 1 );
      }

      try {
         /** Open the tire.dat file in RW mode.
          *  If the file does not exist, create it
          *  and initialize it to 250 empty records.
          */

         sysPrint("initTire(): 1ab - checking to see if " + linkDat + " exist." );
         if ( !linkFile.exists() ) {

            sysPrint("initTire(): 1b - " + linkDat + " does not exist." );

            lFile = new RandomAccessFile( linkDat , "rw" );
            data = new Record() ;

            for ( int ii = 0 ; ii < loopCTL ; ii++ ) {
               data.setRecID( Integer.parseInt( tireRecord[ ii ][ 0 ] ) ) ;
               sysPrint("initTire(): 1c - The value of record ID is " + data.getRecID() ) ;
               data.setQuantity( Integer.parseInt( tireRecord[ ii ][ 2 ] ) ) ;
               data.setToolType( tireRecord[ ii ][ 3 ] ) ;
               data.setBrandName( tireRecord[ ii ][ 4 ] ) ;
               data.setToolDesc( tireRecord[ ii ][ 5 ] ) ;
               data.setPartNumber( tireRecord[ ii ][ 6 ] ) ;
               data.setCost( tireRecord[ ii ][ 7 ] ) ;


               sysPrint("Calling Linklist method add() during initialization. " + ii );
               pp.add( lFile, hFile, data );

            }
         }
         else {
            lFile = new RandomAccessFile( linkDat , "rw" );
         }

         lFile.close();
         hFile.close();

      }
      catch ( IOException e ) {
            System.err.println( e.toString() );
            System.exit( 1 );
      }
   }

   /** ****************************************************************
    *  close() - is used to exit the application. It is called by:
    *            1- WindowAdapter's windowClosing() method
    *            2- MenuHandler class' actionPerformed() method
    ******************************************************************** */
   public void close()   {
      System.exit( 0 ) ;
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

   /** ************************************************************
    *  main() - This is the main entry poing point that is called
    *           by Java on application startup.
    ************************************************************** */
   public static void main(String args[]) {
      final CarStore cs = new CarStore() ;

      cs.addWindowListener(
         new WindowAdapter() {
            public void windowClosing( WindowEvent e )  {
               cs.close() ;
            }
         }
      );
   }

   /** ***************************************************************
    *  MenuHandler class - is an event handler that responds to GUI
    *                      events caused by GUI items being pressed.
    ****************************************************************** */
   public class MenuHandler implements ActionListener {
      public void actionPerformed( ActionEvent e )  {

         if ( e.getSource() == eMI || e.getSource() == exit ) {
         /**The Exit menu Item was selected. */
            close();
         }
         else if ( e.getSource() == debugON ) {
            myDebug    = true ;
            pp.myDebug = true ;
            sysPrint ("Debugging for this execution is turned on.\n" );
         }
         else if ( e.getSource() == debugOFF ) {
            sysPrint ("Debugging for this execution is turned off.\n" );
            myDebug    = false ;
            pp.myDebug = false ;
         }
         else if ( e.getSource() == viewTiresMI ) {
            sysPrint ("View all tires.\n" ) ;
            display( "Tires" ) ;
         }
         else if ( e.getSource() == viewBatteriesMI ) {
            sysPrint ("View all batteries.\n" ) ;
            display( "Batteries" ) ;
         }
         else if ( e.getSource() == oilFiltersMI ) {
            sysPrint ("View all oil Filters.\n" ) ;
            display( "Oil Filters" ) ;
         }
         else if ( e.getSource() == airFiltersMI ) {
            sysPrint ("View all air Filters.\n" ) ;
            display( "Air Filters" ) ;
         }
         else if ( e.getSource() == gasFiltersMI ) {
            sysPrint ("View all gas Filters.\n" ) ;
            display( "Gas Filters" ) ;
         }
         else if ( e.getSource() == deleteMI ) {
            sysPrint ("The Delete menu Item was selected.\n" );
            deleteRec = new DeleteRec( cs, lFile , hFile , table, pData );
            deleteRec.setVisible( true );
         }
         else if ( e.getSource() == addMI ) {
            sysPrint ("The Add menu Item was selected.\n" );
            newRec = new NewRec( cs, lFile , hFile , table, pData );
            newRec.setVisible( true );
         }
         else if ( e.getSource() == updateMI ) {
            sysPrint ("The Update menu Item was selected.\n" );
            update = new UpdateRec( cs , lFile , hFile , pData , -1);
            update.setVisible( true );
         }
         else if ( e.getSource() ==  refresh ) {
            sysPrint ("The refresh button was pressed.\n" ) ;
         }
         else if ( e.getSource() == helpHWMI ) {
            sysPrint ("The Help menu Item was selected.\n" );
            File hd = new File("LL_Tutorial_4.html");
            sysPrint( "the path for help_doc is " + hd.getAbsolutePath() );
            //sysPrint( "the path for netscape is " + net.getAbsolutePath() );

            Runtime rt = Runtime.getRuntime();
            //String[] callAndArgs = { "d:\\Program Files\\netscape\\netscape\\Netscp.exe" ,
            String[] callAndArgs = { "c:\\Program Files\\Internet Explorer\\IEXPLORE.exe" ,
                        "" + hd.getAbsolutePath() };

            try {

               Process child = rt.exec( callAndArgs );
               child.waitFor();
               sysPrint ("Process exit code is: " +
                                 child.exitValue());
            }
            catch(IOException e2) {
               sysPrint (
                  "IOException starting process!");
            }
            catch(InterruptedException e3) {
               System.err.println(
                     "Interrupted waiting for process!");
            }
         }
         else if ( e.getSource() == aboutHWMI ) {
            sysPrint ("The About menu Item was selected.\n" );
            Runtime rt = Runtime.getRuntime();
            String[] callAndArgs = { "c:\\Program Files\\Internet Explorer\\IEXPLORE.exe" ,
                           "http://www.sumtotalz.com/TotalAppsWorks/ProgrammingResource.html" };
            try {
               Process child = rt.exec(callAndArgs);
               child.waitFor();
               sysPrint ("Process exit code is: " +
                                 child.exitValue());
            }
            catch(IOException e2) {
               System.err.println(
                  "IOException starting process!");
            }
            catch(InterruptedException e3) {
               System.err.println(
                     "Interrupted waiting for process!");
            }
         }
      }
   }


   /** **************************************************************
    * UpdateRec class - is used to create the Update dialog, which is
    *                   is used to gather user input to udate a record.
    ****************************************************************** */
   public class UpdateRec extends Dialog
         implements ActionListener {
      private RandomAccessFile lFile , hFile;
      private JTextField recID, toolType, brandName, toolDesc,
                partNum, quantity, price;
      private JLabel recIDLabel,  toolTypeLabel, brandNameLabel,
                  toolDescLabel,  partNumLabel, quantityLabel,
                    priceLabel;
      private JButton cancel, save;
      private Record data;
      private int theRecID, ii, iii, toCont, loopCtrl;
      private String pData [] [] ;
      private CarStore carstore ;
      private boolean found = false ;
      private String oldBrandName ;

   /** ***********************************************************
    *  UpdateRec() - this constructor is used to initialize the
    *                UpdateRec object.
    ************************************************************* */
      public UpdateRec( CarStore car_store, RandomAccessFile l_File ,
             RandomAccessFile h_File, String p_Data [] [], int iiPassed)
      {
         super( new Frame(), "Update Record", true );
         setSize( 400, 280 );
         setLayout( new GridLayout( 9, 2 ) );
         lFile = l_File;
         hFile = h_File;
         pData = p_Data ;
         ii = iiPassed ;
         carstore = car_store ;
         sysPrint( "The value of  car_store is " + car_store  );

         recID      = new JTextField( 10 );
         toolType   = new JTextField( 10 );
         brandName  = new JTextField( 10 );
         toolDesc   = new JTextField( 10 );
         partNum    = new JTextField( 10 );
         quantity   = new JTextField( 10 );
         price      = new JTextField( 10 );
         recIDLabel     = new JLabel( "Record ID" );
         toolTypeLabel  = new JLabel( "Type of Tool" );
         brandNameLabel = new JLabel( "Brand Name" );
         toolDescLabel  = new JLabel( "Tool Description" );
         partNumLabel   = new JLabel( "Part Number" );
         quantityLabel  = new JLabel( "Quantity" );
         priceLabel     = new JLabel( "Price" );
         save = new JButton( "Save Changes" );
         cancel = new JButton( "Cancel" );

         recID.addActionListener( this );
         save.addActionListener( this );
         cancel.addActionListener( this );

         add( recIDLabel );
         add( recID );
         add( toolTypeLabel );
         add( toolType );
         add( brandNameLabel );
         add( brandName );
         add( toolDescLabel );
         add( toolDesc );
         add( partNumLabel );
         add( partNum );
         add( quantityLabel );
         add( quantity );
         add( priceLabel );
         add( price );
         add( save );
         add( cancel );

         data = new Record();
         JOptionPane.showMessageDialog(null,
                       "To update a record, do the following:\n" +
                       "1- Enter one the record IDs from those in the list.\n" +
                       "2- Press enter.",
                       "How to update a record",
                       JOptionPane.INFORMATION_MESSAGE) ;
      }

   /** *************************************************
    *  checkDigit() - is used to ensure that the data
    *  entered is a digit
    ************************************************ */
      public boolean  checkDigit(String strVal) {

         int strLength = 0;
         boolean notDig = true;

         strLength = strVal.length();

         for (int ii = 0; ii < strLength; ii++) {
            if (!Character.isDigit(strVal.charAt(ii)) ) {
               notDig = false;
               break;
            }
         }

         return notDig;
      }

   /** *********************************************************************
    *  actionPerformed() - is used to respond to UpdateRec dialog
    *                      GUI events. These events are:
    *                      1- Enter key pressed with cursor in the recordID
    *                         text field.
    *                      2- Save button being pressed
    *                      3- Cancel button being pressed
    *********************************************************************** */
      public void actionPerformed( ActionEvent e )   {

         LinkedList ll = new LinkedList() ;

         if ( e.getSource() == recID )  {
            if ( checkDigit( recID.getText() ) ) {
               theRecID = Integer.parseInt( recID.getText() );
               if ( theRecID < 0 || theRecID > 250
                      || theRecID > carstore.getEntries() ) {
                  JOptionPane.showMessageDialog(null,
                       "A recID entered " + recID.getText() + " does not exist in the list.\n" +
                       "Please enter one of the record IDs that are in the list.", "RecID Entered",
                       JOptionPane.ERROR_MESSAGE) ;
               }
               else if ( !checkpData( theRecID , pData ) ) {
                  JOptionPane.showMessageDialog(null,
                       "A recID of " + theRecID + " was entered, and it does not exist in the list. This is an invalid number.\n" +
                       "Please enter a valid number from the list.", "Invalid RecID Entered",
                       JOptionPane.INFORMATION_MESSAGE) ;
               }
               else {
                  for ( int i = 0 ; i <= 15 ; i++ ) {
                     if ( Integer.parseInt( pData[ i  ] [ 0 ] ) == theRecID ) {
                        theRecID = Integer.parseInt(  pData[ i ] [ 0 ]  ) ;

                        break ;
                     }
                  }

                  try {
                     carstore.sysPrint( "\nThe value of theRecID is " + theRecID ) ;
                     lFile = new RandomAccessFile( linkFile , "rw" );
                     lFile.seek( ( theRecID  ) * data.getSize() );
                     data.ReadRec( lFile );

                     recID.setText( String.valueOf( data.getRecID() ) );
                     toolType.setText( data.getToolType().trim() );
                     brandName.setText( data.getBrandName().trim() ) ;
                     oldBrandName = new String( brandName.getText() )  ;
                     toolDesc.setText( data.getToolDesc().trim() ) ;
                     partNum.setText( data.getPartNumber().trim() ) ;
                     quantity.setText( Integer.toString( data.getQuantity() ) );
                     price.setText(  data.getCost().trim() );
                  }
                  catch ( IOException ex ) {
                     carstore.sysPrint( "\nUpdateRec()1b: actionPerformed(): Error reading " + lFile + " file" );
                  }
               }
            }
            else {
                JOptionPane.showMessageDialog(null,
                      "The number " + recID.getText() + " entered is invalid.",
                      "Invalid Record ID",
                      JOptionPane.ERROR_MESSAGE);
            }

         }
         else if ( e.getSource() == save ) {
            try {
               lFile = new RandomAccessFile( carstore.linkFile , "rw" );
               hFile = new RandomAccessFile( carstore.headFile , "rw" );

               theRecID = Integer.parseInt( recID.getText() ) ;

               data.setRecID(  theRecID );
               data.setToolType( toolType.getText().trim() );
               data.setBrandName( brandName.getText().trim() );
               data.setToolDesc( toolDesc.getText().trim() );
               data.setPartNumber( partNum.getText().trim() ) ;
               data.setQuantity( Integer.parseInt( quantity.getText() ) );
               data.setCost(  price.getText().trim()  );
               lFile.seek( 0 ) ;
               lFile.seek( ( Integer.parseInt( recID.getText() )  ) * data.getSize() );
               data.write( lFile );

               if (oldBrandName.equals( brandName.getText().trim() ) ) {
                  lFile.seek( 0 ) ;
                  lFile.seek( ( Integer.parseInt( recID.getText() )  ) * data.getSize() );
                  data.write( lFile );

                  loopCtrl = carstore.getEntries() ;
                  for ( iii = 0;  iii < loopCtrl ; iii++ ) {
                     if ( (pData[ iii  ] [ 0 ] ).equals( "" + theRecID ) ) {
                        theRecID = iii ;
                        break ;
                     }
                  }

                  sysPrint("The value of the index iii is " + iii) ;

                  sysPrint("The value of the theRecID is " + theRecID ) ;

                  // Account for index starting at 0 and for the next slot

                  theRecID = iii ;

               }
               else {
                  ll.Remove( lFile, hFile,
                         Integer.parseInt( recID.getText() ) ) ;
                  ll.add( lFile, hFile, data ) ;
               }

               Redisplay( lFile , hFile , pData )  ;
            }
            catch ( IOException ex ) {
               recID.setText( "Error writing file" );
              return;
            }


            toCont = JOptionPane.showConfirmDialog(null,
                   "Do you want to add another record? \nChoose one",
                   "Choose one",
                   JOptionPane.YES_NO_OPTION);

            if ( toCont == JOptionPane.YES_OPTION  )  {
               recID.setText( "" );
               toolType.setText( ""  );
               quantity.setText( ""  );
               brandName.setText( ""  );
               toolDesc.setText( ""  );
               partNum.setText( ""  );
               price.setText( ""  );
            }
            else {
               UpClear();
            }
         }
         else if ( e.getSource() == cancel ) {
            setVisible( false );
            UpClear();
         }
      }

   /** ***********************************************************
    *  UpClear() - is called to close the UpdateRec dialog.
    *************************************************************** */
      private void UpClear()   {
         recID.setText( "" );
         brandName.setText( "" );
         quantity.setText( "" );
         price.setText( "" );
         setVisible( false );
         try {
            lFile.close(); ;
            hFile.close(); ;
         }
         catch ( IOException ex ) {
            carstore.sysPrint( "Error closing file " + ex);
         }
      }
   }

   /** *************************************************************
    * NewRec class - is used to create the NewRec Dialog, which is
    *                used to gather user input for a new record.
    *************************************************************** */
   public class NewRec extends Dialog
        implements ActionListener {
      private RandomAccessFile lFile ;
      private JTextField recID, toolType, brandName, toolDesc,
                partNum, quantity, price;
      private JLabel recIDLabel,  toolTypeLabel, brandNameLabel,
                  toolDescLabel,  partNumLabel, quantityLabel,
                    priceLabel;
      private JButton cancel, save;
      private Record data;
      private int recIDNum, toCont , len;
      private JTable table ;
      private String pData[] [] ;
      private String columnNames[] = {"Record ID", "Type of tool",
          "Brand Name",  "Tool Description", "partNum",
          "Quantity", "Price"} ;
      CarStore carstore ;

     /** ****************************************************************
      *  NewRec() - the NewRec constructor is used to create the NewRec
      *             dialog. This dialog is used to gather user data
      *             for a new record.
      ******************************************************************* */
      public NewRec( CarStore car_store, RandomAccessFile l_File,
              RandomAccessFile hFile, JTable tab,  String p_Data[] []  )
      {
         super( new Frame(), "New Record", true );
         setSize( 400, 250 );
         setLayout( new GridLayout( 9, 2 ) );
         lFile = l_File;
         table = tab ;
         pData = p_Data ;
         carstore = car_store ;
         NewSetup() ;
      }

      public void NewSetup() {
         recID      = new JTextField( 10 );
         toolType   = new JTextField( 10 );
         brandName  = new JTextField( 10 );
         toolDesc   = new JTextField( 10 );
         partNum    = new JTextField( 10 );
         quantity   = new JTextField( 10 );
         price      = new JTextField( 10 );
         recIDLabel     = new JLabel( "Record ID" );
         toolTypeLabel  = new JLabel( "Type of Tool" );
         brandNameLabel = new JLabel( "Brand Name" );
         toolDescLabel  = new JLabel( "Tool Description" );
         partNumLabel   = new JLabel( "Part Number" );
         quantityLabel  = new JLabel( "Quantity" );
         priceLabel     = new JLabel( "Price" );
         save = new JButton( "Save Changes" );
         cancel = new JButton( "Cancel" );

         recID.addActionListener( this );
         save.addActionListener( this );
         cancel.addActionListener( this );

         add( recIDLabel );
         add( recID );
         add( toolTypeLabel );
         add( toolType );
         add( brandNameLabel );
         add( brandName );
         add( toolDescLabel );
         add( toolDesc );
         add( partNumLabel );
         add( partNum );
         add( quantityLabel );
         add( quantity );
         add( priceLabel );
         add( price );
         add( save );
         add( cancel );
         try {
            lFile = new RandomAccessFile( carstore.linkFile , "rw" ) ;
            len = (int)lFile.length() / data.getSize() ;
            recID.setText("" + len ) ;
            recID.setEnabled( false ) ;
            lFile.close() ;
         }
         catch ( IOException ex ) {
            partNum.setText( "Error reading file" );
         }

         data = new Record();
      }

     /** ******************************************************************
      *  actionPerformed() - is used to respond to NewRec dialog
      *                      GUI events. These events are:
      *                      1- Enter key pressed with cursor in the recordID
      *                         text field.
      *                      2- Save button being pressed
      *                      3- Cancel button being pressed
      ******************************************************************** */
      public void actionPerformed( ActionEvent e )   {
         if ( e.getSource() == recID )  {
            recIDNum = Integer.parseInt( recID.getText() );

            if ( recID.getText().equals(null) || recIDNum < 0 ||
                  recIDNum > 250   ) {
               partNum.setText( "Invalid account" );
               //return;
           }
           else {

               try {
                  lFile.seek( ( data.getRecID() ) * data.getSize() );
                  data.ReadRec( lFile );
               }
               catch ( IOException ex ) {
                  partNum.setText( "Error reading file" );
               }

               if ( ( data.getRecID() < 0 ) ||  ( data.getRecID() > 250 ) ) {
                  recID.setText( "Enter a record ID between 0 and 251" );
                  toolType.setText( "Enter tool name" );
                  quantity.setText( "Enter quantity" );
                  price.setText( "Enter price" );
                  toolType.selectAll();
                  quantity.selectAll();
                  price.selectAll();
               }
               else {
                  partNum.setText( data.getPartNumber() + " already exists" );
                  toolType.setText( "" );
                  quantity.setText( "" );
                  price.setText( "" );
               }
            }
         }
         else if ( e.getSource() == save ) {
            if ( recID.getText().equals("") ) {
               JOptionPane.showMessageDialog(null,
                    "A recID entered was:  null or blank, which is invalid.\n" +
                    "Please enter a number greater than 0 and less than 251.", "RecID Entered",
                    JOptionPane.INFORMATION_MESSAGE) ;
               return ;
            }
            else {
               try {
                  lFile = new RandomAccessFile( carstore.linkFile , "rw" );
                  hFile = new RandomAccessFile( carstore.headFile , "rw" );
                  data.setRecID( Integer.parseInt( recID.getText() ) );
                  data.setToolType( toolType.getText().trim() );
                  data.setBrandName( brandName.getText().trim() );
                  data.setToolDesc( toolDesc.getText().trim() );
                  data.setPartNumber( partNum.getText().trim() );
                  data.setQuantity( Integer.parseInt( quantity.getText() ) );
                  data.setCost(  price.getText().trim()  );
                  LinkedList ll = new LinkedList() ;
                  ll.add( lFile , hFile , data );

                  Redisplay( lFile , hFile , pData ) ;

                  // Account for index starting at 0 and for the next slot
                  recIDNum =  carstore.getEntries() + 2 ;
                  carstore.sysPrint("NewRec 1: The numbers of entries is " + (recIDNum - 1) ) ;

                  carstore.sysPrint("NewRec 2: A new record is being added at " +
                     recIDNum   );
                  carstore.setEntries( carstore.getEntries() + 1 );

                  recID.setText("" + (int)lFile.length() / data.getSize() ) ;
                  recID.setEnabled( false ) ;

                  lFile.close() ;
                  hFile.close() ;
               }
               catch ( IOException ex ) {
                  partNum.setText( "Error writing file" );
                  return;
               }

            }

            toCont = JOptionPane.showConfirmDialog(null,
                   "Do you want to add another record? \nChoose one",
                   "Choose one",
                   JOptionPane.YES_NO_OPTION);

            if ( toCont == JOptionPane.YES_OPTION  )  {
               toolType.setText( ""  );
               quantity.setText( ""  );
               brandName.setText( ""  );
               toolDesc.setText( ""  );
               partNum.setText( ""  );
               price.setText( ""  );
            }
            else {
               NewClear();
            }
         }
         else if ( e.getSource() == cancel ) {
            NewClear();
         }
      }

     /** *************************************************
      *  NewClear() - is used to
      ************************************************ */
      private void NewClear()   {
         partNum.setText( "" );
         toolType.setText( "" );
         quantity.setText( "" );
         price.setText( "" );
         setVisible( false );
      }
   }

   /** **************************************************************
    * DeleteRec class - is used to create the DeleteRec dialog.
    *                   This dialog is used to gather the record
    *                   ID for the record the user wants to delete.
    *
    *************************************************************** */
   public class DeleteRec extends Dialog
          implements ActionListener {
      private RandomAccessFile lFile , hFile;
      private JTextField recID;
      private JLabel recIDLabel;
      private JButton cancel, delete;
      private Record data;
      private int partNum , loopCTL = 0;
      private int theRecID =  -1 , toCont ;
      private JTable table ;
      private String pData[] [] ;
      private CarStore carstore  ;
      private boolean found = false ;

     /** *******************************************************
      *  DeleteRec() - this constructor is used to create
      *                the GUI on the DeleteRec dialog. It
      *                1- Processes the input parameters
      *                2- Creates the labels and text fields
      *                3- Attaches event handlers to the buttons
      *                   and the record if textfield.
      ************************************************************ */
      public DeleteRec( CarStore car_store,  RandomAccessFile lF,
                    RandomAccessFile hF, JTable tab, String p_Data[] []  )
      {
         super( new Frame(), "Delete Record", true );
         setSize( 400, 150 );
         setLayout( new GridLayout( 2, 2 ) );
         lFile = lF;
         hFile = hF;
         table = tab ;
         pData = p_Data ;
         carstore = car_store ;

         recIDLabel = new JLabel( "Record ID" );
         recID = new JTextField( 10 );
         delete = new JButton( "Delete Record" );
         cancel = new JButton( "Cancel" );

         cancel.addActionListener( this );
         delete.addActionListener( this );
         recID.addActionListener( this );

         add( recIDLabel);
         add( recID );
         add( delete );
         add( cancel );

         data = new Record();
      }

     /** ***********************************************************************
      *  actionPerformed() - is used to respond to DeleteRec dialog
      *                      GUI events. These events are:
      *                      1- Enter key pressed with cursor in the recordID
      *                         text field.
      *                      3- Delete button being pressed
      *                      3- Pressing the Cancel button.
      ********************************************************************* */
      public void actionPerformed( ActionEvent e )   {

         LinkedList ll = new LinkedList() ;

         if ( e.getSource() == recID  )  {
            if ( checkDigit( recID.getText() ) ) {

               theRecID = Integer.parseInt( recID.getText() );

               for ( int iii = 0;  iii < pData.length ; iii++ ) {
                  if ( (pData[ iii  ] [ 0 ] ).equals( "" + theRecID ) ) {
                     theRecID = Integer.parseInt( pData[ iii  ] [ 0 ] ) ;
                     found = true ;
                     System.out.println( "DeleteRec(): 2 - The record id was found is  "
                            +  pData[ iii  ] [ 0 ] ) ;
                     break ;
                  }
               }

               if ( found ) {
                  found = false ;

                  try {
                     lFile = new RandomAccessFile( carstore.linkFile , "rw" );
                     hFile = new RandomAccessFile( carstore.headFile , "rw" );

                     lFile.seek(  theRecID * data.getSize() );
                     data.ReadRec( lFile );
                  }
                  catch ( IOException ex ) {
                     recID.setText( "Error reading file" );
                  }
               }
               else {
                  /** The number entered was not found in the pData array */
                  JOptionPane.showConfirmDialog(null,
                      "The number you entered is invalid.",
                      "Invalid Record ID",
                      JOptionPane.ERROR_MESSAGE);
               }
            }
         }
         else if ( e.getSource() == delete ) {

            if ( checkDigit( recID.getText() ) && IsInPData( recID.getText() ) ) {
               try {

                  lFile = new RandomAccessFile( carstore.linkFile , "rw" );
                  hFile = new RandomAccessFile( carstore.headFile , "rw" );
                  sysPrint( "DeleteRec(): 1a - The record id to be deleted is "
                   + recID.getText() );
                  theRecID = Integer.parseInt( recID.getText() );
                  sysPrint( "DeleteRec(): 1b - The record id to be deleted is "
                      + theRecID );
                  theRecID =  Integer.parseInt( recID.getText() ) ;
                  data.setRecID( theRecID ) ;
                  //data.setNext( -1 ) ;

                  ll.Remove( lFile, hFile,
                             Integer.parseInt( recID.getText() ) ) ;
                 carstore.setEntries( carstore.getEntries() - 1 );

                  for ( int iii = 0;  iii < pData.length ; iii++ ) {
                     if ( (pData[ iii  ] [ 0 ] ).equals( "" + theRecID ) ) {
                        theRecID = Integer.parseInt( pData[ iii  ] [ 0 ] ) ;
                        break ;
                     }
                  }

                  Redisplay( lFile , hFile , pData );

                  lFile.close() ;
                  hFile.close() ;
               }
               catch ( IOException ex ) {
                  recID.setText( "Error writing file" );
                  return;
               }


               toCont = JOptionPane.showConfirmDialog(null,
                      "Do you want to add another record? \nChoose one",
                      "Select Yes or No",
                      JOptionPane.YES_NO_OPTION);

               if ( toCont == JOptionPane.YES_OPTION  )  {
                  recID.setText( "" );
               }
               else {

                  DelClear();
               }
            }
            else {
                  /** The number entered was not found in the pData array */
                  JOptionPane.showMessageDialog(null,
                      "The number " + recID.getText() + " entered is invalid.",
                      "Invalid Record ID",
                      JOptionPane.ERROR_MESSAGE);
            }
         }
         else if ( e.getSource() == cancel ) {
            DelClear( );
         }
      }

     /** *****************************************************
      *  DelClear() - is used to close the DeleteRec dialog.
      ******************************************************* */
       private void DelClear()   {
         setVisible( false );
         recID.setText( "" );
      }
   }
}
