package br.com.cpfl.util;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sap.aii.mapping.api.StreamTransformation;
import com.sap.aii.mapping.api.StreamTransformationException;

public class TesteParserXml implements StreamTransformation {

	private Map param;
	Document dadosEntrada;
	Document dadosSaida;

	@Override
	public void setParameter(Map map) {
		this.param = map;
		if (param == null) {
			param = new HashMap();
		}
	}

	@Override
	public void execute(InputStream in, OutputStream out) throws StreamTransformationException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builderIn = factory.newDocumentBuilder();
			DocumentBuilder builderOut = factory.newDocumentBuilder();

			dadosEntrada = builderIn.parse(in);
			dadosSaida = builderOut.newDocument();

			Element rootElement = dadosSaida.createElement("SCS");
			rootElement.setAttribute("CLIENTE", getNode("cliente"));
			rootElement.setAttribute("NOME-CLIENTE", getNode(""));
			rootElement.setAttribute("RG-CLIENTE", getNode(""));
			dadosSaida.appendChild(rootElement);
			
			NodeList endList = dadosEntrada.getElementsByTagName("endereco");
			Element end  = null;
			for(int i = 0;i < endList.getLength(); i++){
//				endList.item(i).getFirstChild() sByTagName("endereco")
				end = criarNode("END_CLIENTE", endList.item(i).getFirstChild().getNodeValue(),endList);
			}
			rootElement.setAttribute("CIDADE-CLIENTE", getNode(""));
			rootElement.setAttribute("", getNode(""));

															
		} catch (Exception e) {
			e.printStackTrace();
			throw new StreamTransformationException("Falha no maping", e);
		}
	}

	private Element criarNode(String nodeName, String nodeValue, NodeList endList) {
		Element element = dadosSaida.createElement(nodeName);
		
		return null;
	}

	private String getNode(String tagName) {
		String valorNode = null;
		NodeList nodeList = dadosEntrada.getElementsByTagName(tagName);
		if (nodeList.getLength() != 0) {
			Node txtNode = (Node) nodeList.item(0).getFirstChild();
			if (txtNode != null) {
				valorNode = txtNode.getNodeValue();
			}
		}
		return valorNode == null ? "" : valorNode;
	}
}
