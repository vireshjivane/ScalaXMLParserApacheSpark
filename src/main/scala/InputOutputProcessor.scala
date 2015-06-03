import java.io.{FileOutputStream, OutputStreamWriter, File}

import scala.collection.mutable.{ArrayBuffer, ListBuffer}

/**
 * Created by Viresh on 6/1/15.
 */
object InputOutputProcessor {

  def parsedOutputWriter(fileName: String, elements: ListBuffer[ArrayBuffer[Element]]): Unit = {

    val elementArray = new ArrayBuffer[Element]

    for (element <- elements) {

      for (innerElement <- element) {

        elementArray.append(innerElement)
      }

      documentWriter(fileName, elementArray)

    }
  }

  def documentWriter(fileName: String, elements: ArrayBuffer[Element]): File = {

    val file = new File(fileName)

    val writer = new OutputStreamWriter(new FileOutputStream(file));

    elements.foreach({ element =>

      val xPath = element.xpath
      val data = element.data
      val depth = element.depth
      var attributes = ""

      if (element.attributes.length == 0) {
        attributes = ""
      }
      else {


        element.attributes.foreach {

          attribute => attributes += attribute.attributeName + ": " + attribute.attributeValue + " "

        }
      }
      writer.write("xPath => " + xPath + ", " + "Data => " + data + ", " + "Depth => " + depth + ", " + "Attributes => " + attributes + "\n");
    })

    writer.close()
    file
  }


}
