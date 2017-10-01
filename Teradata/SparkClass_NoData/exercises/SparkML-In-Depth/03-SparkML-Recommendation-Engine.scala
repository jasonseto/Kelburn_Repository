/////////////////////////////////////////////////////////////////////
// Create a recommendations engine using Alternating Least Squares //
/////////////////////////////////////////////////////////////////////

import org.apache.spark.sql.functions._
// Import Spark SQL data types 
import org.apache.spark.sql._
// Import mllib recommendation data types 
import org.apache.spark.mllib.recommendation.{ALS, MatrixFactorizationModel, Rating}
import org.apache.spark.util.random

// input format MovieID|Title|Genres
case class Movie(movieid: Int, title: String)

// input format is UserID|Gender|Age|Occupation|Zip-code
case class User(userid: Int,  age: Int, gender: String, occupation: String, zip: String)

// function to parse movie record into Movie class
def parseMovie(str: String): Movie = {
      val fields = str.split("\\|")
      Movie(fields(0).toInt, fields(1))
 }
// function to parse input into User class
def parseUser(str: String): User = {
      val fields = str.split("\\|")
      assert(fields.size == 5)
      User(fields(0).toInt, fields(1).toInt, fields(2).toString, fields(3).toString, fields(4).toString)
 }

// function to parse input UserID\tMovieID\tRating
//  Into org.apache.spark.mllib.recommendation.Rating class
def parseRating(str: String): Rating= {
      val fields = str.split("\\t")
      Rating(fields(0).toInt, fields(1).toInt, fields(2).toDouble)
}

/////////////////////
// Ingest the data //
/////////////////////

// OK, now read in the user and movie info files
val users = sc.textFile("hdfs:///data/ml-100k/u.user").map(parseUser)
val usersDF = sc.textFile("hdfs:///data/ml-100k/u.user").map(parseUser).toDF()

val movies = sc.textFile("/data/ml-100k/u.item")
val movieTitles = movies.map(line => line.split("\\|").take(2)).map(array => (array(0).toInt, array(1))).collectAsMap()
val moviesDF = movies.map(parseMovie).toDF()

val fileratings = sc.textFile("hdfs:///data/ml-100k/u.data").map(parseRating)
val myratings = sc.textFile("file:/mnt/sparkclass/exercises/SparkML-In-Depth/personalRatings.txt").map(parseRating)
val ratings = fileratings
val ratingsDF = fileratings.toDF()

// register the DataFrames as a temp table for SQL queries
ratingsDF.registerTempTable("ratings")
moviesDF.registerTempTable("movies")
usersDF.registerTempTable("users")


////////////////////////////////////////
// Let's see what the data looks like //
////////////////////////////////////////

// We'll read the tables back in on general principles

val usersDF = spark.read.table("users")
val moviesDF = spark.read.table("movies")
val ratingsDF = spark.read.table("ratings")


// Look at the top 10 rows of usersDF
usersDF.show(10)

// You should see something like this. However,
// because we can't guarantee the order in which the rows
// were written out, you may see a different set of rows.
// +------+---+------+-------------+-----+
// |userid|age|gender|   occupation|  zip|
// +------+---+------+-------------+-----+
// |     1| 24|     M|   technician|85711|
// |     2| 53|     F|        other|94043|
// |     3| 23|     M|       writer|32067|
// |     4| 24|     M|   technician|43537|
// |     5| 33|     F|        other|15213|
// |     6| 42|     M|    executive|98101|
// |     7| 57|     M|administrator|91344|
// |     8| 36|     M|administrator| 5201|
// |     9| 29|     M|      student| 1002|
// |    10| 53|     M|       lawyer|90703|
// +------+---+------+-------------+-----+
// only showing top 10 rows

// Same for movies
moviesDF.show(10)
// +-------+--------------------+
// |movieid|               title|
// +-------+--------------------+
// |      1|    Toy Story (1995)|
// |      2|    GoldenEye (1995)|
// |      3|   Four Rooms (1995)|
// |      4|   Get Shorty (1995)|
// |      5|      Copycat (1995)|
// |      6|Shanghai Triad (Y...|
// |      7|Twelve Monkeys (1...|
// |      8|         Babe (1995)|
// |      9|Dead Man Walking ...|
// |     10|  Richard III (1995)|
// +-------+--------------------+
// only showing top 10 rows

// and ratings
ratingsDF.show(10)
// +----+-------+------+
// |user|product|rating|
// +----+-------+------+
// | 196|    242|   3.0|
// | 186|    302|   3.0|
// |  22|    377|   1.0|
// | 244|     51|   2.0|
// | 166|    346|   1.0|
// | 298|    474|   4.0|
// | 115|    265|   2.0|
// | 253|    465|   5.0|
// | 305|    451|   3.0|
// |   6|     86|   3.0|
// +----+-------+------+
// only showing top 10 rows

// Let's do some simple queries both without and with SQL
// Let's find Star Wars in the movies DataFrame

moviesDF.filter(moviesDF("title").contains("Star Wars")).show

// You could also do this by saying
sql("""SELECT movieid, title FROM movies 
WHERE title LIKE 'Star Wars%'""").show

// You should see this
// +-------+----------------+
// |movieid|           title|
// +-------+----------------+
// |     50|Star Wars (1977)|
// +-------+----------------+

// Let's join together ratings and movie titles so we can understand them better
val ratings_with_titleDF = ratingsDF.join(moviesDF, ratingsDF("product") === moviesDF("movieid"))
ratings_with_titleDF.show(5)

// See ratings for Star Wars now
val starwars = ratings_with_titleDF.
  filter($"title".contains("Star Wars"))
starwars.show(5)

// Let's go one step further and join in the user info
// Again, we'll look for Star Wars
val denorm_ratingsDF = ratings_with_titleDF.join(usersDF, ratings_with_titleDF("user") === usersDF("userid"))
val starwars = denorm_ratingsDF.filter(denorm_ratingsDF("title").contains("Star Wars"))
starwars.show(5)
starwars.persist

// So let's compare the ratings of Star Wars by women versus men
starwars.groupBy("gender").agg(avg($"rating")).show

// Could also do this by
starwars.registerTempTable("starwars")
sql("SELECT gender, AVG(rating) FROM starwars GROUP BY gender").show

// Both yield
// +------+-----------------+                                                      
// |gender|      avg(rating)|
// +------+-----------------+
// |     F|4.245033112582782|
// |     M|4.398148148148148|
// +------+-----------------+

// check the results against the total set of movies for sanity
denorm_ratingsDF.groupBy("gender").agg(avg($"rating")).show // compare with total set

// Get the max, min ratings along with the count of users who have rated a movie. 
val results = sql("SELECT movies.title, movierates.maxr, movierates.minr, movierates.cntu FROM (SELECT ratings.product, max(ratings.rating) AS maxr, min(ratings.rating) AS minr, COUNT(DISTINCT user) AS cntu FROM ratings group BY ratings.product ) movierates JOIN movies ON movierates.product=movies.movieid ORDER BY movierates.cntu DESC")
results.show(5)

// Show the top 5 most-active users and how many times they rated a movie
val mostActiveUsersSchemaRDD = sql("SELECT ratings.user, count(*) AS ct FROM ratings GROUP BY ratings.user ORDER BY ct DESC LIMIT 5")
println(mostActiveUsersSchemaRDD.collect().mkString("\n"))

// Find the movies that user 92 rated higher than 4 
val results = sql("SELECT ratings.user, ratings.product, ratings.rating, movies.title FROM ratings JOIN movies ON movies.movieid=ratings.product WHERE ratings.user=92 AND ratings.rating > 4")
results.show(5)

// Import mllib recommendation data types 
import org.apache.spark.mllib.recommendation.{ALS, MatrixFactorizationModel, Rating}
import org.apache.spark.util.random

//////////////////////////////////////
// Time to try some recommendations //
//////////////////////////////////////

// Moving on now to doing some machine learning
// Now that we have ratings, movies, and users, we can train a recommendation engine
// Set up training and test data sets

// Randomly split ratings RDD into training data RDD (80%) and test data RDD (20%)
// We are going to add in our myratings to get some personal recommendations for us

val splits = ratings.randomSplit(Array(0.8, 0.2), 0L)
val trainingRDD = splits(0).cache()
val testRatingsRDD = splits(1).cache()

// now add our ratings to the training set
val trainingRatingsRDD = trainingRDD.union(myratings).cache()

 val numTraining = trainingRatingsRDD.count()
 val numTest = testRatingsRDD.count()
 println(s"Training: $numTraining, test: $numTest.")
//Training: 80018, test: 19993.


// Set up the parameters for training

val rank = 5             // number of hidden features in approximation matrices
val iterations = 10       // iterations of model to run
val lambda = 0.01         // tunable parameter controlling regularization and fitting 

val model = ALS.train(trainingRatingsRDD, rank, iterations, lambda)

// The model is an object of MatrixFactorization type. It contains two
// objects: userFeatures and productFeatures. These correspond to the two
// matrices whose dot product generates the full ratings matrix

// We'll test it by using it on our ratings (user 0)
val user0ratings = trainingRatingsRDD.filter(rating => rating.user === 0)
val user0ratingsDF = user0ratings.toDF()
// these are the user 0 ratings used to train the model
user0ratingsDF.join(moviesDF,
  user0ratingsDF("product") === moviesDF("movieid")).show

// We now have a model built
// Let's look at predictions for a given user.
// We'll try user 0 that we showed above
val topRecForUser0 = model.recommendProducts(0, 3)
topRecForUser0.map(rating => (movieTitles(rating.product), rating.rating)).foreach(println)

val user1ratings = trainingRatingsRDD.filter(rating => rating.user == 1)
val user1ratingsDF = user1ratings.toDF()
user1ratingsDF.join(moviesDF,
  user1ratingsDF("product") === moviesDF("movieid")).show

val topRecForUser1 = model.recommendProducts(1, 5)
topRecForUser1.map(rating => (movieTitles(rating.product), rating.rating)).foreach(println)


// Now let's try to evaluate this model. How well does it perform?
// To do this, we'll have it do predictions for the test data set
// Once we have a set of predictions, we'll then compare those with
// the actual ratings using a Mean Absolute Error (MAE) function


// use a case class to get user product pair from testRatingsRDD
// generates a simple pair of just user and product

val testUserProductRDD = testRatingsRDD.map { 
  case Rating(user, product, rating) => (user, product)
}

// get predicted ratings to compare to test ratings
// remember that testRatingsRDD is our test sample of all our data

val predictionsForTestRDD  = model.predict(testUserProductRDD)
predictionsForTestRDD.take(5).mkString("\n")


// prepare  predictions for comparison
val predictionsKeyedByUserProductRDD = predictionsForTestRDD.map{ 
  case Rating(user, product, rating) => ((user, product), rating)
}
// prepare  test for comparison
val testKeyedByUserProductRDD = testRatingsRDD.map{ 
  case Rating(user, product, rating) => ((user, product), rating) 
}

//Join the  test with  predictions
val testAndPredictionsJoinedRDD = testKeyedByUserProductRDD.join(predictionsKeyedByUserProductRDD)

// print the (user, product),( test rating, predicted rating)
testAndPredictionsJoinedRDD.take(5).mkString("\n")

val falsePositives =(testAndPredictionsJoinedRDD.filter{
  case ((user, product), (ratingT, ratingP)) => (ratingT <= 1 && ratingP >=4) 
  })
falsePositives.take(3)
falsePositives.count

//Evaluate the model using Mean Absolute Error (MAE) between test and predictions
val meanAbsoluteError = testAndPredictionsJoinedRDD.map { 
  case ((user, product), (testRating, predRating)) => 
    val err = (testRating - predRating)
    Math.abs(err)
}.mean() 
println("Mean Absolute Error = " + meanAbsoluteError)

//We can also calculate the Mean Squared Error (MSE) equally easily

val MSE = testAndPredictionsJoinedRDD.map{
    case ((user, product), (actual, predicted)) =>
    math.pow((actual - predicted), 2)
}.reduce(_ + _) / testAndPredictionsJoinedRDD.count
println("Mean Squared Error = " + MSE)

// Can we improve this? What if we did more interations? More factors? Different lambda?
// Who can come up with the best MAE?



