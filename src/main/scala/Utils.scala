import java.io.{FileWriter, BufferedWriter, File}

/**
 * Created by Viresh on 5/29/2015.
 */
object Utils {

  def createFileToLoadInRDD(contents : String) : File = {

    val file = File.createTempFile("tempfile", ".tmp")

    val bw = new BufferedWriter(new FileWriter(file));
    bw.write(contents);
    bw.close();

    file
  }

  def deleteFile(file: File) : Boolean = {
    file.delete()

  }




}
