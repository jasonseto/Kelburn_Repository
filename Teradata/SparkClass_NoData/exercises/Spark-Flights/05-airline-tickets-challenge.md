# Analyzing Airline Tickets

To exercise our Big Data skills, this lab will analyze a fairly large dataset of 10% of all airline tickets issued in the US during the year 2016. It comes from the US 
Bureau of Transportation Statistics and is divided into three compressed .csv files with headers:

* tickets.csv.bz2: This table contains summary characteristics of each domestic itinerary on the Origin and Destination Survey, including the reporting carrier, itinerary fare, number of passengers, originating airport, roundtrip indicator, and miles flown.
* markets.csv.bz2: This table contains directional market characteristics of each domestic itinerary of the Origin and Destination Survey, such as the reporting carrier, origin and destination airport, prorated market fare, number of market coupons, market miles flown, and carrier change indicators.
* coupons.csv.bz2: This table provides coupon-specific information for each domestic itinerary of the Origin and Destination Survey, such as the operating carrier, origin and destination airports, number of passengers, fare class, coupon type, trip break indicator, and distance.

You can find out more about what's in the schemas by examining the table profiles listed at this location:

[https://www.transtats.bts.gov/tables.asp?DB_ID=125&DB_Name=&DB_Short_Name=](https://www.transtats.bts.gov/tables.asp?DB_ID=125&DB_Name=&DB_Short_Name=)

We'd like to analyze this data to answer a pretty simple question:

### Which Were The 25 Most Expensive Routes To Fly in 2016?
When we say "route", that's an origin and destination pair.

To answer this question, you'll really only need one of these tables, which is `markets`. It's 25 million rows, so you won't be able to analyze it in Excel. 

We'll also give you another file that translates airport ICAO codes (the typical airport abbreviations like BOS for Boston and SAN for San Diego) into City and State. That file is called airports.dat. We'll even give you the code necessary to read in these data sets in all 3 languages. All you have to do is to construct a few joins to get the city names, do a groupBy or two, and order the results.

You may wish to refer to the Spark API documentation for the language you're working in. A good starting point is:

[https://spark.apache.org/docs/latest/](https://spark.apache.org/docs/latest/)

Good luck!


## Scala Code

Here's the file reading code in Scala:

~~~scala
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
~~~

## Python Code

Here's the file reading code in Python:

~~~python
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
~~~

## R Code
Finally here's the code in R:

~~~r
if (nchar(Sys.getenv("SPARK_HOME")) < 1) {
  Sys.setenv(SPARK_HOME = "/vagrant/latest-spark") # or whereever your Spark install lives
}
if (nchar(Sys.getenv("HADOOP_CONF_DIR")) < 1) {
  Sys.setenv(HADOOP_CONF_DIR = "/etc/hadoop/conf")            # or whereever your Hadoop lives
}
if (nchar(Sys.getenv("YARN_CONF_DIR")) < 1) {
  Sys.setenv(YARN_CONF_DIR = "/etc/hadoop/conf")              # or whereever your YARN config lives
}

library(magrittr)
# do not load dplyr or you will have function name conflicts

library(SparkR, lib.loc = c(file.path(Sys.getenv("SPARK_HOME"), "R", "lib")))
sparkR.session(master = "yarn",                                # execution type
               sparkConfig = list(spark.driver.memory = "3g")) # configure driver and executors

# The following is more than 25 million rows; it takes a minute or two
rawmarkets <- read.df("/data/flightdata/tickets/markets.csv.bz2", "csv", 
                   header = "true", 
                   inferSchema = "true", 
                   na.strings = "NA")

rawairports <- read.df("/data/flightdata/tickets/airports.dat", "csv", 
                   header = "true", 
                   inferSchema = "true", 
                   na.strings = "NA")

airports <- rawairports %>% select("IATA", "Name", "City")
markets <- rawmarkets %>% select("QUARTER", "ORIGIN", "ORIGIN_STATE_ABR", 
                                 "DEST", "DEST_STATE_ABR", "TICKET_CARRIER", 
                                 "OPERATING_CARRIER", "PASSENGERS", 
                                 "MARKET_FARE", "MARKET_DISTANCE", 
                                 "MARKET_COUPONS")
cache(markets)

## There's no way to do these joins in R without getting duplicated key columns,
## so we're going to create two copies of the airports data with different
## column names to avoid confusion in the later analysis.

originairports <- airports
names(originairports) <- c("ORIGIN_IATA", "Origin_Name", "Origin_City")
destairports <- airports
names(destairports) <- c("DEST_IATA", "Destination_Name", "Destination_City")
~~~

