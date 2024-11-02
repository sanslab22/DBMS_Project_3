
# Project 3 - Performance Comparison

The project implements relational database tables with attribute names, domains and a list of tuples to be inserted into the database. The five basic relational algebra operators, project, select, union, minus, and join (natural, equi, and theta), are provided as part of the Table class. Insert, data manipulation, and private  methods that help with the overall functionality of the database are implemented. The main objective of this project is to implement a Tuple Generator that generates large amount of tuples and inserts it into a Table. Then, using the test cases, the performance of 2 select and join operators are to be tested for NO_MAP, TREE_MAP, HASH_MAP, and BPTREE_MAP.

## Key Features

- **Objective**
  
    Implement tuples generator and test cases to anlyze the performance of different mapping data structures.  

- **User Roles**
  
    Catering to individuals that interested in learning the relational algebra operators select and join and how their performance varies based on the data structure used to implement it.
  
- **Key Goals**

    - Improve overall efficieny of relational algebra operators
    - Speed up the implementation of the relational algebra operators
    - Provide a well-structured representation of a database 
    - Facilitate the learning and understanding of different mapping data structures and their functionalities.
    - Performance comparison of the Maps depenidng on the number of tuples inserted into the table

## Authors

    - Sanjana Arun
    - Mukta Deshmukh
    - Tristan Dominy
    - Fidel Arroyo
    - Madhu Sudhan Reddy Chencharapu

## Prerequisites

Before you begin, ensure you have met the following requirements:

- You have installed the latest version of Java (Java 22)
- Run the project using Java SDK 22 with the language level `22 (Preview)`

## Technologies Used

- Java 22
- GitHub
- IDE: IntelliJ and Visual Studio Code

## Execution

1. You can download the Zip file and extract it
2. Open the project in you preferred IDE with the root folder named `Arun_Project3`
3. Modify the MapType used for testing in `Table.java`
      `private static final MapType mType = MapType.<INSERT MAP TYPE>;`
4. Navigate to `TestTupleGenerator.java` and modify the number of tuples to be generated
       `private static int numTuples = 10000;`
       `var tups   = new int [] { numTuples, numTuples, numTuples, numTuples, numTuples };`

6. Run `TestTupleGenerator.java` which contain the main method
7. Run the project and check the terminal to ensure that the project is up and running successfully



## Contribution

This project is a collaborative effor involving a team of 5 members, including the manager.

Please refer to the file `Manger_Report.pdf`. for details on each member's contributions.

## Documentation

Please refer to the documentation file named `Documentation.pdf`.

## Graph Analysis

Please refer to the graph analysis file named `graph_analysis.pdf`.
