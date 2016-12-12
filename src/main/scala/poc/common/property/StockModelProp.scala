package poc.common.property


class StockModelProp(var symbol: String, var globalMax: Int) {

  // (a) convert Stock fields to XML
  def toXml = {
    <modleProperty>
      <symbol>{symbol}</symbol>
      <globalMax>{globalMax}</globalMax>
    </modleProperty>
  }

  override def toString = 
    s"symbol: $symbol,  globalMax: globalMax"

}

object Property {

  // (b) convert XML to a Stock
  def fromXml(node: scala.xml.Node):StockModelProp = {
    val symbol = (node \ "symbol").text
    val globalMax = (node \ "globalMax").text.toInt
    new StockModelProp(symbol, globalMax)
  }

}