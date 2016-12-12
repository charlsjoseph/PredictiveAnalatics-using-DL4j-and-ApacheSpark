package poc.examples

 import org.nd4j.linalg.dataset.api.iterator.fetcher.BaseDataFetcher
 import org.nd4j.linalg.dataset.DataSet



class StockDataFetcher( list : java.util.List[DataSet] , labelCount :Int,  featureCount : Int ) extends BaseDataFetcher {
  
  numOutcomes = labelCount;
  inputColumns = featureCount
  totalExamples = 1
  this.initializeCurrFromList(list);

  
   override def  fetch(size : Int) {
    cursor = cursor + 1 ;
  }
}