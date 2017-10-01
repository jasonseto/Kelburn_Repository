## Spark read-tickets

## This import is key; otherwise you will run into 
## issues using non-parallel versions of functions like round and mean

from pyspark.sql import SparkSession
from pyspark.sql.functions import *

spark = SparkSession.builder. \
master("yarn"). \
appName("read-tickets"). \
config("spark.driver.memory", "1g"). \
getOrCreate()

rawmarkets = spark.read.format("csv"). \
option("header", "true"). \
csv("/data/flightdata/tickets/markets.csv.bz2")

rawairports = spark.read.format("csv"). \
option("header", "true"). \
csv("/data/flightdata/tickets/airports.dat")

## Make the table smaller by eliminating columns we aren't going to analyze
markets = rawmarkets.select("QUARTER", "ORIGIN", "ORIGIN_STATE_ABR", "DEST",
"DEST_STATE_ABR", "TICKET_CARRIER", "OPERATING_CARRIER", "PASSENGERS", "MARKET_FARE", "MARKET_DISTANCE", "MARKET_COUPONS")
markets.cache()

airports = rawairports.select("IATA", "Name", "City")
airports.cache()

originairports = airports.toDF("ORIGIN", "Origin_Name", "Origin_City")
destairports = airports.toDF("DEST", "Destination_Name", "Destination_City")

citymarkets1 = markets.join(originairports, "ORIGIN").join(destairports, "DEST")
citymarkets2 = citymarkets1.withColumn("ORIGIN_CITYSTATE", concat(citymarkets1["Origin_City"], lit(", "), citymarkets1["ORIGIN_STATE_ABR"]))
citymarkets = citymarkets2.withColumn("DEST_CITYSTATE", concat(citymarkets2["Destination_City"], lit(", "), citymarkets2["DEST_STATE_ABR"]))
citymarkets.cache()

## citymarkets should have 25,535,793 rows

##  Figure out average fare in each market for every market with more than 100
## flights per year
market_fares = citymarkets.groupBy("ORIGIN", "ORIGIN_CITYSTATE"). \
agg(round(mean(citymarkets.MARKET_FARE.cast("double"))).alias("avg_price"), \
count(lit(1)).alias("num_flights")). \
filter(col("num_flights") >= 100). \
sort(desc("avg_price"))
market_fares.show()

## Compute most expensive routesCompute most expensive routes for routes with more than 100 flights per year
route_fares = citymarkets. \
groupBy("ORIGIN", "DEST", "ORIGIN_CITYSTATE", "DEST_CITYSTATE"). \
agg(round(mean("MARKET_FARE")).alias("avg_price"), \
count(lit(1)).alias("num_flights"), \
round(mean("MARKET_COUPONS")).alias("mean_coupons")). \
filter(col("num_flights") >= 100). \
sort(desc("avg_price"))
route_fares.show()

spark.SparkSession.stop()

