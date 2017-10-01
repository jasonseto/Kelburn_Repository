// In the following code after loading and parsing data, we use the KMeans object to cluster the data into two clusters. 
// The number of desired clusters is passed to the algorithm. We then compute Within Set Sum of Squared Error (WSSSE). 
// You can reduce this error measure by increasing k. In fact the optimal k is usually one where there is 
// an “elbow” in the WSSSE graph.

import org.apache.spark.mllib.clustering.KMeans
import org.apache.spark.mllib.linalg.Vectors

// Load and parse the data
val data = sc.textFile("/data/sparkml-data/kmeans_data.txt")
val parsedData = data.map(s => Vectors.dense(s.split(' ').map(_.toDouble))).cache()


// Cluster the data into two classes using KMeans
val numClusters = 2
val numIterations = 20
val clusters = KMeans.train(parsedData, numClusters, numIterations)

// Evaluate clustering by computing Within Set Sum of Squared Errors
val WSSSE = clusters.computeCost(parsedData)
println("Within Set Sum of Squared Errors = " + WSSSE)
