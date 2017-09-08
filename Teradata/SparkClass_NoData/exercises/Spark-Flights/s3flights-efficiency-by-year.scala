
val hiveContext = new org.apache.spark.sql.hive.HiveContext(sc)
val s3flights = hiveContext.read.parquet("/data/flightdata/parquet-trimmed/")

val flights = s3flights.select("year", "month", "dayofmonth", "carrier", "tailnum",
    "actualelapsedtime", "origin", "dest", "deptime", "arrdelayminutes")
// flights.saveAsTable("flights")
flights.persist()
flights.printSchema
flights.count

// if you need to read the flights table back in once you've created it in Hive, this works
// val flights = hiveContext.open.table("flights")

val timeinair = flights.groupBy("year", "carrier", "tailnum").agg(sum("actualelapsedtime"))
val efficiency = timeinair.col("sum(actualelapsedtime)")/ (24*60*365.25)
val newtia = timeinair.withColumn("eff", efficiency)
val utilization = newtia.groupBy("year", "carrier").agg(mean("eff") as "avgeff")
val sorted = utilization.sort(desc("avgeff"))
sorted.persist()
sorted.show

sorted.write.format("orc").save("/tmp/sorted.orc")
sorted.write.format("parquet").save("/tmp/sorted.parquet")
sorted.saveAsTable("sortedflights")  // This is what required us to use a HiveContext

// To save this as a .csv file, we will need a .csv package loaded from Databricks.
// You have to run the spark-shell as follows:
//     spark-shell --packages com.databricks:spark-csv_2.10:1.3.0

sorted.write.
  format("com.databricks.spark.csv").
  option("header", "true").
  save("/tmp/sorted.csv")

