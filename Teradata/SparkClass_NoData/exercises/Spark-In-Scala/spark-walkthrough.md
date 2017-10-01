![](../../images/ThinkBig_logo_ORANGE-RGB_tiny.png)
# Spark-Walkthrough

This exercise walks through some basic operations in Spark's interactive shell for Scala and Python.

## Turn down the logging level

By default, many of Spark's logging parameters are set to `INFO` leading to extremely verbose messages printed to the console. In fact, if you don't turn these down, the prompt will soon scroll off the screen!

To do so, edit `~hadoop/spark/conf/log4j.properties` on the cluster with your text editor of choice (`emacs`, `vi`, `nano`) to selectively change `INFO` entries to `WARN`. To change them all easily:

    sed -i.bak 's/INFO/WARN/' /home/hadoop/spark/conf/log4j.properties

    
## Launch Spark's interactive environment

#### *Scala:*

```
$ which spark-shell
~/spark/bin/spark-shell

$ spark-shell --master yarn-client
``` 

#### *Python:*

```
$ which pyspark
~/spark/bin/pyspark

$ pyspark --master yarn-client
```

## Examine the SparkContext

The SparkContext is your connection to the Spark cluster. Note that in both environments, the SparkContext will already be initialized and available to you as `sc`:

#### *Scala:*

```
scala> sc
res0: org.apache.spark.SparkContext = org.apache.spark.SparkContext@57308da3

scala> sc.isLocal
res1: Boolean = false

```

Note that tab completion works by default in the Scala shell to show which methods and fields are available on the SparkContext `sc`. 

Try some of them out to explore the SparkContext:

```
scala> sc.appName
res2: String = Spark shell

scala> sc.getExecutorMemoryStatus
res3: scala.collection.Map[String,(Long, Long)] = Map(ip-10-69-25-111.ec2.internal:34393 -> (560497950,560497950), ip-10-69-25-111.ec2.internal:38475 -> (258201354,258201354), ip-10-69-25-111.ec2.internal:48361 -> (560497950,560497950))

scala> sc.hadoopConfiguration
res4: org.apache.hadoop.conf.Configuration = Configuration: core-default.xml, core-site.xml, mapred-default.xml, mapred-site.xml, yarn-default.xml, yarn-site.xml, hdfs-default.xml, hdfs-site.xml

```

#### *Python:*

    >>> sc
    <pyspark.context.SparkContext object at 0x7f8fac33b250>

Tab completion is available to Python users through the [IPython](http://ipython.org/) shell. You can also use Python's built-in methods to inspect the SparkContext:

```
>>> dir(sc)
['PACKAGE_EXTENSIONS', '__class__', '__delattr__', '__dict__', '__doc__', '__enter__', '__exit__', '__format__', '__getattribute__', '__getnewargs__', '__hash__', '__init__', '__module__', '__new__', '__reduce__', '__reduce_ex__', '__repr__', '__setattr__', '__sizeof__', '__str__', '__subclasshook__', '__weakref__', '_accumulatorServer', '_active_spark_context', '_batchSize', '_callsite', '_checkpointFile', '_conf', '_dictToJavaMap', '_do_init', '_ensure_initialized', '_gateway', '_getJavaStorageLevel', '_initialize_context', '_javaAccumulator', '_jsc', '_jvm', '_lock', '_next_accum_id', '_pickled_broadcast_vars', '_python_includes', '_temp_dir', '_unbatched_serializer', 'accumulator', 'addFile', 'addPyFile', 'appName', 'binaryFiles', 'binaryRecords', 'broadcast', 'cancelAllJobs', 'cancelJobGroup', 'clearFiles', 'defaultMinPartitions', 'defaultParallelism', 'dump_profiles', 'environment', 'getLocalProperty', 'hadoopFile', 'hadoopRDD', 'master', 'newAPIHadoopFile', 'newAPIHadoopRDD', 'parallelize', 'pickleFile', 'profiler_collector', 'pythonExec', 'runJob', 'sequenceFile', 'serializer', 'setCheckpointDir', 'setJobGroup', 'setLocalProperty', 'setSystemProperty', 'show_profiles', 'sparkHome', 'sparkUser', 'statusTracker', 'stop', 'textFile', 'union', 'version', 'wholeTextFiles']

>>> sc.appName
u'PySparkShell'

>>> sc.version
u'1.3.1'

```

## Basic RDD Operations

First create a simple, small RDD by hand:

#### *Scala:*

    var smallprimes = sc.parallelize(Array(2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97))

#### *Python:*

    smallprimes = sc.parallelize( [ 2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97 ] )


Try some basic operations on the RDD:

#### *Scala:*

```
scala> smallprimes.count
res6: Long = 25

scala> smallprimes.min
res8: Int = 2

scala> smallprimes.max
res9: Int = 97

scala> smallprimes.sum
res7: Double = 1060.0                                                                                                                

scala> smallprimes.collect
res5: Array[Int] = Array(2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97)             
```

#### *Python:*

```
>>> smallprimes.count()
25                                                                                                                                   
>>> smallprimes.min()
2                                                                                                                                    
>>> smallprimes.max()
97                                                                                                                                   
>>> smallprimes.sum()
1060                                                                                                                                 
>>> smallprimes.collect()
[2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97]               

```

## Reading data from HDFS

A more realistic scenario is to create an RDD from data read from disk. Spark can natively access HDFS and S3 in addition to the local file system. 

Try reading in the stock quote data from HDFS:

#### *Scala:*

```
scala> val rdd = sc.textFile("hdfs:///data/stocks-flat/input")
rdd: org.apache.spark.rdd.RDD[String] = hdfs:///data/stocks-flat/input MapPartitionsRDD[1] at textFile at <console>:21

scala> rdd.first
res5: String = NASDAQ,ABXA,2009-12-09,2.55,2.77,2.50,2.67,158500,2.67

scala> rdd.count
res6: Long = 2075394                                                                                                                

```

#### *Python:*

```
>>> rdd = sc.textFile("hdfs:///data/stocks-flat/input")

>>> rdd.first()
u'NASDAQ,ABXA,2009-12-09,2.55,2.77,2.50,2.67,158500,2.67'

>>> rdd.count()
2075394
```

## Simple Transformations and Actions

Let's first `filter` the data set so we only see AAPL records:

#### *Scala:*

```
scala> val aapl = rdd.filter( line => line.contains("AAPL") )
aapl: org.apache.spark.rdd.RDD[String] = MapPartitionsRDD[5] at filter at <console>:23
```

#### *Python:*

```
>>> aapl = rdd.filter( lambda line: 'AAPL' in line )
```

Note that transformations return instantly because they're executed *lazily* (just as in Pig). Only when an action such as `count` or `saveAsTextFile is requested do the required transformations execute to satisfy the dependencies:

#### *Scala:*

```
scala> aapl.count
res15: Long = 6412
```

#### *Python:*

```
>>> aapl.count()
6412
>>> 
```

