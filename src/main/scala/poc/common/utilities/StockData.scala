package poc.common.utilities

import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.dataset.DataSet
import org.nd4j.linalg.factory.Nd4j
import poc.common._
import scala.util.control.Breaks._

object StockData {
   def fetchStockData(symbol : String): java.util.List[Double] = {

          val hiveQueryForStockCloseValue =  "select Date, cast( close as double) as close   from stock_partitioned_data where symbol = '"  + symbol + "' order by Date" ;

          val sqlData = Spark.hiveContext.sql(hiveQueryForStockCloseValue);
          var  arr1List : java.util.List[Double] = new java.util.ArrayList[Double]();

          sqlData.collect().filter(row => row.get(1) != null).foreach { row => 
                  arr1List.add(row.getDouble(1));
                 }
                 arr1List
      }
     
     def fetchMaxAndMinStockData(symbol : String): java.util.List[Int] = {
          val hiveQueryForMinMaxStockCloseValue =  "select max(cast(Close as double)) , min(cast(Close as double))  from stock_partitioned_data where symbol = '" + symbol + "'" ; 
           var  maxMinList : java.util.List[Int] = new java.util.ArrayList[Int]();
       val maxmin = Spark.hiveContext.sql(hiveQueryForMinMaxStockCloseValue);
       maxmin.collect().foreach (println )
       
       maxmin.collect().filter { x => x.get(0) !=null || x.get(1) !=null }.foreach { rec => 
           var max = rec.getDouble(0).ceil.toInt
           rec.getDouble(0)
                      println("max - unformatted : " + rec.getDouble(0)) ;

           println("max : " + max) ;
           
           var min = rec.getDouble(1).toInt
                      println("min : " + min) ;

           var buffer  = .5 * max;
           var maxBuffer = max + buffer.toInt;
           maxMinList.add(maxBuffer);
           maxMinList.add(min);
        }
        maxMinList
      }
     
        def fetchStockBatchData(symbol : String, startDt : String , endDt : String): java.util.List[Double] = {
          
          val dataRange = "date between '" + startDt + "' and '" + endDt + "' " 

          val hiveQueryForStockCloseValue =  "select Date, Close  from stock_partitioned_data where symbol = '"  + symbol + "' and "  + dataRange + " order by Date" ;
          
          val sqlData = Spark.hiveContext.sql(hiveQueryForStockCloseValue);
          var  arr1List : java.util.List[Double] = new java.util.ArrayList[Double]();
                 sqlData.collect().foreach { row => 
                              println("row  : " + row.getDouble(1)) ;

                  arr1List.add(row.getDouble(1));
                 }
                 arr1List
      }
        
        
      def  fillArrayWithScaledData(scaledFeature : java.util.List[Double] , inputArray: INDArray , colIndex: Int , no_of_examples : Int) = {
        
        val scaleIterator = scaledFeature.iterator()
        var i = 0 ;
        System.out.println("Shape : " + inputArray.shape());
        
       breakable { while(scaleIterator.hasNext()) {
                  System.out.println("i : " + i);
                  System.out.println("colIndex : " + colIndex);

          inputArray.putScalar(Array(i,colIndex), scaleIterator.next())
          i = i+1;
          if ( i == no_of_examples ) 
             break;
        } }
      }
  
        
        
      def fetchSPStockData(startDt : String , endDt : String) : java.util.List[DataSet] = {

        
        val starDate = new java.util.Date(startDt);
        val endDate = new java.util.Date(endDt);

        val duration  = endDate.getTime() - starDate.getTime();
        val noOfDays
        = java.util.concurrent.TimeUnit.DAYS.toDays(duration);
        
        
        val dataRange = "date between '" + startDt + "' and '" + endDt + "' " 
        var  spCompaniesMap  = constants.spCompanyList
        val no_of_examples = Util.deriveNoOfWorkingDays(startDt, endDt); 
        // build an array of no of companies  * no_of_examples 
        var inputArray = Nd4j.zeros(no_of_examples,spCompaniesMap.size)
        var labelArray = Nd4j.zeros(no_of_examples,1)

        val spCompaniesIter = spCompaniesMap.iterator
        while(spCompaniesIter.hasNext) { 
          val curr = spCompaniesIter.next();
           val symbol = curr._2
           var colIndex = curr._1
           var hiveQueryForStockCloseValue =  "select Date, cast( close as double) as close  from stock_partitioned_data where symbol = '"  + symbol + "' and "  + dataRange + " order by Date" ;
           val sqlData = Spark.hiveContext.sql(hiveQueryForStockCloseValue);
           System.out.println("symbol : " + symbol );         
          var  arr1List : java.util.List[Double] = new java.util.ArrayList[Double]();
                 sqlData.collect().foreach { row => 
                              println("row  : " + row.getDouble(1)) ;

                  arr1List.add(row.getDouble(1));
                 }
          // scale into sigmoid values 
         val max = fetchMaxAndMinStockData(symbol)
         if (!max.isEmpty()) {
           val scaledFeature =   Scale.scaleToSigmoidInput(arr1List, max.get(0))
           // fill the inputArray with scaled Values 
           fillArrayWithScaledData(scaledFeature, inputArray, colIndex, no_of_examples);
         }
        }
        // fetch the label from sp500_stock_data 
        
         //var hiveQueryForSnPCloseValue =  "select Date, cast( close as double) as close  from sp500_stock_data " + dataRange + " order by Date" ;
        // val snpCloseData = Spark.hiveContext.sql(hiveQueryForSnPCloseValue);
        // fetch snp Data  
        val spArrList = SP500Data.fetchSP500Data()
         //fetch the max value 
         val max = SP500Data.fetchMaxAndMinSP500Data();
         // scale down 
         val scaledSnPLabels =   Scale.scaleToSigmoidInput(spArrList, max.get(0))
         fillArrayWithScaledData(scaledSnPLabels, labelArray, 0, no_of_examples);

         println(scaledSnPLabels);
       
         val datasetList  = new java.util.ArrayList[DataSet]();
         for (i <- 0 to no_of_examples -1 ) {

           val inputFeatures = inputArray.getRow(i)
           val outputLablel = labelArray.getRow(i)
           datasetList.add(new DataSet(inputFeatures, outputLablel))
         }
         datasetList
      }
        

}