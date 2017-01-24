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
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sap.aii.mapping.api.StreamTransformation;
import com.sap.aii.mapping.api.StreamTransformationException;

/**
 * @author Dorival 24 de nov de 2016
 * 
 */
public class ZTrataFalhaDeParaV3 implements StreamTransformation {

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
			NodeList nList = docEntrada.getElementsByTagName("*");

			//////////////////////////////
			for (int i = 0; i < nList.getLength(); i++) {
				Node node = nList.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					System.out.println("name: " + node.getNodeName());
					if (node.hasChildNodes()) {
						if (node.getFirstChild().getTextContent() != null
								&& !"".equals(node.getFirstChild().getTextContent().trim())) {
							System.out.println(
									node.getFirstChild().getNodeName() + ">>" + node.getFirstChild().getTextContent());
							if (node.getFirstChild().getTextContent().contains("NAO ENCONTRADO NO PI")) {
								// limpa nodes
								// nList.item(0).getParentNode().removeChild(""));
								Node oldChild = docEntrada.getDocumentElement().getFirstChild().getParentNode();
								System.out.println("Removendo... " + oldChild.getNodeName());
								docEntrada.getDocumentElement().getParentNode().removeChild(oldChild);
							}
						}
					}
				}
			}

			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(docEntrada);
			// StreamResult result = new StreamResult(new
			StreamResult result = new StreamResult(outputStream);
			transformer.transform(source, result);

			System.out.println("Done");
			// Runtime.getRuntime().exec("D:\\desenv\\CPFL\\Workspace\\sap-jco-connect\\instanceTrataFalhaOUT.xml");
		} catch (Exception e) {
			e.printStackTrace();
			throw new StreamTransformationException("Falha no mapping", e);
		}

	}

	public static void main(String[] args) throws Exception {
		ZTrataFalhaDeParaV3 ztf = new ZTrataFalhaDeParaV3();

		InputStream inputStream = new FileInputStream(
				new File("D:\\desenv\\CPFL\\Workspace\\sap-jco-connect\\instanceTrataFalha.xml"));
		OutputStream outputStream = new FileOutputStream(
				new File("D:\\desenv\\CPFL\\Workspace\\sap-jco-connect\\instanceTrataFalhaOUT.xml"));

		ztf.execute(inputStream, outputStream);
	}
}
