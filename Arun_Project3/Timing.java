
/************************************************************************************
 * @file Timing.java
 *
 * @author  John Miller
 *
 * compile javac --enable-preview --release 21 Timing.java
 * run     java --enable-preview Timing
 */

import java.io.Serializable;
import java.util.*;

import static java.lang.System.out;
import static java.lang.System.nanoTime;

/********************************************************************************
 * The `Timing` class illustrates how to time code in Java.  Six iterations are taken
 * with the first one skipped due the JIT overhead.  An average of the last five is used. 
 * It uses `nanoTime` for timing elapsed time with resolution somewhere in the
 *    micro-seconds (mu-sec) range.
 *    https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/System.html#nanoTime
 * This class compares the runtimes of
 * 1. Arrays.sort -- Dual-Pivot Quicksort
 *    https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Arrays.html#sort(int%5B%5D)
 * 2. Collections.sort -- Stable, Adaptive, Iterative Mergesort adapted from TimSort
 *    https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Collections.html#sort(java.util.List)
 *    https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/List.html#sort(java.util.Comparator)
 */
public class Timing
{
    private static Random rand = new Random ();

    /********************************************************************************
     * Generate an array of random integers.
     * @param size  the number of integers to generate.
     */
    public static int [] randomArray (int size)
    {
        var ra = new int [size];
        for (var i = 0; i < size; i++) ra[i] = rand.nextInt (10 * size);
        return ra;
    } // randomArray

    /********************************************************************************
     * Convert an "int []" to an "ArrayList <Integer>".
     * @param arr  the given int array to be converted
     */
    public static ArrayList <Integer> toArrayList (int [] arr)
    {
        var al = new ArrayList <Integer> ();
        for (var a : arr) al.add (a);
        return al;
    } // toArrayList

    /********************************************************************************
     * Main method for testing the runtime performance of Java's built-in sorting algorithms.
     * @param args  the command-line arguments
     */
    public static void main (String [] args)
    {
        var nKeys = 1000000;
        var sum   = 0;
        for (var it = 0; it < 6; it++) {
            var ra = randomArray (nKeys);
            var t0 = nanoTime ();
            Arrays.sort (ra);
            var et = (nanoTime () - t0) / 1000;
            out.println ("for it = " + it + " Arrays.sort time = " + et + " mu-sec");
            if (it > 0) sum += et;
        } // for
        out.println ("Average for Arrays.sort time = " + sum / 5 + " mu-sec");

        sum = 0;
        for (var it = 0; it < 6; it++) {
            var ra = toArrayList (randomArray (nKeys));
            var t0 = nanoTime ();
            Collections.sort (ra);
            var et = (nanoTime () - t0) / 1000;
            out.println ("for it = " + it + " Collections.sort time = " + et + " mu-sec");
            if (it > 0) sum += et;
        } // for
        out.println ("Average for Collections.sort time = " + sum / 5 + " mu-sec");
    } // main

} // Timing

