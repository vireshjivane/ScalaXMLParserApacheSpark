/**
 * Created by Viresh on 6/1/15.
 */

import javax.xml.xpath.XPathFactory
import javax.xml.parsers._
import javax.xml.xpath.XPathConstants
import org.xml.sax.InputSource

import scala.collection.mutable.{ListBuffer, ArrayBuffer}
import org.w3c.dom._
import java.io._

object ScalaXMLParser {

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

  def getAllChildNodes(doc: Node): NodeList = {

    val xpath = XPathFactory.newInstance().newXPath()
    val expr = xpath.compile("child::*") // This XPath Expression will fetch all the leaf nodes
    val result = expr.evaluate(doc, XPathConstants.NODESET)

    val childNodes = result.asInstanceOf[NodeList]

    childNodes

  }

  def getRoot(doc: Document): Node = {

    doc.getFirstChild
  }

  def parserDocument(doc: Document): ListBuffer[ArrayBuffer[Element]] = {

    val root = getRoot(doc)
    val allChildNodes: NodeList = getAllChildNodes(root)

    val parsedXMLNodes = new ListBuffer[ArrayBuffer[Element]]

    for (counter <- 0 to allChildNodes.getLength - 1) {
      parsedXMLNodes.append(nodeProcessor(allChildNodes.item(counter), root.getNodeName))
    }

    parsedXMLNodes
  }

  def nodeProcessor(doc: Node, root: String): ArrayBuffer[Element] = {

    val xpath = XPathFactory.newInstance().newXPath()
    val expr = xpath.compile("descendant-or-self::*") // This XPath Expression will fetch all the descendant nodes
    val result = expr.evaluate(doc, XPathConstants.NODESET)

    val nodes = result.asInstanceOf[NodeList]

    var counter = 0

    var elements = new ArrayBuffer[Element]

    /*The NodeList can be converted to an Iterable collection. For the time being I have used simple for.*/
    for (counter <- 0 to nodes.getLength - 1) {

      println("Node Read = > " + nodes.item(counter).getNodeName)

      val node = nodes.item(counter)
      val parsedElement = parserEngine(node, root)

      elements.append(parsedElement)
    }
    elements
  }

  def parserEngine(node: Node, root: String): Element = {

    val xpath = XPathFactory.newInstance().newXPath()
    val expr = xpath.compile("ancestor-or-self::*") // This XPath Expression will fetch all the ancestor nodes
    val result = expr.evaluate(node, XPathConstants.NODESET)

    val nodes = result.asInstanceOf[NodeList]

    val path = xPathBuilder(nodes, root)

    val value = valueExtractor(node)

    val depth = nodes.getLength

    var attributes = new ArrayBuffer[Attribute]

    if (node.hasAttributes()) {

      attributes = attributeHandler(node)
    }

    val xpathToElement = path

    val nodeDepth = depth

    val element = new Element(value, xpathToElement, nodeDepth, attributes)

    element

  }

  def xPathBuilder(nodes: NodeList, root: String): String = {

    val path = new StringBuilder

    for (counter <- 0 to (nodes.getLength - 1)) {

      path.append(nodes.item(counter).getNodeName + "/")
    }

    println("BuiltPath => " + path.toString())

    path.toString()

  }

  def valueExtractor(node: Node): String = {

    println("Type Node => " + node.getChildNodes.getLength + "=> " + node.getNodeName)

    if (hasFurtherElements(node)) {
      "PARENT_NODE"
    } else {

      if (node.getTextContent == "") {
        "NO_DATA_CONTENT"
      }
      else {
        node.getTextContent
      }
    }

  }

  def hasFurtherElements(node: Node): Boolean = {

    val xpath = XPathFactory.newInstance().newXPath()
    val expr = xpath.compile("descendant::*") // This XPath Expression will fetch all the decendent nodes
    val result = expr.evaluate(node, XPathConstants.NODESET)

    val nodes = result.asInstanceOf[NodeList]

    if (nodes.getLength == 0) {
      return false
    }

    true

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


}
