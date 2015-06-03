import java.io.File
import java.util.UUID

import org.apache.spark.rdd.RDD

/**
 * Created by Viresh on 6/1/15.
 */
object SparkScalaXMLDriverProgram extends App {

  println("Driver start...")


  var accessKey = "########################"
  var secretKey = "####################################"
  val bucketName = "scalaxmlparsere1257ad9-1cef-4039-8292-6f17724dbde8"
  val objectKey = "anotherInput.xml"

  SparkConfiguration.initializeSpark("ScalaXMLParserOnApacheSpark", "local")
  val context = SparkConfiguration.getConfiguredSpark
  SparkConfiguration.configureSparkContextForS3Access(accessKey,secretKey)

  ScalaXMLParser.initializeParser()
  ScalaS3Client.initializeS3Client(accessKey,secretKey)


  /*
  val file = new File(objectKey)
  ScalaS3Client.putFileObjectInBucket(bucketName, objectKey, file)
  */

  val inputRDD = ScalaS3Client.loadObjectFromS3ToSparkRDD(bucketName, objectKey)

  xmlParserOnApacheSpark(inputRDD)

  println("Driver end...")


  def xmlParserOnApacheSpark(inputRDD: RDD[String]): Boolean = {

    println("Entering parser...")

    val line = inputRDD.collect().mkString("")
    val outputRDD = context.parallelize(List(line))

    outputRDD.foreach(parser)

    println("Exiting parser...")

    true
  }


  def parser(line: String): Boolean = {

    val document = ScalaXMLParser.getDocumentFromString(line)
    val elements = ScalaXMLParser.parserDocument(document)

    val objectKey = "ParserOutput" + UUID.randomUUID()
    InputOutputProcessor.parsedOutputWriter(objectKey + ".txt", elements)
    ScalaS3Client.S3ObjectWriter("scalaxmlparsere1257ad9-1cef-4039-8292-6f17724dbde8", objectKey, elements)

    println("Object written => " + objectKey)

    true
  }

}
