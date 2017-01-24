/**
 * 
 */
package br.com.cpfl.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sap.aii.mapping.api.StreamTransformation;
import com.sap.aii.mapping.api.StreamTransformationException;

/**
 * @author Dorival 24 de nov de 2016
 * 
 */
public class ZTrataFalhaDeParaV1 implements StreamTransformation {

	private Map param;
	private Document docSaida;
	private Document docEntrada;

	@Override
	public void setParameter(Map param) {
		this.param = param;
		if (param == null)
			this.param = new HashMap();
	}

	@Override
	public void execute(InputStream inputStream, OutputStream outputStream) throws StreamTransformationException {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builderEntrada = factory.newDocumentBuilder();

			// Preparacao para o xml de saida
			DocumentBuilderFactory factorySaida = DocumentBuilderFactory.newInstance();
			DocumentBuilder builderSaida = factorySaida.newDocumentBuilder();
			this.docSaida = builderSaida.newDocument();
			this.docSaida = builderSaida.newDocument();

			// parse do documento XML de entrada
			this.docEntrada = builderEntrada.parse(inputStream);

			System.out.println("Root element: " + docEntrada.getDocumentElement().getNodeName());
			NodeList nList = docEntrada.getElementsByTagName("MeterDataMessage");

			for (int i = 0; i < nList.getLength(); i++) {
				Node node = nList.item(i);
				System.out.println("\nCurrent Element :" + node.getNodeName());
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element element = (Element) node;
					System.out.println(element.getNodeName() + " " + element.getNodeValue());
				}

			}

			if (docEntrada.hasChildNodes()) {
				printNode(docEntrada.getChildNodes());
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new StreamTransformationException("Falha no mapping", e);
		}

	}

	private void printNode(NodeList nodeList) {
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node tempNode = nodeList.item(i);
			if (tempNode.getNodeType() == Node.ELEMENT_NODE) {
				System.out.println(" tempNode.getNodeName(): " + tempNode.getNodeName() + " tempNode.getTextContent(): "
						+ tempNode.getTextContent());
				if (tempNode.hasAttributes()) {
					NamedNodeMap nodeMap = tempNode.getAttributes();
					for (int j = 0; j < nodeMap.getLength(); j++) {
						Node node = nodeMap.item(i);
						System.out.println(" node.getNodeName(): " + node.getNodeName() + " node.getNodeValue(): "
								+ node.getNodeValue());
					}
				}

				if (tempNode.hasChildNodes()) {
					printNode(tempNode.getChildNodes());
				}
				System.out.println("Node Name = " + tempNode.getNodeName() + " [CLOSE] ");
			}
		}
	}

	public static void main(String[] args) throws Exception {
		ZTrataFalhaDeParaV1 ztf = new ZTrataFalhaDeParaV1();

		InputStream inputStream = new FileInputStream(
				new File("D:\\desenv\\CPFL\\Workspace\\sap-jco-connect\\instanceTrataFalha.xml"));
		OutputStream outputStream = new FileOutputStream(
				new File("D:\\desenv\\CPFL\\Workspace\\sap-jco-connect\\instanceTrataFalhaOUT.xml"));

		ztf.execute(inputStream, outputStream);
	}
}
