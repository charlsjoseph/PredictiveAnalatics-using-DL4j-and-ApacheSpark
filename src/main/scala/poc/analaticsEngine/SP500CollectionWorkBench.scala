package poc.analaticsEngine


import poc.common.entities._;
import poc.common.utilities._;
import poc.common._
import org.deeplearning4j.util.ModelSerializer
import java.io.File;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import java.util.{Collections, Random}
import org.nd4j.linalg.dataset.DataSet
import org.nd4j.linalg.factory.Nd4j
import org.apache.spark.api.java.{JavaRDD, JavaSparkContext}

import org.deeplearning4j.nn.conf.{MultiLayerConfiguration, NeuralNetConfiguration, Updater}
import org.deeplearning4j.nn.conf.layers.{DenseLayer, OutputLayer}
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.nn.weights.WeightInit
import org.deeplearning4j.optimize.api.IterationListener
import org.deeplearning4j.optimize.listeners.ScoreIterationListener
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction
import org.deeplearning4j.nn.api.OptimizationAlgorithm
import org.deeplearning4j.spark.impl.multilayer.SparkDl4jMultiLayer
class SP500CollectionWorkBench extends AnalaticsStockWorkBench{
  
  
  var startDt = "2009-01-01"; // default
  var endDt = "2016-01-01"; // default

      override def trainModel(modelProgramId: String ,  hyperParamters : HyperParameters ) : StockModel = {
        // fetch and scale the training set
        // configure and build the NNeral net
        val sparkBufferIteration = 100
        val  numHiddenNodes = 2* hyperParamters.numInputs +1;

    val dataSetList =  StockData.fetchSPStockData(startDt, endDt)
              val rng = new Random(12)
              Collections.shuffle(dataSetList, rng)

        
                // Create training set ( 80% ) and eval set ( 20% )
        var trainSetList : java.util.List[DataSet] = new java.util.ArrayList[DataSet]();
        var evalDataSetList : java.util.List[DataSet] = new java.util.ArrayList[DataSet]();
        var divider = (dataSetList.size()*0.8).toInt 
        println("size " + dataSetList.size());
        println("divider " + divider);
        evalDataSetList.addAll(dataSetList.subList(divider + 1, dataSetList.size() -1))
        trainSetList.addAll(dataSetList.subList(0, divider))

            val conf: MultiLayerConfiguration = new NeuralNetConfiguration.Builder()
                .seed(hyperParamters.seed)
               .iterations(hyperParamters.numIteration + sparkBufferIteration)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .learningRate(hyperParamters.learningRate)
                .weightInit(WeightInit.XAVIER)
               .updater(Updater.NESTEROVS).momentum(hyperParamters.momentum)    
                .list()
                .layer(0, new DenseLayer.Builder().nIn(hyperParamters.numInputs).nOut(numHiddenNodes)
                      .activation("sigmoid").build()).backprop(true)
                .layer(1, new DenseLayer.Builder().nIn(numHiddenNodes).nOut(numHiddenNodes)
                      .activation("sigmoid").build()).backprop(true)
                .layer(2, new OutputLayer.Builder(LossFunction.MSE)
                        .activation("identity")
                         .nIn(numHiddenNodes).nOut(hyperParamters.numOutputs).build()).backprop(true)
                 .build()
                 
                  val sparkNeuralNetwork = new SparkDl4jMultiLayer(Spark.sc, conf)
               val trainData: JavaRDD[DataSet] = Spark.sc.parallelize(trainSetList).cache;
               // train the model with training dataset 
        (0 until hyperParamters.nEpochs).foreach { _ => 
                sparkNeuralNetwork.fitDataSet(trainData);
        } 
         val  dataEvalIterator = evalDataSetList.iterator();
     
        while(dataEvalIterator.hasNext()) {
         var data = dataEvalIterator.next();
           val  predicted = sparkNeuralNetwork.getNetwork.output(data.getFeatures);  
            println("features: " + data.getFeatures);
            println("predicted:      " + predicted);
            println("actual:         " + data.getLabels);
            println("===================");
        }
         println("===================Training stats=======================");
         println("total data set count " + dataSetList.size() ) ;
         println("total training  set count " + trainSetList.size() ) ;
         println("total evaluation set count " + evalDataSetList.size() ) ;
         println("=========================================================");
         // return the trained model 
         StockModel(sparkNeuralNetwork.getNetwork(), 50);
        
      }

       override  def getHyperParameter(modelProgramId: String) : HyperParameters = { 
         val numiteration = 1000 
         val numInputs = constants.spCompanyList.size;
         val numOutputs = 1;

        HyperParameters(0.1920904871327523 , .9, numiteration , 15, 123, numInputs,numOutputs ) 

       }

       def setDateBoundary(startDt : String , endDt : String) {
         
         this.startDt = startDt;
         this.endDt = endDt;
       }
       
            
  
}