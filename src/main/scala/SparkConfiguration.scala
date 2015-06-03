import org.apache.spark.{SparkContext, SparkConf}
import org.apache.spark.api.java.JavaSparkContext

/**
 * Created by Viresh on 5/27/2015.
 */

object SparkConfiguration {

  var conf: SparkConf = null
  var context: SparkContext = null

  def initializeSpark(appName: String, master: String) {

    conf = new SparkConf()
      .setAppName(appName)
      .setMaster(master)

    context = new SparkContext(conf)
  }

  def configureSparkContextForS3Access(accessKey: String, secretKey: String): Unit = {

    val hadoopConf = context.hadoopConfiguration;
    hadoopConf.set("fs.s3n.impl", "org.apache.hadoop.fs.s3native.NativeS3FileSystem")
    hadoopConf.set("fs.s3n.awsAccessKeyId", accessKey)
    hadoopConf.set("fs.s3n.awsSecretAccessKey", secretKey)

  }

  def getConfiguredSpark: SparkContext = {

    return context

  }


}
