// Spark read-tickets

import org.apache.spark.sql.SparkSession

def main(args: Array[String]) {
val spark = SparkSession.
      builder().
      appName("read-tickets").
      config("spark.driver.memory", "3G").
      getOrCreate()

val rawmarkets = spark.read.format("csv").
  option("header", "true").
  csv("/data/tickets/markets.csv.bz2")

val rawairports = spark.read.format("csv").
  option("header", "true").
  csv("/data/tickets/airports.dat")

// Make the table smaller by eliminating columns we aren't going to analyze
val markets = rawmarkets.select("QUARTER", "ORIGIN", "ORIGIN_STATE_ABR", "DEST", "DEST_STATE_ABR",
  "TICKET_CARRIER", "OPERATING_CARRIER", "PASSENGERS", "MARKET_FARE", "MARKET_DISTANCE", "MARKET_COUPONS")
markets.cache()

val airports = rawairports.select("IATA", "Name", "City")
airports.cache()

val originnames = Seq("ORIGIN", "Origin_Name", "Origin_City")
val originairports = airports.toDF(originnames: _*)

val destnames = Seq("DEST", "Destination_Name", "Destination_City")
val destairports = airports.toDF(destnames: _*)

val citymarkets1 = markets.join(originairports, "ORIGIN").join(destairports, "DEST")
val citymarkets2 = citymarkets1.withColumn("ORIGIN_CITYSTATE", concat($"Origin_City", lit(", "), $"ORIGIN_STATE_ABR"))
val citymarkets = citymarkets2.withColumn("DEST_CITYSTATE", concat($"Destination_City", lit(", "), $"DEST_STATE_ABR"))
citymarkets.cache()

// citymarkets should have 25,535,793 rows.

// Figure out average fare in each market for every market with more than 100
// flights per year

val market_fares = citymarkets.groupBy("ORIGIN", "ORIGIN_CITYSTATE").
  agg(round(mean("MARKET_FARE")) as "avg_price", count(lit(1)) as "num_flights").
  filter($"num_flights" >= 100).
  sort(desc("avg_price"))
market_fares.show()

// Compute most expensive routes for routes with more than 100 flights per year

val route_fares = citymarkets.
  groupBy("ORIGIN", "DEST", "ORIGIN_CITYSTATE", "DEST_CITYSTATE").
  agg(round(mean("MARKET_FARE")) as "avg_price",
    count(lit(1)) as "num_flights",
    round(mean("MARKET_COUPONS")) as "mean_coupons").
  filter($"num_flights" >= 100).
  sort(desc("avg_price"))
route_fares.show()

spark.stop()

}
