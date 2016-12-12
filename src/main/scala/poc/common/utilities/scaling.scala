package poc.common.utilities

import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.dataset.DataSet
import org.nd4j.linalg.factory.Nd4j
import scala.util.control.Breaks._


object Scale {
  
  
   def scaleToSigmoidDatset( arrDataList: java.util.List[Double], numofInput : Int, max: Double ) : java.util.List[DataSet] = {
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
   
   def scaleToSigmoidInput( arrDataList: java.util.List[Double], numofInput : Int, max: Double ) : java.util.List[DataSet] = {
          var FeatureArray  = new  Array[Double](numofInput);
          var LabelArray  = new  Array[Double](numofInput);
         var  dataSetList : java.util.List[DataSet] = new java.util.ArrayList[DataSet]();

     for( i <- 0 to arrDataList.size()-1){
        FeatureArray(i) = arrDataList.get(i)/max;
     }
       dataSetList.add(new DataSet(Nd4j.create(FeatureArray) , Nd4j.create(LabelArray)));
       dataSetList
   }
   
    def scaleToSigmoidInput( arrDataList: java.util.List[Double] , max: Double ) : java.util.List[Double] = {
         var  scaledArray : java.util.List[Double] = new java.util.ArrayList[Double]();
     
     for(  i <- 0 to arrDataList.size()-1){
           scaledArray.add(i, arrDataList.get(i)/max) 
     }
    scaledArray
    }
   
   def createSPData(): INDArray = {
     
     val no_of_examples = 50  ;
     val sp_count = 55;
     // input feature 
     val inputArray = Nd4j.zeros(no_of_examples,sp_count)
     // output label 
     val outputArray = Nd4j.zeros(no_of_examples,1)
     for (i<-0 to 54) {
        inputArray.putScalar(Array(5,i), 0.52)
     }
  inputArray
   }
   
   
}