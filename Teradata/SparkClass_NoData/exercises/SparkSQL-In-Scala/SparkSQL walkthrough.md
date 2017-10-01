![](../../images/ThinkBig_logo_ORANGE-RGB_tiny.png)
# Spark-SparkSQL

This exercise introduces Spark's distributed query engine, SparkSQL

*Before running this or other examples, you should turn down Spark's default logging. See the Spark-Walkthrough exercise for details.*


### Launch Spark SQL's shell

    $ spark-sql --master yarn-client --executor-memory 4G

#### Reduce log level

```
set mapreduce.map.log.level=WARN;
set mapreduce.reduce.log.level=WARN;
set hive.tez.log.level=WARN;
```

Spark SQL should be configured to share Hive's metastore:

```
spark-sql> show tables;
flights_clean
flights_hive
flights_orc
flights_parquet
flights_raw
flights_seq
Time taken: 0.134 seconds
```

File formats make a big difference:

```
spark-sql> select count(*) from flights_hive;   
[...] 
15/03/04 03:03:46 INFO mapred.FileInputFormat: Total input paths to process : 18
4612266                                                                                                          
Time taken: 5.053 seconds

spark-sql> select count(*) from flights_seq;
[...]
15/03/04 03:04:05 INFO mapred.FileInputFormat: Total input paths to process : 6
4612266                                                                                                          
Time taken: 10.177 seconds

spark-sql> select count(*) from flights_orc;   
[...]
15/03/04 03:04:34 INFO orc.OrcInputFormat: FooterCacheHitRatio: 0/6
[...]
4612266                                                                                                          
Time taken: 9.092 seconds

spark-sql> select count(*) from flights_parquet;
[...]
15/03/04 03:03:31 INFO hadoop.ParquetInputFormat: Total input paths to process : 6
4612266
Time taken: 0.78 seconds
```

Let's do some basic summaries:

```
SELECT year, count(*)
FROM flights_parquet
GROUP BY year
ORDER BY year;
[...]
2007    648560                                                                                                   
2008	627931
2009	580134
2010	570788
2011	547219
2012	545131
2013	571623
2014	520880
Time taken: 3.534 seconds

```


### Combining SQL with the Spark shell

Even more interesting is the ability to intersperse Spark SQL in regular Spark scripts be they Scala, Python or Java:




### Run the Spark shell

#### *Scala:*

    MASTER=yarn-client /home/hadoop/spark/bin/spark-shell

#### *Python:*

    MASTER=yarn-client /home/hadoop/spark/bin/pyspark
    
Note the Spark context will already be initialized and available to you as `sc`.



```

scala> val sqlc = new org.apache.spark.sql.hive.HiveContext(sc)

scala> val byYear = sqlc.sql("SELECT year, count(*) FROM flights_parquet GROUP BY year")
[...]
scala> byYear.first()
15/03/04 03:50:50 INFO input.FileInputFormat: Total input paths to process : 6
15/03/04 03:50:50 INFO hadoop.ParquetInputFormat: Total input paths to process : 6
15/03/04 03:50:50 INFO hadoop.ParquetFileReader: Initiating action with parallelism: 5
[...]
res1: org.apache.spark.sql.Row = [2007,648560] 

```


