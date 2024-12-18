
/* ***************************************************************************************
 * @file  Table.java
 *
 * @author   John Miller
 *
 * compile javac --enable-preview --release 21 *.java
 * run     java --enable-preview MovieDB    
 */

import java.io.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import static java.lang.System.arraycopy;
import static java.lang.System.out;

/****************************************************************************************
 * The Table class implements relational database tables (including attribute names, domains
 * and a list of tuples.  Five basic relational algebra operators are provided: project,
 * select, union, minus and join.  The insert data manipulation operator is also provided.
 * Missing are update and delete data manipulation operators.
 */
public class Table
       implements Serializable

{
    /** Relative path for storage directory
     */
    private static final String DIR = "store" + File.separator;

    /** Filename extension for database files
     */
    private static final String EXT = ".dbf";

    /** Counter for naming temporary tables.
     */
    private static int count = 0;

    /** Table name.
     */
    private final String name;

    /** Array of attribute names.
     */
    private final String [] attribute;

    /** Array of attribute domains: a domain may be
     *  integer types: Long, Integer, Short, Byte
     *  real types: Double, Float
     *  string types: Character, String
     */
    private final Class [] domain;

    /** Collection of tuples (data storage).
     */
    private final List <Comparable []> tuples;

    /** Primary key (the attributes forming). 
     */
    private final String [] key;

    /** Index into tuples (maps key to tuple).
     */
    private final Map <KeyType, Comparable []> index;

    /** The supported map types.
     */
    public enum MapType { NO_MAP, TREE_MAP, HASH_MAP, BPTREE_MAP }

    /** The map type to be used for indices.  Change as needed.
     * PLEASE MODIFY THIS FOR TESTING PURPOSES
     */
    private static final MapType mType = MapType.NO_MAP;

    /************************************************************************************
     * Make a map (index) given the MapType.
     */
    private static Map <KeyType, Comparable []> makeMap ()
    {
        return switch (mType) {
            case NO_MAP      -> null;
            case TREE_MAP    -> new TreeMap <> ();
            case HASH_MAP    -> new HashMap <> ();
            //  case LINHASH_MAP -> new LinHashMap <> (KeyType.class, Comparable [].class);
            case BPTREE_MAP  -> new BpTreeMap <> (KeyType.class, Comparable [].class);
            default          -> null;
        }; // switch
    } // makeMap

    /************************************************************************************
     * Concatenate two arrays of type T to form a new wider array.
     *
     * @see <a href="http://stackoverflow.com/questions/80476/how-to-concatenate-two-arrays-in-java">...</a>
     *
     * @param arr1  the first array
     * @param arr2  the second array
     * @return a wider array containing all the values from arr1 and arr2
     */
    public static <T> T [] concat (T [] arr1, T [] arr2)
    {
        T [] result = Arrays.copyOf (arr1, arr1.length + arr2.length);
        arraycopy (arr2, 0, result, arr1.length, arr2.length);
        return result;
    } // concat

    /************************************************************************************
     * Returns the Map Types used in Table.java
     * Used to determine what select or join operation to use depending on if NO_MAP or a
     * different Map Data Structure is initialized
     *
     * @author Sanjana Arun
     */
    public static MapType getMapType(){
        return mType;
    }
    //-----------------------------------------------------------------------------------
    // Constructors
    //-----------------------------------------------------------------------------------

    /************************************************************************************
     * Construct an empty table from the meta-data specifications.
     *
     * @param _name       the name of the relation
     * @param _attribute  the string containing attributes names
     * @param _domain     the string containing attribute domains (data types)
     * @param _key        the primary key
     */
    public Table (String _name, String [] _attribute, Class [] _domain, String [] _key)
    {
        name      = _name;
        attribute = _attribute;
        domain    = _domain;
        key       = _key;
        tuples    = new ArrayList <> ();
        index     = makeMap ();
        out.println (Arrays.toString (domain));
    } // constructor

    /************************************************************************************
     * Construct a table from the meta-data specifications and data in _tuples list.
     *
     * @param _name       the name of the relation
     * @param _attribute  the string containing attributes names
     * @param _domain     the string containing attribute domains (data types)
     * @param _key        the primary key
     * @param _tuples     the list of tuples containing the data
     */  


    public Table (String _name, String [] _attribute, Class [] _domain, String [] _key,
                  List <Comparable []> _tuples)
    {
        name      = _name;
        attribute = _attribute;
        domain    = _domain;
        key       = _key;
        tuples    = _tuples;
        index     = makeMap ();
    } // constructor

    /************************************************************************************
     * Construct an empty table from the raw string specifications.
     *
     * @param _name       the name of the relation
     * @param attributes  the string containing attributes names
     * @param domains     the string containing attribute domains (data types)
     * @param _key        the primary key
     */
    public Table (String _name, String attributes, String domains, String _key)
    {
        this (_name, attributes.split (" "), findClass (domains.split (" ")), _key.split(" "));

        out.println (STR."DDL> create table \{name} (\{attributes})");
    } // constructor

    //----------------------------------------------------------------------------------
    // Public Methods
    //----------------------------------------------------------------------------------

    /************************************************************************************
     * Project the tuples onto a lower dimension by keeping only the given attributes.
     * Check whether the original key is included in the projection.
     *
     * #usage movie.project ("title year studioNo")
     *
     * @param attributes  the attributes to project onto
     * @return  a table of projected tuples
     */
    public Table project (String attributes)
    {
        out.println ("RA> " + name + ".project (" + attributes + ")");
        var attrs     = attributes.split (" ");
        int[] colPos = match(attrs);
        var colDomain = extractDom (match (attrs), domain);
        var newKey    = (Arrays.asList (attrs).containsAll (Arrays.asList (key))) ? key : attrs;

        List <Comparable []> rows = new ArrayList <> ();


        // check if the given column are valid
        for(int col : colPos){
            if (col == -1){
                out.println(" you are given an invalid attributes please check the attributes");
                return new Table (name + count++, attrs, colDomain, newKey, rows);
            }
        }

        // table to store the resulting project table with its attributes, domain and key
        Table newTable = new Table (name + count++, attrs, colDomain, newKey);
        for(int i = 0 ; i < tuples.size() ; i++){
            // create new tuple with projected column
            var newtuple = new Comparable[colPos.length];
            for(int j = 0 ; j < colPos.length ;j++) {
                int colContent = colPos[j];
                // copy the value of from original tuple to newtuple
                newtuple[j] = tuples.get(i)[colContent];
            }
            KeyType compareR = new KeyType(newtuple);
            // insert newtuple into the newTable if its not in the index
            // eliminates duplicates
            if (newTable.index.get(compareR) == null) {
                newTable.insert(newtuple);
            }
        }

        return newTable;
       // return new Table (name + count++, attrs, colDomain, newKey, rows);
    } // project

    /************************************************************************************
     * Select the tuples satisfying the given predicate (Boolean function).
     *
     * #usage movie.select (t -> t[movie.col("year")].equals (1977))
     *
     * @param predicate  the check condition for tuples
     * @return  a table with tuples satisfying the predicate
     */
    public Table select (Predicate <Comparable []> predicate)
    {
        out.println (STR."RA> \{name}.select (\{predicate})");

        return new Table (name + count++, attribute, domain, key,
                   tuples.stream ().filter (t -> predicate.test (t))
                                   .collect (Collectors.toList ()));
    } // select

    /************************************************************************************
     * Select the tuples satisfying the given simple condition on attributes/constants
     * compared using an <op> ==, !=, <, <=, >, >=.
     *
     * #usage movie.select ("year == 1977")
     *
     * @param condition  the check condition as a string for tuples
     * @return  a table with tuples satisfying the condition
     */
    public Table select (String condition)
    {
        out.println (STR."RA> \{name}.select (\{condition})");

        List <Comparable []> rows = new ArrayList <> ();

        var token = condition.split (" ");
        var colNo = col (token [0]);
        for (var t : tuples) {

            if (satisfies (t, colNo, token [1], token [2])) rows.add (t);
        } // for

        return new Table (name + count++, attribute, domain, key, rows);
    } // select

    /************************************************************************************
     * Does tuple t satify the condition t[colNo] op value where op is ==, !=, <, <=, >, >=?
     *
     * #usage satisfies (t, 1, "<", "1980")
     *
     * @param colNo  the attribute's column number
     * @param op     the comparison operator
     * @param value  the value to compare with (must be converted, String -> domain type)
     * @return  whether the condition is satisfied
     */
    private boolean satisfies (Comparable [] t, int colNo, String op, String value)

    {
        var t_A = t[colNo];
        out.println (STR."satisfies: \{t_A} \{op} \{value}");
        var valt = switch (domain [colNo].getSimpleName ()) {      // type converted

            case "Byte"      -> Byte.valueOf (value);
            case "Character" -> value.charAt (0);
            case "Double"    -> Double.valueOf (value);
            case "Float"     -> Float.valueOf (value);
            case "Integer"   -> Integer.valueOf (value);
            case "Long"      -> Long.valueOf (value);
            case "Short"     -> Short.valueOf (value);
            case "String"    -> value;
            default          -> value;
        }; // switch
        var comp = t_A.compareTo (valt);

        return switch (op) {
        case "==" -> comp == 0;
        case "!=" -> comp != 0;
        case "<"  -> comp <  0;
        case "<=" -> comp <= 0;
        case ">"  -> comp >  0;
        case ">=" -> comp >= 0;
        default   -> false;

        }; // switch
    } // satisfies

    /************************************************************************************
     * Select the tuples satisfying the given key predicate (key = value).  Use an index
     * (Map) to retrieve the tuple with the given key value.  INDEXED SELECT algorithm.
     *
     * @param keyVal  the given key value
     * @return  a table with the tuple satisfying the key predicate
     */
    public Table select (KeyType keyVal)
    {
        out.println (STR."RA> \{name}.select (\{keyVal})");

        List <Comparable []> rows = new ArrayList <> ();

        //Find the tuples associated with the keyVal
        Comparable[] tups = index.get(keyVal);

        if (tups != null) {
            rows.add (tups);
        }

        return new Table (name + count++, attribute, domain, key, rows);
    } // select

    /************************************************************************************
     * Union this table and table2.  Check that the two tables are compatible.
     *
     * #usage movie.union (show)
     *
     * @param table2  the rhs table in the union operation
     * @return  a table representing the union
     */
    public Table union (Table table2)
    {
        //Print out a message to let the users know that the 'Union Operation' is being performed
        System.out.println (STR."RA> \{name}.union (\{table2.name})");

        //Check if the tables are compatible and if not then return a null
        if (! compatible (table2)) return null;

        //Create a new list to store the rows of the resulting union
        List <Comparable []> rows = new ArrayList <> ();


        //Add all the rows from the current (this) table to the new list
        for (Comparable [] row : this.tuples) {
            rows.add (row);
        } //for

        //Add all the rows from table2 to the new list
        for (Comparable [] row2 : table2.tuples) {
            var keyVal = new Comparable [key.length];
            var cols   = match (key);
            for (var j = 0; j < keyVal.length; j++) keyVal [j] = row2 [cols [j]];
            KeyType compareR = new KeyType(keyVal);
            //checks if tuple is not already found in table1
            if (this.index.get(compareR) == null) {
                rows.add (row2);
            } //if
        } //for
        // Create and return a new Table object with the combined rows
        return new Table (name + count++, attribute, domain, key, rows);
    } // union

    /************************************************************************************
     * Take the difference of this table and table2.  Check that the two tables are
     * compatible.
     *
     * #usage movie.minus (show)
     *
     * @param table2  The rhs table in the minus operation
     * @return  a table representing the difference
     */
    public Table minus (Table table2)
    {
        out.println (STR."RA> \{name}.minus (\{table2.name})");
        if (! compatible (table2)) return null;

        List <Comparable []> rows = new ArrayList <> ();

        Map<KeyType, Comparable[]> index = new HashMap<>();

        // loop through table2 and set the index
        for(Comparable[] row2 : table2.tuples){
            var keyVal = new Comparable[key.length];
            var cols = match(key);
            for(var j = 0; j < keyVal.length; j++){
                // keyVal stores the values from the current row from relevant columns
                keyVal [j] = row2[cols [j]];
            }
            KeyType keyType = new KeyType(keyVal);
            // key and row is inserted into a HashMap index
            index.put(keyType, row2);
        }

        // loop through each row, check if the rows are in 'this' but not in table2
        // handling duplicates
        for(Comparable[] row : this.tuples){
            var keyVal = new Comparable[key.length];
            var cols = match(key);
            for(var j = 0; j < keyVal.length; j++){
                keyVal [j] = row[cols [j]];
            }
            KeyType keyType = new KeyType(keyVal);
            if (index.get(keyType) == null){
                rows.add(row);
            }
        }
        return new Table (name + count++, attribute, domain, key, rows);
    } // minus

    /************************************************************************************
     * Join this table and table2 by performing an "equi-join".  Tuples from both tables
     * are compared requiring attributes1 to equal attributes2.  Disambiguate attribute
     * names by appending "2" to the end of any duplicate attribute name.  Implement using
     * a NESTED LOOP JOIN ALGORITHM.
     *
     * #usage movie.join ("studioName", "name", studio)
     *
     * @param attributes1  the attributes of this table to be compared (Foreign Key)
     * @param attributes2  the attributes of table2 to be compared (Primary Key)
     * @param table2       the rhs table in the join operation
     * @return  a table with tuples satisfying the equality predicate
     */
    public Table join (String attributes1, String attributes2, Table table2)
    {
        out.println (STR."RA> \{name}.join (\{attributes1}, \{attributes2}, \{table2.name})");

        var t_attrs = attributes1.split (" ");
        var u_attrs = attributes2.split (" ");
        var rows    = new ArrayList <Comparable []> ();
        for (var t: tuples) {
            for(var j: table2.tuples) {

                if (t[col(attributes1)].equals(j[table2.col(attributes2)])) {
                    rows.add(concat(t,j));
                }
            }
        }

        // Handling disambiguation
        String[] combined_arr_Attributes = concat(attribute, table2.attribute); // Create a combined array of attributes

        for(int i = 0; i<attribute.length; i++)
        {
            for(int j = 0; j < table2.attribute.length; j++)
            {
                if (attribute[i].equals(table2.attribute[j]))
                {
                    combined_arr_Attributes[attribute.length + j] = table2.attribute[j] + "2";
                    table2.attribute[j] += "2";

                }
            }
        }

        return new Table (name + count++, concat (attribute, table2.attribute),
                concat (domain, table2.domain), key, rows);
    } // join

    /************************************************************************************
     * Join this table and table2 by performing a "theta-join".  Tuples from both tables
     * are compared attribute1 <op> attribute2.  Disambiguate attribute names by appending "2"
     * to the end of any duplicate attribute name.  Implement using a Nested Loop Join algorithm.
     *
     * #usage movie.join ("studioName == name", studio)
     *
     * @param condition  the theta join condition
     * @param table2     the rhs table in the join operation
     * @return  a table with tuples satisfying the condition
     */
    public Table join (String condition, Table table2)
    {
        out.println (STR."RA> \{name}.join (\{condition}, \{table2.name})");

        var rows = new ArrayList <Comparable []> ();

        //Next we split the condition into three parts: attr1, attr2 and operator
        String[] conditionParts = condition.split(" ");

        String attr1 = conditionParts[0].trim();
        String operator = conditionParts[1].trim();
        String attr2 = conditionParts[2].trim();

        // We find the index values of the attributes to ensure its location within a tuple
        int index1 = col(attr1);
        int index2 = table2.col(attr2);

        // Nested Loop
        for (var tuple1 : tuples) {
            for (var tuple2 : table2.tuples) {
                //Now we compare attr1 and attr2
                boolean condsatisfied = false;
                switch (operator) {
                    case "==":
                        condsatisfied = tuple1[index1].compareTo(tuple2[index2]) == 0;
                        break;
                    case "!=":
                        condsatisfied = tuple1[index1].compareTo(tuple2[index2]) != 0;
                        break;
                    case "<":
                        condsatisfied = tuple1[index1].compareTo(tuple2[index2]) < 0;
                        break;
                    case ">":
                        condsatisfied = tuple1[index1].compareTo(tuple2[index2]) > 0;
                        break;
                    case "<=":
                        condsatisfied = tuple1[index1].compareTo(tuple2[index2]) <= 0;
                        break;
                    case ">=":
                        condsatisfied = tuple1[index1].compareTo(tuple2[index2]) >= 0;
                        break;
                    default:
                        throw new IllegalArgumentException ();
                }

                if (condsatisfied){
                    // Join the tuples by creating a new array to hold the combined tuple and add the concated result
                    Comparable[] resultTuple = concat(tuple1, tuple2);
                    rows.add (resultTuple);
                }
            }
        }

        // Handling disambiguation
        String[] combined_arr_Attributes = concat(attribute, table2.attribute); // Create a combined array of attributes

        for(int i = 0; i<attribute.length; i++)
        {
            for(int j = 0; j < table2.attribute.length; j++)
            {
                if (attribute[i].equals(table2.attribute[j]))
                {
                    combined_arr_Attributes[attribute.length + j] = table2.attribute[j] + "2";
                }
            }
        }
        // I M P L E M E N T E D

        return new Table (name + count++, concat (attribute, table2.attribute),
                concat (domain, table2.domain), key, rows);
    } // join

    /************************************************************************************
     * Join this table and table2 by performing an "equi-join".  Same as above equi-join,
     * but implemented using an INDEXED JOIN algorithm.
     *
     * @param attributes1  the attributes of this table to be compared (Foreign Key)
     * @param attributes2  the attributes of table2 to be compared (Primary Key)
     * @param table2       the rhs table in the join operation
     * @return  a table with tuples satisfying the equality predicate
     */
    public Table i_join (String attributes1, String attributes2, Table table2)
    {
        out.println("RA> " + name + ".join (" + attributes1 + ", " + attributes2 + ", " + table2.name + ")");

        var t_attrs = attributes1.split(" ");
        var u_attrs = attributes2.split(" ");
        var rows = new ArrayList<Comparable[]>();

        // To check whether primary key and foreign key relationship is satisfied or not
        int count1 = 0;
        for (int i = 0; i < t_attrs.length; i++) {
            if (Arrays.asList(attribute).contains(t_attrs[i])) {
                count1++;
            }
        }
        int count2 = 0;
        for (int i = 0; i < u_attrs.length; i++) {
            if (Arrays.asList(table2.key).contains(u_attrs[i])) {
                count2++;
            }
        }

        // Perform join on valid key types
        if (count1 == t_attrs.length && count2 == u_attrs.length) {
            for (int i = 0; i < tuples.size(); i++) {

                // Getting keyType for foreign key of table1 to comparing with primary key of table2
                KeyType keyTypeTable1 = new KeyType(extract(tuples.get(i), t_attrs));

                // retrieving  table2 tuples that matches primary key with foreign key of table1
                Comparable[] tuplesTable2 = table2.index.get(keyTypeTable1);

                // Null pointer check in case no key matches the condition
                if (tuplesTable2 == null) {
                    continue;
                }

                // Initializing an empty tuple having a size of columns of table1 plus table2
                Comparable[] joinRow = new Comparable[tuples.get(i).length + tuplesTable2.length];

                // Concatenating two arrays - tuple array from table1 and tuple array from
                // table2 to joinRow tuple using ArrayUtil class
                joinRow = concat(tuples.get(i), tuplesTable2);

                // Adding joinRow to rows
                rows.add(joinRow);
            }
        } else {
            System.out.println("ERROR:  you are given an invalid attributes please check the attributes");
        }

        // adding ambiguous column name with 2
        for (int i = 0; i < attribute.length; i++) {
            for (int j = 0; j < table2.attribute.length; j++) {
                if (attribute[i].equals(table2.attribute[j])) {
                    table2.attribute[j] += "2";
                }
            }
        }

        return new Table(name + count++, concat(attribute, table2.attribute),
                concat(domain, table2.domain), key, rows);
    } // i_join

    /************************************************************************************
     * Join this table and table2 by performing an NATURAL JOIN.  Tuples from both tables
     * are compared requiring common attributes to be equal.  The duplicate column is also
     * eliminated.
     *
     * #usage movieStar.join (starsIn)
     *
     * @param table2  the rhs table in the join operation
     * @return  a table with tuples satisfying the equality predicate
     */
    public Table join (Table table2)
    {
        out.println (STR."RA> \{name}.join (\{table2.name})");

        var rows = new ArrayList <Comparable []> ();

        //  NOT USED FOR PROJECT 3

        // FIX - eliminate duplicate columns
        return new Table (name + count++, concat (attribute, table2.attribute),
                                          concat (domain, table2.domain), key, rows);

    } // join

    /************************************************************************************
     * Return the column position for the given attribute name or -1 if not found.
     *
     * @param attr  the given attribute name
     * @return  a column position
     */
    public int col (String attr)
    {
        for (var i = 0; i < attribute.length; i++) {
           if (attr.equals (attribute [i])) return i;

        } // for

        return -1;       // -1 => not found
    } // col

    /************************************************************************************
     * Insert a tuple to the table.
     *
     * #usage movie.insert ("Star_Wars", 1977, 124, "T", "Fox", 12345)
     *
     * @param tup  the array of attribute values forming the tuple
     * @return  the insertion position/index when successful, else -1
     */
    public int insert (Comparable [] tup)
    {
        out.println (STR."DML> insert into \{name} values (\{Arrays.toString (tup)})");

        if (typeCheck (tup)) {
            tuples.add (tup);
            var keyVal = new Comparable [key.length];
            var cols   = match (key);
            for (var j = 0; j < keyVal.length; j++) keyVal [j] = tup [cols [j]];
            if (mType != MapType.NO_MAP) index.put (new KeyType (keyVal), tup);
            return tuples.size () - 1;                             // assumes it is added at the end
        } else {
            return -1;                                             // insert failed
        } // if
    } // insert

    /************************************************************************************
     * Get the tuple at index position i.
     *
     * @param i  the index of the tuple being sought
     * @return  the tuple at index position i
     */
    public Comparable [] get (int i)
    {
        return tuples.get (i);
    } // get

    /************************************************************************************
     * Get the name of the table.
     *
     * @return  the table's name
     */
    public String getName ()
    {
        return name;
    } // getName

    /************************************************************************************
     * Print tuple tup.
     * @param tup  the array of attribute values forming the tuple
     */
    public void printTup (Comparable [] tup)
    {
        out.print ("| ");
        for (var attr : tup) out.printf ("%15s", attr);
        out.println (" |");
    } // printTup

    /************************************************************************************
     * Print this table.
     */
    public void print ()
    {
        out.println (STR."\n Table \{name}");
        out.print ("|-");
        out.print ("---------------".repeat (attribute.length));
        out.println ("-|");
        out.print ("| ");
        for (var a : attribute) out.printf ("%15s", a);
        out.println (" |");
        out.print ("|-");
        out.print ("---------------".repeat (attribute.length));
        out.println ("-|");
        for (var tup : tuples) printTup (tup);
        out.print ("|-");
        out.print ("---------------".repeat (attribute.length));
        out.println ("-|");
    } // print

    /************************************************************************************
     * Print this table's index (Map).
     */
    public void printIndex ()
    {
        out.println (STR."\n Index for \{name}");
        out.println ("-------------------");
        if (mType != MapType.NO_MAP) {
            for (var e : index.entrySet ()) {
                out.println (STR."\{e.getKey ()} -> \{Arrays.toString (e.getValue ())}");
            } // for
        } // if
        out.println ("-------------------");
    } // printIndex

    /************************************************************************************
     * Load the table with the given name into memory. 
     *
     * @param name  the name of the table to load
     */
    public static Table load (String name)
    {
        Table tab = null;
        try {
            ObjectInputStream ois = new ObjectInputStream (new FileInputStream (DIR + name + EXT));
            tab = (Table) ois.readObject ();
            ois.close ();
        } catch (IOException ex) {
            out.println ("load: IO Exception");
            ex.printStackTrace ();
        } catch (ClassNotFoundException ex) {
            out.println ("load: Class Not Found Exception");
            ex.printStackTrace ();
        } // try
        return tab;
    } // load

    /************************************************************************************
     * Save this table in a file.
     */
    public void save ()
    {
        try {
            var oos = new ObjectOutputStream (new FileOutputStream (DIR + name + EXT));
            oos.writeObject (this);
            oos.close ();
        } catch (IOException ex) {
            out.println ("save: IO Exception");
            ex.printStackTrace ();
        } // try
    } // save

    //----------------------------------------------------------------------------------
    // Private Methods
    //----------------------------------------------------------------------------------

    /************************************************************************************
     * Determine whether the two tables (this and table2) are compatible, i.e., have
     * the same number of attributes each with the same corresponding domain.
     *
     * @param table2  the rhs table
     * @return  whether the two tables are compatible
     */
    private boolean compatible (Table table2)
    {
        if (domain.length != table2.domain.length) {
            out.println ("compatible ERROR: table have different arity");
            return false;
        } // if
        for (var j = 0; j < domain.length; j++) {
            if (domain [j] != table2.domain [j]) {
                out.println (STR."compatible ERROR: tables disagree on domain \{j}");
                return false;
            } // if
        } // for
        return true;
    } // compatible

    /************************************************************************************
     * Match the column and attribute names to determine the domains.
     *
     * @param column  the array of column names
     * @return  an array of column index positions
     */
    private int [] match (String [] column)
    {
        int [] colPos = new int [column.length];

        for (var j = 0; j < column.length; j++) {
            var matched = false;
            for (var k = 0; k < attribute.length; k++) {
                if (column [j].equals (attribute [k])) {
                    matched = true;
                    colPos [j] = k;
                } // for
            } // for
            if ( ! matched) out.println (STR."match: domain not found for \{column [j]}");
        } // for

        return colPos;
    } // match

    /************************************************************************************
     * Extract the attributes specified by the column array from tuple t.
     *
     * @param t       the tuple to extract from
     * @param column  the array of column names
     * @return  a smaller tuple extracted from tuple t 
     */
    private Comparable [] extract (Comparable [] t, String [] column)
    {
        var tup    = new Comparable [column.length];
        var colPos = match (column);
        for (var j = 0; j < column.length; j++) tup [j] = t [colPos [j]];
        return tup;
    } // extract

    /************************************************************************************
     * Check the size of the tuple (number of elements in array) as well as the type of
     * each value to ensure it is from the right domain. 
     *
     * @param t  the tuple as a array of attribute values
     * @return  whether the tuple has the right size and values that comply
     *          with the given domains
     */
    private boolean typeCheck (Comparable [] t)
    {
        for(int i = 0 ; i < this.domain.length; i++){
            if(t[i] != null) {
                String tuple_domain = t[i].getClass().getName();
                String attribute_domain = this.domain[i].getName();

                if (!tuple_domain.equals(attribute_domain)) {
                    return false;
                }
            }
        }
        return true;
    } // typeCheck

    /************************************************************************************
     * Find the classes in the "java.lang" package with given names.
     *
     * @param className  the array of class name (e.g., {"Integer", "String"})
     * @return  an array of Java classes
     */
    private static Class [] findClass (String [] className)
    {
        var classArray = new Class [className.length];

        for (var i = 0; i < className.length; i++) {
            try {
                classArray [i] = Class.forName (STR."java.lang.\{className [i]}");
            } catch (ClassNotFoundException ex) {
                out.println (STR."findClass: \{ex}");
            } // try
        } // for

        return classArray;
    } // findClass

    /************************************************************************************
     * Extract the corresponding domains.
     *
     * @param colPos  the column positions to extract.
     * @param group   where to extract from
     * @return  the extracted domains
     */
    private Class [] extractDom (int [] colPos, Class [] group)
    {
        var obj = new Class [colPos.length];

        for (var j = 0; j < colPos.length; j++) {
            obj [j] = group [colPos [j]];
        } // for

        return obj;
    } // extractDom

} // Table

