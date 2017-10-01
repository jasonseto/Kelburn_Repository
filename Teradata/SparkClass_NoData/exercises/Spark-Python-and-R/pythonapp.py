# UserPurchaseHistory.csv contains:
#   John,iPhone Cover,9.99
#   John,Headphones,5.49
#   Jack,iPhone Cover,9.99
#   Jill,Samsung Galaxy Cover,8.95
#   Bob,iPad Cover,5.49

from pyspark import SparkContext
# We only need the following if we are NOT running this in pyspark
# sc = SparkContext("yarn", "First Spark App")
# we take the raw data in CSV format and convert it into a set of records of the form (user, product, price)
data = sc.textFile("/data/python/UserPurchaseHistory.csv").map(lambda line: line.split(",")).map(lambda record: (record[0], record[1], record[2]))
# let's count the number of purchases
numPurchases = data.count()
print numPurchases

# let's count how many unique users made purchases
uniqueUsers = data.map(lambda record: record[0]).distinct().count()
# let's sum up our total revenue
totalRevenue = data.map(lambda record: float(record[2])).sum()
# let's find our most popular product
products = data.map(lambda record: (record[1], 1.0)).reduceByKey(lambda a, b: a + b).collect()
mostPopular = sorted(products, key=lambda x: x[1], reverse=True)[0]

# Finally, print everything out
print "Total purchases: %d" % numPurchases
print "Unique users: %d" % uniqueUsers
print "Total revenue: %2.2f" % totalRevenue
print "Most popular product: %s with %d purchases" % (mostPopular[0], mostPopular[1])

# stop the SparkContext; again, only need this if not in pyspark
# sc.stop()
