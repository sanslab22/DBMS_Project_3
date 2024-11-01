 
/*****************************************************************************************
 * @file  TestTupleGenerator.java
 *
 * @author   Sadiq Charaniya, John Miller
 */

import java.util.Random;

import static java.lang.System.nanoTime;
import static java.lang.System.out;

/*****************************************************************************************
 * This class tests the TupleGenerator on the Student Registration Database defined in the
 * Kifer, Bernstein and Lewis 2006 database textbook (see figure 3.6).  The primary keys
 * (see figure 3.6) and foreign keys (see example 3.2.2) are as given in the textbook.
 */
public class TestTupleGenerator
{
    /********************************************************************************
     * Run the first test for this table related to select search. This involves not having found a tuple in table.
     * Test case for regular select operation WITHOUT Indexing.
     *
     * @param testTable the test table to be used for this method
     * @param size the size of the table therefore the ref -1
     * @return a long representing the time it took to run select, in nanotime
     */
    public static long testOne(Table testTable, int size)
    {
        var rand       = new Random();
        var tupC = new Comparable[4];
        tupC[0] = (Integer)rand.nextInt(1000000);
        tupC[1] = (String)("name" + rand.nextInt(1000000));
        tupC[2] = (String)("address" + rand.nextInt(1000000));
        tupC[3] = (String)("status" + rand.nextInt(1000000));
        int ref = (int)tupC[0] % size + 1;
        out.println("<<<<< TEST ONE >>>>>");
        KeyType searching = new KeyType(tupC[0]);
        //prints operation
       // testTable.select(searching).print();
        //runs operation without printing
        var t0 = nanoTime ();
        testTable.select(searching);
        return (nanoTime () - t0);
    } //testOne

    /********************************************************************************
     * Run the second test for this table related to select search. This involves having found a tuple in table.
     * Test case for indexed select operation using DIndex to index tuples.
     *
     * @param testTable the Student table to be used for this method
     * @param dIndex the dIndex that is being used
     * @param size the size of the table therefore the ref -1
     * @return a long representing the time it took to run the select
     */
    public static long testTwo(Table testTable, DIndex dIndex, int size)
    {
        var rand       = new Random ();
        var tupC = new Comparable[4];
        tupC[0] = (Integer)rand.nextInt(1000000);
        tupC[1] = (String)("name" + rand.nextInt(1000000));
        tupC[2] = (String)("address" + rand.nextInt(1000000));
        tupC[3] = (String)("status" + rand.nextInt(1000000));
        int ref = (int)tupC[0] % (size + 1);
        dIndex.put (ref, testTable.insert (tupC));
        KeyType searching = new KeyType(tupC[0]);
        out.println("<<<<< TEST TWO >>>>>");
        //prints operation
       // testTable.select(searching).print();
        //runs operation without printing
        var t0 = nanoTime ();
        testTable.select(searching);
        return (nanoTime () - t0);
    } //testTwo

    /********************************************************************************
     * Run the third test for this table related to join. This involves having found all tuples to join.
     * Performs an equi join on testerTable and oTesterTable assuming that the tables have data in them. 
     *
     * @param testerTable the first tester table to be used for this method
     * @param oTesterTable the other tester table to be used for this method
     * @return a long representing the time it took to run index Join
     */
    public static long testThree(Table testerTable, Table oTesterTable)
    {
        out.println("<<<<< TEST THREE >>>>>");
        //prints operation
       // oTesterTable.i_join("studId", "id", testerTable).print();
        //does operation without printing
        var t0 = nanoTime ();
        oTesterTable.i_join("studId", "id", testerTable);
        return (nanoTime () - t0);
    } //testThree

    /********************************************************************************
     * Run the fourth test for this table related to join. This involves having found all tuples to join.
     * Performs an indexed equi-join with the help of DIndex to assign indicies to all the generated tuples
     *
     * @param oTesterTable the other tester table to be used for this method
     * @param size the size of the table to be joined (original table)
     * @return a long representing the time it took to run index Join
     */
    public static long testFour(Table oTesterTable, int size)
    {
        out.println("<<<<< TEST FOUR >>>>>");
        var test = new TupleGeneratorImpl ();
        test.addRelSchema ("Student",
                "id name address status",
                "Integer String String String",
                "id",
                null);
        var studentTable = new Table ("Student",
                "id name address status",
                "Integer String String String",
                "id");
        var  studentDIndex = new DIndex(size + 1);
        var tups   = new int [] {size};
        var resultTest = test.generate (tups);
        for (var i = 0; i < resultTest.length; i++) {
            for (var j = 0; j < resultTest [i].length; j++) {
                int ref = (int)resultTest[i][j][0] % (resultTest[i].length + 1);
               studentDIndex.put (ref, studentTable.insert(resultTest[i][j]));
            } // for
         //   studentTable.print();
        } // for
        //prints operation
       // oTesterTable.i_join("studId", "id", studentTable).print();
        var t0 = nanoTime ();
        //does operation without printing
        oTesterTable.i_join("studId", "id", studentTable);
        return (nanoTime () - t0);
    } //testFour

    /*************************************************************************************
     * The main method is the driver for TestGenerator.
     * @param args  the command-line arguments
     */
    public static void main (String [] args)
    {
        var test = new TupleGeneratorImpl ();

        test.addRelSchema ("Student",
                           "id name address status",
                           "Integer String String String",
                           "id",
                           null);

        var studentTable = new Table ("Student",
                "id name address status",
                "Integer String String String",
                "id");

        test.addRelSchema ("Professor",
                           "id name deptId",
                           "Integer String String",
                           "id",
                           null);

        var professorTable = new Table ("Professor",
                "id name deptId",
                "Integer String String",
                "id");

        test.addRelSchema ("Course",
                           "crsCode deptId crsName descr",
                           "Integer String String String",
                           "crsCode",
                           null);

        var courseTable = new Table ("Course",
                "crsCode deptId crsName descr",
                "Integer String String String",
                "crsCode");

        test.addRelSchema ("Teaching",
                           "crsCode semester profId",
                           "Integer String Integer",
                           "crsCode semester",
                           new String [][] {{ "profId", "Professor", "id" },
                                            { "crsCode", "Course", "crsCode" }});

        var teachingTable = new Table ("Teaching",
                "crsCode semester profId",
                "Integer String Integer",
                "crsCode semester");

        test.addRelSchema ("Transcript",
                           "studId crsCode semester grade",
                           "Integer String String String",
                           "studId crsCode semester",
                new String [][] {{ "studId", "Student", "id"},
                        { "crsCode", "Course", "crsCode" },
                        { "crsCode semester", "Teaching", "crsCode semester" }});

        var transcriptTable = new Table ("Transcript",
                "studId crsCode semester grade",
                "Integer String String String",
                "studId crsCode semester"
        );
        var tables = new String [] { "Student", "Professor", "Course", "Teaching", "Transcript" };
        var tableObjs = new Table[] {studentTable,professorTable,courseTable,teachingTable,transcriptTable};
        var tups   = new int [] { 10000, 10000, 10000, 10000, 10000 };
        var  studentDIndex = new DIndex(tups[0] + 1);
        var  professorDIndex = new DIndex(tups[1] + 1);
        var  courseDIndex = new DIndex(tups[2] + 1);
        var  teachingDIndex = new DIndex(tups[3] + 1);
        var  transcriptDIndex = new DIndex(tups[4] + 1);
        var dIndexObjs = new DIndex[] {studentDIndex, professorDIndex, courseDIndex, teachingDIndex, transcriptDIndex};
        // 1000, 2000,  5000, 7500, 10000, 25000, 50000
        var resultTest = test.generate (tups);

        for (var i = 0; i < resultTest.length; i++) {
            out.println (tables [i]);
            for (var j = 0; j < resultTest [i].length; j++) {
                int ref = (int)resultTest[i][j][0] % (resultTest[i].length + 1);
                dIndexObjs[i].put (ref, tableObjs[i].insert (resultTest[i][j]));
                out.println ();
            } // for
            out.println ();
            tableObjs[i].print();
        } // for
        ///to commit
        long [] testAverage = new long[4];
        for (int i = 0; i < 6; i++) {
            if (i >0) {
                out.println("Test trail " + i);
                testAverage[0] += testOne(tableObjs[0], tups[0]);
                testAverage[1] += testTwo(tableObjs[0], dIndexObjs[0], tups[0]);
                testAverage[2] += testThree(tableObjs[0], tableObjs[4]);
                testAverage[3] += testFour(tableObjs[4], tups[0]);
            } //if
            if (i == 5) {
                out.println("Test one - Select: " + (testAverage[0]/5) + " ns");
                out.println("Test two - Indexed Select: " + (testAverage[1]/5) + " ns");
                long selecter = ((testAverage[0]/5) + (testAverage[1]/5)) / 2;
                out.println("Average time taken for Select: " + selecter + " ns");
                out.println("Test three - Equi Join: " + (testAverage[2]/5)+ " ns");
                out.println("For test four average: " + (testAverage[3]/5)+ " ns");
                long joiner = ((testAverage[2]/5) + (testAverage[3]/5)) / 2;
                out.println("For joiner average: " + joiner+ " ns");

            } //if
        } //for
    } // main

} // TestTupleGenerator
