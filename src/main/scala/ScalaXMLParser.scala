/**
 * Created by Viresh on 5/26/2015.
 */

import javax.xml.xpath.XPathFactory
import javax.xml.parsers._
import javax.xml.xpath.XPathConstants
import org.xml.sax.InputSource

import scala.collection.mutable.ArrayBuffer
import org.w3c.dom._
import java.io._

class ScalaXMLParser {


  case class Element(data: String, xpath: String, depth: Int, attributes: ArrayBuffer[Attribute])

  case class Attribute(attributeName: String, attributeValue: String)

  var domFactory: DocumentBuilderFactory = null
  var builder: DocumentBuilder = null

  def initializeParser(): Boolean = {

    domFactory = DocumentBuilderFactory.newInstance()
    domFactory.setNamespaceAware(true)

    builder = domFactory.newDocumentBuilder()

    true

  }

  def getDocumentBuilder(): DocumentBuilder = {

    builder
  }


  def getDocumentFromFile(file: File): Document = {

    val doc = builder.parse(file)

    doc
  }

  def getDocumentFromInputStram(inputStream: InputStream): Document = {

    val doc = builder.parse(inputStream)

    doc
  }

  def getDocumentFromString(input: String): Document = {

    val domFactory = DocumentBuilderFactory.newInstance()
    domFactory.setNamespaceAware(true)

    val builder = domFactory.newDocumentBuilder()

    val document = builder.parse(new InputSource(new StringReader(input)))

    document

  }


  def parseDocument(doc: Document): ArrayBuffer[Element] = {

    val elements = parser(doc)

    elements

  }


  /**
   * @param doc
			 * This function will execute an XPath expression to fetch all leaf element from a dom Document
   */

  def parser(doc: Document): ArrayBuffer[Element] = {

    val xpath = XPathFactory.newInstance().newXPath()
    val expr = xpath.compile("//*[not(*)]") // This XPath Expression will fetch all the leaf nodes
    val result = expr.evaluate(doc, XPathConstants.NODESET)

    val nodes = result.asInstanceOf[NodeList]

    var counter = 0

    var elements = new ArrayBuffer[Element];

    /*The NodeList can be converted to an Iterable collection. For the time being I have used simple for.*/
    for (counter <- 0 to nodes.getLength - 1) {

      var element = getPathWithXPath(nodes.item(counter));
      elements.append(element)

    }
    elements
  }


  /**
   * @param node
	 * This function will fetch all the parent nodes for every node passed as parameter.
   * And based on list of fetched ancestors, the XPath is built.
   * This solution might be heavy from performance view point because at some point
   * as it will fetch the whole document when reached to root.
   */
  def getPathWithXPath(node: Node): Element = {

    val xpath = XPathFactory.newInstance().newXPath()

    val expr = xpath.compile("ancestor::*") // This XPath Expression will fetch all the leaf nodes
    val result = expr.evaluate(node, XPathConstants.NODESET)

    val nodes = result.asInstanceOf[NodeList]

    val path = new StringBuilder()

    val value = node.getTextContent
    val depth = nodes.getLength

    var attributes = new ArrayBuffer[Attribute]

    if (node.hasAttributes()) {
      attributes = attributeHandler(node)
    }

    var counter: Int = 0

    for (counter <- 0 to depth - 1) {

      path.append(nodes.item(counter).getNodeName + "/")
    }

    path.append(node.getNodeName)

    val xpathToElement = path.toString()

    val nodeDepth = depth + 1

    val element = new Element(value, xpathToElement, nodeDepth, attributes)

    element

  }

   def attributeHandler(node: Node): ArrayBuffer[Attribute] = {

    println("Processing attribute handler...")

    val nodeMap: NamedNodeMap = node.getAttributes

    println("NodeMapLength => " + nodeMap.getLength)

    var counter: Int = 0
    var length = nodeMap.getLength

    val attributes = new ArrayBuffer[Attribute]

    for (counter <- 0 to length - 1) {

      var attribute = new Attribute(nodeMap.item(counter).getNodeName, nodeMap.item(counter).getNodeValue)

      attributes.append(attribute)

    }
    attributes
  }

  def documentWriter(fileName: String, elements: ArrayBuffer[Element]): File = {

    val file = new File(fileName);

    val writer = new OutputStreamWriter(new FileOutputStream(file));

    elements.foreach { element =>

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
    }
    writer.close();
    file
  }


  def S3ObjectWriter(s3Client: ScalaApplicationS3, bucketName: String, objectKey: String, elements: ArrayBuffer[Element]): Boolean = {

    val temporaryFile = "tempFile.txt"

    val file = new File(temporaryFile);

    val writer = new OutputStreamWriter(new FileOutputStream(file));

    elements.foreach { element =>

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
    }
    writer.close();

    file.deleteOnExit()
    s3Client.putFileObjectInBucket(bucketName, objectKey, file)


    true
  }


}
