############################################################
## Basic SparkSQL walkthrough                             //
## This set of code is expected to run on SparkSQL 2.2.0  //
############################################################

## This vals is already set up in spark-shell
## sc: SparkContext  ## An existing SparkContext.
## spark: SparkSession

df = spark.read.json("/data/spark-resources-data/people.json")

## Displays the content of the DataFrame to stdout
df.show()

## Print the schema in a tree format
df.printSchema()
## root
## |-- age: long (nullable = true)
## |-- name: string (nullable = true)

## Select only the "name" column
df.select("name").show()
## name
## Michael
## Andy
## Justin

## Select everybody, but increment the age by 1
df.select(df.name, df.age + 1).show()
## name    (age + 1)
## Michael null
## Andy    31
## Justin  20

## Select people older than 21
df.filter(df.age > 21).show()
## age name
## 30  Andy

## Count people by age
df.groupBy("age").count().show()
## age  count
## null 1
## 19   1
## 30   1

##//////////////////////////////////////////////
## Let's read a table from the Hive metastore //
##//////////////////////////////////////////////

## If you ran the Hive-Tables2 and Hive-LoadingData queries in exercises, you should
## have a Hive table called stocks which has a lot of data loaded into 4 partitions
## Let's read that one

## Remember that you probably read that into a database with your name on it (DB),
## so we have to change to that DB.
## Here, I'm just using default.


## Existing Spark session is assumed to be in spark
spark.sql("USE default")           ## Select my database

stocks = spark.sql("SELECT * FROM STOCKS")
stocks.show()  ## Will only show the first 20 rows
stocks.count() ## How many rows? Should be 40,547


## This is one way to create a s3flights dataframe. I don't recommend it.
## val creates3flightsSQL = "CREATE EXTERNAL TABLE IF NOT EXISTS s3flights (  Year INT,  Quarter INT,  Month INT,  DayofMonth INT,  DayOfWeek INT,  FlightDate STRING,  UniqueCarrier STRING,  AirlineID INT,  Carrier STRING,  TailNum STRING,  FlightNum INT,  OriginAirportID INT,  OriginAirportSeqID INT,  OriginCityMarketID INT,  Origin STRING,  OriginCityName STRING,  OriginState STRING,  OriginStateFips INT,  OriginStateName STRING,  OriginWac INT,  DestAirportID INT,  DestAirportSeqID INT,  DestCityMarketID INT,  Dest STRING,  DestCityName STRING,  DestState STRING,  DestStateFips INT,  DestStateName STRING,  DestWac INT,  CRSDepTime INT,  DepTime INT,  DepDelay INT,  DepDelayMinutes INT,  DepDel15 INT,  DepartureDelayGroups INT,  DepTimeBlk STRING,  TaxiOut INT,  WheelsOff INT,  WheelsOn INT,  TaxiIn INT,  CRSArrTime INT,  ArrTime INT,  ArrDelay INT,  ArrDelayMinutes INT,  ArrDel15 INT,  ArrivalDelayGroups INT,  ArrTimeBlk STRING,  Cancelled TINYINT,  CancellationCode STRING,  Diverted TINYINT,  CRSElapsedTime INT,  ActualElapsedTime INT,  AirTime INT,  Flights INT,  Distance INT,  DistanceGroup INT,  CarrierDelay INT,  WeatherDelay INT,  NASDelay INT,  SecurityDelay INT,  LateAircraftDelay INT,  FirstDepTime INT,  TotalAddGTime INT,  LongestAddGTime INT,  DivAirportLandings INT,  DivReachedDest INT,  DivActualElapsedTime INT,  DivArrDelay INT,  DivDistance INT,  Div1Airport STRING,  Div1AirportID INT,  Div1AirportSeqID INT,  Div1WheelsOn INT,  Div1TotalGTime INT,  Div1LongestGTime INT,  Div1WheelsOff INT,  Div1TailNum STRING,  Div2Airport STRING,  Div2AirportID INT,  Div2AirportSeqID INT,  Div2WheelsOn INT,  Div2TotalGTime INT,  Div2LongestGTime INT,  Div2WheelsOff INT,  Div2TailNum STRING,  Div3Airport STRING,  Div3AirportID INT,  Div3AirportSeqID INT,  Div3WheelsOn INT,  Div3TotalGTime INT,  Div3LongestGTime INT,  Div3WheelsOff INT,  Div3TailNum STRING,  Div4Airport STRING,  Div4AirportID INT,  Div4AirportSeqID INT,  Div4WheelsOn INT,  Div4TotalGTime INT,  Div4LongestGTime INT,  Div4WheelsOff INT,  Div4TailNum STRING,  Div5Airport STRING,  Div5AirportID INT,  Div5AirportSeqID INT,  Div5WheelsOn INT,  Div5TotalGTime INT,  Div5LongestGTime INT,  Div5WheelsOff INT,  Div5TailNum STRING ) STORED AS PARQUET LOCATION 's3://think.big.academy.aws/ontime/parquet'"
##val s3flights = hiveContext.sql(creates3flightsSQL)

## The reason I don't recommend this approach is that SQL
## requires us to specify all the columns and their types.
## That information is already in the parquet file,
## so it seems most redundant.

## We can take advantage of SparkSQL's intelligence by simply
## loading the parquet file as a dataframe and letting it infer the
## table schema.

## We happen to already have this file in a slightly trimmed down form in HDFS. It's at `hdfs:///data/flightdata/parquet-trimmed`.

s3flights = spark.read.parquet("hdfs:///data/flightdata/parquet-trimmed")
## Now let's select only some of the fields. Further, we're going to
## filter it to only be January of 2013. Note that the use of a
## data pipeline allows Spark to lazily evaluate s3flights
## and only pull those records that are of interest into memory.

## Note that because this is a dataframe,
## our filter tests MUST use === for equality, not ==
## The good news: this runs pretty fast, much faster than the SQL equivalent.

flights = s3flights.select("year", "month", "dayofmonth", \
"carrier", "tailnum","actualelapsedtime", \
"origin", "dest", "deptime", "arrdelayminutes"). \
filter(s3flights.year == 2013)
flights.show()
flights.printSchema()

## You should see the following
## root
##  |-- year: integer (nullable = true)
##  |-- month: integer (nullable = true)
##  |-- dayofmonth: integer (nullable = true)
##  |-- carrier: string (nullable = true)
##  |-- tailnum: string (nullable = true)
##  |-- actualelapsedtime: integer (nullable = true)
##  |-- origin: string (nullable = true)
##  |-- dest: string (nullable = true)
##  |-- deptime: integer (nullable = true)
##  |-- arrdelayminutes: integer (nullable = true)

## Now let's do some simple operations on this DataFrame

flights.groupBy("carrier").count().show()

## You should see something like this
## +-------+-------+                                                               
## |carrier|  count|
## +-------+-------+
## |     AA| 537891|
## |     HA|  72286|
## |     AS| 154743|
## |     B6| 241777|
## |     UA| 505798|
## |     US| 412373|
## |     OO| 626359|
## |     VX|  57133|
## |     WN|1130704|
## |     DL| 754670|
## |     EV| 748696|
## |     F9|  75612|
## |     9E| 296701|
## |     YV| 140922|
## |     FL| 173952|
## |     MQ| 439865|
## +-------+-------+

## Let's now compute average delay by carrier and destination

flights.groupBy("carrier", "dest").mean("arrdelayminutes").show()

## +-------+----+--------------------+                                             
## |carrier|dest|avg(arrdelayminutes)|
## +-------+----+--------------------+
## |     DL| MKE|   9.809099526066351|
## |     DL| OGG|  10.538461538461538|
## |     F9| PHX|  18.697253851306094|
## |     AA| IND|  14.449148167268973|
## |     AA| DTW|   12.57840616966581|
## |     UA| RNO|  10.562189054726367|
## |     WN| SJC|   9.942645044408113|
## |     MQ| GSO|   12.31949934123847|
## |     OO| LEX|  14.217391304347826|
## |     YV| LAS|                1.25|
## |     DL| ABQ|    8.49182763744428|
## |     DL| MDW|   9.127437641723356|
## |     EV| MTJ|  15.106382978723405|
## |     EV| MGM|  13.201232777374909|
## |     MQ| GSP|   14.62779397473275|
## |     AA| IAH|   13.26107425636636|
## |     VX| FLL|   7.720149253731344|
## |     MQ| CHA|  13.570500927643785|
## |     YV| BHM|  13.418788410886743|
## |     HA| SFO|  11.093406593406593|
## +-------+----+--------------------+
##
## We are only seeing the top 20 rows, by the way, because that's what
## show does

delays = flights.groupBy("carrier", "dest", "origin").mean("arrdelayminutes")
delays.printSchema()

## Note that we've created a new column named avg(arrdelayminutes)
## Let's sort this to show the worst January flights in terms of average
## delays in 2013

delays.sort(desc("avg(arrdelayminutes)")).show()

## Looks a bit like this
## +-------+----+------+--------------------+                                      
## |carrier|dest|origin|avg(arrdelayminutes)|
## +-------+----+------+--------------------+
## |     EV| EYW|   MIA|               375.0|
## |     EV| GPT|   MSY|               315.0|
## |     UA| DEN|   MSN|               285.0|
##
## Worst delays were between Key West and Miami, FL
## Who knew?

## Persisting DataFrames

## Now let's look at some timing.
## Let's time the following operation using the spark.time method:

