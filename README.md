Predictive Analaytics using DeepLearning4j
=========================================


Prerequsites: 
=============

Hive table and data to be loaded using hive ddl in PredictiveAnalatics/StockTimeSeries/hive_tables.ddl
Stock Time Series data Location : PredictiveAnalatics/StockTimeSeries


Compile:
======== 

compile the jar file using below command. Make sure sbt is installed.

Go to Project Home dir 
==> sbt assembly 

Convert into eclipse Project: 
===============================
Go to Project Home dir 
==> sbt eclipse


Executing : 

spark-submit --class poc.analaticsEngine.StockPrediction --master local[2] /home/cloudera/workspace/PredictiveAnalatics/target/scala-2.10/PredicitiveAnalatics-assembly-1.0.jar





