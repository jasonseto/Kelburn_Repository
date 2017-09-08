# Hadoop Architecture Labs

## Lab 3: Loading Data Into HDFS

Begin by connecting to the command line on your cluster using the `vagrant ssh` command.

```
    vagrant ssh edge
```
You should see output that looks like the following:

```
Last login: Sun Aug 20 18:06:02 2017 from 10.0.2.2
[vagrant@edge ~]$ 
```

Now change directories to /vagrant and examine the contents of that directory:

```
    cd /vagrant
    ls
```

You should see the following contents that live in the edge node's local file system. Your formatting should be prettier than what's shown below due to this document's line length limits.

```
data        flightdata.tgz        install-r-studio-edge.sh
provision-control.sh  README.md   Vagrantfile
exercises   images                latest-spark              
provision-edge.sh     spark-setup-control.sh
flightdata  install-r-control.sh  latest-spark.tgz
README.html           spark-setup-edge.sh
[vagrant@edge vagrant]$ 

```

Now look at the contents of the HDFS file system current directory using the `hdfs dfs -ls` command. You'll see different results:

```
[vagrant@edge vagrant]$ hdfs dfs -ls
Found 1 items
drwxr-xr-x   - vagrant supergroup          0 2017-08-20 17:29 .sparkStaging
[vagrant@edge vagrant]$ 

```

Let's examine the root directory of HDFS. From this point on, we'll simply show the commands we type (as indicated by the `[vagrant@edge vagrant]$` prompt) and the result in the same code block.

```
[vagrant@edge vagrant]$ hdfs dfs -ls /
Found 5 items
drwxrwxrwt   - hdfs supergroup          0 2017-08-20 17:26 /data
drwxr-xr-x   - hdfs supergroup          0 2017-08-20 20:39 /rstudio
drwxrwxrwt   - hdfs supergroup          0 2017-08-20 17:28 /tmp
drwxrwxrwt   - hdfs supergroup          0 2017-08-20 17:22 /user
drwxrwxrwt   - hdfs supergroup          0 2017-08-20 17:22 /var
[vagrant@edge vagrant]$ hdfs dfs -ls /data
[vagrant@edge vagrant]$
```

We're now going to add the contents of the local data directory to the HDFS directory /data. We do this using the `hdfs dfs -put` command and then examine the /data directory with `hdfs dfs -ls /data` command.

```
[vagrant@edge vagrant]$ hdfs dfs -put data /
[vagrant@edge vagrant]$ hdfs dfs -ls /data
Found 21 items
-rw-r--r--   1 vagrant supergroup      10244 2017-08-23 20:31 /data/.DS_Store
drwxr-xr-x   - vagrant supergroup          0 2017-08-23 20:31 /data/bag-o-words
drwxr-xr-x   - vagrant supergroup          0 2017-08-23 20:31 /data/big-r-data
drwxr-xr-x   - vagrant supergroup          0 2017-08-23 20:31 /data/dividends
drwxr-xr-x   - vagrant supergroup          0 2017-08-23 20:31 /data/dividends-flat
drwxr-xr-x   - vagrant supergroup          0 2017-08-23 20:31 /data/employees
drwxr-xr-x   - vagrant supergroup          0 2017-08-23 20:31 /data/employees-pig
drwxr-xr-x   - vagrant supergroup          0 2017-08-23 20:31 /data/ge
drwxr-xr-x   - vagrant supergroup          0 2017-08-23 20:31 /data/logs
drwxr-xr-x   - vagrant supergroup          0 2017-08-23 20:31 /data/ml-100k
drwxr-xr-x   - vagrant supergroup          0 2017-08-23 20:31 /data/movies
drwxr-xr-x   - vagrant supergroup          0 2017-08-23 20:31 /data/orders-pig
drwxr-xr-x   - vagrant supergroup          0 2017-08-23 20:31 /data/python
drwxr-xr-x   - vagrant supergroup          0 2017-08-23 20:31 /data/shakespeare
drwxr-xr-x   - vagrant supergroup          0 2017-08-23 20:31 /data/shakespeare_wc
drwxr-xr-x   - vagrant supergroup          0 2017-08-23 20:31 /data/spark-resources-data
drwxr-xr-x   - vagrant supergroup          0 2017-08-23 20:31 /data/sparkml-data
drwxr-xr-x   - vagrant supergroup          0 2017-08-23 20:31 /data/stocks
drwxr-xr-x   - vagrant supergroup          0 2017-08-23 20:31 /data/stocks-flat
drwxr-xr-x   - vagrant supergroup          0 2017-08-23 20:31 /data/twitter
drwxr-xr-x   - vagrant supergroup          0 2017-08-23 20:31 /data/twitter-pig
[vagrant@edge vagrant]$ 
```

These directories contain source datasets that we can use throughout this Hadoop course. However, not many of these datasets are large ones. To allow us to give our Hadoop cluster a bit more exercise, we'll load some multi-million line datasets regarding US airline flights that we've downloaded from the FAA. These datasets are in the local file directory `flightdata`. We'll add that dataset to the `/data` directory on HDFS using another `put` command.

One way you could do that is as follows.

```
[vagrant@edge vagrant]$ hdfs dfs -mkdir /data/flightdata
[vagrant@edge vagrant]$ ls flightdata
aircraft-registrations  parquet-trimmed  tickets
[vagrant@edge vagrant]$ hdfs dfs -put flightdata/* /data/flightdata
```

Just do demonstrate how we would remove a dataset, we'll delete that directory and its contents from HDFS and load the data in a simpler way.

```
[vagrant@edge vagrant]$ hdfs dfs -rm -R /data/flightdata
17/08/23 20:40:03 INFO fs.TrashPolicyDefault: Namenode trash configuration: Deletion interval = 0 minutes, Emptier interval = 0 minutes.
Deleted /data/flightdata
[vagrant@edge vagrant]$ hdfs dfs -put flightdata /data
```

This step concludes the lab.

