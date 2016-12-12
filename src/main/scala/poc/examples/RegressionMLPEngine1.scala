
package poc.examples

import java.io.File
import java.util.Collections
import poc.common.utilities._;


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
import org.deeplearning4j.util.ModelSerializer

import scala.util.control.Breaks._

import org.deeplearning4j.util.ModelSerializer
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;




//need to be removed 

import org.nd4j.linalg.dataset.api.iterator.fetcher._
import org.nd4j.linalg.dataset.api.iterator._
import jersey.repackaged.com.google.common.cache.Weigher




object RegressionMLPEngine1 {
    def main(args: Array[String]): Unit = {
        val seed : Long = 123
        val learningRate = 0.1920904871327523
        val nEpochs = 10
        val numInputs = 3
        val numOutputs = 3
        val numHiddenNodes = 7
        val iterationCount_DL4j = 120
        val sparkBufferIteration = 100
        val iterationCount_spark = iterationCount_DL4j + sparkBufferIteration
         val momentum = 0.8851524008535444;

      val arr1List=   fetchStockData("YHOO");
      val maxMinList =     fetchMaxAndMinStockData("YHOO");
       
       println("min" + maxMinList.get(1) ) ;
       println("max" + maxMinList.get(0) ) ;

      println(arr1List);
      
      var dataSetList = convertToNormalizedDataSet(arr1List,numInputs, maxMinList.get(0)  ) ;
      
      
      val rng = new Random(12)
      Collections.shuffle(dataSetList, rng)
      
      var trainSetList : java.util.List[DataSet] = new java.util.ArrayList[DataSet]();
      var evalDataSetList : java.util.List[DataSet] = new java.util.ArrayList[DataSet]();
     var divider = (dataSetList.size()*0.8).toInt 
      println("size " + dataSetList.size());
           println("divider " + divider);


     trainSetList.addAll(dataSetList.subList(divider + 1, dataSetList.size() -1))
     evalDataSetList.addAll(dataSetList.subList(0, divider))

      
      val trainData: JavaRDD[DataSet] = Spark.sc.parallelize(trainSetList).cache;

        // Initializing the MLP Neural Net with 3 layers for 3 input and 3 output 
        val conf: MultiLayerConfiguration = new NeuralNetConfiguration.Builder()
                .seed(seed)
      //        .iterations(iterationCount_DL4j)
              .iterations(iterationCount_spark)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .learningRate(learningRate)
                .weightInit(WeightInit.XAVIER)
               .updater(Updater.NESTEROVS).momentum(momentum)    
                .list()
                .layer(0, new DenseLayer.Builder().nIn(numInputs).nOut(numHiddenNodes)
                      .activation("sigmoid").build()).backprop(true)
                .layer(1, new DenseLayer.Builder().nIn(numHiddenNodes).nOut(numHiddenNodes)
                      .activation("sigmoid").build()).backprop(true)
                .layer(2, new OutputLayer.Builder(LossFunction.MSE)
                        .activation("identity")
                         .nIn(numHiddenNodes).nOut(numOutputs).build()).backprop(true)
                 .build()

                 
      // plugging in the MLP Neural Net to Spark and creating SparkDl4jMultiLayer 
     val sparkNeuralNetwork = new SparkDl4jMultiLayer(Spark.sc, conf)

      // train the model with training dataset 
      (0 until nEpochs).foreach { _ => 
                sparkNeuralNetwork.fitDataSet(trainData);

      } 
  //val modelFile = new File("/home/cloudera/NeuralNets/YHOO.model");
  //println("writing into : " + modelFile.getAbsolutePath + " : " + modelFile.getAbsoluteFile  + " : " + modelFile.getTotalSpace);
  //ModelSerializer.writeModel(sparkNeuralNetwork.getNetwork, modelFile, true);
  //val  model = ModelSerializer.restoreMultiLayerNetwork(modelFile)        
      // making eval training set :
      /* val sqlEvalDF = Spark.hiveContext.sql("select Date, Close  from stock_partitioned_data where symbol = 'YHOO' and date between '2016-01-01' and '2016-03-01 order by date' ");
       var  arrevalList : java.util.List[Double] = new java.util.ArrayList[Double]();

      sqlEvalDF.collect().foreach { row => 
        arrevalList.add(row.getDouble(1));
      }
      println(arrevalList);
      val dataEvalSetList = convertToNormalizedDataSet(arrevalList,numInputs, maxMinList.get(0)   ) ;
      val dataEval: JavaRDD[DataSet] = Spark.sc.parallelize(dataEvalSetList);
      */
     val  dataEvalIterator = evalDataSetList.iterator();
     
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
      
      
      println("max " + maxMinList.get(0)  );
      println("total data set count " + dataSetList.size() ) ;
      println("total training  set count " + trainSetList.size() ) ;
      println("total evaluation set count " + evalDataSetList.size() ) ;

    } 
    
   def convertToNormalizedDataSet( arrDataList: java.util.List[Double], numofInput : Int, max: Double ) : java.util.List[DataSet] = {
     var FeatureArray  = new  Array[Double](numofInput);
     var LabelArray  = new  Array[Double](numofInput);
     var  dataSetList : java.util.List[DataSet] = new java.util.ArrayList[DataSet]();
   
  breakable { for( i <- 0 to arrDataList.size()-1){
     if ( i > arrDataList.size()- (numofInput +1) ){
          break;
        }
      for( j <- 0  to numofInput -1) {
       FeatureArray(j) = arrDataList.get(i+j)/max;
       LabelArray(j) = arrDataList.get(i+j+1)/max;
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
   
     def fetchStockData(symbol : String): java.util.List[Double] = {

          val hiveQueryForStockCloseValue =  "select Date, Close  from stock_partitioned_data where symbol = '"  + symbol + "' order by Date" ;
          
          val sqlData = Spark.hiveContext.sql(hiveQueryForStockCloseValue);
          var  arr1List : java.util.List[Double] = new java.util.ArrayList[Double]();
                 sqlData.collect().foreach { row => 
                  arr1List.add(row.getDouble(1));
                 }
                 arr1List
      }
     
     def fetchMaxAndMinStockData(symbol : String): java.util.List[Int] = {
          val hiveQueryForMinMaxStockCloseValue =  "select max(Close), min(Close)  from stock_partitioned_data where symbol = '" + symbol + "'" ; 
           var  maxMinList : java.util.List[Int] = new java.util.ArrayList[Int]();
       val maxmin = Spark.hiveContext.sql(hiveQueryForMinMaxStockCloseValue);
       maxmin.collect().foreach { rec => 
           var max = rec.getDouble(0).ceil.toInt
           var min = rec.getDouble(1).toInt
           var buffer  = .5 * max;
           var maxBuffer = max + buffer.toInt;
           maxMinList.add(maxBuffer);
           maxMinList.add(min);
        }
        maxMinList
      }

    
}