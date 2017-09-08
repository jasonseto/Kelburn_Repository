// DStreams Wordcount example
// If you are running this on the two-node vagrant cluster used for bootcamp,
// you must run this program from the control node because it's the only
// node configured for 2 cores.
//
// You should also log into the control node in another window and start up
// a netcat prior to running this program using the following command
// % nc -lk 9999
//
// Once you've done that, you can run this program using the following command:
// % spark-shell --master local[2] -i dstreams-example.scala
// or you can just copy and paste these lines in after typing spark-shell --master local[2]
//
// You need the --master local[2] to ensure that you'll have two cores available;
// One core is needed for the receiver and the other for the DStream.
// If you don't run with two cores, your system will simply hang due to starvation.
//
// Also double check that you are running the proper version of Spark. You may
// have to invoke spark-setup-control.sh again to get your SPARK_HOME set properly
// on your control node.

import org.apache.spark._
import org.apache.spark.streaming._
import org.apache.spark.streaming.StreamingContext._ // not necessary since Spark 1.3

// Create a local StreamingContext with two working thread and batch interval of 1 second.
// The master requires 2 cores to prevent from a starvation scenario.

val ssc = new StreamingContext(sc, Seconds(10))

val lines = ssc.socketTextStream("localhost", 9999)
val words = lines.flatMap(_.split(" "))

// Count each word in each batch
val pairs = words.map(word => (word, 1))
val wordCounts = pairs.reduceByKey(_ + _)

// Print the first ten elements of each RDD generated in this DStream to the console
wordCounts.print()

ssc.start()             // Start the computation
ssc.awaitTermination()  // Wait for the computation to terminate

