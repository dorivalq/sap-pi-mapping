package br.com.cpfl.mapping.provisionamento;

import static javax.naming.Context.URL_PKG_PREFIXES;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sap.aii.mapping.api.StreamTransformation;
import com.sap.aii.mapping.api.StreamTransformationException;
import com.sap.conn.jco.JCoField;
import com.sap.conn.jco.JCoFieldIterator;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoParameterList;
import com.sap.conn.jco.JCoTable;

import br.com.cpfl.daoejb.MappingPODataAccessRemote;
import br.com.cpfl.daoejb.entidades.CpflZCcsXiT001;
import br.com.cpfl.daoejb.entidades.CpflZProvGrupo;
import br.com.cpfl.daoejb.entidades.CpflZmodelObisc;
import br.com.cpfl.mapping.PIMessages;

/**
 * Implementação em Java do programa Abap Z_MDM_XI_ZFA_PROVISIONAMENTO_D
 * 
 * @author Dorival Querino da Silva
 * 
 *         - 23 de jan de 2017 - CSC
 * 
 */
public class ZMdmXiZfaProvisionamentoD implements StreamTransformation {
	private static final String CONSULTAR_MODELO_TODOS = "consultarModeloTodos";
	private static final String CONSULTAR_Z_PROV_GROUP = "consultarZProvGroup";
	private static final String CONSULTAR_CCS_XI = "consultarCcsXi";

	private Map map;

	private String wDateTime;

	private String wData;

	private Object ejbObj;
	private String wModel;

	private Document document;

	private String wInv;

	private String wType;

	private class ZProvGroup {
		String name;
		String sequencial;
	}

	private ZProvGroup wZProvGroup = new ZProvGroup();

	private class FsMeterAsset {
		String mRid;
		String modelNumber;
		String serialNumber;
		String parameter/* Model */;
	}

	private FsMeterAsset wMeterAsset = new FsMeterAsset();

	private class FsServiceDeliverypoint {
		String mRid;
		String parameter;
	}

	private FsServiceDeliverypoint wServiceDeliveryPoint = new FsServiceDeliverypoint();

	private class FsHeader {
		String verb;
		String noun;
		String revision;
		String datetime;
		String messageId;
	}

	private FsHeader wHeader = new FsHeader();

	private String wCodCcee;
	private Short wSequencial;
	private String wPrefix;
	private String wDsk;

	private String wIdent1;

	private String lEqunr;

	private class WaRfcOpt {
		String text;
	}

	private WaRfcOpt waRfcOpt = new WaRfcOpt();
	private List<WaRfcOpt> TRfcOpt = new ArrayList<ZMdmXiZfaProvisionamentoD.WaRfcOpt>();

	private class TRfcFields {
		String fieldName;
		Long offSet;
		Long length;
		String type;
	}

	private TRfcFields waRfcFields = new TRfcFields();
	private List<TRfcFields> tRfcFields = new ArrayList<ZMdmXiZfaProvisionamentoD.TRfcFields>();

	CpflZCcsXiT001 wZCcsXit001;
	private boolean lModeloEloAbnt;

	private class FsComfunction {
		String mRid;
		String amrAddress;
		String pathName;
		String parameter;
	}

	List<FsComfunction> tComfunction = new ArrayList<ZMdmXiZfaProvisionamentoD.FsComfunction>();
	private FsComfunction wComfunction = new FsComfunction();
	private String lInterface;
	private String wProgramCode;
	private String lCcsFabricante;
	private String lCcsModelo;
	private FunctionBuilder functionBuilder;
	
	private Logger logger = Logger.getLogger("ProvisionamentoDelete");
	private FileHandler fileHandler;
	
	@SuppressWarnings("rawtypes")
	@Override
	public void setParameter(Map map) {
		this.map = map;
		if (map == null) {
			this.map = new HashMap();
		}
	}

	@Override
	public void execute(InputStream inputStream, OutputStream outputStream) throws StreamTransformationException {
		try {

			try {
				fileHandler = new FileHandler("/interf/temp/ProvisionamentoDelete.log", 0, 1);
				logger.addHandler(fileHandler);
				fileHandler.setFormatter(new SimpleFormatter());
			} catch (SecurityException e) {
				e.printStackTrace();
				logger.warning("Erro ao gerar o arquivo de log em /interf/temp/ProvisionamentoDelete.log  "
						+ "\n"+e.getCause()+e.getMessage()+e.getLocalizedMessage());
			} catch (IOException e) {
				e.printStackTrace();
				logger.warning("Erro ao gerar o arquivo de log em /interf/temp/ProvisionamentoDelete.log  "
						+ "\n"+e.getCause()+e.getMessage()+e.getLocalizedMessage());
			}
			
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			document = builder.parse(inputStream);

			// popular objetos internos
			popularWHeader();
			popularAsset();
			popularDeliveryPoint();
			popularComFunction();

			wData = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

			Element programCodeE = findNodeByValue("Program Code");
			if (programCodeE != null) {
				wProgramCode = getValue((Element) programCodeE.getParentNode(), "ami:value");
			}

			Element invMeterE = findNodeByValue("InvertedMeter");
			if (invMeterE != null) {
				wInv = getValue((Element) invMeterE.getParentNode(), "ami:value");
			}
			
			lEqunr = wMeterAsset.mRid;
			String wSdpC = wServiceDeliveryPoint.mRid;
			char[] leqArray = new char[18];
			Arrays.fill(leqArray, 0, 18, '0');
			lEqunr = String.valueOf(leqArray).substring(lEqunr.length()) + lEqunr;
			
			//Inicializa configuracao de JcoFunction
//			List<CpflZCcsXiT001> lista = executarMetodoRemoto(CONSULTAR_CCS_XI);
//			wZCcsXit001 = lista.get(0);
			wZCcsXit001 = popularCcsXi();
			logger.info("------------------------- Chamada da funcao ZCCSGLEF0083 -----------------------------------");
			functionBuilder = new FunctionBuilder();
			functionBuilder.setupDestinationProperties(wZCcsXit001);
			//Fim Inicializa configuracao de JcoFunction
			
			//Capra 26.11.2014-in2929-inicio{
			//No PROVISIONAMENTO-DELETE deve chamar a funcao 83
			
			Map<String, String> importParamMap = new HashMap<String, String>();
			importParamMap.put("EQUNR", lEqunr);
			logger.info("EQUNR: "+ lEqunr);
			importParamMap.put("DEVLOC", wSdpC);
			logger.info("DEVLOC: "+wSdpC);
			functionBuilder.setupFunction("ZCCSGLEF0083", importParamMap);

			JCoFunction function = functionBuilder.executeCalls();

			JCoParameterList jcoParamList = function.getChangingParameterList();

			Date wDateZBisTime = jcoParamList.getTime("ZBIS_TIME");
			logger.info("jcoParamList.getTime(\"ZBIS_TIME\"): "+wDateZBisTime);

			Calendar calDateTime = null;
			if (wDateZBisTime != null) {
				calDateTime = Calendar.getInstance();
				calDateTime.setTime(wDateZBisTime);
				logger.info("calDateTime : "+calDateTime.getTime());
			}
			
			Date wDateZBis = jcoParamList.getDate("ZBIS");
			logger.info("jcoParamList.getTime(\"ZBIS\"): "+wDateZBis);
			Calendar calDate = null;
			if (wDateZBis != null) {
				calDate = Calendar.getInstance();
				calDate.setTime(wDateZBis);
				logger.info("calDate : "+calDate.getTime());
				logger.info("calDateTime ANTES DE SETAR A DATA: "+calDateTime.getTime());
				//se retornou uma data, ajusta a variavel que contem o time com a data correta
				calDateTime.set(Calendar.YEAR, calDate.get(Calendar.YEAR));
				calDateTime.set(Calendar.MONTH, calDate.get(Calendar.MONTH));
				calDateTime.set(Calendar.DAY_OF_MONTH, calDate.get(Calendar.DAY_OF_MONTH));
				logger.info("calDateTime DEPOIS DE SETAR A DATA: "+calDateTime.getTime());
			}
			//verifica se a data nao esta zerada - calDateTime.get(Calendar.YEAR) > 1970
			if (calDateTime != null && calDateTime.get(Calendar.YEAR) > 1970) {
				wDateTime = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss").format(calDateTime.getTime()) + "-03:00";
			} else {
				wDateTime = wData + "T00:00:00-03:00";
			}
			
			if (!isNullEmpty(wDateTime)) {
				// posicao da segunda casa dos minutos
				int index = wDateTime.indexOf(':') + 2;
				char[] wDateTimeAux = wDateTime.toCharArray();
				int unidadeMin = Integer.parseInt(String.valueOf(wDateTimeAux[index]));
				if (unidadeMin < 5) {
					wDateTimeAux[index] = '0';
				} else {
					wDateTimeAux[index] = '5';
				}
				
			} // wDateTime notNull
			
			wModel = wMeterAsset.modelNumber;
			if (wServiceDeliveryPoint.parameter != null) {
				wCodCcee = wServiceDeliveryPoint.parameter;
			}

			if ("MeterAdd".equals(wHeader.noun)) {
				wType = "meter-mounting";
			} else if ("MeterDelete".equals(wHeader.noun)) {
				wType = "meter-dismantling";
			}

			DocumentBuilder builder2 = factory.newDocumentBuilder();
			Document oDocument = builder2.newDocument();

			Element importE = oDocument.createElement("import");
			importE.setAttribute("name", "master-data-transfer");

			Element versionE = oDocument.createElement("version");
			versionE.setAttribute("date", wData);
			versionE.setTextContent("1.0");
			importE.appendChild(versionE);

			Element groupList = oDocument.createElement("group-list");
			importE.appendChild(groupList);

			Element groupE = oDocument.createElement("group");
			groupE.setAttribute("type", "customer");

			wZProvGroup.name = "Grupo";

			Element mediaTypeE = findNodeByValue("Media Type");
			String wSemMidia = null;
			if (mediaTypeE != null) {
				wSemMidia = getValue((Element) mediaTypeE.getParentNode(), "ami:value");
			}
			if (wSemMidia != null) {
				if (!"SEM MIDIA".equals(wSemMidia)) {
					CpflZProvGrupo zProvGrupo = new CpflZProvGrupo();
					zProvGrupo.setName("GRUPO");
					zProvGrupo = executarMetodoRemoto(CONSULTAR_Z_PROV_GROUP, zProvGrupo.getName());
					wSequencial = zProvGrupo.getSequencial();
					wZProvGroup.sequencial = "Grupo0" + wSequencial;
					wSequencial++;
					if (wSequencial > 9) {
						wSequencial = 0;
					}
					gravarSequencialZProvGroup(zProvGrupo.getName(), wSequencial);

				} else {
					wZProvGroup.sequencial = "GrupoDSK";
					wPrefix = "DISCO";
					wDsk = "X";
				}
			} else {
				wZProvGroup.sequencial = "GrupoDSK";
				wPrefix = "DISCO";
			}

			groupE.setAttribute("name", wZProvGroup.sequencial);

			Element meterPointList = oDocument.createElement("metering-point-list");
			groupE.appendChild(meterPointList);
			groupList.appendChild(groupE);

			Element meteringPoint = oDocument.createElement("metering-point");
			meteringPoint.setAttribute("name", wMeterAsset.mRid);
			meteringPoint.setAttribute("nr", wMeterAsset.mRid);

			meterPointList.appendChild(meteringPoint);

			Element exchange = oDocument.createElement("exchange");
			exchange.setAttribute("date", wDateTime);
			if (wType != null) {
				exchange.setAttribute("type", wType);
			}
			meteringPoint.appendChild(exchange);

			Element groupExange = oDocument.createElement("group");
			groupExange.setAttribute("type", "device");
			groupExange.setAttribute("name", wZProvGroup.sequencial);

			exchange.appendChild(groupExange);

			Element device = oDocument.createElement("device");
			device.setAttribute("nr", wMeterAsset.serialNumber);
			groupExange.appendChild(device);

			if ("Y".equals(wInv)) {
				wIdent1 = "Template_" + wModel + "_Inv";
			} else {
				wIdent1 = "Template_" + wModel;
			}

			if (wDsk != null) {
				wIdent1 = wIdent1 + "_Dsk";
			}

			Element assignment = oDocument.createElement("assignment");
			assignment.setAttribute("ident1", wIdent1);
			assignment.setAttribute("ident2", wMeterAsset.serialNumber);
			assignment.setAttribute("ident3", wMeterAsset.mRid);
			assignment.setAttribute("ident4", "delete");

			device.appendChild(assignment);

			Element identification = oDocument.createElement("identification");
			identification.setAttribute("ident1", wHeader.messageId);
			identification.setAttribute("ident3", wCodCcee);
			identification.setAttribute("ident4", wServiceDeliveryPoint.mRid);
			identification.setAttribute("uident1", wMeterAsset.mRid);

			device.appendChild(identification);

			Element transDevice = oDocument.createElement("trans-device");
			device.appendChild(transDevice);

			Element communication = oDocument.createElement("communication");

			if (wPrefix != null) {
				communication.setAttribute("target-prefix", wPrefix);
			}

			String lIpOrig = wComfunction.mRid;

			tRfcFields.add(waRfcFields);

			importParamMap = new HashMap<String, String>();
			importParamMap.put("QUERY_TABLE", "EQUI");

			functionBuilder.setupFunction("RFC_READ_TABLE", importParamMap);

			Map<String, String> fieldMap = new HashMap<String, String>();
			fieldMap.put("TEXT", "EQUNR = '" + lEqunr + "'");
			functionBuilder.addTableValue("OPTIONS", fieldMap);

			fieldMap = new HashMap<String, String>();
			fieldMap.put("FIELDNAME", "HERST");
			fieldMap.put("OFFSET", "000202");
			fieldMap.put("LENGTH", "000030");
			fieldMap.put("TYPE", "C");
			functionBuilder.addTableValue("FIELDS", fieldMap);

			fieldMap = new HashMap<String, String>();
			fieldMap.put("FIELDNAME", "TYPBZ");
			fieldMap.put("OFFSET", "000295");
			fieldMap.put("LENGTH", "000020");
			fieldMap.put("TYPE", "C");
			functionBuilder.addTableValue("FIELDS", fieldMap);

			function = functionBuilder.executeCalls();

			JCoTable table = function.getTableParameterList().getTable("DATA");

			if (table.getNumRows() > 0) {
				JCoFieldIterator it = table.getFieldIterator();
				while (it.hasNextField()) {
					JCoField field = it.nextField();
					String fieldValue = (String) field.getValue();
					lCcsFabricante = fieldValue.length() >= 30 ? fieldValue.substring(0, 30).trim() : "";
					lCcsModelo = fieldValue.length() >= 50 ? fieldValue.substring(30, 50).trim() : "";
				}
			}

			// ini - Valida Fabricante = ELO
			if (lCcsFabricante != null && lCcsFabricante.contains("ELO")) {
				lModeloEloAbnt = true;
			}

			Element lParamList = oDocument.createElement("param-list");

			Element lParamTypeChannel = oDocument.createElement("param");
			lParamTypeChannel.setAttribute("type", "UCR_CHANNEL");

			String lVlUcrChannel = "1";
			String lVlPwd = "senha1";
			String lVlUser = "usuario1";

			lParamTypeChannel.setTextContent(lVlUcrChannel);

			Element lParamTypeIpAddr = oDocument.createElement("param");
			lParamTypeIpAddr.setAttribute("type", "UCR_IP_ADR");

			lParamTypeIpAddr.setTextContent(lIpOrig);

			Element lParamTypePwd = oDocument.createElement("param");
			lParamTypePwd.setAttribute("type", "PWD");
			lParamTypePwd.setTextContent(lVlPwd);

			Element lParamTypeUser = oDocument.createElement("param");
			lParamTypeUser.setAttribute("type", "USER");
			lParamTypeUser.setTextContent(lVlUser);

			lParamList.appendChild(lParamTypeChannel);
			lParamList.appendChild(lParamTypeIpAddr);
			lParamList.appendChild(lParamTypePwd);
			lParamList.appendChild(lParamTypeUser);

			lIpOrig = "IP:" + wComfunction.mRid;
			if (lModeloEloAbnt) {
				// Se Fabricante = ELO, envia branco
				communication.setAttribute("target-phone-number", "");
				communication.appendChild(lParamList);
			} else {
				// envia IP:X.X.X.X
				communication.setAttribute("target-phone-number", lIpOrig);
			}
			// * PN - 07.05.2015 - Inc.#3659 - Fim

			transDevice.appendChild(communication);

			Element dataChannelList = oDocument.createElement("datachannel-list");
			device.appendChild(dataChannelList);

			List<CpflZmodelObisc> listaModelo = executarMetodoRemoto(CONSULTAR_MODELO_TODOS);
			//remove duplicados
			Map<String, CpflZmodelObisc> uniqueMap =  new LinkedHashMap<String, CpflZmodelObisc>();
			for (CpflZmodelObisc zmodel : listaModelo) {
				String chave = zmodel.getId().getObiscode();
				uniqueMap.put(chave, zmodel);
			}
			listaModelo.clear();
			listaModelo.addAll(uniqueMap.values());
			Comparator<CpflZmodelObisc> comp = new Comparator<CpflZmodelObisc>() {
				@Override
				public int compare(CpflZmodelObisc o1, CpflZmodelObisc o2) {
					return o1.getId().getObiscode().compareTo(o2.getId().getObiscode());
				}
			};
			Collections.sort(listaModelo, comp);
			
			String wObiscode;
			for (CpflZmodelObisc wZModelObiscAux : listaModelo) {
				wObiscode = wZModelObiscAux.getId().getObiscode();
				Element dataChannel = oDocument.createElement("datachannel");
				dataChannel.setAttribute("obis-id-code", wObiscode);

				identification = oDocument.createElement("identification");
				identification.setAttribute("ident1", wObiscode);//canal
				dataChannel.appendChild(identification);

				dataChannelList.appendChild(dataChannel);
			}
			

			oDocument.appendChild(importE);
			// cria o path do arquivo de saída
			String local = PIMessages.getString("output_file.path") + "PROV_" + wServiceDeliveryPoint.mRid + "_"
					+ new SimpleDateFormat("yyyy-MM-dd'_'hhmmss").format(new Date()) + "_"
					+ System.getProperty("user.name") + ".xml";

			// criar o arquivo de saida
			File arquivoSaida = new File(local);
			arquivoSaida.setReadable(true, false);
			arquivoSaida.setWritable(true, false);
			arquivoSaida.setExecutable(true, false);
			
			arquivoSaida.getParentFile().mkdirs();
			arquivoSaida.createNewFile();
			// gera o stream de saída
			OutputStream localOutputStream = new FileOutputStream(arquivoSaida, false);
			DOMSource domSource = new DOMSource(oDocument);
			StreamResult streamResult = new StreamResult(localOutputStream);
			// gerar o conteudo do arquivo de saida
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer newTransformer = transformerFactory.newTransformer();

			newTransformer.transform(domSource, streamResult);

			//limpa o conteudo que foi gerado no arquivo, para gerar o output padrao
			Node importRemove = oDocument.getElementsByTagName("import").item(0);
			if(importRemove != null){
				oDocument.removeChild(importRemove);				
			}
			
			// // * PN - 07.05.2015 - Inc.#3659 - Inicio
			if (lModeloEloAbnt) {
				Node oldChild = oDocument.getElementsByTagName("param-list").item(0);
				oDocument.getParentNode().removeChild(oldChild);
			}
			
			Element ZGLE_MDM = oDocument.createElementNS("urn:sap-com:document:sap:rfc:functions","rfc:ZGLE_MDM_XI_PI_PROVISIONAMENTO");
			ZGLE_MDM = (Element) oDocument.appendChild(ZGLE_MDM);
			Node ID = oDocument.createElement("ID");
			ID.setTextContent(wHeader.messageId);
			ZGLE_MDM.appendChild(ID);
			Element XML = oDocument.createElement("XML");
		
			XML.appendChild(importE);
			ZGLE_MDM.appendChild(XML);
			Node FILENAME = oDocument.createElement("FILENAME");
		
			FILENAME.setTextContent(PIMessages.getString("nome.interface"));// lInterface
			oDocument.getFirstChild().appendChild(FILENAME);
			
			// * PN - 07.05.2015 - Inc.#3659 - Fim
			Transformer transformer = transformerFactory.newTransformer();
			Source sourceNew = new DOMSource(oDocument);
			Result resultNew = new StreamResult(outputStream);
			transformer.transform(sourceNew, resultNew);
		} catch (Exception e) {
			e.printStackTrace();
			throw new StreamTransformationException("Falha ao processar o mapping ", e);
		}finally {
			try {
				fileHandler.close();				
			} catch (Exception e2) {
				//nothing to do here
			}
		}
	}

	private void popularComFunction() {
		Element wComFunctionE = (Element) document.getElementsByTagName("ami:comFunction").item(0);

		wComfunction.mRid = getValue(wComFunctionE, "ami:mRID");
		wComfunction.amrAddress = getValue(wComFunctionE, "ami:amrAddress");
		wComfunction.pathName = getValue(wComFunctionE, "ami:pathName");
		wComfunction.parameter = getValue(wComFunctionE, "ami:parameter");
	}

	private void popularDeliveryPoint() {
		Element wServiceDeliveryPointE = (Element) document.getElementsByTagName("ami:serviceDeliveryPoint").item(0);

		wServiceDeliveryPoint.mRid = getValue(wServiceDeliveryPointE, "ami:mRID");
		NodeList listParam = wServiceDeliveryPointE.getElementsByTagName("ami:parameter");
		if (listParam.getLength() > 0) {
			wServiceDeliveryPoint.parameter = getValue((Element)listParam.item(0), "ami:value");
		}
	}

	private void popularAsset() {
		Element wMeterAssetE = (Element) document.getElementsByTagName("ami:meterAsset").item(0);
		wMeterAsset.mRid = getValue(wMeterAssetE, "ami:mRID");
		wMeterAsset.modelNumber = getValue(wMeterAssetE, "ami:modelNumber");

		wMeterAsset.serialNumber = getValue(wMeterAssetE, "ami:serialNumber");
	}

	private void popularWHeader() {
		Element wHeaderE = (Element) document.getElementsByTagName("ami:header").item(0);

		wHeader.verb = getValue(wHeaderE, "ami:verb");
		wHeader.noun = getValue(wHeaderE, "ami:noun");
		wHeader.revision = getValue(wHeaderE, "ami:revision");
		wHeader.datetime = getValue(wHeaderE, "ami:dateTime");
		wHeader.messageId = getValue(wHeaderE, "ami:messageID");
	}

	private String getValue(Element elementP, String name) {
		if (elementP == null || elementP.getElementsByTagName(name) == null) {
			return "";
		}
		Element e = (Element) elementP.getElementsByTagName(name).item(0);
		if (e == null) {
			return "";
		}
		if (e.hasChildNodes()) {
			String retorn = e.getTextContent() == null ? "" : e.getFirstChild().getTextContent();
			return retorn;
		}
		return "";
	}

	private <T> boolean isNullEmpty(T t) {
		if (t instanceof String) {
			return t == null || "".equals(t);
		}
		return t == null;
	}

	private Element findNodeByValue(String value) throws XPathExpressionException {
		NodeList nodelist = document.getElementsByTagName("*");
		for (int i = 0; i < nodelist.getLength(); i++) {
			Node node = nodelist.item(i);
			if (Node.ELEMENT_NODE == node.getNodeType()) {
				if (value.equals(node.getTextContent())) {
					return (Element) node;
				}
			}
		}
		return null;
	}

	/**
	 * Metodo para acessar o Ejb MappingPODataAccessImpl com tratativa para o
	 * problema de class cast causado por diferença de classLoader
	 * 
	 * @param parametro String
	 * @return T(s) encontrado(s) no BD
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T> T executarMetodoRemoto(String metodo, String parametro) throws Exception {

		Properties props = getProperties();

		Class<?> remoteInterface = getService(props);

		Class args[] = { parametro.getClass() };

		Method method = remoteInterface.getMethod(metodo, args);
		Object params[] = { parametro };

		List<T> lista = null;
		Object retorno = method.invoke(ejbObj, params);
		if (retorno == null) {
			return null;
		} else {
			lista = (List<T>) retorno;
			return lista.size() > 0 ? lista.get(0) : null;
		}
	}

	/**
	 * Metodo que grava o novo sequencial do ZProvGroup atraves do Ejb remoto
	 * 
	 * @param groupName
	 * @param sequencial
	 * @throws Exception
	 */
	@SuppressWarnings({"rawtypes" })
	public void gravarSequencialZProvGroup(String groupName, Short sequencial ) throws Exception {
		String metodo = "gravarSequencialZProvGroup";
		Properties props = getProperties();

		Class<?> remoteInterface = getService(props);

		Class args[] = { groupName.getClass(), sequencial.getClass() };

		Method method = remoteInterface.getMethod(metodo, args);
		Object params[] = { groupName, sequencial };

		method.invoke(ejbObj, params);
	}
	
	/**
	 * Metodo de chamada remota
	 * 
	 * @param paramModelo
	 * @return T(s) encontrado(s) no BD
	 */
	@SuppressWarnings("unchecked")
	public <T> T executarMetodoRemoto(String metodo) throws Exception {
		Properties props = getProperties();

		Class<?> remoteInterface = getService(props);

		Method method = remoteInterface.getMethod(metodo);

		T tipoRetorno = (T) method.invoke(ejbObj);

		return tipoRetorno;
	}

	private Properties getProperties() {
		Properties props = new Properties();

		props.put(Context.INITIAL_CONTEXT_FACTORY,

				PIMessages.getString("ejbclient.context"));
		props.put(Context.PROVIDER_URL, PIMessages.getString("ejbclient.host_port"));

		props.put(URL_PKG_PREFIXES, PIMessages.getString("ejbclient.pkg_prefix"));
		return props;
	}

	private Class<?> getService(Properties props) throws Exception {
		InitialContext ctx = new InitialContext(props);
		ejbObj = ctx.lookup(PIMessages.getString("ejbclient.remote_url"));
		Class<?> remoteInterface;
		ClassLoader classLoader = ejbObj.getClass().getClassLoader();
		remoteInterface = classLoader.loadClass(MappingPODataAccessRemote.class.getCanonicalName());
		return remoteInterface;
	}

	/**
	 * Metodo para buscar modelo com tratativa para o problema de class cast
	 * causado por diferença de classLoader
	 * 
	 * @param paramModelo
	 * @return Lista com os modelos encontrados no BD
	 */
	@SuppressWarnings("rawtypes")
	public List consultarModelo(String paramModelo) throws Exception {

		Properties props = new Properties();

		props.put(Context.INITIAL_CONTEXT_FACTORY,

				PIMessages.getString("ejbclient.context"));
		props.put(Context.PROVIDER_URL, PIMessages.getString("ejbclient.host_port"));

		props.put(URL_PKG_PREFIXES, PIMessages.getString("ejbclient.pkg_prefix"));

		InitialContext ctx = new InitialContext(props);

		Object ejbObj = ctx.lookup(PIMessages.getString("ejbclient.remote_url"));

		ClassLoader classLoader = ejbObj.getClass().getClassLoader();
		Class<?> remoteInterface = classLoader.loadClass(MappingPODataAccessRemote.class.getCanonicalName());

		Class args[] = { paramModelo.getClass() };
		Method method = remoteInterface.getMethod("consultarModelo", args);
		Object params[] = { paramModelo };
		Object result = method.invoke(ejbObj, params);

		return (List) result;
	}

	private CpflZCcsXiT001 popularCcsXi() {
		CpflZCcsXiT001 wZCcsXit001 = new CpflZCcsXiT001();
		//TODO: Valores usados somente adequar à classe utilitaria de Jco e para testes locais 
		// Dentro do PO, a classe FunctionBuilder busca a destination 'pi.destination.name' que 
		//esta no PIMessage.properties
		
//		wZCcsXit001.setDest("CCS_160_IDOC");
		wZCcsXit001.setDest("CCQ_350_IDOC");
		wZCcsXit001.setJcoAshost("192.168.35.150");
//		wZCcsXit001.setJcoAshost("192.168.35.153");
		wZCcsXit001.setJcoClient("160");
//		wZCcsXit001.setJcoClient("350");
		wZCcsXit001.setJcoLang("en");
		wZCcsXit001.setJcoPasswd("rfcALL01");
		wZCcsXit001.setJcoSysnr("20");
		wZCcsXit001.setJcoUser("pirfcuser");

		
		return wZCcsXit001;
	}
	public static void main(String[] args) throws Exception {

		ZMdmXiZfaProvisionamentoD zProv = new ZMdmXiZfaProvisionamentoD();

		System.out.println("Inicio: " + new SimpleDateFormat("HH:mm:ss.SSS").format(new Date()));
		long timeInMillis = Calendar.getInstance().getTimeInMillis();

		InputStream inputStream = new FileInputStream(new File(
				"C:\\Users\\dorival1\\AppData\\Roaming\\Skype\\My Skype Received Files\\add-group\\novo\\prov_add_entrada2-del.xml"));
		OutputStream outputStream = new FileOutputStream(new File(
				"C:\\Users\\dorival1\\AppData\\Roaming\\Skype\\My Skype Received Files\\add-group\\novo\\prov_add_del_entrada2-OUT.xml"));

		zProv.execute(inputStream, outputStream);

		System.out.println("Fim: " + new SimpleDateFormat("HH:mm:ss.SSS").format(new Date()));
		long timeInMillis2 = Calendar.getInstance().getTimeInMillis();
		System.out.println("Tempo gasto = " + (Double.valueOf(timeInMillis2 - timeInMillis) / 1000) + " segundo(s)");

		Runtime.getRuntime().exec(
				"explorer C:\\Users\\dorival1\\AppData\\Roaming\\Skype\\My Skype Received Files\\add-group\\novo\\prov_add_del_entrada2-OUT.xml");
	}
}
