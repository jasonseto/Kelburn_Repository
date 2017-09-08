
val hadoopConf=sc.hadoopConfiguration;
hadoopConf.set("fs.s3.impl", "org.apache.hadoop.fs.s3native.NativeS3FileSystem")
hadoopConf.set("fs.s3.awsAccessKeyId","AKIAI7YHYQPE5RA6OCPQ")
hadoopConf.set("fs.s3.awsSecretAccessKey","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")

val hiveContext = new org.apache.spark.sql.hive.HiveContext(sc)
// val s3flights = hiveContext.read.parquet("s3://AKIAI7YHYQPE5RA6OCPQ:o6IxX5AJwcNzdfQCCo5SgYgWDQ9pCO1BsPQVGsP6@thinkbig.academy.aws/ontime/parquet")
val s3flights = hiveContext.read.parquet("s3://thinkbig.academy.aws/ontime/parquet/")

// The following filters the original down to the last 5 years
val flights201x = s3flights.select("year", "month", "dayofmonth", "carrier", "tailnum",
  "actualelapsedtime", "origin", "dest", "deptime", "arrdelayminutes").
  filter(s3flights("year") >= 2010)
// The following creates a new dataset we can work with more conveniently in a 4GB Virtualbox VM image
flights201x.saveAsParquetFile("s3://thinkbig.academy.aws/ontime/2010-2014parquet/"

val flights = s3flights.select("year", "month", "dayofmonth", "carrier", "tailnum",
    "actualelapsedtime", "origin", "dest", "deptime", "arrdelayminutes").
  filter(s3flights("year") === 2013)
flights.saveAsTable("flights")
flights.printSchema

// if you need to read the flights table back in once you've created it in Hive, this works
// val flights = hiveContext.open.table("flights")

