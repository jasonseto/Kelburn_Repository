// This file does some examination of the ml-100k data set mostly using
// dataFrames

val user_data = sc.textFile("hdfs:///data/ml-100k/u.user") // read from HDFS by defaul
user_data.first

val user_fields = user_data.map(line => line.split("\\|"))
user_fields.take(1)
val num_users = user_fields.map(fields => fields(0)).count
val num_genders = user_fields.map(fields => fields(2)).distinct.count
val num_occupations = user_fields.map(fields => fields(3)).distinct.count
val num_zipcodes = user_fields.map(fields => fields(4)).distinct.count
println("Users: %d, genders: %d, occupations: %d, ZIP codes: %d".format(num_users, num_genders, num_occupations, num_zipcodes))

val count_by_occupation = user_fields.map(fields => (fields(3), 1)).reduceByKey(_+_).collect()

// or you can write this more simply if we are OK with getting a Map object back instead of a Tuple

val count_by_occupation2 = user_fields.map(fields => fields(3)).countByValue

// We can do this in a more SQL-like way by using a class object for the schema and
// making this into a dataframe

case class Occ(userid: Int, age: Int, gender: String, occupation: String, zip: String)


val user_data = sc.textFile("hdfs:///data/ml-100k/u.user") // read from HDFS by defaul
user_data.first
val user_fields = user_data.map(line => line.split("\\|"))
user_fields.take(5)
val users_array = user_fields.map(p => Occ(p(0).toInt, p(1).toInt, p(2), p(3), p(4)))
val users_df = users_array.toDF()
users_df.show
val occupation_count = users_df.groupBy("occupation").count.sort(desc("count"))
occupation_count.show


// We define this function because the date field has some incorrectly formatted years
def convert_year(x: String): Int = {
  try {
    x.takeRight(4).toInt
  } catch {
    case e: Exception => 1900
  }
}

// OK, now read in the movie info file and count the years they were created in

val movie_data = sc.textFile("hdfs:///data/ml-100k/u.item")
val movie_fields = movie_data.map(lines => lines.split("\\|"))
val years = movie_fields.map(fields => fields(2)).map(y => convert_year(y))
val years_filtered = years.filter(y => y != 1900)
val years_df = years_filtered.toDF()
val year_count = years_df.groupBy("value").count.sort(desc("count"))


// input format MovieID|Title|Genres
case class Movie(movieid: Int, title: String)

// function to parse movie record into Movie class
def parseMovie(str: String): Movie = {
      val fields = str.split("\\|")
      Movie(fields(0).toInt, fields(1))
 }

// Let's join the movie file in so we can get titles

val movies_df = sc.textFile("hdfs:///data/ml-100k/u.item").map(parseMovie).toDF()

// Alternative way of reading in movies_df

import org.apache.spark.sql.types._

val schema = new StructType().
  add($"movieid".long.copy(nullable = false)).
  add($"title".string)

val movies_df2 = spark.read.
    schema(schema).
    option("sep", "|").
    csv("/data/ml-100k/u.item")

// Same deal for ratings now

import org.apache.spark.sql.functions._
import org.apache.spark.mllib.recommendation.{ALS, MatrixFactorizationModel, Rating}

case class RatingObj(userid: Int, itemid: Int, rating: Int, timestamp: String)
val ratings_data = sc.textFile("hdfs:///data/ml-100k/u.data")
val ratings_fields = ratings_data.map(line => line.split("\\t"))
val ratings_array = ratings_fields.map(p => RatingObj(p(0).toInt, p(1).toInt, p(2).toInt, p(3)))
val ratings_df = ratings_array.toDF()
ratings_data.count
ratings_data.take(10)

// Alternative read of the data

val ratingsschema = new StructType().
  add($"userid".long.copy(nullable = false)).
  add($"itemid".long.copy(nullable = false)).
  add($"rating".long).
  add($"timestamp".string)

val ratings_df2 = spark.read.
    schema(ratingsschema).
    option("sep", "\t").
    csv("/data/ml-100k/u.data")


val ratings_means = ratings_df.groupBy("itemid").
  agg(avg(ratings_df("rating")).alias("avg_rating"))

ratings_means.orderBy(desc("avg_rating")).show

ratings_mean.show

val ratings_with_titleDF = ratings_df.
  join(movies_df, ratings_df("itemid") === movies_df("movieID")).
  cache
ratings_with_titleDF.
  groupBy($"title").
  agg(avg($"rating").alias("avg_rating")).
  orderBy(desc("avg_rating")).show(25)

// +--------------------+------------------+                                       
// |               title|        avg_rating|
// +--------------------+------------------+
// |Great Day in Harl...|               5.0|
// |  Prefontaine (1997)|               5.0|
// |     Star Kid (1997)|               5.0|
// |Saint of Fort Was...|               5.0|
// |Aiqing wansui (1994)|               5.0|
// |Marlene Dietrich:...|               5.0|
// |Entertaining Ange...|               5.0|
// |Santa with Muscle...|               5.0|
// |They Made Me a Cr...|               5.0|
// |Someone Else's Am...|               5.0|
// |Pather Panchali (...|             4.625|
// |Maya Lin: A Stron...|               4.5|
// |Some Mother's Son...|               4.5|
// |         Anna (1996)|               4.5|
// |      Everest (1998)|               4.5|
// |Close Shave, A (1...| 4.491071428571429|
// |Schindler's List ...| 4.466442953020135|
// |Wrong Trousers, T...| 4.466101694915254|
// |   Casablanca (1942)|  4.45679012345679|
// |Wallace & Gromit:...| 4.447761194029851|
// |Shawshank Redempt...| 4.445229681978798|
// |  Rear Window (1954)|4.3875598086124405|
// |Usual Suspects, T...| 4.385767790262173|
// |    Star Wars (1977)|4.3584905660377355|
// | 12 Angry Men (1957)|             4.344|
// +--------------------+------------------+

// Conclusion: the users rating these movies have terrible taste
// Star Kid rates higher than Casablanca?
//
// Maybe some of the movies weren't rated many times, allowing them to have
// surprisingly high ratings

ratings_with_titleDF.
  groupBy($"title").
  agg(avg($"rating").alias("avg_rating"),
      count($"rating").alias("n")).
  orderBy(desc("avg_rating")).show(25)

// +--------------------+-----+-----------------+                                  
// |               title|count|       meanrating|
// +--------------------+-----+-----------------+
// |They Made Me a Cr...|    1|              5.0|
// |Someone Else's Am...|    1|              5.0|
// |Marlene Dietrich:...|    1|              5.0|
// |  Prefontaine (1997)|    3|              5.0|
// |Saint of Fort Was...|    2|              5.0|
// |Santa with Muscle...|    2|              5.0|
// |Great Day in Harl...|    1|              5.0|
// |Aiqing wansui (1994)|    1|              5.0|
// |     Star Kid (1997)|    3|              5.0|
// |Entertaining Ange...|    1|              5.0|
// |Pather Panchali (...|    8|            4.625|
// |      Everest (1998)|    2|              4.5|
// |Maya Lin: A Stron...|    4|              4.5|
// |         Anna (1996)|    2|              4.5|
// |Some Mother's Son...|    2|              4.5|
// |Close Shave, A (1...|  112|4.491071428571429|
// |Schindler's List ...|  298|4.466442953020135|
// |Wrong Trousers, T...|  118|4.466101694915254|
// |   Casablanca (1942)|  243| 4.45679012345679|
// |Wallace & Gromit:...|   67|4.447761194029851|
// +--------------------+-----+-----------------+
// only showing top 20 rows
//
// OK, I feel better now

// The following SQL works too

sql("""select first(title), 
              avg(rating) as avg_rating, 
              count(itemid) from ratings 
        group by itemid, movieid, title 
	     order by avg_rating DESC""").show

// So for our final computation, let's look at the highest rated
// movies that have more than 100 ratings.

ratings_with_titleDF.
  groupBy($"title").
  agg(round(avg($"rating"),2).alias("avg_rating"),
      count($"rating").alias("n")).
  orderBy(desc("avg_rating")).where("n >= 200").show(10)


// Finally, let's register all these dataframes as global tables

users_df.write.saveAsTable("users")
movies_df.write.saveAsTable("movies")
ratings_df.write.saveAsTable("ratings")
