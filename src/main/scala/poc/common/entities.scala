package poc.common

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork

object entities {
  
    case class HyperParameters(
    learningRate: Double,
    momentum: Double,
    numIteration : Int,
    nEpochs : Int,
    seed : Long,
    numInputs: Int,
    numOutputs : Int
)


    case class StockModel(
    model: MultiLayerNetwork,
    globalMax : Int
    
    )

}