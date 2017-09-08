-- Copyright (c) 2011-2013 Think Big Analytics.

-- Table Management

-- NOTE: Since we created a database in the Hive-Walkthrough, you should exit
-- from the hive CLI and start it again, this time passing the option:
--   -d DB=mydatabase
-- where "mydatabase" should be replaced with your user name.

use ${DB};

-- Create a table containing counts for all the words in Shakespeare's plays.
-- Note that we'll use tabs to separate fields. We wouldn't really care
-- Hive does this internally, but in this case, we'll load the data
-- from a file that uses this format. 

CREATE TABLE IF NOT EXISTS shakespeare_wc (
  word  STRING, count INT) 
ROW FORMAT DELIMITED FIELDS TERMINATED BY '\t';

-- Now, load the table with data from the following file.
-- It uses the same tab field delimiter. We'll learn more about
-- the LOAD DATA command in a subsequent module. For now, notice
-- the LOCAL keyword, which means the path will be a "local" path,
-- not a path in the cluster.

LOAD DATA LOCAL INPATH '/home/hadoop/data/shakespeare_wc/input'
INTO TABLE shakespeare_wc;

-- SIDE NOTE: We hard-coded the home directory path
--   /home/hadoop
-- We can avoid this by using a system property that holds the user's home
-- directory:
-- LOAD DATA LOCAL INPATH '${system:user.home}/data/shakespeare_wc/input' 
-- INTO TABLE shakespeare_wc;

-- What's different between the following three commands?

DESCRIBE shakespeare_wc;
DESCRIBE EXTENDED shakespeare_wc;
DESCRIBE FORMATTED shakespeare_wc;

-- Select words that end with the letter 'e'.

SELECT * FROM shakespeare_wc WHERE word LIKE '%e' LIMIT 20;

-- Now count the number of rows (records). This might take several minutes:

SELECT COUNT(*) FROM shakespeare_wc;

-- EXERCISE: Find the words that start with "br" and end with "le".
-- You can match on substrings, not just letters, with LIKE clauses
-- and you can also match both beginning and ending substrings with
-- one (or more!) % in the middle. 

-- EXERCISE: What's the average word count?
-- See the "Built-in Functions" section of the "Hive Cheat Sheet" 
-- for documentation on count() and other functions.

-- EXERCISE: Try more variations of all these queries. 

-- We won't drop this table now, because we'll use it in subsequent
-- exercises. 
-- DROP TABLE IF EXISTS shakespeare_wc;
