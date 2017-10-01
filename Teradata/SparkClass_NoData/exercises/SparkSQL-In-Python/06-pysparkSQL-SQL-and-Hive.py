## If we haven't run the Spark Hive Fundametals labs, we will need to read in the tables. If
## you already have these tables in place, skip down to SQL Examples below.

emptable = """CREATE EXTERNAL TABLE IF NOT EXISTS employees
       (name string, 
        salary float, 
        subordinates array<string>, 
        deductions map<string, float>, 
        address struct<street:string, city:string, state:string, zip:int>) 
row format delimited 
lines terminated by '\n' 
stored as textfile location '/data/employees/input/'"""

spark.sql(emptable)
spark.sql("select * from employees").show()

## Now we'll read in the stocks table.
## Here, we're going to use spark's ability to read .csv files,
## which was added in Spark 2.x.

stocks = spark.read.format("csv"). \
load("/data/stocks-flat/input/"). \
toDF("exchg","symbol", \
"ymd", "price_open", \
"price_high", \
"price_low", \
"price_close", \
"volume", \
"price_adj_close")

## Now write this out as a table called stocks

spark.sql("drop table stocks") ## delete if already exists
stocks.write.partitionBy("exchg", "symbol").saveAsTable("stocks")
spark.sql("describe stocks").show(50)


## Existing Spark session is assumed to be in spark

spark.sql("SELECT name FROM employees WHERE address.zip = 60500").show()

## Now let's switch to stocks
## Before we start doing lots of SQL queries, let's look at how we read a Hive table into a DataFrame.
## We'll use the data frame to count up the number of stock quotes we have.

stks = spark.read.table("stocks")
stks.count()

## Of course, we can do that with SQL as well as follows:
spark.sql("SELECT COUNT(*) FROM stocks").show()
## You should get a value of 2,131,092

## One of the nice things about the SQL interface is that types get converted on the fly based on context.
## If we look at the descrioption of the stock table above, every column was a string type. We're
## now going to do some numeric comparisons.

## Up until this point, we've invoked sql as a method on our Spark Session. However, SQL is used so commonly that
## you can leave off the spark session reference.

sql("""SELECT ymd, price_open, price_close FROM stocks 
WHERE symbol = 'AAPL' AND exchg = 'NASDAQ' LIMIT 20""").show()

sql("SELECT year(s.ymd), avg(s.price_close) FROM stocks s  WHERE s.symbol = 'AAPL' AND s.exchg = 'NASDAQ' GROUP BY year(s.ymd) HAVING avg(s.price_close) NOT BETWEEN 50.0 AND 100.0").show()

## How would you sort that output by year?

## Give some thought to how fast these run compared with the Hive versions. In fact, you should go back
## run the same SQL statements in Hive for comparison and then run them in spark-sql

