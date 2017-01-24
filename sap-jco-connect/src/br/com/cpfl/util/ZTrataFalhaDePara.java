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
public class ZTrataFalhaDePara implements StreamTransformation {

	private static final String NAO_ENCONTRADO_NO_PI = "NAO ENCONTRADO NO PI";
	private Map param;
	private Document document;

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
			DocumentBuilder builder = factory.newDocumentBuilder();

			// parser do documento XML de entrada
			this.document = builder.parse(inputStream);

			NodeList nList = document.getElementsByTagName("*");

			for (int i = 0; i < nList.getLength(); i++) {
				Node node = nList.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					if (node.hasChildNodes()) {
						if (node.getFirstChild().getTextContent() != null
								&& node.getFirstChild().getTextContent().contains(NAO_ENCONTRADO_NO_PI)) {
							// limpa o conteudo do xml
							Node rootNode = document.getDocumentElement().getFirstChild().getParentNode();
							document.getDocumentElement().getParentNode().removeChild(rootNode);
						}
					}
				}
			}

			// escreve o conteudo do xml de saida
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(document);
			StreamResult result = new StreamResult(outputStream);
			transformer.transform(source, result);

		} catch (Exception e) {
			e.printStackTrace();
			throw new StreamTransformationException("Falha ao efetuar o mapping", e);
		}

	}

	public static void main(String[] args) throws Exception {
		ZTrataFalhaDePara ztf = new ZTrataFalhaDePara();

		InputStream inputStream = new FileInputStream(
				new File("D:\\desenv\\CPFL\\Workspace\\sap-jco-connect\\instanceTrataFalha.xml"));
		OutputStream outputStream = new FileOutputStream(
				new File("D:\\desenv\\CPFL\\Workspace\\sap-jco-connect\\instanceTrataFalhaOUT.xml"));

		ztf.execute(inputStream, outputStream);
	}
}
