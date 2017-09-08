//
// Spark-Wordcount
// This exercise walks through the classic *Wordcount* example in Spark in both Scala and Python.
// Before running this or other examples, you should turn down Spark's default logging. See the Spark-Walkthrough exercise for details.*
// Run the Spark shell
//    $ spark-shell --master yarn-client

//Note the Spark context will already be initialized and available to you as `sc`.

// Read the data from HDFS
// First, let's load in our Shakespeare text and apply a regular expression to split on non-word characters:

val text = sc.textFile("hdfs:///data/shakespeare/input/")
val words = text.flatMap( line => line.split("\\W+") )

// Construct key-value pairs
// Generate the "(word, 1)" key-value pairs:

val kv = words.map( word => (word.toLowerCase(), 1) )

// Group by key and sum
// Use `reduceByKey` to group the data by key and apply the addition operator to add the values:

val totals = kv.reduceByKey( (v1, v2) => v1 + v2  )

// Execute the job and save the results
totals.saveAsTextFile("hdfs:///user/hadoop/spark-wc")
totals.take(10)
// Should be Array[(String, Int)] = Array((pinnace,3), (bone,21), (lug,3), (vailing,3), (bombast,4), (gaping,11), (hem,10), (stinks,1), (forsooth,48), (been,738))

words.max()
// Should be res18: String = zwaggered
// Examine the output on disk
//
// $ hadoop fs -cat spark-wc/part-00000 | head -20
// (pinnace,3)
// (bone,21)
// (lug,3)
// (vailing,3)
// (bombast,4)
// (gaping,11)
// (hem,10)
// (forsooth,48)
// (stinks,1)
// (been,738)
// (fuller,2)
// (jade,16)
// (countervail,3)
// (jove,98)
// (crying,36)
// (breath,238)
// (battering,3)
// (contemptible,5)
// (swain,24)
// (clients,3)

// Note the lack of sorting. Spark does not perform the same merge-sort as MapReduce during its shuffles.

// Data pipeline syntax

// Both Scala and Python allow you to chain function calls making it possible to write both Spark jobs much more compactly:

val text = sc.textFile("hdfs:///data/shakespeare/input/")
val totals = text.flatMap( line => line.split("\\W+") ).map( word => (word.toLowerCase(), 1) ).reduceByKey( (v1, v2) => v1 + v2  )
totals.saveAsTextFile("hdfs:///user/hadoop/spark-wc2")



