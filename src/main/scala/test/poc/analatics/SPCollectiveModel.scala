package test.poc.analatics
import poc.analaticsEngine._

object SPCollectiveModel {
  
   def main(args: Array[String]): Unit = {
   val snp500Net  =  new SP500CollectionWorkBench();
   val hyperParamters = snp500Net.getHyperParameter("SnPCollectionModel")
   snp500Net.setDateBoundary(args(0), args(1)) // set the start date and end date
   val model = snp500Net.trainModel("SnPCollectionModel", hyperParamters)
   snp500Net.saveModel(model, "SnpCollectiveModel")
   }
  
}