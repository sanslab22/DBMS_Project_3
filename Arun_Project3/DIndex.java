
/************************************************************************************
 * @file DIndex.java
 *
 * @author  John Miller
 *
 * compile javac --enable-preview --release 21 DIndex.java
 * run     java --enable-preview DIndex
 */

import java.io.Serializable;
import java.util.*;

import static java.lang.System.out;

/************************************************************************************
 * The `DIndex` class provides Direct Index maps, where keys are only implicitly stored
 * and the values are stored by using the key as an index into an array, i.e.,
 *     dindex [key] = value
 * It can be used to store the location of tuples in a Table.
 */
public class DIndex
       extends AbstractMap <Integer, Integer>
       implements Serializable, Cloneable
{
    private int [] dindex;                                 // direct index strorage array

    /********************************************************************************
     * Return the size (number of available entries) in the Direct Index map.
     */
    public int size ()
    {
        return dindex.length;
    } // size

    /********************************************************************************
     * Construct an empty Direct Index map.
     * @param nKeys  the number of possible keys (0 ... nKeys-1)
     */
    public DIndex (int nKeys)
    {
        dindex = new int [nKeys];
        Arrays.fill (dindex, -1);                          // -1 marker for not found
    } // constructor

    /********************************************************************************
     * Put the key-value pair in the Direct Index map.  Only the value needs to be stored.
     * @param key    the key to implicitly insert
     * @param value  the value to insert (e.g., index/location of tuple)
     * @return  the previous value for this key
     */
    public int put (int key, int value)
    {
        var old = dindex [key];
        dindex [key] = value;
        return old;
    } // put

    /********************************************************************************
     * Given the key, look up the value in the Direct Index map.
     * @param key  the key used for look up
     * @return  the value associated with the key or -1 if not found
     */
    @SuppressWarnings("unchecked")
    public Integer get (Object key)
    {
        return dindex [(int) key];
    } // get

    /********************************************************************************
     * Return a set containing all the entries as pairs of keys and values.
     * @return  the set view of the map
     */
    public Set <Map.Entry <Integer, Integer>> entrySet ()
    {
        return null;                                                 // not supported
    } // entrySet

    /********************************************************************************
     * Main method for creating, populating and querying a student table via pin-codes.
     * @param args  the command-line arguments
     */
    public static void main (String [] args)
    {
        var nKeys = 2000000;                                 // number of possible keys

        var student = new Table ("student", "pin name",
                                            "Integer String", "pin");

        var pin = new int [] { 811234567, 810000000, 811999999 };

        var s0 = new Comparable [] { pin[0], "Peter" };
        var s1 = new Comparable [] { pin[1], "Paul" };
        var s2 = new Comparable [] { pin[2], "Mary" };

        var sindex = new DIndex (nKeys);                     // create a Direct Index for the student table
        sindex.put (pin[0] % nKeys, student.insert (s0));
        sindex.put (pin[1] % nKeys, student.insert (s1));
        sindex.put (pin[2] % nKeys, student.insert (s2));

        student.print ();
        out.println ("Find the tuples using the pin");
        for (var k = pin.length-1; k >= 0; k--)
            student.printTup (student.get (sindex.get (pin[k] % nKeys)));

       out.println ("size (entries) of sindex = " + sindex.size ());
    } // main

} // DIndex

