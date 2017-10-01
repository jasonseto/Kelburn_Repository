# Calculate airline utilization on average
## Most of this material is from the Apache Spark walkthrough for 2.2.0
## https://spark.apache.org/docs/latest/sql-programming-guide.html

if (nchar(Sys.getenv("SPARK_HOME")) < 1) {
  Sys.setenv(SPARK_HOME = "/vagrant/latest-spark") # or whereever your Spark install lives
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
               sparkConfig = list(spark.driver.memory = "3g")) # configure driver and executors

library(magrittr)
library(ggplot2)

s3flights <- read.df("/data/flightdata/parquet-trimmed", "parquet")
bigcarriers <- filter(s3flights$carrier %in% c("AA", "DL", "US", "UA", "WN", "B6"))
cache(bigcarriers)
print(sprintf("Number of rows = %d", count(bigcarriers)))

timeinair <- bigcarriers %>%
    groupBy(bigcarriers$year, bigcarriers$carrier, bigcarriers$tailnum) %>%
    summarize(timeair = sum(bigcarriers$actualelapsedtime))
timeinair$eff <- timeinair$timeair / (24*60*365.25)
utilization <- timeinair %>%
    groupBy("year", "carrier") %>%
    summarize(avgeff = mean(timeinair$eff))
util <- collect(utilization)

theme_set(theme_bw())
g <- ggplot(util, aes(x = year, y = avgeff, group=carrier, color=carrier))
g <- g + geom_point() + stat_smooth(aes(group=carrier), method = "loess", se=FALSE)
print(g)

sparkR.session.stop()
