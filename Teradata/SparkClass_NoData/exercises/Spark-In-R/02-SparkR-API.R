## Most of this material is from the Apache Spark walkthrough for 2.2.0
## https://spark.apache.org/docs/latest/sql-programming-guide.html

## We have adjusted the environment variables below for an Amazon EMR 5.8.0 instance.

if (nchar(Sys.getenv("SPARK_HOME")) < 1) {
  Sys.setenv(SPARK_HOME = "/usr/lib/spark") # or whereever your Spark install lives
}
if (nchar(Sys.getenv("HADOOP_CONF_DIR")) < 1) {
  Sys.setenv(HADOOP_CONF_DIR = "/etc/hadoop/conf")            # or whereever your Hadoop lives
}
if (nchar(Sys.getenv("YARN_CONF_DIR")) < 1) {
  Sys.setenv(YARN_CONF_DIR = "/etc/hadoop/conf")              # or whereever your YARN config lives
}

library(magrittr)
# please note -- do not load dplyr or you will have function name conflicts

library(SparkR, lib.loc = c(file.path(Sys.getenv("SPARK_HOME"), "R", "lib")))
sparkR.session(master = "yarn",                                # execution type
               sparkConfig = list(spark.driver.memory = "3g"))s # configure driver and executors

head(faithful)
##   eruptions waiting
## 1     3.600      79
## 2     1.800      54
## 3     3.333      74
## 4     2.283      62
## 5     4.533      85
## 6     2.883      55

str(faithful)
## 'data.frame':	272 obs. of  2 variables:
##  $ eruptions: num  3.6 1.8 3.33 2.28 4.53 ...
##  $ waiting  : num  79 54 74 62 85 55 88 85 51 85 ...

## Note: in some installations of Spark, converting a data.frame to a DataFrame can
## cause session crashes where you will lose your Spark connection. If that happens,
## go to Session, Quit Session, don't save your .RData, log back in again, and restart
## from the beginning, being careful to comment out the following as.DataFrame statement.
df <- as.DataFrame(faithful)            # note this is as.Dataframe, not as.data.frame
# Displays the first part of the SparkDataFrame
head(df)
## you see the same numbers but it takes longer.
##  eruptions waiting
##1     3.600      79
##2     1.800      54
##3     3.333      74

# This does work though
df <- read.df("/data/spark-resources-data/people.json", "json")
# Show the content of the DataFrame
showDF(df)
##   age    name
## 1  NA Michael
## 2  30    Andy
## 3  19  Justin

printSchema(df)
## root
##  |-- age: long (nullable = true)
##  |-- name: string (nullable = true)

names(df)
## [1] "age"  "name"

df %>% select("name") %>% showDF()
## +-------+
## |   name|
## +-------+
## |Michael|
## |   Andy|
## | Justin|
## +-------+

## If you don't like pipelines, you can also express this as:
showDF(select(df, "name"))
## +-------+
## |   name|
## +-------+
## |Michael|
## |   Andy|
## | Justin|
## +-------+
##
## However, this gets messy for more complicated pipelines

df %>% select(df$name, df$age + 1) %>% showDF()
## +-------+-----------+
## |   name|(age + 1.0)|
## +-------+-----------+
## |Michael|       null|
## |   Andy|       31.0|
## | Justin|       20.0|
## +-------+-----------+

df %>% filter(df$age > 21) %>% showDF()
## +---+----+
## |age|name|
## +---+----+
## | 30|Andy|
## +---+----+

df %>% groupBy("age") %>% count() %>% showDF()
## +----+-----+
## | age|count|
## +----+-----+
## |  19|    1|
## |null|    1|
## |  30|    1|
## +----+-----+

## Let's save this as a table
createOrReplaceTempView(df, "people")
sql("show tables") %>% showDF()
## +---------+-----------+
## |tableName|isTemporary|
## +---------+-----------+
## |   people|      false|
## +---------+-----------+

teenagers <- sql("select name from people where age >= 13 and age <= 19")
showDF(teenagers)
## +------+
## |  name|
## +------+
## |Justin|
## +------+


## Let's read in the stocks tables.
## If you've already done the partitioned stocks table in Hive, you don't need to do the following
## In this case, though, we're just creating a single flat table from simple csv files
## In Spark 1.x versions, you'd need to type:
## stocks <- read.df("/data/stocks-flat/input", "com.databricks.spark.csv")
## In Spark 2.x, csv files are a standard input format

stocks <- read.df("/data/stocks-flat/input", "csv")
names(stocks) <- c("exchg", "symbol", "ymd", "price_open", "price_high", "price_low", "price_close", "volume", "price_adj_close")
createOrReplaceTempView(stocks, "stocks")

first10 <- sql("select * from stocks limit 10")
showDF(first10)
## +------+------+----------+----------+----------+---------+-----------+------+---------------+
## | exchg|symbol|       ymd|price_open|price_high|price_low|price_close|volume|price_adj_close|
## +------+------+----------+----------+----------+---------+-----------+------+---------------+
## |NASDAQ|  AAIT|2015-06-22| 35.299999| 35.299999|35.299999|  35.299999|   300|      35.299999|
## |NASDAQ|  AAIT|2015-06-19| 35.259998| 35.259998|35.259998|  35.259998|   400|      35.259998|
## |NASDAQ|  AAIT|2015-06-18|     34.52| 34.830002|    34.52|  34.830002|   300|      34.830002|
## |NASDAQ|  AAIT|2015-06-17| 34.650002| 34.650002|34.650002|  34.650002|   200|      34.650002|
## |NASDAQ|  AAIT|2015-06-16| 34.799999| 34.799999|34.709999|      34.77|   700|          34.77|
## |NASDAQ|  AAIT|2015-06-15| 35.009998| 35.009998|35.009998|  35.009998|     0|      35.009998|
## |NASDAQ|  AAIT|2015-06-12| 34.639999| 35.009998|34.639999|  35.009998|   300|      35.009998|
## |NASDAQ|  AAIT|2015-06-11|     34.68|     34.68|    34.68|      34.68|   200|          34.68|
## |NASDAQ|  AAIT|2015-06-10| 34.689999| 34.689999|34.689999|  34.689999|     0|      34.689999|
## |NASDAQ|  AAIT|2015-06-09| 34.189999| 34.689999|34.189999|  34.689999|  3500|      34.689999|
## +------+------+----------+----------+----------+---------+-----------+------+---------------+

count(stocks)
## Answer should be 2131092

stocks %>% filter(stocks$symbol == "AAPL") %>% count()
## Answer should be 8706

sparkR.session.stop()

