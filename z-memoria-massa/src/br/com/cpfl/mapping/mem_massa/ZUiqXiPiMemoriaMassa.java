package br.com.cpfl.mapping.mem_massa;

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
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.omg.CORBA.WStringSeqHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sap.aii.mapping.api.StreamTransformation;
import com.sap.aii.mapping.api.StreamTransformationException;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoParameterList;

import br.com.cpfl.daoejb.MappingPODataAccessRemote;
import br.com.cpfl.daoejb.entidades.CpflZCcsXiT001;
import br.com.cpfl.mapping.PIMessages;

public class ZUiqXiPiMemoriaMassa implements StreamTransformation {

	private static final char SPACE = ' ';
	private static final String CONSULTAR_CCS_XI = "consultarCcsXi";
	private Map map;

	private Object ejbObj;

	private OutputStream newOut;
	private StringBuilder wFileContent;

	class TyArqAux implements Comparable<TyArqAux> {
		String pod;
		String ref;
		String st;
		String et;
		String vl;
		String un;
		String eq;

		@Override
		public int compareTo(TyArqAux o) {
			return this.st.compareTo(o.st);
		}
	}

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
			DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = builderFactory.newDocumentBuilder();
			Document document = documentBuilder.parse(inputStream);
			String wEqunr = null;

			String wChanel = null;
			String wData = null;
			String wDataC = null;
			CpflZCcsXiT001 wZCcsXit001;
			String wStartTime;
			String endTimeF;
			String wValue;

			TyArqAux wArq = new TyArqAux();
			TyArqAux wArqAux = new TyArqAux();
			List<TyArqAux> tArq = new ArrayList<TyArqAux>();
			Map<String, TyArqAux> tArqAux = new HashMap<String, TyArqAux>();

			// Inicializa configuracao de JcoFunction
			List<CpflZCcsXiT001> lista = executarMetodoRemoto(CONSULTAR_CCS_XI);
			wZCcsXit001 = lista.get(0);
			FunctionBuilder functionBuilder = new FunctionBuilder();
			functionBuilder = new FunctionBuilder();
			functionBuilder.setupDestinationProperties(wZCcsXit001);
			// Fim Inicializa configuracao de JcoFunction

			char wFileCharArray[] = new char[155];
			Arrays.fill(wFileCharArray, SPACE);

			NodeList meterReadingList = document.getElementsByTagName("MeterReading");
			for (int meterIndex = 0; meterIndex < meterReadingList.getLength(); meterIndex++) {
				Element meterReading = (Element) meterReadingList.item(meterIndex);
				
				Element meter = (Element) meterReading.getElementsByTagName("Meter").item(0);
				if (meter != null) {
					wEqunr = meter.getAttribute("MeterNm");
				}
				
				NodeList IntervalReadingList = meterReading.getElementsByTagName("IntervalReading");
				for (int i = 0; i < IntervalReadingList.getLength(); i++) {
					Element intervalReading = (Element) IntervalReadingList.item(i);

					// Element intervalReading = (Element)
					// intervalReading.getElementsByTagName("IntervalReading").item(0);
					if (intervalReading != null) {
						wChanel = intervalReading.getAttribute("Channel");
					}

					String beginTime = intervalReading.getAttribute("BeginTime");
					if (beginTime != null) {
						wData = beginTime.substring(0, 10);
					}
					if (wData != null && !wData.isEmpty()) {
						wDataC = wData.replaceAll("-", "");
					}

					// CONVERSION_EXIT_ALPHA_INPUT - trata o campo wEqunr no
					// java mesmo
					// ao inves de chamar a funcao CONVERSION_EXIT_ALPHA_INPUT
					char[] charArray = new char[18];
					Arrays.fill(charArray, 0, 18, '0');
					String wEqunrC = String.valueOf(charArray).substring(wEqunr.length()) + wEqunr;

					Map<String, String> importParamMap = new HashMap<String, String>();
					importParamMap.put("EQUNR", wEqunrC);
					importParamMap.put("CHANNEL", wChanel);
					importParamMap.put("DATA", wDataC);

					functionBuilder.setupFunction("ZCCSGLEF0082", importParamMap);

					JCoFunction function = functionBuilder.executeCalls();

					JCoParameterList jcoParamList = function.getChangingParameterList();
					String wExtUiC = jcoParamList.getString("EXT_UI");
					String wRefNumberC = jcoParamList.getString(1);
					String wUnitC = jcoParamList.getString("UNIT");
					int wTcD = jcoParamList.getInt("TC");
					int wTpD = jcoParamList.getInt("TP");

					Calendar calEndtime = Calendar.getInstance();
					Calendar calStartTime = Calendar.getInstance();

					NodeList readingList = intervalReading.getElementsByTagName("Reading");
					// DO w_NumIntrvs_i TIMES.
					for (int indexReading = 0; indexReading < readingList.getLength(); indexReading++) {
						Element reading = (Element) readingList.item(indexReading);
						String endTime = null;
						if (reading != null) {
							endTime = reading.getAttribute("EndTime");
						}
						Date date = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss").parse(endTime);
						calEndtime.setTime(date);

						calStartTime.setTime(calEndtime.getTime());
						calStartTime.add(Calendar.SECOND, -300);

						if (calStartTime.compareTo(calEndtime) > 0) {
							calStartTime.add(Calendar.DAY_OF_MONTH, -1);
						}

						wStartTime = new SimpleDateFormat("yyyyMMddhhmmss").format(calStartTime.getTime()) + "-"
								+ endTime.substring(25, 26);

						endTimeF = new SimpleDateFormat("yyyyMMddhhmmss").format(calEndtime.getTime()) + "-"
								+ endTime.substring(25, 26);

						wValue = reading.getAttribute("Value");
						if (wValue != null && !wValue.isEmpty()) {
							wValue = onlyNumbers(wValue);
						}
						// if (wValue != null && !wValue.isEmpty() ) {
						// wValue += "000";
						// }

						Long wValueD = Long.valueOf(wValue);
						wValueD = wValueD * 12;

						if (wTpD != 0 && wTcD != 0) {
							wValueD = wValueD * wTcD * wTpD;
						} else if (wTpD == 0 && wTcD != 0) {
							wValueD = wValueD * wTcD;
						} else if (wTpD != 0 && wTcD == 0) {
							wValueD = wValueD * wTpD;
						}

						wValue = wValueD.toString();

						wArq.pod = wExtUiC;
						wArq.ref = wRefNumberC;
						wArq.st = wStartTime;
						wArq.et = endTimeF;
						wArq.vl = wValue;
						wArq.un = wUnitC;
						wArq.eq = wEqunr;
						wArqAux.pod = wExtUiC;
						wArqAux.st = wStartTime.substring(0, 8);
						wArqAux.eq = wEqunr;

						// if w_CHANNEL_C co '1234'.
						// APPEND W_ARQ TO T_ARQ.
						// COLLECT W_ARQ_AUX INTO T_ARQ_AUX.
						// endif.
						// TODO: VALIDAR?? IF ACIMA
						if (wChanel.matches("[1234]")) {
							tArq.add(wArq);
							tArqAux.put(wArqAux.eq, wArqAux);
						}
						wArq = new TyArqAux(); 
						wArqAux = new TyArqAux();
					} // indexReading
				} // IntervalReadingList

			} // meterReadingList

			// ordenacao pelo campo 'st'
			Collections.sort(tArq);
			List<TyArqAux> tArqAuxList = new ArrayList<TyArqAux>();
			tArqAuxList.addAll(tArqAux.values());
			Collections.sort(tArqAuxList);
			for (int i = 0; i < tArqAuxList.size(); i++) {
				// TyArqAux tArqAux = tArqAux.get(i);
				wArqAux = tArqAuxList.get(i);
				String vloc;
				vloc = "/interf/gle/mm/out/UIQ" + wArqAux.pod + "_" + wArqAux.eq + "_" + wArqAux.st + "_" + (i+1) + ".txt";

				// criar o arquivo de saida
				File arquivoSaida = new File(vloc);
				arquivoSaida.getParentFile().mkdirs();
				arquivoSaida.createNewFile();

				newOut = new FileOutputStream(new File(vloc));
				wFileContent = new StringBuilder();
				StringBuilder wFile = null;

				List<TyArqAux> listArq = selectTarqComparing(tArq, wArqAux.pod, wArqAux.eq);
																							
																							

				for (TyArqAux wArqItem : listArq) {
					if (wArqItem.st.substring(0,8).equals(wArqAux.st)) {
						wFile = new StringBuilder(String.valueOf(wFileCharArray));
						
						wFile.replace(0, wArqItem.pod.length(), wArqItem.pod);
						wFile.replace(50, 50 + wArqItem.ref.length(), wArqItem.ref);
						wFile.replace(65, (65 + wArqItem.st.length()), wArqItem.st);
						wFile.replace(82, (82 + wArqItem.et.length()), wArqItem.et);
						wFile.replace(99, (99 + wArqItem.vl.length()), wArqItem.vl);
						wFile.replace(130,(130 + "01".length()), "01");
						wFile.replace(134,(134 + wArqItem.ref.length()), wArqItem.ref);
						System.out.println(wFile);// TODO:
						wFile.append("\n");

						wFileContent.append(wFile.toString());
					}
					newOut.write(wFileContent.toString().getBytes());
				}
				// rename de .tmp para .txt//TODO:
//				String wLoc = vloc.replace(".tmp", ".txt");
//				String wCommand = "mv" + vloc + " " + wLoc;
//				Runtime.getRuntime().exec(wCommand);
			}
//			newOut.write(wFileContent.toString().getBytes());
			System.out.println("--------------------------------------------------------------");// TODO:
																									// remover
			System.out.println(wFileContent);
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			Source source = new DOMSource(document);
			Result result = new StreamResult(outputStream);
			transformer.transform(source, result);

		} catch (Exception e) {
			e.printStackTrace();
			throw new StreamTransformationException("[Java Mapping]: Falha ao efetuar o Java Mapping: ", e);
		} finally {
			if (newOut != null) {
				try {
					newOut.close();
				} catch (IOException e) {
					System.err.println("nao foi possivel liberar o recurso: newOut.close() ");
					e.printStackTrace();
				}
			}
		}

	}

	private List<TyArqAux> selectTarqComparing(List<TyArqAux> tbArqFull, String pod, String eq) {
		List<TyArqAux> tbArqNew = new ArrayList<TyArqAux>();
		if (tbArqFull == null || tbArqFull.isEmpty()) {
			return tbArqNew;
		}
		for (TyArqAux arqItem : tbArqFull) {
			if (arqItem.pod.equals(pod) && arqItem.eq.equals(eq)) {
				tbArqNew.add(arqItem);
			}
		}
		return tbArqNew;
	}

	public String onlyNumbers(String str) {
		if (str != null) {
			return str.replaceAll("[^0123456789]", "").trim();
		} else {
			return "";
		}
	}

	private String getValue(Element elementP, String name) {
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

	private String getAttributeValue(Element elementParent, String nodeName, String attributeName) {
		if (elementParent != null) {
			Element e = (Element) elementParent.getElementsByTagName(nodeName).item(0);
			if (e != null) {
				return e.getAttribute(attributeName) == null ? "" : e.getAttribute(attributeName);
			}
		}

		return "";
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

	public static void main(String[] args) throws Exception {

		ZUiqXiPiMemoriaMassa zProv = new ZUiqXiPiMemoriaMassa();

		// char wFileCharArray[] = new char[153];
		// Arrays.fill(wFileCharArray, SPACE);

		// StringBuilder wFile = new
		// StringBuilder(String.valueOf(wFileCharArray));
		// List<String> listArq = new
		// ArrayList<String>();//selectTarqComparing(tbArq, tArqAux.pod,
		// tArqAux.eq);// arqI.pod,
		// listArq.add("dorival");
		// listArq.add("cpfl");
		// listArq.add("1.001");
		// for (String wArqItem : listArq) {
		// //TODO: add formatação ex: w_file+50(07)
		//// wFile.append(wArqItem.ref, 50, (50+7));
		// wFile.replace(50, 50+wArqItem.length(), wArqItem);
		//
		// wFile.replace(82, (82+wArqItem.length()), wArqItem);
		// wFile.replace(99, (99+wArqItem.length()), wArqItem);
		// wFile.replace(130, (130+wArqItem.length()),"01");
		// wFile.replace(134, (134+wArqItem.length()),wArqItem);
		// wFile.append("\n");
		// }

		System.out.println(Double.valueOf("0.1571").intValue());
		System.out.println(Integer.parseInt(zProv.onlyNumbers("0.1571")));

		System.out.println("Inicio: " + new SimpleDateFormat("HH:mm:ss.SSS").format(new Date()));
		long timeInMillis = Calendar.getInstance().getTimeInMillis();

		InputStream inputStream = new FileInputStream(new File(
				"C:\\Users\\dorival1\\AppData\\Roaming\\Skype\\My Skype Received Files\\MEM_MASSA_entrada_java.xml"));
		OutputStream outputStream = new FileOutputStream(new File(
				"C:\\Users\\dorival1\\AppData\\Roaming\\Skype\\My Skype Received Files\\billing\\Billing_det_entrada_java-OUT.xml"));

		zProv.execute(inputStream, outputStream);

		System.out.println("Fim: " + new SimpleDateFormat("HH:mm:ss.SSS").format(new Date()));
		long timeInMillis2 = Calendar.getInstance().getTimeInMillis();
		System.out.println("Tempo gasto = " + (Double.valueOf(timeInMillis2 - timeInMillis) / 1000) + " segundo(s)");

		// Runtime.getRuntime().exec(
		// "explorer C:\\Users\\dorival1\\AppData\\Roaming\\Skype\\My Skype
		// Received Files\\billing\\MEM_MASSA_entrada_java-OUT.xml");
	}
}
