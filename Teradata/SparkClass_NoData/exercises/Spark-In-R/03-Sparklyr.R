# load the sparklyr package
library(sparklyr)

if (nchar(Sys.getenv("SPARK_HOME")) < 1) {
  Sys.setenv(SPARK_HOME = "/vagrant/latest-spark")                   # or whereever your Spark install lives
}
if (nchar(Sys.getenv("HADOOP_CONF_DIR")) < 1) {
  Sys.setenv(HADOOP_CONF_DIR = "/etc/hadoop/conf")            # or whereever your Hadoop lives
}
if (nchar(Sys.getenv("YARN_CONF_DIR")) < 1) {
  Sys.setenv(YARN_CONF_DIR = "/etc/hadoop/conf")              # or whereever your YARN config lives
}

# create the Spark context, for EMR 4.7/4.8 use version = "1.6.2" and for EMR 4.6 use "1.6.1"
sc <- spark_connect(master = "yarn", version = "2.2.0")

# load dplyr
library(dplyr)

# copy some built-in sample data to the Spark cluster
iris_tbl <- copy_to(sc, iris)

# this takes a long time
srcflights <- spark_read_parquet(sc, "flights", "/data/flightdata/parquet-trimmed")
recentflights <- srcflights %>% filter(year >= 2015)
flights <- recentflights %>% 
  select("year", "month", "dayofmonth", "flightdate", "carrier", "tailnum",
         "deptime", "depdelay", "depdelayminutes", "origin",
         "dest", "arrdelay", "arrdelayminutes",
         "distance", "airtime", "cancelled")
flights_tbl <- flights %>% mutate(date = as.Date(flightdate))

src_tbls(sc)

# filter by departure delay and print the first few records
flights_tbl %>% filter(depdelay == 2)

# plot data on flight delays
delay <- flights_tbl %>% 
  group_by(tailnum) %>%
  summarise(count = n(), dist = mean(distance), delay = mean(arrdelayminutes)) %>%
  filter(count > 20, dist < 2000, !is.na(delay)) %>%
  collect()

library(ggplot2)
ggplot(delay, aes(dist, delay)) +
  geom_point(aes(size = count), alpha = 1/2) +
  geom_smooth() +
  scale_size_area(max_size = 2)

# use SQL  
library(DBI)
iris_preview <- dbGetQuery(sc, "SELECT * FROM iris LIMIT 10")
iris_preview

# Machine learning example using Spark MLlib

# copy the mtcars data into Spark
mtcars_tbl <- copy_to(sc, mtcars)

# transform the data set, and then partition into 'training', 'test'
partitions <- mtcars_tbl %>%
  filter(hp >= 100) %>%
  mutate(cyl8 = cyl == 8) %>%
  sdf_partition(training = 0.5, test = 0.5, seed = 1099)

# fit a linear regression model to the training dataset
fit <- partitions$training %>%
  ml_linear_regression(response = "mpg", features = c("wt", "cyl"))
fit

summary(fit)

# get the 10th row in test data
car <- tbl_df(partitions$test) %>% slice(10)
# predict the mpg 
predicted_mpg <- car$cyl * fit$coefficients["cyl"] + car$wt * fit$coefficients["wt"] + fit$coefficients["(Intercept)"]

# print the original and the predicted
sprintf("original mpg = %s, predicted mpg = %s", car$mpg, predicted_mpg)
