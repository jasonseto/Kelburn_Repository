/** Take Shakespeare
  * A simple program to simply print out the first line of Shakespeare's works
  */

import org.apache.spark.api.java.*;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.function.Function;

public class TakeShakespeare {
  public static void main(String[] args) {
    String shakesFile = "/data/shakespeare/input/all-shakespeare.txt"; 
    
    SparkConf conf = new SparkConf().setAppName("Take Shakespeare");
    JavaSparkContext sc = new JavaSparkContext(conf);
    
    JavaRDD<String> shakesData = sc.textFile(shakesFile);

    System.out.println("The first line of Shakespeare's works is\n " + shakesData.take(1));
    
    sc.stop();
  }
}
