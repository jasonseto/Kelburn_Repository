-- Copyright (c) 2011-2013 Think Big Analytics.

-- Table Schemas

use ${DB};

-- An Employees table that has simple fields, like name and salary,
-- but also complex fields, including an array of subordinates' names,
-- A map of names of deductions and the percentage amount to be 
-- deducted at each pay period, and a struct containing the employee's
-- address. 
-- Note that for the complex data types, Java-style generic type 
-- arguments are used.
-- Finally, if you know the table doesn't already exist, you can drop
-- the clause IF NOT EXISTS.

CREATE TABLE IF NOT EXISTS employees (
  name         STRING,
  salary       FLOAT,
  subordinates ARRAY<STRING>,
  deductions   MAP<STRING, FLOAT>,
  address      STRUCT<street:STRING, city:STRING, state:STRING, zip:INT>
);

-- Now let's load data into this table, similar to what we did in the previous
-- exercise. Note that LOCAL is omitted.
-- But first, we have to make a copy of the data file, because LOAD DATA actually
-- MOVES the file(s) when they are already in HDFS!

dfs -cp /data/employees/input/employees.txt /data/employees/input/employees-copy.txt;
dfs -ls /data/employees/input;

LOAD DATA INPATH '/data/employees/input/employees-copy.txt' INTO TABLE employees;

-- Confirm that the file has been moved.

dfs -ls /data/employees/input;

DESCRIBE employees;

-- How fast is the following query?
-- Notice how the complex data values are formatted on output.

SELECT * FROM employees;

-- EXERCISE: Instead of selecting "*", run a query that selects the 
-- employee name and the subordinates field. (That is, replace "*"
-- with "name, subordinates").
-- What does the output look like? How does the speed compare to the 
-- previous query?
-- Repeat the query selecting the deductions instead.
-- Then repeat it for the address.

-- NOTE: We'll see in an upcoming module that the CREATE TABLE statement
-- above is equivalent to the following, which shows all the default
-- row format "terminator" (separator) values indicated explicitly. 
-- (It's safe to run this command because IF NOT EXISTS means it will do
-- nothing!)

CREATE TABLE IF NOT EXISTS employees (
  name         STRING,
  salary       FLOAT,
  subordinates ARRAY<STRING>,
  deductions   MAP<STRING, FLOAT>,
  address      STRUCT<street:STRING, city:STRING, state:STRING, zip:INT>
)
ROW FORMAT DELIMITED
FIELDS TERMINATED BY '\001' 
COLLECTION ITEMS TERMINATED BY '\002' 
MAP KEYS TERMINATED BY '\003'
LINES TERMINATED BY '\n'
STORED AS TEXTFILE;

-- We won't drop this table now, because we'll use it in subsequent
-- exercises. You could omit the IF EXISTS clause if you know it exists!
-- DROP TABLE IF EXISTS employees;

-- EXERCISE: Create a (simple) table representing customer orders. 
-- Include the customer's name, total cost, shipping address, 
-- extra charges (shipping and handling, gift wrapping, etc.), and 
-- the items bought. It's fine to assume a separate "items" table
-- exists and you can assume the user buys only one of any item, if
-- you wish.
-- Drop the table after you're done with it.
