// Simple Wordcount example using File Input

// If you are running this on the two-node vagrant cluster used for bootcamp,
// you must run this program from the control node because it's the only
// node configured for 2 cores.

// you can run this program using the following command:
// % spark-shell --master local[2] -i 01-Spark-DStreaming-Files.scala
// or you can just copy and paste these lines in after typing spark-shell --master local[2]

import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}

// Create the context
val ssc = new StreamingContext(sc, Seconds(10))

// Create the FileInputDStream on the directory and use the
// stream to count words in new files created
val lines = ssc.textFileStream("/tmp/streaming-input")
val words = lines.flatMap(_.split(" "))
val wordCounts = words.map(x => (x, 1)).reduceByKey(_ + _)
wordCounts.print()

ssc.start()
ssc.awaitTermination()

