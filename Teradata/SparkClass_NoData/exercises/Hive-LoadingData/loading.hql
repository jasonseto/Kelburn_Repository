-- Copyright (c) 2011-2013 Think Big Analytics.

-- Loading Data into Tables

-- NOTE: Start hive with a definition for DATA:
--   hive -d DATA=/data
-- If you wanted to use S3 directories instead, e.g.:
--   hive -d DATA=s3n://bucketname/stocks

use ${DB};

-- We already saw how to use LOAD DATA [LOCAL] INPATH in previous
-- exercises. Here we'll focus on "loading" external partitions (that is
-- just defining their location), populating a table with a query result
-- set and how to export a query result set to the local file system.

-- Recall that we created this table in the previous exercise:

-- CREATE EXTERNAL TABLE IF NOT EXISTS stocks (
--   ymd             STRING,
--   price_open      FLOAT, 
--   price_high      FLOAT,
--   price_low       FLOAT,
--   price_close     FLOAT,
--   volume          INT,
--   price_adj_close FLOAT
-- )
-- PARTITIONED BY (exchg STRING, symbol STRING)
-- ROW FORMAT DELIMITED FIELDS TERMINATED BY ',';

-- Now we will setup the partitions for this table. Because it's external, all
-- we have to do is define the partitions and where on the file system they are
-- located.

-- Note that we must specify a value for each partition "column".
-- WARNING: If DATA is not defined (see the top of this file), these commands 
-- will "work", but the actual path with be an HDFS path starting with the
-- literal string "//data".

ALTER TABLE stocks ADD PARTITION(exchg = 'NASDAQ', symbol = 'AAPL')
LOCATION '/data/stocks/input/plain-text/NASDAQ/AAPL';

ALTER TABLE stocks ADD PARTITION(exchg = 'NASDAQ', symbol = 'INTC')
LOCATION '/data/stocks/input/plain-text/NASDAQ/INTC';

ALTER TABLE stocks ADD PARTITION(exchg = 'NYSE', symbol = 'GE')
LOCATION '/data/stocks/input/plain-text/NYSE/GE';

ALTER TABLE stocks ADD PARTITION(exchg = 'NYSE', symbol = 'IBM')
LOCATION '/data/stocks/input/plain-text/NYSE/IBM';

-- Note that you if you used an S3 path in your definition of DATA, then you would be pointed to S3 directories for the partitions!

SHOW PARTITIONS stocks;

-- Run DESCRIBE EXTENDED. What new information do you see, compared to previous tables?

DESCRIBE EXTENDED stocks;

-- DESCRIBE EXTENDED doesn't show you the actual partition locations. 
-- To see where they are located, use this command, for example:

DESCRIBE FORMATTED stocks PARTITION(exchg='NASDAQ', symbol='AAPL');

-- (You can use EXTENDED, too.)

-- Try a test query:

SELECT * FROM stocks WHERE exchg = 'NASDAQ' AND symbol = 'AAPL' LIMIT 10;

-- EXERCISE: Do the same query for INTC, GE, and IBM. What happens if you omit the
-- the exchg qualifier in the WHERE clause.


-- Not only can you fill an existing table with the results of a query, you
-- can combine the steps; create the table and insert data from a single query.

CREATE TABLE apple AS 
SELECT ymd, price_close FROM stocks 
WHERE symbol = 'AAPL' AND exchg = 'NASDAQ';

-- Sanity check: The count should be 8706.

SELECT count(*) FROM apple;
SELECT * FROM apple LIMIT 10;

-- We'll keep both stocks tables for subsequent use, but we're done with 
-- the apple table.

DROP TABLE apple;
