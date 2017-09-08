# Calculate airline utilization on average

if (nchar(Sys.getenv("SPARK_HOME")) < 1) {
+     Sys.setenv(SPARK_HOME = "/usr/lib/spark")
+ }
library(SparkR, lib.loc = c(file.path(Sys.getenv("SPARK_HOME"), "R", "lib")))
sc <- sparkR.init(master = "local[*]", sparkEnvir = list(spark.driver.memory="3g"))
sqlContext <- sparkRSQL.init(sc)

library(magrittr)
library(ggplot2)

flights <- read.df(sqlContext, "s3://thinkbig.academy.aws/ontime/parquet-trimmed", "parquet")
bigcarriers <- filter(flights,
                      flights$carrier == 'UA' |
                          flights$carrier == 'AA' |
                              flights$carrier == 'US' |
                                  flights$carrier == 'B6' |
                                      flights$carrier == 'WN' |
                                          flights$carrier == 'DL')

timeinair <- bigcarriers %>% groupBy(bigcarriers$year, bigcarriers$carrier, bigcarriers$tailnum) %>% summarize(timeair = sum(bigcarriers$actualelapsedtime))
timeinair$eff <- timeinair$timeair / (24*60*365.25)
utilization <- timeinair %>% groupBy("year", "carrier") %>% summarize(avgeff = mean(timeinair$eff))
util <- collect(utilization)

theme_set(theme_bw())
g <- ggplot(util, aes(x = year, y = avgeff, group=carrier, color=carrier))
g <- g + geom_point() + stat_smooth(aes(group=carrier), method = "loess", se=FALSE)
print(g)
