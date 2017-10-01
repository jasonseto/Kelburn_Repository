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

// This particular demo will run the output in "complete" mode (i.e., it will process
// the complete data set accumulated since the beginning of time). You may wish to run it in
// "append" or "update" mode as well to reproduce the behavior of the DStreams example. However, at
// present, neither "append" or "update" modes are supported on networked streaming sources.

import org.apache.spark.sql.functions._
import org.apache.spark.sql.SparkSession

// val spark = SparkSession
//   .builder
//  .appName("StructuredNetworkWordCount")
//  .getOrCreate()
  
import spark.implicits._

// Create DataFrame representing the stream of input lines from connection to localhost:9999
val lines = spark.readStream.format("socket").option("host", "localhost").option("port", 9999).load()

// Split the lines into words
val words = lines.as[String].flatMap(_.split(" "))

// Generate running word count
val wordCounts = words.groupBy("value").count()

// Start running the query that prints the running counts to the console
// In theory, changing "complete" to "append" would make this work like the
// original DStreams example, but unfortunately, Spark doesn't yet support
// append mode on network streams as of Spart 2.1.1.

val query = wordCounts.writeStream.
  outputMode("complete").
  format("console")
query.start().awaitTermination()




