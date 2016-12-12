package test.poc.analatics

import java.util.ArrayList;
import poc.analaticsEngine.SP500WorkBench

object SP500Prediction {
        def main(args: Array[String]): Unit = {
        if (args(0) == "testTrain") {
          testTrainSaveModel();
        } else if (args(0) == "testPredict") {
          testPredictTimeSeriesData();
        } 
}
        
         def testTrainSaveModel()={
       val stockPredict = new SP500WorkBench();
       val programId = "SP500";
       val hyperParameter =  stockPredict.getHyperParameter("SP500");
       val model = stockPredict.trainModel(programId, hyperParameter);
       stockPredict.saveModel(model,programId);
        }
         
       def testPredictTimeSeriesData() {
             val programId = "SP500";
             val stockPredict = new SP500WorkBench();
             val inputList : java.util.List[Double] = new ArrayList[Double];
             inputList.add(2139.120117);
             inputList.add(2139.76001);
             inputList.add(2163.120117);
             val predictedValue = stockPredict.predictTSValue(programId, inputList)
             println("Input List: " + inputList);
             println("Actual Value : " + 2177.179932);
             println("Predicted Time Series Value for " +  programId + ": " + predictedValue);

    }
         
}
