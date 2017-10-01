
val hiveContext = new org.apache.spark.sql.hive.HiveContext(sc)
val s3flights = hiveContext.read.parquet("/data/flightdata/parquet-trimmed/")


val flights = s3flights.select("year", "month", "dayofmonth", "carrier", "tailnum",
    "actualelapsedtime", "origin", "dest", "deptime", "arrdelayminutes").
  filter(s3flights("year") === 2013)
flights.saveAsTable("flights")
flights.printSchema

// if you need to read the flights table back in once you've created it in Hive, this works
// val flights = hiveContext.open.table("flights")

