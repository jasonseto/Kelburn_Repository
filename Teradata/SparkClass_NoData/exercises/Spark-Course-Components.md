# Spark Course Components

As with all the ThinkBig Academy materials, the Spark materials are modular, which allows you to build out whatever length course you like. The outline for the 3-day Spark Analytics course, which includes all our Spark materials, is attached so you can see the framework. I've also attached a nearly identical outline that emphasizes Data Science for Data Scientist Audiences; it's the same modules with different durations and emphasis. Both of these outlines are in Box under

```
Academy - Marketing/Course Outlines/2017 Analytics Training Outlines/
```

I'm presently updating all the Spark content with new labs and slides with student problems, but the content should be in the github repo (although a couple of modules are incomplete because I'm still working on them). If you clone the repo before September 4, recognize that I'll probably be doing a pretty major merge of my Elevate github branch into the master around that day, and you should pull the content again then.

Here's how to interpret the structure in Academy-Courses.

* The slides for the modules in `Academy-Courses/slides`. The slides are in Keynote form with PDFs. Where we need PowerPoint, we export from Keynote.
* Most of the material for the Spark courses starts with the name "Spark-". The exceptions are broader architectural presentations such as `Hadoop-Architecture`, and `Architecture-Big-Data-Bottlenecks-wide`. I've provided a list of modules for the 3-day course below.
* The exercises are in `Academy-Courses/exercises`. The exercises are all text files, either straight Scala, Python, R, or Java code or Markdown files. At present all the exercises are what we'd call walkthroughs, where the instructor walks the class through the code executing each statement at the command line. I'm currently working to add more open-ended labs to encourage students to explore the content with less guidance than they get in the walkthroughs.
* The exercises depend on data that is loaded from `Academy-Courses/data` directory. One of the new labs for the `Hadoop-Architecture` module explicitly loads that data into HDFS for the vagrant virtual machines we use in Bootcamp. I'll likely generate a similar lab for loading the data into AWS EMR instances for Elevate, and we can use that procedure for other commercial training (we prefer vagrant internally because it doesn't require students to incur AWS EMR costs and management hassles).
* The exercises and labs that area associated with a presentation should be in a folder in `Academy-Courses/exercises` that corresponds to the name of the Keynote file. As a result, if you are looking for the exercises for the `Academy-Courses/slides/Spark-In-Scala.key` presentation, they should be in `Academy-Courses/exercises/Spark-In-Scala` folder.
* I'm creating a standard Makefile that will compile all the PDFs and exercises into handouts for distribution at Elevate. Again, we should be able to use that Makefile with other clients, and we can tailor it as we tailor the course content for others.

Here's my listing of modules for the "standard" 3-day Spark Course. Just so you know, the -wide appendices to the names indicates that the presentation has been converted to wide 6:9 format instead of 3:4.

1. Quick-Introduction-to-Big-Data-wide -- this can be abbreviated. Includes the note card exercise for MapReduce, but you can eliminate that for Spark class because we don't do MapReduce in Spark. However, if you have the time, I find this gets people to viscerally learn why MapReduce can be slow.
2. Hadoop-Architecture-wide  (new labs to help students on how to use yarn and hdfs command line and Web interfaces)
3. SQL-on-Hadoop-wide  (just a quick introduction to SQL fits into Hadoop as prep for SparkSQL)
4. HiveCourse-wide  (excerpts only to introduce the Metastore and Schema on Read ideas; you can't do all 250 slides nor should you)
5. Architecture-Big-Data-Bottlenecks-wide
6. Spark-Architecture-and-Concepts-wide
7. Spark-In-Scala-wide
8. SparkSQL-and-Dataframes-wide
9. SparkML-In-Depth  (not really very deep -- in depth was just to distinguish it from an older preso. Lots of real code though).
10. Spark-Streaming-with-Datasets   (still under development. Has both types of streams, but the Datasets version is incomplete as is its Spark 2.2.0 implementation)
11. Spark-In-Python-wide
12. Spark-In-R-wide
13. (if of interest) Spark-In-Java (still under development -- not all exercises present)

Please note all of these require updating with our new PowerPoint templates and ThinkBigAnalytics logos. I'm in the process of doing that for Elevate, but it's not done yet. FYI, the new ThinkBigAnalytics PowerPoint template doesn't have copyright notices, so we have to add those too.

I'm sure you'll see lots of places where the content can be improved or is incomplete. That said, it does teach and use Spark 2.x and certainly has different emphasis than the vendor courses; for example, we don't spend as much time on RDDs because of the increasing emphasis by the Spark team on DataFrame and Dataset models. We also can teach in 3.5 of the 4 languages (3.5 because Java isn't done yet), which I haven't seen any other vendor doing. As such, I think we can make the case that our content is a better fit for today's developers who have modern Spark releases.

I hope that gives you enough context to get started. Let me know what questions come up. I'm very cognizant that the Spark content needs expert instructors currently -- it assumes that you KNOW Spark to teach it. I'm working to update all the content with speaker notes to make that less essential, but I think it's a good starting point.

Carl Howe
