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
	private String wChanel;
	private String wData;
	private String wDataC;
	private CpflZCcsXiT001 wZCcsXit001;
	private Object ejbObj;
	private String wStartTime;
	private String endTimeF;
	private String wValue;
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

			String wChanel;
			String wData;
			String wDataC;
			CpflZCcsXiT001 wZCcsXit001;
			Object ejbObj;
			String wStartTime;
			String endTimeF;
			String wValue;

			TyArqAux wArq = new TyArqAux();
			TyArqAux wArqAux = new TyArqAux();
			List<TyArqAux> tArq = new ArrayList<TyArqAux>();
			List<TyArqAux> tArqAux = new ArrayList<TyArqAux>();

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
			for (int i = 0; i < meterReadingList.getLength(); i++) {
				Element meterReading = (Element) meterReadingList.item(i);

				Element meter = (Element) meterReading.getElementsByTagName("Meter").item(0);
				if (meter != null) {
					wEqunr = meter.getAttribute("MeterNm");
				}

				Element intervalReading = (Element) meterReading.getElementsByTagName("IntervalReading").item(0);
				wChanel = intervalReading.getAttribute("Channel");

				wData = intervalReading.getAttribute("BeginTime").substring(0, 10);

				wDataC = wData.replaceAll("-", "");

				// CONVERSION_EXIT_ALPHA_INPUT - trata o campo wEqunr
				char[] charArray = new char[18];
				Arrays.fill(charArray, 0, 18, '0');
				wEqunr = String.valueOf(charArray).substring(wEqunr.length()) + wEqunr;

				Map<String, String> importParamMap = new HashMap<String, String>();
				importParamMap.put("EQUNR", wEqunr);
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

				Calendar calEndtime = GregorianCalendar.getInstance();
				Calendar calStartTime = GregorianCalendar.getInstance();

				NodeList readingList = intervalReading.getElementsByTagName("Reading");
				// DO w_NumIntrvs_i TIMES.
				for (int indexReading = 0; indexReading < readingList.getLength(); indexReading++) {
					Element reading = (Element) readingList.item(indexReading);
					String endTime = reading.getAttribute("EndTime");
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

					wValue = onlyNumbers(wValue);
					// if (wValue != null && !wValue.isEmpty() ) {//TODO:
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

					wArq.pod = wExtUiC;// NPE
					wArq.ref = wRefNumberC;
					wArq.st = wStartTime;
					wArq.et = endTimeF;
					wArq.vl = wValue;
					wArq.un = wUnitC;
					wArq.eq = wEqunr;
					wArqAux.pod = wExtUiC;
					wArqAux.st = wStartTime;
					wArqAux.eq = wEqunr;

					if (wChanel.matches("[1234]")) {
						tArq.add(wArq);
						tArqAux.add(wArqAux);
					}

				} // indexReading

				// meterReading.getAttribute("NumIntrvs");//TODO: Só apagar?

			} // meterReadingList

			// ordenacao pelo campo 'st'
			Collections.sort(tArq);
			Collections.sort(tArqAux);
			for (int i = 0; i < tArqAux.size(); i++) {/verificar loop aninhado.. ver qtd  linhas bate com o controle
				// TyArqAux tArqAux = tArqAux.get(i);
				wArqAux = tArqAux.get(i);
				String vloc;
				vloc = "/interf/gle/mm/out/UIQ" + wArqAux.pod + "_" + wArqAux.eq + "_" + wArqAux.st + "_" + i + ".tmp";

				// criar o arquivo de saida
				File arquivoSaida = new File(vloc);
				arquivoSaida.getParentFile().mkdirs();
				arquivoSaida.createNewFile();

				newOut = new FileOutputStream(new File(vloc));
				wFileContent = new StringBuilder();
				StringBuilder wFile = null;

				List<TyArqAux> listArq = selectTarqComparing(tArq, wArqAux.pod, wArqAux.eq);// arqI.pod,

				for (TyArqAux wArqItem : listArq) {
					if (wArqItem.st.equals(wArqAux.st)) {
						wFile = new StringBuilder(String.valueOf(wFileCharArray));
						// wFile.append(" ", 0, 153);

						wFile.replace(50, 50 + wArqItem.ref.length(), wArqItem.ref);
						wFile.replace(82, (82 + wArqItem.st.length()), wArqItem.st);
						wFile.replace(99, (99 + wArqItem.et.length()), wArqItem.et);
						wFile.replace(130, (130 + "01".length()), "01");
						wFile.replace(134, (134 + wArqItem.un.length()), wArqItem.un);
						wFile.replace(134, (134 + wArqItem.ref.length()), wArqItem.ref);
						System.out.println(wFile);// TODO:
						wFile.append("\n");

						wFileContent.append(wFile);
					}
				}
			}
			newOut.write(wFileContent.toString().getBytes());

			// TransformerFactory transformerFactory =
			// TransformerFactory.newInstance();
			// Transformer transformer = transformerFactory.newTransformer();
			// Source source = new DOMSource(document);
			// Result result = new StreamResult(outputStream);
			// transformer.transform(source , result);

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
