package poc.analaticsEngine

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import poc.common.entities._


/* Abstract Class for AnalaticsWorkBench
 * 
 */


abstract class AnalaticsWorkBench {
      /* Abstract method for fetching model from disk */
      def getModel(modelProgramId: String):StockModel ;
      
      /* Abstract method to determine the hyperParamters  */
      def getHyperParameter(modelProgramId: String) : HyperParameters;
      
      /* Abstract method to train the model*/
      def trainModel(modelProgramId: String ,  hyperParamters : HyperParameters ) : StockModel
      
      /* Abstract method to save the model into disk */
      def saveModel(model : StockModel , modelProgramId: String) : Boolean       
      
      /* Abstract method for complete training or fresh training  */
      def doCompleteTraining(modelProgramId: String) : StockModel
      
      /* Abstract method for online feed training ( mainly for daily training )*/
      def doOnlineTraining(modelProgramId: String ,startDt : String , endDt : String, model :StockModel )  : StockModel 
      
      /* Abstract method for predicting  */
      def predictTSValue(modelProgramId: String, inputList : java.util.List[Double] ) : Double 
      
      /* Abstract method to implement the fetch logic for different training data set */
      def fetchTrainingDataSet( modelProgramId: String) : java.util.List[Double]
      
      /* Abstract method to implement the fetch the max range Value for different training data set */
      def fetchMaxRangeValue( modelProgramId: String) : java.util.List[Int]
      
}