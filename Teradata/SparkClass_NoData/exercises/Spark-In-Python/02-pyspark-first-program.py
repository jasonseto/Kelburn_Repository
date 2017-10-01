# First Program In Pyspark (02-Pyspark-First-Program)

rdd = sc.textFile("hdfs:///data/shakespeare/input")
kings = rdd.filter(lambda line: "king" in line.lower())
kings.count()

# You should have gotten 4773 lines that reference
# a king in all of Shakespeare. That's almost 3%
# of the 175,376 lines in all of Shakepeare's plays and poems.

