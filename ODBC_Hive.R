
library(RODBC)
odbc_hadoop<-odbcConnect("Hive")

Databases <-sqlQuery(odbc_hadoop, gsub("[\r\n]","",paste0("SHOW DATABASES")))

Tables  <-sqlQuery(odbc_hadoop, gsub("[\r\n]","",paste0("DESCRIBE third_party_rv.accurintbps_xml")))

