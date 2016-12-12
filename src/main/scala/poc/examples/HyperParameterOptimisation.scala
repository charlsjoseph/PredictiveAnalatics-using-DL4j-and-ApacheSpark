package poc.examples

import org.deeplearning4j.arbiter.optimize.api.ParameterSpace;
import org.deeplearning4j.arbiter.optimize.parameter.continuous.ContinuousParameterSpace;
import org.deeplearning4j.arbiter.optimize.parameter.integer.IntegerParameterSpace;
import poc.common.utilities._;

import org.deeplearning4j.arbiter.MultiLayerSpace;
import org.deeplearning4j.arbiter.optimize.api.saving.ResultSaver;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.arbiter.layers.DenseLayerSpace;
import org.deeplearning4j.arbiter.layers.OutputLayerSpace
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.deeplearning4j.arbiter.optimize.candidategenerator.RandomSearchGenerator;
import org.deeplearning4j.arbiter.scoring.multilayer.TestSetLossScoreFunction
import org.deeplearning4j.arbiter.optimize.api.termination.MaxCandidatesCondition;
import org.deeplearning4j.arbiter.optimize.api.termination.MaxTimeCondition;
import org.deeplearning4j.arbiter.optimize.api.termination.TerminationCondition;
import org.deeplearning4j.arbiter.saver.local.multilayer.LocalMultiLayerNetworkSaver;
import org.deeplearning4j.arbiter.optimize.config.OptimizationConfiguration;
import org.deeplearning4j.arbiter.data.DataSetIteratorProvider;
import org.deeplearning4j.arbiter.DL4JConfiguration;
import org.deeplearning4j.arbiter.MultiLayerSpace;
import org.deeplearning4j.arbiter.optimize.api.score.ScoreFunction;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.deeplearning4j.arbiter.optimize.api.CandidateGenerator;
import org.deeplearning4j.arbiter.optimize.api.data.DataProvider;
import org.deeplearning4j.arbiter.optimize.runner.IOptimizationRunner;
import org.deeplearning4j.arbiter.optimize.runner.LocalOptimizationRunner;
import org.deeplearning4j.arbiter.optimize.ui.ArbiterUIServer;
import org.deeplearning4j.arbiter.optimize.ui.listener.UIOptimizationRunnerStatusListener;
import org.deeplearning4j.arbiter.task.MultiLayerNetworkTaskCreator;
import org.deeplearning4j.arbiter.optimize.ui.ArbiterUIServer;
import org.deeplearning4j.arbiter.optimize.api.OptimizationResult;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator._
import org.deeplearning4j.nn.weights.WeightInit
import org.deeplearning4j.nn.conf.Updater
import java.util.{Collections, Random}




import java.util.concurrent.TimeUnit;
import org.deeplearning4j.arbiter.optimize.api.saving.ResultReference;
import java.io.File;
import java.util.List
import java.util.ArrayList

import java.util.concurrent.TimeUnit;
import org.deeplearning4j.datasets.iterator.impl.IrisDataSetIterator
import org.nd4j.linalg.dataset.api.iterator.fetcher.DataSetFetcher
import poc.common.entities.HyperParameters


object HyperParameterOptimisation {
  
  def main(args: Array[String]): Unit = { 
    
     val  learningRateHyperparam = new ContinuousParameterSpace(0.01, 0.3); //Values will be generated uniformly at random between 0.0001 and 0.1 (inclusive)
     val  momentumHyperparam = new ContinuousParameterSpace(0.1, 0.9); //Integer values will be generated uniformly at random between 16 and 256 (inclusive)
     //val  itrationSizeHyperparam = new IntegerParameterSpace(1,250); //Integer values will be generated uniformly at random between 16 and 256 (inclusive)
     val seed = 123456;
     
  
     val hyperparameterSpace = new MultiLayerSpace.Builder()
            //These next few options: fixed values for all models
            .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
            .iterations(150).seed(seed)
             .weightInit(WeightInit.XAVIER)
             .updater(Updater.NESTEROVS).momentum(momentumHyperparam)    

            //Learning rate: this is something we want to test different values for
            .learningRate(learningRateHyperparam)
            .addLayer( new DenseLayerSpace.Builder()
                    //Fixed values for this layer:
                    .nIn(3)  
                    .activation("sigmoid")
                    //One hyperparameter to infer: layer size
                    .nOut(7)
                    .build())
            .addLayer( new OutputLayerSpace.Builder()
                //nIn: set the same hyperparemeter as the nOut for the last layer.
                .nIn(7)
                //The remaining hyperparameters: fixed for the output layer
                .nOut(3)
                .activation("identity")
                .lossFunction(LossFunctions.LossFunction.MSE)
                .build())
            .pretrain(false).backprop(true).build();
     
     
     
     
     // candidate generator 
      val candidateGenerator : CandidateGenerator[DL4JConfiguration] = new RandomSearchGenerator(hyperparameterSpace); //Alternatively: new GridSearchCandidateGenerator<>(hyperparameterSpace, 5, GridSearchCandidateGenerator.Mode.RandomOrder);
      // score function Loss Score 
      val  scoreFunction : ScoreFunction[MultiLayerNetwork,DataSetIterator]= new TestSetLossScoreFunction();
      // termination function 
      val terminationConditions : List[TerminationCondition] = new ArrayList[TerminationCondition]();
      terminationConditions.add(new MaxTimeCondition(15, TimeUnit.MINUTES))
      terminationConditions.add(new MaxCandidatesCondition(30))
      // model saver function 
      val  baseSaveDirectory = "/home/cloudera/arbiterExample/";
        val f = new File(baseSaveDirectory);
        if(f.exists()) f.delete();
        f.mkdir();
        val modelSaver : ResultSaver[DL4JConfiguration,MultiLayerNetwork,Object] = new LocalMultiLayerNetworkSaver(baseSaveDirectory);
       // data provider 
        
        
        /* training data */
       val sqlTrainingDF = Spark.hiveContext.sql("select Date, Close  from stock_partitioned_data where symbol = 'YHOO' and date < '2016-01-01' order by Date");
       var  arr1List : java.util.List[Double] = new java.util.ArrayList[Double]();
       val maxmin = Spark.hiveContext.sql("select max(Close), min(Close)  from stock_partitioned_data where symbol = 'YHOO'");

       var maxBuffer : Int = 0; // Default 

         maxmin.collect().foreach { rec => 
           var max = rec.getDouble(0).ceil.toInt
           var buffer  = .5 * max;
           maxBuffer = max + buffer.toInt;
        }
       


         
      sqlTrainingDF.collect().foreach { row => 
        arr1List.add(row.getDouble(1));
      }
      println(arr1List);
     var dataSetList=  RegressionMLPEngine1.convertToNormalizedDataSet(arr1List, 3, maxBuffer)
           val rng = new Random(12)

     Collections.shuffle(dataSetList, rng);
     dataSetList.size() 
      var trainSetList : java.util.List[DataSet] = new java.util.ArrayList[DataSet]();
      var testSetList : java.util.List[DataSet] = new java.util.ArrayList[DataSet]();

     var divider = (dataSetList.size()*0.8).toInt 
      println("size " + dataSetList.size());
           println("divider " + divider);

     testSetList.addAll(dataSetList.subList(0, divider))
     trainSetList.addAll(dataSetList.subList(divider + 1, dataSetList.size() -1))

     val fetcher : DataSetFetcher =  new StockDataFetcher(trainSetList, 3, 3);
     val datasetTrainIter = new BaseDatasetIterator(4, trainSetList.size(), fetcher) 

     val testfetcher : DataSetFetcher =  new StockDataFetcher(testSetList, 3, 3);
     val datasetTestIter = new BaseDatasetIterator(4, testSetList.size(), testfetcher) 

     
     
//     while(datasetIter.hasNext()){
//       println("Dataset : "  + datasetIter.next());
//     }
     val dataProvider : DataProvider[DataSetIterator] = new DataSetIteratorProvider(datasetTrainIter, datasetTestIter );
        
     
     
                //Given these configuration options, let's put them all together:
        val configuration = new OptimizationConfiguration.Builder()
        .candidateGenerator(candidateGenerator)
        .scoreFunction(scoreFunction)
        .dataProvider(dataProvider)
        .terminationConditions(terminationConditions).modelSaver(modelSaver).build()
        
        
                //And set up execution locally on this machine:
       val runner : IOptimizationRunner[DL4JConfiguration,MultiLayerNetwork,Object] 
            = new LocalOptimizationRunner(configuration, new MultiLayerNetworkTaskCreator());


        //Start the UI
        val server : ArbiterUIServer  = ArbiterUIServer.getInstance();
        runner.addListeners(new UIOptimizationRunnerStatusListener(server));


        //Start the hyperparameter optimization
        runner.execute();

        
        //Print out some basic stats regarding the optimization procedure
        val sb : StringBuilder = new StringBuilder();
        sb.append("Best score: ").append(runner.bestScore()).append("\n")
            .append("Index of model with best score: ").append(runner.bestScoreCandidateIndex()).append("\n")
            .append("Number of configurations evaluated: ").append(runner.numCandidatesCompleted()).append("\n");
        System.out.println(sb.toString());


        //Get all results, and print out details of the best result:
        val  indexOfBestResult = runner.bestScoreCandidateIndex();
        val allResults : List[ResultReference[DL4JConfiguration,MultiLayerNetwork,Object]]  = runner.getResults();

        
        val bestResult :OptimizationResult[DL4JConfiguration,MultiLayerNetwork,Object]  = allResults.get(indexOfBestResult).getResult();
        val bestModel : MultiLayerNetwork  = bestResult.getResult();

        System.out.println("\n\nConfiguration of best model:\n");
        System.out.println(bestModel.getLayerWiseConfigurations().toJson());

        val learningRate = bestModel.getLayerWiseConfigurations().getConf(0).getLearningRateByParam("W");
        val numIteration = bestModel.getLayerWiseConfigurations().getConf(0).getNumIterations;
        val momentum = bestModel.getLayerWiseConfigurations().getConf(0).getLayer.getMomentum
        val hyperParameters =        new  HyperParameters(learningRate ,momentum, numIteration , 10, seed, 3, 3) 

        
         println (" HyperParameters : " + hyperParameters) ;
        
        //Note: UI server will shut down once execution is complete, as JVM will exit
        //So do a Thread.sleep(1 minute) to keep JVM alive, so that network configurations can be viewed
        Thread.sleep(60000);
        
      println("maxBuffer" + maxBuffer ) ;
      println("total data set count " + dataSetList.size() ) ;
      println("TrainingData  set count " + trainSetList.size() ) ;
      println("TestData set count " + testSetList.size() ) ;
        System.exit(0);
        

  }
  
}
