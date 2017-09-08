# Hadoop Architecture Labs

These labs are designed for working within the Think Big Analytics 2-node environment launched by Vagrant. While the general principles are the same when using an Amazon Web Services Elastic Map Reduce cluster, the specific details and file locations are different. Please refer to the versions of the lab used for AWS EMR for that environment.

These labs familiarize you with four major components of a Hadoop cluster and how to move data among them:

* The edge node local file system
* The Hadoop Distributed File System (HDFS)
* YARN, the cluster operating system
* The Hive Warehouse

## Preparing Your Environment

You should have been given a zip file of the class materials, which we will assume here is named `hadoop-class.zip.` You should unzip that file into a new directory called `hadoop-class` and list its contents using the following commands

```
   unzip hadoop-class.zip
   cd hadoop-class
   ls
```
You should see the following files in the file listing.

```
data		exercises	handouts	images		slides		vagrant		vmbox
```

Now change your directory to the `vagrant` directory and tell vagrant to start your virtual machines.

```
    cd vagrant
    vagrant up
```

Once vagrant has finished provisioning your virtual machines, you are ready to start Lab 1.

