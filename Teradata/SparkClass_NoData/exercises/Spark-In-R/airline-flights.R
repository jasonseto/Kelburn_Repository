#########################################################
## SparkR program to examine and plot our airline data ##
#########################################################

## Most of this material is from the Apache Spark walkthrough for 2.1.0
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
cache(s3flights)
print(sprintf("Number of rows = %d", count(s3flights)))

flightcounts <- s3flights %>% 
  groupBy(s3flights$year, s3flights$month) %>% 
  summarize(count = n(s3flights$month))
counts <- collect(flightcounts)
counts$ym <- as.Date(sprintf("%d-%02d-01", counts$year, counts$month), format="%Y-%m-%d")
counts$unit <- 1

                                        # Basic plot of flights

theme_set(theme_bw())
g <- ggplot(counts, aes(x = ym, y = count))
g <- g + geom_point() + stat_smooth(aes(group = unit), method = "loess", span=0.1, se=FALSE)
print(g)

                                        # what's the monthly pattern?

theme_set(theme_bw())
g <- ggplot(counts, aes(x = month, y = count, group=as.factor(year), color=as.factor(year)))
g <- g + geom_point() + stat_smooth(aes(group = as.factor(year), color=as.factor(year)), method = "loess", span=0.8, se=FALSE)
print(g)

carriercounts <- s3flights %>% 
  filter(s3flights$carrier %in% c("AA", "DL", "US", "UA", "WN", "B6")) %>%
  groupBy(s3flights$year, s3flights$carrier) %>% 
  summarize(count = n(s3flights$month),
            avgdelay = mean(s3flights$arrdelayminutes))
ccounts <- collect(carriercounts)
ccounts$ym <- as.Date(sprintf("%d-%02d-01", ccounts$year, ccounts$month), format="%Y-%m-%d")
ccounts$unit <- 1

theme_set(theme_bw())
g <- ggplot(ccounts, aes(x = year, y = count, color=carrier))
g <- g + geom_point() + 
  stat_smooth(aes(group = carrier), method = "loess", span=0.9, se=FALSE) +
  ylim(0, 1300000)
print(g)

cmonthlycounts <- s3flights %>% 
  filter(s3flights$carrier %in% c("AA", "DL", "US", "UA", "WN", "B6")) %>%
  groupBy(s3flights$year, s3flights$month, s3flights$carrier) %>% 
  summarize(count = n(s3flights$month),
            avgdelay = mean(s3flights$arrdelayminutes))
cmcounts <- collect(cmonthlycounts)
cmcounts$ym <- as.Date(sprintf("%d-%02d-01", cmcounts$year, cmcounts$month), format="%Y-%m-%d")
ccounts$unit <- 1


theme_set(theme_bw())
g <- ggplot(cmcounts, aes(x = ym, y = avgdelay, color=carrier))
g <- g + 
  stat_smooth(aes(group = carrier), method = "loess", span=0.9, se=FALSE) +
  facet_wrap(~ carrier)
print(g)

sparkR.session.stop()
