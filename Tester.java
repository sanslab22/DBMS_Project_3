/************************************************************************************
 * @file Tester.java
 *
 * @author  Fidel Arroyo
 *
 * compile javac --enable-preview --release 21 Tester.java
 * run     java --enable-preview Tester
 */

import java.io.Serializable;
import java.util.*;

import static java.lang.System.out;
import static java.lang.System.setOut;

/************************************************************************************
 * The `Tester` class provides the test cases for DBMS Project 03. It will
 * involve the use of DIndex.java, TupleGeneratorImpl.java, and Table.java.
 * It can provide methods to have the test cases run on other java files.
 */
public class Tester {
    private static Table testerTable;
    private static Table testerTable2;
   private DIndex dIndex;
   private int keys;
    /********************************************************************************
     * Construct an empty Tester table .
     * @param nKeys  the total number of tuples to have per table
     */
    public Tester (int nKeys)
    {
        this.keys = nKeys;
        testerTable = new Table ("student", "id name address status",
                "Integer String String String", "id");
        testerTable2 = new Table ("transcript", "studId crsCode semester grade", "Integer String String String", "studId, crsCode, semester");
        dIndex = new DIndex(nKeys);
    } // constructor

    /********************************************************************************
     * Fill the  empty Tester table . Also keeps a DIndex reference to created tuples.
     */
    public  void fillTable()
    {
        var test = new TupleGeneratorImpl ();
        test.addRelSchema ("Student",
                "id name address status",
                "Integer String String String",
                "id",
                null);
        var tables = new String [] { "Student"};
        var tups   = new int [] { keys };
        var resultTest = test.generate (tups);
        for (var i = 0; i < resultTest.length; i++) {
            out.println (tables [i]);
            for (var j = 0; j < resultTest [i].length; j++) {
                out.println ();
                int ref = (int)resultTest[i][j][0] % keys;
                out.println(ref);
                dIndex.put (ref, testerTable.insert (resultTest[i][j]));
              //  testerTable.insert(resultTest[i][j]);
            } // for
            out.println ();
        } // for
        testerTable.print();
    } //fillTable

    /********************************************************************************
     * Fill the  empty Tester table 2 . Also keeps a DIndex reference to created tuples.
     */
    public  void fillTableTwo()
    {
        var test = new TupleGeneratorImpl ();
        test.addRelSchema ("Transcript",
                "studId crsCode semester grade",
                "Integer String String String",
                "studId crsCode semester",
                null );
        var tables = new String [] { "Transcript"};
        var tups   = new int [] { keys };
        var resultTest = test.generate (tups);
        for (var i = 0; i < resultTest.length; i++) {
            out.println (tables [i]);
            for (var j = 0; j < resultTest [i].length; j++) {
                out.println ();
                int ref = (int)resultTest[i][j][0] % keys;
                out.println(ref);
                dIndex.put (ref, testerTable2.insert (resultTest[i][j]));
                //  testerTable.insert(resultTest[i][j]);
            } // for
            out.println ();
        } // for
        testerTable2.print();
    } //fillTable


    /********************************************************************************
     * Run the first test for this table related to select search. This involves having found a tuple in table.
     */
    public void testOne()
    {
        var rand       = new Random ();
        var tupC = new Comparable[4];
        tupC[0] = (Integer)rand.nextInt(1000000);
        tupC[1] = (String)("name" + rand.nextInt(1000000));
        tupC[2] = (String)("address" + rand.nextInt(1000000));
        tupC[3] = (String)("status" + rand.nextInt(1000000));
        int ref = (int)tupC[0] % keys;
        dIndex.put (ref, testerTable.insert (tupC));
        KeyType searching = new KeyType(tupC[0]);
        testerTable.print();
        testerTable.select(searching).print();
    } //testOne

    /********************************************************************************
     * Run the second test for this table related to select search. This involves not having found a tuple in table.
     */
    public void testTwo()
    {
        var rand       = new Random ();
        var tupC = new Comparable[4];
        tupC[0] = (Integer)rand.nextInt(1000000);
        tupC[1] = (String)("name" + rand.nextInt(1000000));
        tupC[2] = (String)("address" + rand.nextInt(1000000));
        tupC[3] = (String)("status" + rand.nextInt(1000000));
        int ref = (int)tupC[0] % keys;
        testerTable.print();
        KeyType searching = new KeyType(tupC[0]);
        testerTable.select(searching).print();


    } //testOne



    /********************************************************************************
     * Main method for running the test cases created and displaying the results.
     */
    public static void main (String [] args)
    {
        var testing = new Tester(10);
        testing.fillTable();
        testing.testOne();
        testing.testTwo();
        testing.fillTableTwo();
    } // main
}
