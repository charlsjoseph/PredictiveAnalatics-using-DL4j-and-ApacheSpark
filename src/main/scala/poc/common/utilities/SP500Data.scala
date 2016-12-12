package poc.common.utilities

object SP500Data {
  
     def fetchSP500Data(): java.util.List[Double] = {

          val hiveQueryForStockCloseValue =  "select Date, Close  from sp500_stock_data order by Date" ;
          
          val sqlData = Spark.hiveContext.sql(hiveQueryForStockCloseValue);
          var  arr1List : java.util.List[Double] = new java.util.ArrayList[Double]();
                 sqlData.collect().foreach { row => 
                  arr1List.add(row.getDouble(1));
                 }
                 arr1List
      }
     
     def fetchMaxAndMinSP500Data(): java.util.List[Int] = {
          val hiveQueryForMinMaxStockCloseValue =  "select max(Close), min(Close)  from sp500_stock_data" ; 
           var  maxMinList : java.util.List[Int] = new java.util.ArrayList[Int]();
       val maxmin = Spark.hiveContext.sql(hiveQueryForMinMaxStockCloseValue);
       maxmin.collect().foreach (println )
       
       maxmin.collect().foreach { rec => 
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

}