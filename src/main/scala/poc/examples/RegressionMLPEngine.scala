
package poc.examples

import java.io.File
import java.util.Collections

import org.canova.api.records.reader.RecordReader
import org.canova.api.records.reader.impl.CSVRecordReader
import org.canova.api.split.FileSplit
import org.deeplearning4j.datasets.iterator.DataSetIterator
import org.deeplearning4j.eval.RegressionEvaluation
import org.deeplearning4j.nn.api.OptimizationAlgorithm
import org.deeplearning4j.nn.conf.{MultiLayerConfiguration, NeuralNetConfiguration, Updater}
import org.deeplearning4j.nn.conf.layers.{DenseLayer, OutputLayer}
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.nn.weights.WeightInit
import org.deeplearning4j.optimize.api.IterationListener
import org.deeplearning4j.optimize.listeners.ScoreIterationListener
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.dataset.DataSet
import org.nd4j.linalg.factory.Nd4j
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction


import org.apache.spark.api.java.{JavaRDD, JavaSparkContext}
import org.apache.spark.mllib.evaluation.BinaryClassificationMetrics
import org.apache.spark.{SparkConf, SparkContext}
import org.deeplearning4j.spark.impl.multilayer.SparkDl4jMultiLayer
import org.deeplearning4j.spark.util.MLLibUtil
import org.apache.spark.sql.Row
import org.apache.spark.sql.hive.HiveContext
import java.util.{Collections, Random}

import scala.util.control.Breaks._


import org.nd4j.linalg.dataset.api.iterator.fetcher._
import org.nd4j.linalg.dataset.api.iterator._


object Spark1 {
        // val warehouseLocation = "hdfs://synecloud:9000/user/hive/warehouse/";
      val warehouseLocation = "hdfs://quickstart.cloudera:8020/user/hive/warehouse/";
      val sparkConf = new SparkConf().setMaster("local").setAppName("LinearRegMLP").set("spark.sql.warehouse.dir", warehouseLocation)

    //val sparkConf = new SparkConf().setMaster("spark://synecloud:7077").setAppName("LinearRegMLP").set("spark.sql.warehouse.dir", warehouseLocation)
       val sc = new JavaSparkContext(new SparkContext(sparkConf))

       //   val metastore = "thrift://synecloud:9083";
     val metastore = "thrift://quickstart.cloudera:9083";
       val hiveContext = new HiveContext(sc)
               hiveContext.setConf("hive.metastore.uris", metastore)


      val sqlContext = new org.apache.spark.sql.SQLContext(sc)
      import sqlContext.implicits._

  }

/*

object RegressionMLPEngine {
    def main(args: Array[String]): Unit = {
        val seed = 123
        val learningRate = 0.19
        val nEpochs = 10
        val numInputs = 3
        val numOutputs = 3
        val numHiddenNodes = 10
        val iterationCount_DL4j = 100
        val iterationCount_spark = 500

//        // Intialize the spark context 
//       val sparkConf = new SparkConf().setMaster("local").setAppName("LinearRegMLP")
//      val sc = new JavaSparkContext(new SparkContext(sparkConf))
//
//       val warehouseLocation = "hdfs://quickstart.cloudera:8020/user/hive/warehouse/";
//        
//       val hiveContext = new HiveContext(sc);
//       hiveContext.setConf("hive.metastore.uris", "thrift://quickstart.cloudera:9083");

       val sqlTrainingDF = Spark.hiveContext.sql("select customer_id from customers order by customer_id limit 10");
       var  arr1List : java.util.List[Double] = new java.util.ArrayList[Double]();

      sqlTrainingDF.collect().foreach { row => 
        arr1List.add(row.getInt(0));
      }
      println(arr1List);
      var dataSetList = covertDFtoLabledDataSet(arr1List,numInputs ) ;
      
//      val fetcher : DataSetFetcher =  new StockDataFetcher(dataSetList, 3, 3);
//      val datasetIteartor = new BaseDatasetIterator(2, dataSetList.size(), fetcher) 
//      
//      while(datasetIteartor.hasNext()) {
//              println("dataset : " + datasetIteartor.next());
//      }
//      
      
       
      
      val rng = new Random(12)
      Collections.shuffle(dataSetList, rng)
      val data: JavaRDD[DataSet] = Spark.sc.parallelize(dataSetList).cache;

        // Initializing the MLP Neural Net with 3 layers for 3 input and 3 output 
        val conf: MultiLayerConfiguration = new NeuralNetConfiguration.Builder()
                .seed(seed)
      //        .iterations(iterationCount_DL4j)
              .iterations(iterationCount_spark)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .learningRate(learningRate)
                .weightInit(WeightInit.XAVIER)
               .updater(Updater.NESTEROVS).momentum(0.9)    
                .list()
                .layer(0, new DenseLayer.Builder().nIn(numInputs).nOut(numHiddenNodes)
                      .activation("identity").build()).backprop(true)
                .layer(1, new DenseLayer.Builder().nIn(numHiddenNodes).nOut(numHiddenNodes)
                      .activation("sigmoid").build()).backprop(true)
                .layer(2, new OutputLayer.Builder(LossFunction.MSE)
                        .activation("identity")
                         .nIn(numHiddenNodes).nOut(numOutputs).build()).backprop(true)
                 .build()
              
        //   using plain dl4j  
       //val model: MultiLayerNetwork = new MultiLayerNetwork(conf)
       //model.init()
       //model.setListeners(Collections.singletonList[IterationListener](new ScoreIterationListener(1)))
 				
      // plugging in the MLP Neural Net to Spark and creating SparkDl4jMultiLayer 
     val sparkNeuralNetwork = new SparkDl4jMultiLayer(Spark.sc, conf)
     
      // train the model with training dataset 
      (0 until nEpochs).foreach { _ => 
        sparkNeuralNetwork.fitDataSet(data) 
      }
      // making eval training set :
       val sqlEvalDF = Spark.hiveContext.sql("select customer_id from customers order by customer_id limit 50");
       var  arrevalList : java.util.List[Double] = new java.util.ArrayList[Double]();

      sqlEvalDF.collect().foreach { row => 
        arrevalList.add(row.getInt(0));
      }
      println(arrevalList);
      val dataEvalSetList = covertDFtoLabledDataSet(arrevalList,numInputs ) ;
      val dataEval: JavaRDD[DataSet] = Spark.sc.parallelize(dataEvalSetList);
      
     val  dataEvalIterator = dataEvalSetList.iterator();
     
      while(dataEvalIterator.hasNext()) {
         var data = dataEvalIterator.next();
        
             //using plain dl4j
          // val  predicted =  model.output(data.getFeatures);
           val  predicted = sparkNeuralNetwork.getNetwork.output(data.getFeatures);  

            println("features: " + data.getFeatures);
            println("predicted:      " + predicted);
            println("actual:         " + data.getLabels);
            println("===================");

      }
     
    } 
    
   def covertDFtoLabledDataSet( arrDataList: java.util.List[Double], numofInput : Int) : java.util.List[DataSet]  = {
     var FeatureArray  = new  Array[Double](numofInput);
     var LabelArray  = new  Array[Double](numofInput);
     var  dataSetList : java.util.List[DataSet] = new java.util.ArrayList[DataSet]();
   
  breakable { for( i <- 0 to arrDataList.size()-1){
     if ( i > arrDataList.size()- (numofInput +1) ){
          break;
        }
      for( j <- 0  to numofInput -1) {
       FeatureArray(j) = (arrDataList.get(i+j).hashCode() % 10)/10.toDouble;
       LabelArray(j) = (arrDataList.get(i+j+1).hashCode() % 10)/10.toDouble;
      }
        println("Feature: " );
       FeatureArray.foreach { row => println(row)}
      println("Label: " );
       LabelArray.foreach { row => println(row)}
       println("========" );
       dataSetList.add(new DataSet(Nd4j.create(FeatureArray) , Nd4j.create(LabelArray)));
     } }
  
     dataSetList     
   }
    */
