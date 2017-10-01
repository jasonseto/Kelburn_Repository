/** RDD Demos
  * Simple Demonstrations of RDD code in Java
  */

import org.apache.spark.api.java.*;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.function.Function;

public class RDDDemos {
  public static void main(String[] args) {
    
    SparkConf conf = new SparkConf().setAppName("Take Shakespeare");
    JavaSparkContext sc = new JavaSparkContext(conf);
    
    JavaRDD<Integer> distData = sc.parallelize(Arrays.asList(1, 2, 3, 4, 5));

    System.out.println("The first three values of the a " + distData.take(3));
    
    sc.stop();
  }
}
