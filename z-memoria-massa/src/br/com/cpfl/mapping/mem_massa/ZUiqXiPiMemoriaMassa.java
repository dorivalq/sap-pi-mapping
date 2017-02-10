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
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
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
	@SuppressWarnings({ "unused", "rawtypes" })
	private Map map;

	private Object ejbObj;

	private OutputStream newOut;
	private StringBuilder wFileContent;

	class TyArq{
		String pod;
		String ref;
		String st;
		String et;
		String vl;
		String un;
		String eq;
	}
	
	class TyArqAux{
		String pod;
		String st;
		String eq;
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
//			result = prime * result + ((eq == null) ? 0 : eq.hashCode());
//			result = prime * result + ((pod == null) ? 0 : pod.hashCode());
			result = prime * result + ((st == null) ? 0 : st.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			TyArqAux other = (TyArqAux) obj;
			other.eq = other.eq == null ? "" : other.eq;
			other.pod = other.pod == null ? "" : other.pod;
			other.st = other.st == null ? "" : other.st;
//			if (other.st.equals(this.st) && other.eq.equals(this.eq) && other.pod.equals(this.pod)){
			if (other.st.equals(this.st) ){
				return true;
			}
			return false;
		}


	}

	@SuppressWarnings("rawtypes")
	@Override
	public void setParameter( Map map) {
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

			TyArq wArq = new TyArq();
			TyArqAux wArqAux = new TyArqAux();
			List<TyArq> tArq = new ArrayList<TyArq>();

			Set<TyArqAux> tArqAux = //new ArrayList<ZUiqXiPiMemoriaMassa.TyArqAux>();
					new TreeSet<TyArqAux>(new Comparator<TyArqAux>() {
				@Override
				public int compare(TyArqAux o1, TyArqAux o2) {
					return o1.st.compareTo(o2.st);
				}
			});// LinkedHashSet<ZUiqXiPiMemoriaMassa.TyArqAux>();
			
			// Inicializa configuracao de JcoFunction
			List<CpflZCcsXiT001> lista = executarMetodoRemoto(CONSULTAR_CCS_XI);
			wZCcsXit001 = lista.get(0);
			wZCcsXit001.setJcoAshost("192.168.35.153");
			wZCcsXit001.setJcoClient("350");
			wZCcsXit001.setJcoSysnr("21");
//			wZCcsXit001.setDest("CCQ");
//			[15:09:16] Guilherme Almeida: 21
//			[15:09:18] Guilherme Almeida: CCQ
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
					
					int intrvlLength = Integer.parseInt(intervalReading.getAttribute("IntrvlLength"));

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
					for (int indexReading = 0; indexReading < readingList.getLength(); indexReading++) {
						Element reading = (Element) readingList.item(indexReading);
						String endTime = null;
						if (reading != null) {
							endTime = reading.getAttribute("EndTime");
						}
						Date date = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss").parse(endTime);
						calEndtime.setTime(date);

						calStartTime.setTime(calEndtime.getTime());
						calStartTime.add(Calendar.MILLISECOND, -300);

						if (calStartTime.compareTo(calEndtime) > 0) {
							calStartTime.add(Calendar.DAY_OF_MONTH, -1);
						}

						wStartTime = new SimpleDateFormat("yyyyMMddhhmmss").format(calStartTime.getTime()) + "-"
								+ endTime.substring(25, 26);

						endTimeF = new SimpleDateFormat("yyyyMMddhhmmss").format(calEndtime.getTime()) + "-"
								+ endTime.substring(25, 26);

						wValue = reading.getAttribute("Value");
						 if (wValue != null && !wValue.isEmpty() && wValue.matches("[+-]?\\d*(\\.\\d+)?") ) {
							 wValue += "000";
						 }

						Double wValueD = Double.valueOf(wValue);
						wValueD = wValueD * 12;

						if (wTpD != 0 && wTcD != 0) {
							wValueD = wValueD * wTcD * wTpD;
						} else if (wTpD == 0 && wTcD != 0) {
							wValueD = wValueD * wTcD;
						} else if (wTpD != 0 && wTcD == 0) {
							wValueD = wValueD * wTpD;
						}

						wValue = wValueD.toString();
						wValue = String.format("%.4f", wValueD);
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

						if (wChanel.matches("[1234]")) {
							tArq.add(wArq);
							//lista com grupos do tamanho do intrvlLength
//							if ((indexReading>0) && indexReading % intrvlLength == 0) {
								tArqAux.add(wArqAux);
//							}
						}
						wArq = new TyArq(); 
						wArqAux = new TyArqAux();
					} // indexReading
				} // IntervalReadingList

			} // meterReadingList

			
			int i = 0;
//			List<TyArqAux> listaArquivos= new ArrayList<>();
//			//transferimos o valor para uma lista, pos um hashSet
//			listaArquivos.addAll (tArqAux);
//			tArqAux.sort(new Comparator<TyArqAux>() {
//				@Override
//				public int compare(TyArqAux o1, TyArqAux o2) {
//					return o1.eq.compareTo(o2.eq);
//				}
//			});
			
			for (TyArqAux tyArqAux : tArqAux) {
				wArqAux = tyArqAux;
				String vloc;
				vloc = "/interf/gle/mm/out/UIQ" + wArqAux.pod + "_" + wArqAux.eq + "_" + wArqAux.st + "_" + (i++) + ".txt";

				// criar o arquivo de saida
				File arquivoSaida = new File(vloc);
				arquivoSaida.getParentFile().mkdirs();
				arquivoSaida.createNewFile();

				newOut = new FileOutputStream(new File(vloc));
				wFileContent = new StringBuilder();
				StringBuilder wFile = null;

				List<TyArq> listArq = selectTarqComparing(tArq, wArqAux.pod, wArqAux.eq);
				
				Collections.sort(listArq, new Comparator<TyArq>() {
					@Override
					public int compare(TyArq o1, TyArq o2) {
						return o1.pod.compareTo(o2.pod);
					}
				});																			

				Collections.sort(listArq, new Comparator<TyArq>() {
					@Override
					public int compare(TyArq o1, TyArq o2) {
						return o1.ref.compareTo(o2.ref);
					}
				});	
				
				Collections.sort(listArq, new Comparator<TyArq>() {
					@Override
					public int compare(TyArq o1, TyArq o2) {
						return o1.st.compareTo(o2.st);
					}
				});	
				
				for (TyArq wArqItem : listArq) {
					if (wArqItem.st.substring(0,8).equals(wArqAux.st)) {
						wFile = new StringBuilder(String.valueOf(wFileCharArray));
						
						wFile.replace(0, wArqItem.pod.length(), wArqItem.pod);
						wFile.replace(50, 50 + wArqItem.ref.length(), wArqItem.ref);
						wFile.replace(65, (65 + wArqItem.st.length()), wArqItem.st);
						wFile.replace(82, (82 + wArqItem.et.length()), wArqItem.et);
						wFile.replace(99, (99 + wArqItem.vl.length()), wArqItem.vl);
						wFile.replace(130,(130 + "01".length()), "01");
						wFile.replace(134,(134 + wArqItem.ref.length()), wArqItem.ref);
						wFile.append("\n");

						wFileContent.append(wFile.toString());
					}
				}
				newOut.write(wFileContent.toString().getBytes());
			}
			
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

	private List<TyArq> selectTarqComparing(List<TyArq> tbArqFull, String pod, String eq) {
		List<TyArq> tbArqNew = new ArrayList<TyArq>();
		if (tbArqFull == null || tbArqFull.isEmpty()) {
			return tbArqNew;
		}
		for (TyArq arqItem : tbArqFull) {
			if (arqItem.pod.equals(pod) && arqItem.eq.equals(eq)) {
				tbArqNew.add(arqItem);
			}
		}
		return tbArqNew;
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

		System.out.println("Inicio: " + new SimpleDateFormat("HH:mm:ss.SSS").format(new Date()));
		long timeInMillis = Calendar.getInstance().getTimeInMillis();

		InputStream inputStream = new FileInputStream(new File("C:\\Users\\dorival1\\AppData\\Roaming\\Skype\\My Skype Received Files\\MEM_MASSA_entrada_java.xml"));
		OutputStream outputStream = new FileOutputStream(new File("C:\\Users\\dorival1\\AppData\\Roaming\\Skype\\My Skype Received Files\\billing\\Billing_det_entrada_java-OUT.xml"));

		zProv.execute(inputStream, outputStream);

		System.out.println("Fim: " + new SimpleDateFormat("HH:mm:ss.SSS").format(new Date()));
		long timeInMillis2 = Calendar.getInstance().getTimeInMillis();
		System.out.println("Tempo gasto = " + (Double.valueOf(timeInMillis2 - timeInMillis) / 1000) + " segundo(s)");

		// Runtime.getRuntime().exec(
		// "explorer C:\\Users\\dorival1\\AppData\\Roaming\\Skype\\My Skype
		// Received Files\\billing\\MEM_MASSA_entrada_java-OUT.xml");
	}
}
