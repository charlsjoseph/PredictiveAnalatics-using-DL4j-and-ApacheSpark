package poc.common.utilities

import org.apache.spark.api.java.{JavaRDD, JavaSparkContext}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.hive.HiveContext
import poc.common._

object Spark {
  
      val sparkConf = new SparkConf().setMaster(constants.sparkMaster).setAppName(constants.appName).set("spark.sql.warehouse.dir", constants.warehouseLocation)
      val sc = new JavaSparkContext(new SparkContext(sparkConf))
      val hiveContext = new HiveContext(sc)
      hiveContext.setConf("hive.metastore.uris", constants.metastore)

      val sqlContext = new org.apache.spark.sql.SQLContext(sc)
      import sqlContext.implicits._

  }

