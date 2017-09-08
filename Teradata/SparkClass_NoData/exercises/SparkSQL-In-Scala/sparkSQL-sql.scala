// If we haven't run the Spark Hive Fundametals labs, we will need to read in the tables. If
// you already have these tables in place, skip down to SQL Examples below.

val emptable = """CREATE EXTERNAL TABLE IF NOT EXISTS employees
       (name string, 
        salary float, 
        subordinates array<string>, 
        deductions map<string, float>, 
        address struct<street:string, city:string, state:string, zip:int>) 
row format delimited 
lines terminated by '\n' 
stored as textfile location '/data/employees/input/'"""

spark.sql(emptable)
spark.sql("select * from employees").show

// Now we'll read in the stocks table.
// Here, we're going to use spark's ability to read .csv files,
// which was added in Spark 2.x.

val stocks = spark.read.format("csv").load("/data/stocks-flat/input/").
  toDF("exchg", "symbol", "ymd", "price_open", "price_high", "price_low", 
    "price_close", "volume", "price_adj_close")

// Now write this out as a table called stocks

spark.sql("drop table stocks") // delete if already exists
stocks.write.saveAsTable("stocks")

// Existing Spark session is assumed to be in spark

spark.sql("SELECT name FROM employees WHERE address.zip = 60500").show()

spark.sql("SELECT ymd, price_open, price_close FROM stocks WHERE symbol = 'AAPL' AND exchg = 'NASDAQ' LIMIT 20").show()

spark.sql("SELECT year(s.ymd), avg(s.price_close) FROM stocks s  WHERE s.symbol = 'AAPL' AND s.exchg = 'NASDAQ' GROUP BY year(s.ymd) HAVING avg(s.price_close) NOT BETWEEN 50.0 AND 100.0").show()

// Give some thought to how fast these run compared with the Hive versions. In fact, you should go back
// run the same SQL statements in Hive for comparison and then run them in spark-sql

