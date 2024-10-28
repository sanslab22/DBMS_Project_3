
/*****************************************************************************************
 * @file  TestTupleGenerator.java
 *
 * @author   Sadiq Charaniya, John Miller
 */

import java.util.Random;

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
     *
     * @param studentTable the Student table to be used for this method
     * @param size the size of the table therefore the ref -1
     */
    public static void testOne(Table studentTable, int size)
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
        studentTable.select(searching).print();
    } //testOne

    /********************************************************************************
     * Run the second test for this table related to select search. This involves having found a tuple in table.
     *
     * @param studentTable the Student table to be used for this method
     * @param dIndex the dIndex that is being used
     * @param size the size of the table therefore the ref -1
     */
    public static void testTwo(Table studentTable, DIndex dIndex, int size)
    {
        var rand       = new Random ();
        var tupC = new Comparable[4];
        tupC[0] = (Integer)rand.nextInt(1000000);
        tupC[1] = (String)("name" + rand.nextInt(1000000));
        tupC[2] = (String)("address" + rand.nextInt(1000000));
        tupC[3] = (String)("status" + rand.nextInt(1000000));
        int ref = (int)tupC[0] % (size + 1);
        dIndex.put (ref, studentTable.insert (tupC));
        KeyType searching = new KeyType(tupC[0]);
        out.println("<<<<< TEST TWO >>>>>");
        studentTable.select(searching).print();
    } //testTwo

    /********************************************************************************
     * Run the third test for this table related to join. This involves having found all tuples to join.
     *
     * @param studentTable the Student table to be used for this method
     * @param transcriptTable the Transcript table to be used for this method
     * @param stuDIndex the dIndex for student table
     * @param tranDIndex the dIndex for transcript table
     */
    public static void testThree(Table studentTable, Table transcriptTable, DIndex stuDIndex, DIndex tranDIndex)
    {
        out.println("<<<<< TEST THREE >>>>>");
        transcriptTable.i_join("studId", "id", studentTable).print();
    } //testThree

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
        var tups   = new int [] { 10, 10, 10, 10, 10 };
        var  studentDIndex = new DIndex(tups[0] + 1);
        var  professorDIndex = new DIndex(tups[1] + 1);
        var  courseDIndex = new DIndex(tups[2] + 1);
        var  teachingDIndex = new DIndex(tups[3] + 1);
        var  transcriptDIndex = new DIndex(tups[4] + 1);
        var dIndexObjs = new DIndex[] {studentDIndex, professorDIndex, courseDIndex, teachingDIndex, transcriptDIndex};
        //10000, 1000, 2000, 50000, 5000
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
        out.println("Size of dIndex " + dIndexObjs[1].size());
        testOne(tableObjs[0], tups[0]);
        testTwo(tableObjs[0], dIndexObjs[0], tups[0]);
        testThree(tableObjs[0], tableObjs[4], dIndexObjs[0], dIndexObjs[4]);
    } // main

} // TestTupleGenerator

