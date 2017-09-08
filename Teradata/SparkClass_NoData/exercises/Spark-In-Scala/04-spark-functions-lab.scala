////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Basic Spark walkthrough                                                                               //
// This set of code is expected to run on Spark 1.3.1. It has not been tested with 1.5.2 (latest version) //
////////////////////////////////////////////////////////////////////////////////////////////////////////////


////////////////////////////////
// Simple operations in Scala //
////////////////////////////////
1 + 1                                             // Should be Int = 2
1.0 + 1                                           // Should be Double = 2.0
"This is a string"                                // String = This is a string

// allocate a mutable variable using var

var x = 2 + 2
println(x)
x = 3 + 3
println(x)

// but an immutable, which is created using val, can't be changed once it is created

val x = 2 + 2
println(x)
x = 3 + 3
println(x)

// You should see
// <console>:21: error: reassignment to val
//        x = 3 + 3
//          ^




////////////////////////////////////////////
// Function definitions are key for Spark //
////////////////////////////////////////////

def add(x:Int, y:Int):Int = {                     // Takes two Int arguments and returns an Int
  return x + y  
}  
println(add(42,13))  

// Implicit typing and return
def add(x:Int, y:Int) = {                        //result type is inferred   
  x + y                                          //"return" keyword is optional  
}

//Curly braces are optional on single line blocks  
def add(x:Int, y:Int) = x + y  



//////////////////////////////////////////////
// Anonymous Functions or Function Literals //
//////////////////////////////////////////////

// a function with a name greeting

val greeting = (x: String) => "Hello " + x
val names = List("Joe", "Mary", "Barbara")
names.map(greeting)

// should get
//     res2: List[String] = List(Hello Joe, Hello Mary, Hello Barbara)

// Now let's get rid of the name greeting

names.map((x: String) => "Hello " + x)
// should get the same answer:
//    res2: List[String] = List(Hello Joe, Hello Mary, Hello Barbara)


// Define maximize to compute the maximum of two integers using a function literal instead of def
// Function literals are also referred to as anonymous or lambda functions

val maximize = (a: Int, b: Int) => if (a > b) a else b // Two arguments function literal
maximize(5, 3)

// Define doubler as an immutable variable whose value is a function (note we aren't using def)
val doubler = (x: Int) => x * 2                        // This assigns a function literal to doubler
doubler(4)

// OK, now use the _ placeholder for the doubler function argument
// Read this as "doubler is defined as a function literal that takes an integer and
// returns and integer; the function definition value multiplies its argument times 2.

val doubler: (Int) => Int = _ * 2
doubler(4)

// Doubler's value is a function literal. We can now pass this value to other functions that
// take a function as an argument

// Many ways exist to define functions and methods. Here are several

val even = (i: Int) => { i % 2 == 0 } // explicit long form
val even: (Int) => Boolean = i => { i % 2 == 0 }
val even: Int => Boolean = i => ( i % 2 == 0 }
val even: Int => Boolean = i => i % 2 == 0
val even: Int => Boolean = _ % 2 == 0 // _ means first argument

// implicit result approach
val add = (x: Int, y: Int) => { x + y }
val add = (x: Int, y: Int) => x + y

// explicit result approach
val add: (Int, Int) => Int = (x,y) => { x + y }
val add: (Int, Int) => Int = (x,y) => x + y

// Note that all of these defined named functions
// We don't necessarily need the names, even for the arguments

// a named function called greeting

val greeting = (x: String) => "Hello " + x
val names = List("Joe", "Mary", "Barbara")
names.map(greeting)

// should get
//     res2: List[String] = List(Hello Joe, Hello Mary, Hello Barbara)
// Now let's get rid of the name greeting

names.map((x: String) => "Hello " + x)
// should get the same answer:
//    res2: List[String] = List(Hello Joe, Hello Mary, Hello Barbara)

// Because we can infer the types, we can use the underscore shortcut
// for this function definition

names.map("Hello " + _)

// We can also use this for our even number tester
val nums = Array(1, 2, 3, 4, 5)
nums.map(_ % 2 == 0)

// Should produce
// Array[Boolean] = Array(false, true, false, true, false)

// This may be harder to understand; it's a reducing function
// similar to that used in MapReduce

nums.reduceLeft(_ + _)  // first plus second argument




// These literal defintions are incredibly valuable in Spark
// because many of the Spark operations
// take functional arguments. Most of these will never be assigned
// to a variable. For example
// the following finds list entries that have an "f"
// in them using an anonymous function/function literal

val mylist = List("foo", "bar", "fight")
mylist.filter(_.contains("f"))

// We could have written that as follows, but it's longer and less clear

val mylist = List("foo", "bar", "fight")
val ffilter = (s: String) => s.contains("f")      // define a named function to search for f
mylist.filter(ffilter)                            // will generate the same result as previous


////////////////////////////////////
// First full example: Textsearch //
////////////////////////////////////

// load error messages from a log into memory
// then interactively search for various patterns
// base RDD

// log.txt has the following 5 lines
// ERROR	php: dying for unknown reasons
// WARN	dave, are you angry at me?
// ERROR	did mysql just barf?
// WARN	xylons approaching
// ERROR	mysql cluster: replace with spark cluster

val lines = sc.textFile("hdfs:///data/logs/log.txt")

// transformed RDDs
val errors = lines.filter(_.startsWith("ERROR"))
val messages = errors.map(_.split("\t")).map(r => r(1))
messages.cache()

// actions
messages.filter(_.contains("mysql")).count()
messages.filter(_.contains("php")).count()




///////////////////
// Now Wordcount //
///////////////////

// The periods in this dataflow pipeline are at the ends of lines so that we can execute
// this code in the interactive spark-shell without modification

val textFile = sc.textFile("hdfs:///data/shakespeare/input")
val counts = textFile.flatMap(line => line.split(" ")).
                 map(word => (word, 1)).
                 reduceByKey(_ + _)
counts.saveAsTextFile("hdfs:///tmp/shakespeare-wc-scala")



///////////////////////
// Let's estimate Pi //
///////////////////////
val NUM_SAMPLES = 100000
val count = sc.parallelize(1 to NUM_SAMPLES).map{i =>
  val x = Math.random()
  val y = Math.random()
  if (x*x + y*y < 1) 1 else 0
}.reduce(_ + _)
println("Pi is roughly " + 4.0 * count / NUM_SAMPLES)

