-- Copyright (c) 2011-2013 Think Big Analytics.

-- Hive Walkthrough

-- These lines that start with "--" are comments. If you run a Hive script
-- with "hive -f file", it will ignore these lines. However, you can't 
-- copy and paste these lines into Hive; it complains.

-- Now copy and paste the following whole line, including the leading whitespace:

	show tables;

-- What?? Hive is trying to do tab completion.


-- See what properties have been set by Hive or the user.

set;

-- See all Hive and Hadoop properties. (actually the same output as "set")

set -v;

-- See a particular property, e.g., the DB property you defined when you
-- started Hive:

set hivevar:DB;

-- You can set a property:

set foo.bar=baz;

-- Run a shell command (semicolon required!) It doesn't understand
-- file "globs" nor shell environment variables, like $HOME.

! ls -l;
! pwd ;
! ls exercises ;

-- Run a distributed file system command (semicolon required!)

dfs -ls /data;
dfs -ls /exercises;
dfs -ls /data;

-- Quit gracefully (can use Control-D)
-- quit

-- We'll dive into the details for the following examples, but for now
-- just get a "sense" of what they are doing.

-- Show the tables (there aren't any yet).

SHOW TABLES;

-- Create a "database" with the same name as your user name.
-- MAKE SURE YOU USE A NAME THAT ISN'T ALREADY IN USE!
-- If you didn't start the Hive CLI with the "-d DB=some_name" argument,
-- edit the following line and replace "${DB}" with your user name:
-- (Note: The IF NOT EXISTS is optional.)

CREATE DATABASE IF NOT EXISTS ${DB};

-- This will be your working database throughout the class. When working on
-- a shared cluster, everyone needs their own database to avoid name collisions
-- when creating tables. Even when you have a dedicated system of your own,
-- it is still a good practice to organize your tables into databases, so we'll
-- do that in this class.

-- Look at the databases defined. You should see "default" and your database:

SHOW DATABASES;

-- Now "use" your database, instead of the default database. Substitute your 
-- actual database name here, if "DB" is not defined.

use ${DB};

-- Show the tables again (there should be none). Now create a table, 
-- "describe" it to see it's schema, alter it to add a new column, describe 
-- it again, and finally drop it. (We never add any actual data to this table
-- and ALTER TABLE does NOT modify existing data, only the metadata about the
-- table.)

SHOW TABLES;
CREATE TABLE demo1 (id INT, name STRING);
SHOW TABLES;
DESCRIBE demo1;
ALTER TABLE demo1 ADD COLUMNS (other_name STRING);
DESCRIBE demo1;
SELECT * FROM demo1;
DROP TABLE demo1;

-- If you're in the CLI and you want to run a Hive script file.
-- Note that the no quotes are required around the path, just like
-- the dfs and sh commands:

source ${system:user.home}/exercises/Hive-Walkthrough/script.hql;

-- Finally, we'll see later that you can add Java "jar" files, generic 
-- "archives" (e.g., zip files) and files to the session. What this actually
-- means is that they are put into Hadoop's "distributed cache" so they are
-- visible to all nodes in the cluster. They are also put in the working 
-- directory for each Hadoop process. Here are examples
--   ADD JARS path_to_jar1 path_to_jar2 ...;
--   ADD FILES path_to_file1 path_to_file2 ...;
--   ADD ARCHIVES path_to_archive1 path_to_archive2 ...;
--
-- We can list the jars, archives, and files we've added already in the
-- current session. We haven't added any yet, but depending on the version
-- of Hive you're using some may be listed that Hive added itself:

LIST JARS;
LIST ARCHIVES;
LIST FILES;

-- Finally, if for some reason you later want to remove from the
-- distributed cache any jars, archives, or files (which we mentioned
-- in Hive-Walkthrough and we'll see used in Hive-SerDe), then use
-- the same path you used with the corresponding ADD command in the
-- following DELETE commands:
--   DELETE JARS paths_to_jars;
--   DELETE FILES paths_to_files;
--   DELETE ARCHIVES paths_to_archives;
-- 
-- Note that for all the ADD, LIST, and DELETE commands, case doesn't matter
-- as always...

