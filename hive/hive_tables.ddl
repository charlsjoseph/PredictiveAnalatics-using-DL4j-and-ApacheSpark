/* Stock partitioned Table Creation and Data Load */
SET hive.support.sql11.reserved.keywords=false;
CREATE EXTERNAL TABLE stock_partitioned_data (
date String,
open String,
high String,
low String,
close String,
volume String,
adj_close String
)
PARTITIONED BY (symbol string)
ROW FORMAT SERDE 'org.apache.hadoop.hive.serde2.OpenCSVSerde' 
STORED AS TEXTFILE
tblproperties("skip.header.line.count"="1");




LOAD DATA LOCAL INPATH '/home/cloudera/StockTimeSeries/AAPL.txt'  INTO TABLE stock_partitioned_data PARTITION (symbol='AAPL');
LOAD DATA LOCAL INPATH '/home/cloudera/StockTimeSeries/AAPL1.txt'  INTO TABLE stock_partitioned_data PARTITION (symbol='AAPL');
LOAD DATA LOCAL INPATH '/home/cloudera/StockTimeSeries/MSFT.txt'  INTO TABLE stock_partitioned_data PARTITION (symbol='MSFT');
LOAD DATA LOCAL INPATH '/home/cloudera/StockTimeSeries/MSFT1.txt'  INTO TABLE stock_partitioned_data PARTITION (symbol='MSFT');
LOAD DATA LOCAL INPATH '/home/cloudera/StockTimeSeries/YHOO.txt'  INTO TABLE stock_partitioned_data PARTITION (symbol='YHOO');
LOAD DATA LOCAL INPATH '/home/cloudera/StockTimeSeries/YHOO1.txt'  INTO TABLE stock_partitioned_data PARTITION (symbol='YHOO');

/* SP 500 Table Creation and Data Load */

CREATE EXTERNAL TABLE sp500_stock_data (
Date date,
Open Double,
High Double,
Low Double,
Close Double,
Volume Double,
AdjClose Double)
ROW FORMAT DELIMITED FIELDS TERMINATED BY ','
STORED AS TEXTFILE;

LOAD DATA LOCAL INPATH '/home/cloudera/workspace/PredictiveAnalatics/SP500/s&p500_data.txt'  INTO TABLE sp500_stock_data;






                                                                          
