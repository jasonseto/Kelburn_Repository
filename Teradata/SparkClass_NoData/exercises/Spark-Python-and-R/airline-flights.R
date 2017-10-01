#########################################################
## SparkR program to examine and plot our airline data ##
#########################################################

if (nchar(Sys.getenv("SPARK_HOME")) < 1) {
     Sys.setenv(SPARK_HOME = "/usr/lib/spark")
 }
library(SparkR, lib.loc = c(file.path(Sys.getenv("SPARK_HOME"), "R", "lib")))
sc <- sparkR.init(master = "local[*]", sparkEnvir = list(spark.driver.memory="8g"))
sqlContext <- sparkRSQL.init(sc)

library(magrittr)
library(ggplot2)

s3flights <- read.df(sqlContext, "s3://thinkbig.academy.aws/ontime/parquet", "parquet")
flightcounts <- s3flights %>% groupBy(s3flights$year, s3flights$month) %>% summarize(count = n(s3flights$month))
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

