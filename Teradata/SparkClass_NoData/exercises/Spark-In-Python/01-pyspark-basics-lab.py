# Spark Walkthrough 1
#
#
# Begin by starting the pyspark shell from the Linux Shell command line

# pyspark --master yarn-client	     # start in client mode

# First examine the Spark Context you were handed when you started Spark
sc
sc.appName

# First create a simple, small RDD by hand:

smallprimes = sc.parallelize([2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97])

# Try some basic operations on the RDD:

smallprimes.count()                     # if a function takes no arguments, no parens are needed
smallprimes.min()
smallprimes.max()
smallprimes.sum()
smallprimes.collect()

# Reading data from HDFS
# A more realistic scenario is to create an RDD from data read from disk. Spark can natively access HDFS and S3 in addition to the local file system.
# Try reading in the stock quote data from HDFS:

rdd = sc.textFile("hdfs:///data/stocks-flat/input")
rdd.first()
rdd.count()

# Simple Transformations and Actions
# Let's first filter the data set so we only see AAPL records:

aapl = rdd.filter(lambda line: "AAPL" in line)
aapl.count()


