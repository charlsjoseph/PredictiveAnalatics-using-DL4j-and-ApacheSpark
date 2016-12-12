package test.poc.analatics

import java.util.ArrayList
import poc.analaticsEngine._
import poc.common.entities._
import poc.common._

object StockPrediction {
  
      def main(args: Array[String]): Unit = {
        if (args(0) == "testTrain") {
          
          // build hyperparameters if passed
          if (args(2) != null & args(3) != null && args(4) != null && args(5) != null) {
            val hyperParameter =  new HyperParameters(args(2).toDouble, args(3).toDouble, args(4).toInt , args(5).toInt , 123 , 3 , 3 )
            testTrainSaveModel(args(1) , hyperParameter );

          }else {
            testTrainSaveModel(args(1) , null  );
          }
        } else if (args(0) == "testPredict") {
          val arrInput = Array(args(2).toDouble, args(3).toDouble, args(4).toDouble, args(5).toDouble)
          testPredictTimeSeriesData(args(1), arrInput );
        } else if (args(0) == "buildAllModels") {
           val hyperParameter =  new HyperParameters(args(2).toDouble, args(3).toDouble, args(4).toInt , args(5).toInt , 123 , 3 , 3 )
          testTrainSaveAllModel(hyperParameter);
          
        }

       
     }

   def testTrainSaveModel(programId: String ,hyperParameter : HyperParameters ) {
       val stockPredict = new AnalaticsStockWorkBench();
       
       if (hyperParameter == null ){
                val defaultHyperParameter =  stockPredict.getHyperParameter(programId);
                stockPredict.saveModel(stockPredict.trainModel(programId, defaultHyperParameter),programId);
       } else {
                stockPredict.saveModel(stockPredict.trainModel(programId, hyperParameter),programId);
       }

    }
   
   
   
   def testTrainSaveAllModel(hyperParameter : HyperParameters ) {
       val stockPredict = new AnalaticsStockWorkBench();
       val spCompanyList = constants.spCompanyList;
       val spCompanyListIter = spCompanyList.iterator;
       while(spCompanyListIter.hasNext) {
         
          var programId = spCompanyListIter.next()._2;
          if (hyperParameter == null ){
                val defaultHyperParameter =  stockPredict.getHyperParameter(programId);
                stockPredict.saveModel(stockPredict.trainModel(programId, defaultHyperParameter),programId);
          } else {
                stockPredict.saveModel(stockPredict.trainModel(programId, hyperParameter),programId);
                 }
         }
    }
   
   def testOnlineTrainModel() {
       val stockPredict = new AnalaticsStockWorkBench();
       val programId = "YHOO";
       val startDt = "2016-08-01"
       val endDt = "2016-09-01"
       val model =stockPredict.getModel(programId);
       val newModel = stockPredict.doOnlineTraining(programId, startDt, endDt, model)
       stockPredict.saveModel(newModel, programId)
    }   
    def testPredictTimeSeriesData(programId: String , arrofInput : Array[Double] ) {
             val stockPredict = new AnalaticsStockWorkBench();
             val inputList : java.util.List[Double] = new ArrayList[Double];
             
             inputList.add(arrofInput(0));
             inputList.add(arrofInput(1));
             inputList.add(arrofInput(2));
             val predictedValue = stockPredict.predictTSValue(programId, inputList)
             println("Input List: " + inputList);
             println("Actual Value : " + arrofInput(3));
             println("Predicted Time Series Value for " +  programId + ": " + predictedValue);
    }
  
}