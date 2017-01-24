//package br.com.cpfl.pi.mm;
package br.com.cpfl.util;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sap.aii.mapping.api.DynamicConfiguration;
import com.sap.aii.mapping.api.DynamicConfigurationKey;
import com.sap.aii.mapping.api.StreamTransformation;
import com.sap.aii.mapping.api.StreamTransformationConstants;
import com.sap.aii.mapping.api.StreamTransformationException;

/**
* Classe respons�vel por efetuar o mapping da interface CCS_XI_ANNEL_REGISTRAR_FATURA.
* Receber� um arquivo xml enviado pelo File Adapter e criar� um arquivo de XML nos padr�es da ANNEL.
* 
* @version 1.0 15 Jul 2009
* @author Rodrigo Pedrosa - Ultracon Consultoria
*/
public class TesteXmlParser3 implements StreamTransformation {

	private Map param;
	private Document docSaida;
	private Document docEntrada;

	/**
	 * (non-Javadoc)
	 * @see com.sap.aii.mapping.api.StreamTransformation#setParameter(java.util.Map)
	 */
	public void setParameter (Map param) {
		this.param = param;
		if (param == null) this.param = new HashMap();
	}
	
	/**
	 * (non-Javadoc)
	 * @see com.sap.aii.mapping.api.StreamTransformation#execute(java.io.InputStream, java.io.OutputStream)
	 */
	public void execute(InputStream inputStream, OutputStream outputStream) throws StreamTransformationException {
		
		try {
			//Preparacao para o Xml de Entrada
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builderEntrada = factory.newDocumentBuilder();
		
			// Preparacao para o xml de saida
			factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builderSaida = factory.newDocumentBuilder();
			this.docSaida = builderSaida.newDocument();
			
			// parse do documento XML de entrada
			this.docEntrada = builderEntrada.parse(inputStream);
			
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new StreamTransformationException("Falha no mapping", e);
		}
		
		// cria��o do n� root
		Element rootElem = docSaida.createElement("SCS");
		
		String tipo			= lerNo("TIPO");
		rootElem.setAttribute("tipo",(tipo!=null?tipo.trim():""));
		docSaida.appendChild(rootElem);
		
		NodeList ucCodigoLst = this.docEntrada.getElementsByTagName("UC_CODIGO");
		NodeList cbaLst = this.docEntrada.getElementsByTagName("CBA");
		
		Element movimentos = null;
		// cria��o do n� faturas
		Element faturas = criarNo("faturas", null, rootElem, null, null);
		
		// TRATAMENTO DO DETAILS
		NodeList detailsLst = this.docEntrada.getElementsByTagName("DETAILS");
		for(int i=0;i<detailsLst.getLength();i++) {
			//String UC_CODIGO = getValorNo(ucCodigoLst, i);
				// nova fatura
				Element fatura = criarNo("fatura", null, faturas, null, null);
				fatura.setAttribute("unidade_consumidora", getValorNo(ucCodigoLst, i));
				fatura.setAttribute("cba", getValorNo(cbaLst, i));
				
				movimentos = criarNo("movimentos", null, fatura, null, null);
				Element movimento = criarNo("movimento", null, movimentos, null, null);		
		}
		
		setDynamicConfiguration(param, "FileName");
		
		try {	
			// cria documento de sa�da
			gerarXMLSaida(outputStream);
		} catch (Exception e) {
			throw new StreamTransformationException("Falha no mapping", e);
		}
		
	}
	
	
	/**
	 * Recupera dados enviados pela propriedade "Adapter-Specific Message Properties" do File Adapter.
	 * @param param Par�metros transferidos pelo Integration Engine.
	 * @param property Nome da chave a ser recuperada do Dynamic Configuration.
	 * @return Valor da chave especificada.
	 */
	private void setDynamicConfiguration(Map param, String property){
		DynamicConfiguration conf = (DynamicConfiguration) param.get(StreamTransformationConstants.DYNAMIC_CONFIGURATION);
		DynamicConfigurationKey key = DynamicConfigurationKey.create("http:/"+"/sap.com/xi/XI/System/File",property);
		String nomeArquivo = conf.get(key);
		nomeArquivo = nomeArquivo.substring(0,(nomeArquivo.length()-3)) + "xml";
		conf.put(key, nomeArquivo);
	}
	
	/**
	 * Recupera uma tag do XML.
	 * @param tagName Nome da tag a ser lida.
	 * @return Valor da tag especificada.
	 */
	private String lerNo(String tagName) {
		NodeList nodeLst = this.docEntrada.getElementsByTagName(tagName);
		String nodeValue= getValorNo(nodeLst);
		return nodeValue;
	}
	
	/**
	 * Recupera valor de um n� de uma determinada tag do XML.
	 * @param nodeLst Lista de n�s.
	 * @return Valor do primeiro n� da lista especificada.
	 */
	private String getValorNo(NodeList nodeLst) {
		String valorNo = null;
		
		if (nodeLst.getLength() != 0) {
			Node txtNode = (Node) nodeLst.item(0).getFirstChild();
			if (txtNode != null) {
				valorNo = String.valueOf(txtNode);
			}
		}
		return (valorNo==null?"":valorNo);
	}
	
	/**
	 * Recupera valor de um n�, em um �ndice espec�fico, de uma determinada tag do XML.
	 * @param nodeLst Lista de n�s.
	 * @param indice �ndice do n� desejado.
	 * @return Valor do n� desejado da lista especificada.
	 */
	private String getValorNo(NodeList nodeLst, int indice) {
		String valorNo = null;
		
		if (nodeLst.getLength() != 0) {
			Node txtNode = (Node) nodeLst.item(indice).getFirstChild();
			if (txtNode != null) {
				valorNo = String.valueOf(txtNode);
			}
		}
		return (valorNo==null?"":valorNo);
	}
	
	/**
	 * Cria um n� no XML do documento de sa�da.
	 * @param nodeName Nome do n�.
	 * @param nodeValue Valor do n�.
	 * @param parentNode N�-pai a ser atrelado.
	 * @param attNames Array de atributos para o n�.
	 * @paramattValues Array com os valores dos respectivos atributos.
	 * @return N� do XML de sa�da.
	 */
//criarNo("tipo_consumidor", getValorNo(tipoConsumidorLst, i), movimento, null, null);
	
	private Element criarNo(String nodeName, String nodeValue, Element parentNode, String[] attNames, String[] attValues) {
		Element elem = docSaida.createElement(nodeName);
		parentNode.appendChild(elem);
		if (nodeValue != null) {
			Node dateTxtNode = (Node) docSaida.createTextNode(nodeValue.trim());
			elem.appendChild(dateTxtNode);
		}
		if (attNames != null) {
			for (int i=0; i<attNames.length; i++) {
				if (!attValues[i].equals("")) {
					elem.setAttribute(attNames[i], attValues[i].trim());
				}
			}
		}
		return elem;
	}
	
	private String tratarZerosAEsquerda(String valor) {
		String retorno = "";
		
		if (Integer.parseInt(valor) < 10) {
			retorno = String.valueOf(valor.charAt(1));
		}
		return retorno;
		
	}
	
	/**
	 * Gera efetivamente o documento XMl de sa�da.
	 * @param outputStream Stream de sa�da com o conte�do do XML.
	 */
	private void gerarXMLSaida(OutputStream outputStream) throws Exception {
		try {
			TransformerFactory tFactory = TransformerFactory.newInstance();
			Transformer transformer = tFactory.newTransformer();
			//transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1");
			DOMSource source = new DOMSource(docSaida);
			StreamResult result = new StreamResult(outputStream);
			transformer.transform(source, result);
		} catch (Exception e) {
			throw e;
		}
	} 
}
