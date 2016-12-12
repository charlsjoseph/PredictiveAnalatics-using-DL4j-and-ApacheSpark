package poc.analaticsEngine

import poc.common.utilities._ 
import poc.common.constants;
import poc.common.utilities.Scale;
import poc.common.utilities.Util
import poc.common.utilities.StockData

import poc.common.entities._
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
import poc.common.property._
import java.util.Date
/*
 * Base AnalaticsWorkBench implementation 
 * 
 */

class AnalaticsStockWorkBench extends AnalaticsWorkBench{

        def getModel(modelProgramId: String) : StockModel = {
            val path= constants.modelDir + modelProgramId;
           val modelFile = new File(path);
           println("reading Model from filesysem : " + modelFile.getAbsolutePath + " : " + modelFile.getAbsoluteFile  + " : " + modelFile.getTotalSpace);
           val model = ModelSerializer.restoreMultiLayerNetwork(modelFile)        
           
           val propPath= constants.modelDir + modelProgramId + ".xml" ;
           val stockModelProp =  Property.fromXml(scala.xml.XML.loadFile(propPath))
           StockModel(model, stockModelProp.globalMax);
           
        }
      def getHyperParameter(modelProgramId: String) : HyperParameters ={
        
        // use Arbiter Optimization to determine a good hyper parameters 
        /*
         * There is a limitation for Arbiter optimization. Arbiter is not yet integrated to spark. It is only work on local.
         * we can't use parameters optimized by the Arbiter done in local. Because in spark, it does parameter averaging and have to 
         * change the parameters again to get a proper results.
         * For the time being, we use the hard coded parameters which is done after trial and error method.  
         */
       var numiteration = 150
        if (modelProgramId=="AAPL") {
          numiteration = 150;
        } else {
          numiteration = 300
        }
        HyperParameters(0.18 , .9, numiteration , 10, 123, 3, 3) 
      }
      
      
      def fetchTrainingDataSet( modelProgramId: String) : java.util.List[Double] ={
        
        return StockData.fetchStockData(modelProgramId);
      }
      
      def fetchMaxRangeValue( modelProgramId: String) : java.util.List[Int] ={
        
        return StockData.fetchMaxAndMinStockData(modelProgramId);
      }
      
      def trainModel(modelProgramId: String ,  hyperParamters : HyperParameters ) : StockModel={
  
        val arr1List=   fetchTrainingDataSet(modelProgramId);
        val maxMinList =    fetchMaxRangeValue(modelProgramId);
        println("maxMinList : " + maxMinList.get(0));
        
        val dataSetList = Scale.scaleToSigmoidDatset(arr1List,hyperParamters.numInputs, maxMinList.get(0) ) ;
        // Shuffle the total data set 
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

        val sparkBufferIteration = 100
        
        val  numHiddenNodes = 2* hyperParamters.numInputs +1;
        // Parallelize the data into spark cluster 
        val trainData: JavaRDD[DataSet] = Spark.sc.parallelize(trainSetList).cache;
        // DL4j Neural configuration and set the parameters from hyperParamters object
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
                 
        // plugging in the MLP Neural Net to Spark and creating SparkDl4jMultiLayer 
       val sparkNeuralNetwork = new SparkDl4jMultiLayer(Spark.sc, conf)


        // train the model with training dataset 
        (0 until hyperParamters.nEpochs).foreach { _ => 
                sparkNeuralNetwork.fitDataSet(trainData);
        } 
         val  dataEvalIterator = evalDataSetList.iterator();
         var positiveEval = 0;
        while(dataEvalIterator.hasNext()) {
         var data = dataEvalIterator.next();
           val  predicted = sparkNeuralNetwork.getNetwork.output(data.getFeatures);  
            println("features: " + data.getFeatures);
            println("predicted:      " + predicted);
            println("actual:         " + data.getLabels);
             println("predicted.getDouble(2) : " + predicted.getDouble(2) );
             println("data.getLabels.getDouble(2): " + data.getLabels.getDouble(2) );

            if (Util.round(predicted.getDouble(2)).equals(Util.round(data.getLabels.getDouble(2)))){
             println("coming here");
             println("predicted.getDouble(2) : " + predicted.getDouble(2) );
             println("data.getLabels.getDouble(2): " + data.getLabels.getDouble(2) );
              positiveEval = positiveEval + 1 ;
            }
            println("===================");
        }
         println("===================Training stats=======================");
         println("max " + maxMinList.get(0)  );
         println("total data set count " + dataSetList.size() ) ;
         println("total training  set count " + trainSetList.size() ) ;
         println("total evaluation set count " + evalDataSetList.size() ) ;
          println("positiveEval count:  " + positiveEval ) ;
         val accuracy = positiveEval/evalDataSetList.size();
         println("Accuracy of model " + accuracy ) ;

         println("=========================================================");
         // return the trained model 
         val model = new StockModel(sparkNeuralNetwork.getNetwork(), maxMinList.get(0)  );
         
         model
    }
       
      def saveModel(model : StockModel , modelProgramId: String) : Boolean = {
        val path= constants.modelDir + modelProgramId ;
         val modelFile = new File(path);
         println("writing into : " + modelFile.getAbsolutePath + " : " + modelFile.getAbsoluteFile  + " : " + modelFile.getTotalSpace);
         ModelSerializer.writeModel(model.model, modelFile, true);
         /* save the model property file as well 
         */
        val propertObj = new StockModelProp(modelProgramId, model.globalMax);
         propertObj.toXml 
         val propPath= constants.modelDir + modelProgramId + ".xml" ;
         //val propFile = new File(propPath);
         scala.xml.XML.save(propPath, propertObj.toXml )
         true
      }
      
      def doCompleteTraining(modelProgramId: String):  StockModel = {
            val hyperParameter =  getHyperParameter(modelProgramId);
            trainModel(modelProgramId, hyperParameter);

      }
      
      def doOnlineTraining(modelProgramId : String , startDt : String , endDt : String , model :StockModel ) : StockModel ={
           val arr1List=   StockData.fetchStockBatchData(modelProgramId, startDt, endDt);
           val maxMinList =    StockData.fetchMaxAndMinStockData(modelProgramId);
         var  stockModel = new StockModel(null, 0);
         if (maxMinList.get(0) !=  model.globalMax) {
            println("Current Max has crossed the Global max: Model need a Complete training" )
            stockModel =  doCompleteTraining(modelProgramId);
            
         } else {
           println("Online training Starting.." )
           
           val sparkNeuralNetwork = new SparkDl4jMultiLayer(Spark.sc, model.model)
           sparkNeuralNetwork.getNetwork.conf().setNumIterations(2);
           val dataSetList = Scale.scaleToSigmoidDatset(arr1List,3, model.globalMax ) ;
           val rng = new Random(12)
           Collections.shuffle(dataSetList, rng)
           val trainData: JavaRDD[DataSet] = Spark.sc.parallelize(dataSetList).cache;
            (0 until 2).foreach { _ => 
                sparkNeuralNetwork.fitDataSet(trainData);
               stockModel =  new StockModel(sparkNeuralNetwork.getNetwork,model.globalMax)
            } 
         }
         stockModel
      }

       def predictTSValue(modelProgramId: String, inputList : java.util.List[Double] ) : Double = {
         val model = getModel(modelProgramId);
         val globalMax = model.globalMax;
         val dataSetList = Scale.scaleToSigmoidInput(inputList,3, globalMax  ) ;
         val dataSetIter = dataSetList.iterator();
         /* take the first value */
         var data = dataSetIter.next();
         /*predict the ouput */
          println("feature input " + data.getFeatures );
                    println("globalMax " + globalMax );

         val  predicted = model.model.output(data.getFeatures); 
         println("predicted output " + predicted );
                   println("predicted.getDouble(0) " + predicted.getDouble(0) );

         val predictedTSData = predicted.getDouble(0) * globalMax;
         predictedTSData;
       }

     
        
}