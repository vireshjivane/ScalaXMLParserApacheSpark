/**
 * Created by Viresh on 5/26/2015.
 */

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import com.amazonaws.AmazonClientException
import com.amazonaws.AmazonServiceException
import com.amazonaws.ClientConfiguration
import com.amazonaws.Protocol
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.Bucket
import com.amazonaws.services.s3.model.DeleteObjectRequest
import com.amazonaws.services.s3.model.GetObjectRequest
import com.amazonaws.services.s3.model.PutObjectRequest
import com.amazonaws.services.s3.model.S3Object
import org.apache.spark.rdd.RDD

import scala.util.control.Breaks._
import com.amazonaws.AmazonClientException
import com.amazonaws.AmazonServiceException
import com.amazonaws.ClientConfiguration
import com.amazonaws.Protocol
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model._
import scala.collection.mutable.ArrayBuffer
import java.util.List

class ScalaApplicationS3 {

  var s3Client: AmazonS3Client = null

  def initializeS3Client(): Boolean = {

    val accessKey = "##############################"
    val secretKey = "########################################"
    val credentials = new BasicAWSCredentials(accessKey, secretKey)
    val clientConfig = new ClientConfiguration()

    clientConfig.setProtocol(Protocol.HTTP)

    s3Client = new AmazonS3Client(credentials, clientConfig)
    val region = Region.getRegion(Regions.US_WEST_2)

    s3Client.setRegion(region)

    true
  }

  def createS3Bucket(bucketName: String): Boolean = {

    try {
      println("Creating bucket " + bucketName + "\n")
      s3Client.createBucket(bucketName)
      println("Bucket created !")
    } catch {
      case ase: AmazonServiceException =>
        println("Caught an AmazonServiceException, which means your request made it "
          + "to Amazon S3, but was rejected with an error response for some reason.")
        println("Error Message:    " + ase.getMessage())
        println("HTTP Status Code: " + ase.getStatusCode())
        println("AWS Error Code:   " + ase.getErrorCode())
        println("Error Type:       " + ase.getErrorType())
        println("Request ID:       " + ase.getRequestId())

      case ace: AmazonClientException =>
        println("Caught an AmazonClientException, which means the client encountered "
          + "a serious internal problem while trying to communicate with S3, "
          + "such as not being able to access the network.")
        println("Error Message: " + ace.getMessage())


    }
    true
  }

  def putObjectFromFilePathInBucket(objectKey: String, bucketName: String, filePath: String): Boolean = {

    val file = new File(filePath)

    println("Uploading a new object to S3 from a file\n")

    try {
      s3Client.putObject(new PutObjectRequest(bucketName, objectKey, file))
    } catch {
      case ase: AmazonServiceException =>
        println("Caught an AmazonServiceException, which means your request made it "
          + "to Amazon S3, but was rejected with an error response for some reason.")
        println("Error Message:    " + ase.getMessage())
        println("HTTP Status Code: " + ase.getStatusCode())
        println("AWS Error Code:   " + ase.getErrorCode())
        println("Error Type:       " + ase.getErrorType())
        println("Request ID:       " + ase.getRequestId())

      case ace: AmazonClientException =>
        println("Caught an AmazonClientException, which means the client encountered "
          + "a serious internal problem while trying to communicate with S3, "
          + "such as not being able to access the network.")
        println("Error Message: " + ace.getMessage())
    }

    true
  }


  def putFileObjectInBucket(bucketName: String, objectKey: String, file: File): Boolean = {

    println("Uploading a new object to S3 from a file\n")

    try {
      s3Client.putObject(new PutObjectRequest(bucketName, objectKey, file))
    } catch {
      case ase: AmazonServiceException =>
        println("Caught an AmazonServiceException, which means your request made it "
          + "to Amazon S3, but was rejected with an error response for some reason.")
        println("Error Message:    " + ase.getMessage())
        println("HTTP Status Code: " + ase.getStatusCode())
        println("AWS Error Code:   " + ase.getErrorCode())
        println("Error Type:       " + ase.getErrorType())
        println("Request ID:       " + ase.getRequestId())

      case ace: AmazonClientException =>
        println("Caught an AmazonClientException, which means the client encountered "
          + "a serious internal problem while trying to communicate with S3, "
          + "such as not being able to access the network.")
        println("Error Message: " + ace.getMessage())
    }

    true
  }

  def getObject(bucketName: String, objectKey: String, localFileLocation: String): S3Object = {

    println("Downloading an object")

    val s3Object = s3Client.getObject(new GetObjectRequest(bucketName, objectKey))

    s3Object
  }

  def getAllBuckets(): List[Bucket] = {

    println("Returning buckets")

    val buckets = s3Client.listBuckets()

    buckets
  }

  def listingAllBuckets(): ArrayBuffer[String] = {

    println("Listing all buckets")

    val buckets = getAllBuckets()
    var bucketList = new ArrayBuffer[String]

    val size = buckets.size()

    var counter = 0
    for (counter <- 0 to size - 1) {
      bucketList.append(buckets.get(counter).getName)
    }
    bucketList
  }

  def getS3ObjectContentsThroughInputStream(bucketName: String, objectKey: String): InputStream = {

    val s3Object = s3Client.getObject(new GetObjectRequest(bucketName, objectKey))

    val reader = new BufferedReader(new InputStreamReader(s3Object.getObjectContent))

    val objectData = s3Object.getObjectContent

    objectData

  }

  def loadObjectFromS3ToSparkRDD(bucketName: String, objectKey: String) : RDD[String] = {

   // val uri = "s3n://"+ accessKey + ":" + secretKey +"@" + bucketName + "/" + objectKey

    val uri = "s3n://"+ bucketName + "/" + objectKey

    val context = SparkConfiguration.getConfiguredSpark

    val inputRDD = context.textFile(uri)

    inputRDD

  }




  def getS3ObjectContentsInFile(bucketName: String, objectKey: String, localFile: String): File = {

    println("Downloading an object")

    val s3Object = s3Client.getObject(new GetObjectRequest(bucketName, objectKey))

    val reader = new BufferedReader(new InputStreamReader(s3Object.getObjectContent))

    val objectData = s3Object.getObjectContent

    val file = new File(localFile)

    file.deleteOnExit()

    if (!file.exists()) {
      file.createNewFile()
    }

    val bw = new BufferedWriter(new FileWriter(file))

    breakable {

      while (true) {

        var line = reader.readLine()

        if (line == null) {
          break
        }
        else {
          bw.write(line)
        }
      }
    }

    bw.close();

    file
  }

  def deleteObject(bucketName: String, objectKey: String): Boolean = {

    s3Client.deleteObject(new DeleteObjectRequest(bucketName, objectKey))

    true

  }

  def deleteAllObjectsWithSpecificNameFromAllBucketsOfSpecificName(bucketName: String, objectKey: String): Boolean = {

    val bucketObjects = getAllBuckets()

    for (counter <- 0 to bucketObjects.size() - 1) {

      if (bucketObjects.get(counter).getName.contains(bucketName)) {
        s3Client.deleteObject(bucketObjects.get(counter).getName, objectKey)
      }
    }
    true
  }

}
