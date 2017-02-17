package br.com.cpfl.mapping.billing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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

public class BillingDet implements StreamTransformation {

	private Map map;

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
			DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = builderFactory.newDocumentBuilder();
			Document document = documentBuilder.parse(inputStream);

			NodeList meterReadingsList = document.getElementsByTagName("ns0:MeterReading");
			Element readings;
			int readIndex;
			for (readIndex = 0; readIndex < meterReadingsList.getLength(); readIndex++) {
				readings = (Element) meterReadingsList.item(readIndex);
				NodeList startTimeList = readings.getElementsByTagName("ns0:startTime");
				for (int i = 0; i < startTimeList.getLength(); i++) {
					Element startTime = (Element) startTimeList.item(i);
					if (startTime.hasChildNodes() && startTime.getFirstChild().getNodeValue().startsWith("S")) {
						String startTimeValue = startTime.getFirstChild().getNodeValue().substring(1);
						// Percorre novamente as tags 'ns0:startTime', substituindo as que estao
						// marcadas com 'ABAP_MAPPING' com a data correta
						for (int j = 0; j < startTimeList.getLength(); j++) {
							Element nodeMudar = (Element) startTimeList.item(j);
							if (nodeMudar != null && nodeMudar.hasChildNodes()
									&& nodeMudar.getFirstChild().getNodeValue().equals("ABAP_MAPPING")) {

								nodeMudar.getFirstChild().setNodeValue(startTimeValue);
							}
						}
						startTime.getFirstChild().setNodeValue(startTimeValue);
					}
				}
			}
				
			for (readIndex = 0; readIndex < meterReadingsList.getLength(); readIndex++) {
				readings = (Element) meterReadingsList.item(readIndex);
				NodeList endTimeList = readings.getElementsByTagName("ns0:endTime");
				for (int i = 0; i < endTimeList.getLength(); i++) {
					Element endTime = (Element) endTimeList.item(i);
					if (endTime.hasChildNodes() && endTime.getFirstChild().getNodeValue().startsWith("E")) {
						String endTimeValue = endTime.getFirstChild().getNodeValue().substring(1);
						// Percorre novamente as tags 'ns0:endTime', substituindo as que estao
						// marcadas com 'ABAP_MAPPING' 
						for (int j = 0; j < endTimeList.getLength(); j++) {
							Element nodeMudar = (Element) endTimeList.item(j);
							if (nodeMudar != null && nodeMudar.hasChildNodes()
									&& nodeMudar.getFirstChild().getNodeValue().equals("ABAP_MAPPING")) {

								nodeMudar.getFirstChild().setNodeValue(endTimeValue);
								System.out.println(nodeMudar.getTextContent());
							}
						}
						endTime.getFirstChild().setNodeValue(endTimeValue);
					}
				}
			}
			
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			Source source = new DOMSource(document);
			Result result = new StreamResult(outputStream);
			
			transformer.transform(source, result);
		} catch (Exception e) {
			e.printStackTrace();
			throw new StreamTransformationException("[Java Mapping]: Falha ao efetuar o mapeamento no Java ", e);
		}
	}

	public static void main(String[] args) throws Exception {

		BillingDet zProv = new BillingDet();

		System.out.println("Inicio: " + new SimpleDateFormat("HH:mm:ss.SSS").format(new Date()));
		long timeInMillis = Calendar.getInstance().getTimeInMillis();

		InputStream inputStream = new FileInputStream(new File(
//				"C:\\Users\\dorival1\\AppData\\Roaming\\Skype\\My Skype Received Files\\billing\\Billing_det_entrada_java.xml"));
				"C:\\Users\\dorival1\\Documents\\xml de entrada com erro.txt"));
		OutputStream outputStream = new FileOutputStream(new File(
				"C:\\Users\\dorival1\\AppData\\Roaming\\Skype\\My Skype Received Files\\billing\\Billing_det_entrada_java-OUT.xml"));

		zProv.execute(inputStream, outputStream);

		System.out.println("Fim: " + new SimpleDateFormat("HH:mm:ss.SSS").format(new Date()));
		long timeInMillis2 = Calendar.getInstance().getTimeInMillis();
		System.out.println("Tempo gasto = " + (Double.valueOf(timeInMillis2 - timeInMillis) / 1000) + " segundo(s)");

		Runtime.getRuntime().exec(
				"explorer C:\\Users\\dorival1\\AppData\\Roaming\\Skype\\My Skype Received Files\\billing\\Billing_det_entrada_java-OUT.xml");
	}
}
