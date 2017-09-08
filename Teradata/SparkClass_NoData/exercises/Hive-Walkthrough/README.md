![](../../images/SmallThinkBigIcon.png)
# README for Hive-Walkthrough

Copyright &#169; 2011-2013 Think Big Analytics.

This exercise is a walk through of the *Hive* installation and how to run Hive services. 
It demonstrates several concepts:

* Where Hive is installed and configured.
* How to run the Hive services, focusing on the command-line interpreter (CLI).
* How to define parameters on the command line.
* Create, query, alter, and delete a simple table.

## Starting a New JobFlow

If you terminated your job flow after the first day or class or you are new to the class today, you'll need to start a job flow and copies files to it. Here are the commands to use. First, start a job flow that will used interactive, rather than running a batch job flow that will exit when finished:

	elastic-mapreduce --create --alive --name "Hive+Pig interactive" --pig-interactive --hive-interactive --debug

The job flow id is echoed to the console. Assuming that `$EMR_JOBFLOW` is the value of that id, log into the *name node* with this command:

	elastic-mapreduce --ssh $EMR_JOBFLOW

Now, on the name node, run the following commands to load the exercises and data from S3 and install them in both HDFS and the `hadoop` user's home directory in the local file system:

	hadoop distcp s3n://think.big.academy.aws/exercises.zip /exercises.zip
	hadoop distcp s3n://think.big.academy.aws/data /data
	hadoop fs -get /exercises.zip ~/exercises.zip
	unzip exercises.zip
	hadoop fs -get /data ~/exercises

## Starting Hive

For help on the `hive` CLI, use the `-h` option:

	hive -h

Start Hive using this command:

	hive -d DB=hadoop -d DATA=/data

The `DB` variable definition is used as the name for your database. 
Use your login name or a similar name (e.g., no spaces). 

The `DATA` variable definition is used as the root of the exercises data. 
Because we didn't provide a full URL with a *scheme* (e.g., `hdfs` in `hdfs://server/...`), 
the path will default to HDFS.

We will occasionally use `${DATA}` in file system paths as the prefix. As shown, this will
mean that Hive (and Hadoop) will use the `data` directory in the `/`
bucket. If you wanted to use an S3 location instead:

	hive -d DATA=s3n://some-bucket/data


Summary:

* Use `session.hql` to understand what each command does and to copy individual commands from the file and paste them into the CLI.
* Use `hive -f some-file.hql` to run the whole file of commands and queries.

For more information on Hive commands, see the *Hive Cheat Sheet* distributed with the course. 

