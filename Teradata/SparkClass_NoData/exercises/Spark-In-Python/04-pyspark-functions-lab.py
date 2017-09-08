
# Basic PySpark walkthrough                                                                           
# This set of code is expected to run on Spark 2.0.2.

# Just in case sc hasn't been set up, we'll do it ourselves
sc = spark.sparkContext

# Simple operations in Python 

2 + 2                                          # Should be Int = 2
2.0 + 2                                        # Should be Double = 2.0
"This is a string"                             # String = This is a string

##############################
# Named Function definitions #
##############################

def add(x, y):
  return x + y  

add(42,13)

# Shorter version all on one line
def add(x, y): return x + y  

##############################################
# Anonymous Functions or Function Literals # #
##############################################

# a named function greeting
def greeting(x): return "Hello " + x

greeting("Joe")

# an anonymous function whose definition is assigned to variable greeting
greeting = lambda x: "Hello " + x
greeting("Joe")

names = ["Joe", "Mary", "Barbara"]
## comprehension for a local list;
## local lists don't have the map method defined.
[greeting(x) for x in names]

# Now do this in Spark
names = sc.parallelize(["Joe", "Mary", "Barbara"])
names.map(greeting).collect()

# should get
#    ['Hello Joe', 'Hello Mary', 'Hello Barbara']

# Now let's get rid of the name greeting

names.map(lambda x: "Hello " + x).collect()
# should get the same answer:
#    ['Hello Joe', 'Hello Mary', 'Hello Barbara']


# Define maximize to compute the maximum of two integers using a function literal instead of def
# Function literals are also referred to as anonymous or lambda functions

# Please note that unlike in Scala, lambda functions can only work with expression values and
# cannot include statements. So the expression

# maximize = lambda a, b: if (a > b) a else b
# yields a syntax error.

maximize = lambda a, b: a if (a > b) else b
maximize(5, 3)

# Define doubler as an immutable variable whose value is a function (note we aren't using def)
doubler = lambda x: x * 2                        # This assigns a function literal to doubler
doubler(4)

# We can also use this for our even number tester
nums = sc.parallelize([1, 2, 3, 4, 5])
nums.map(lambda x: x % 2 == 0).collect()


# These literal defintions are incredibly valuable in Spark
# because many of the Spark operations
# take functional arguments. Most of these will never be assigned
# to a variable. For example
# the following finds list entries that have an "f"
# in them using an anonymous function/function literal

mylist = sc.parallelize(["foo", "bar", "fight"])
mylist.filter(lambda x: "f" in x).collect()

# We could have written that as follows, but it's longer and less clear

mylist = sc.parallelize(["foo", "bar", "fight"])
def ffilter(s):
    return "f" in s       # define a named function to search for f

mylist.filter(ffilter).collect()      # will generate the same result as previous



####################################
# First full example: Textsearch # #
####################################


# load error messages from a log into memory
# then interactively search for various patterns
# base RDD

# log.txt has the following 5 lines
# ERROR	php: dying for unknown reasons
# WARN	dave, are you angry at me?
# ERROR	did mysql just barf?
# WARN	xylons approaching
# ERROR	mysql cluster: replace with spark cluster

lines = sc.textFile("hdfs:///data/logs/log.txt")

# transformed RDDs
errors = lines.filter(lambda line: line.startswith("ERROR"))
fields = errors.map(lambda message: message.split("\t"))
messages = fields.map(lambda r: r[1])
messages.cache()

# actions
messages.filter(lambda m: "mysql" in m).count()
messages.filter(lambda m: "php" in m).count()



###################
# Now Wordcount # #
###################

text_file = sc.textFile("hdfs:///data/shakespeare/input")
word_counts = text_file \
    .flatMap(lambda line: line.split()) \
    .map(lambda word: (word, 1)) \
    .reduceByKey(lambda a, b: a + b)

# Let's sort the results by count

sorted_counts = word_counts.sortBy(lambda x: x[1], ascending=False)
sorted_counts.saveAsTextFile("hdfs:///tmp/shakespeare-wc-python")


#######################
# Let's estimate Pi # #
#######################

import sys
from random import random
from operator import add
NUM_SAMPLES = 10000
def f(_):
    x = random() * 2 - 1
    y = random() * 2 - 1
    return 1 if x ** 2 + y ** 2 <= 1 else 0

count = sc.parallelize(range(1, NUM_SAMPLES + 1)).map(f).reduce(add)
print("Pi is roughly %f" % (4.0 * count / NUM_SAMPLES))

# Now try a bigger number of samples, say 100000

NUM_SAMPLES = 100000
def f(_):
    x = random() * 2 - 1
    y = random() * 2 - 1
    return 1 if x ** 2 + y ** 2 <= 1 else 0

count = sc.parallelize(range(1, NUM_SAMPLES + 1)).map(f).reduce(add)
print("Pi is roughly %f" % (4.0 * count / NUM_SAMPLES))

# you should have just gotten this warning:
# Warn scheduler.TaskSetManager: Stage 56 contains a task of very large size (152 KB).
# The maximum recommended task size is 100 KB.

# This is harmless in this case, but it illustrates an issue: Python is serializing
# the expression range(1, NUM_SAMPLES + 1). That's an array of 100001 elements that has
# to be passed from the driver to the executors. Python thinks that is rather large and
# worse, the 100K limit is hard coded presently (i.e., doesn't depend on the size of your
# driver or executor VMs

# The simplest solution here is not to create large RDDs by hand and to instead use the
# DataFrame API, which is smarter about distributing data across executing modes. You can
# find Python Dataframe examples in the SparkSQL-on-Python module.

