-- Copyright (c) 2011-2013 Think Big Analytics.

-- Using External Tables with Partitions:

use ${DB};

-- We'll demonstrate the use of two features, external 
-- (vs. internal) tables and partitioning the table to speed
-- up performance.
-- We're going to use historical stock price data from
-- Infochimps.com:
--   NASDAQ: infochimps_dataset_4777_download_16185
--   NYSE:   infochimps_dataset_4778_download_16677
-- We'll actually make the data "available" in the next 
-- exercise. 

-- The EXTERNAL keyword tells Hive that the table storage will
-- be "external" to Hive, rather than the default internal 
-- storage. We'll specify where the storage exists below.
-- We'll also partition the table by the exchange and the 
-- stock symbol, which will speed-up queries selecting on either
-- field, because Hive will know it can skip partitions that 
-- don't match the specified query values!

CREATE EXTERNAL TABLE IF NOT EXISTS stocks (
  ymd             STRING  COMMENT "YYYY-MM-DD format",
  price_open      FLOAT, 
  price_high      FLOAT,
  price_low       FLOAT,
  price_close     FLOAT,
  volume          INT     COMMENT "How many shares were traded today",
  price_adj_close FLOAT   COMMENT "Factors in historic splits and dividend payments"
)
PARTITIONED BY (exchg STRING, symbol STRING)
ROW FORMAT DELIMITED FIELDS TERMINATED BY ',';

-- We'll specify the partitions for this table in the "Loading Data" section.

SHOW PARTITIONS stocks;

-- We won't delete the stocks table as we'll need it for subsequent
-- exercises. However, in this case, deleting the table would NOT
-- delete any actual table data, because the table is external. You would
-- have to delete the partition files yourself. Hence, the table could
-- be dropped and easily recreated without data loss (if you don't 
-- delete the files!).
-- DROP TABLE IF EXISTS stocks;

