/**
 * Created by Viresh on 5/26/2015.
 * Scala XML Parser running on Apache Spark Engine
 */

import java.io.{FileWriter, BufferedWriter, StringReader}
import java.util.UUID
import javax.xml.parsers.{DocumentBuilder, DocumentBuilderFactory}

import org.apache.spark.rdd.RDD

object ScalaXMLParserApp extends App {

  val bucketName = "scalaxmlparsere1257ad9-1cef-4039-8292-6f17724dbde8"
  val objectKey = "inputXML"

  val S3Client = new ScalaApplicationS3()
  S3Client.initializeS3Client()

  SparkConfiguration.initializeSpark("ScalaXMLParserOnApacheSpark", "local")
  val context = SparkConfiguration.getConfiguredSpark

  val inputRDD = S3Client.loadObjectFromS3ToSparkRDD(bucketName, objectKey)

  parserOnSpark(inputRDD)

  println("Exiting App !")

  def parserOnSpark(inputRDD: RDD[String]): Boolean = {

    println("Entering parser...")

    val line = inputRDD.collect().mkString("")
    val outputRDD = context.parallelize(List(line))

    outputRDD.foreach(parser)

    println("Exiting parser...")
    true
  }

  def parser(line: String): Boolean = {

    val parserObject = new ScalaXMLParser
    val document = parserObject.getDocumentFromString(line)
    val elements = parserObject.parseDocument(document)

    val objectKey = "ParserOutput"+UUID.randomUUID()
    parserObject.documentWriter(objectKey+".txt", elements)
    parserObject.S3ObjectWriter(S3Client, "scalaxmlparsere1257ad9-1cef-4039-8292-6f17724dbde8", objectKey, elements)

    println("Object written => " + objectKey)
    true
  }
}
