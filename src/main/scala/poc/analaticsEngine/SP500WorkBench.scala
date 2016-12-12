package poc.analaticsEngine

import poc.common.entities._
import poc.common.utilities.SP500Data

/*
 * SP500WorkBench implementation 
 */

class SP500WorkBench extends AnalaticsStockWorkBench{
  
      override def getHyperParameter(modelProgramId: String) : HyperParameters ={
        
        // use Arbiter Optimization to determine a good hyper parameters 
        /*
         * There is a limitation for Arbiter optimization. Arbiter is not yet integrated to spark. It is only work on local.
         * we can't use parameters optimized by the Arbiter done in local. Because in spark, it does parameter averaging and have to 
         * change the parameters again to get a proper results.
         * For the time being, we use the hard coded parameters which is done after trial and error method.  
         */
        val numiteration = 300
        HyperParameters(0.1920904871327523 , .8851524008535444, numiteration , 10, 123, 3, 3) 
      }
       
       override def fetchTrainingDataSet( modelProgramId: String) : java.util.List[Double] ={
        
        return SP500Data.fetchSP500Data()
      }
      
      override def fetchMaxRangeValue( modelProgramId: String) : java.util.List[Int] ={
        
        return SP500Data.fetchMaxAndMinSP500Data()
      }
  
}