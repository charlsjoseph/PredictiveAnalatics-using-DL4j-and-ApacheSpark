package poc.common.utilities

object Util {
  
  def round(d: Double) : Double = {
    val rounded =  Math.round(d*100.0)/100.0;
    return rounded;
  }
  
  def deriveNoOfWorkingDays(startDt:String , endDt:String) : Int = {
    
  val dateformater = new java.text.SimpleDateFormat("yyyy-mm-dd");
  val starDate  = dateformater.parse(startDt)
  val endDate =   dateformater.parse(endDt)

  val duration  = endDate.getTime() - starDate.getTime();
  
  val noOfDays = java.util.concurrent.TimeUnit.MILLISECONDS.toDays(duration);
  val noOfYears = noOfDays/365;
  val noOfholidays = 53 + 52 + 9 + 10  
 //  53 Saturdays +  52 Sundays +  9 holidays + 10 buffer For United States 
  val noOOFFDays  = noOfYears * noOfholidays ;
  val noOfWorkingDays = noOfDays - noOOFFDays ;

  println("starDate" + starDate );
  println("endDate" + endDate );
  println("noOfDays" + noOfDays );
  println("noOOFFDays" + noOOFFDays );
  println("noOfWorkingDays" + noOfWorkingDays );
  noOfWorkingDays.toInt
  
  }
  
}