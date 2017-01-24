package com.table;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Properties;

import com.sap.conn.jco.ext.DestinationDataProvider;
/*
 SAP Moderator SAP Employee Tom Xing replied May 13, 2015 at 12:02 PM
Hi Remya,

Afaik JCo won't deal with DB user directly.


You can refer to below link for reference. It provides an example you might be interested in:
Using dynamic Open SQL to perform joins on SAP transparent tables
http://www.ibm.com/developerworks/data/library/techarticle/dm-1007sapopensql/
 * */
public class AccessTable {

}

class stepByStepClient {
	static String DESTINATION_NAME1 = "ABAP_AS_WITHOUT_POOL";
	static String DESTINATION_NAME2 = "ABAP_AS_WITH_POOL";

	static {
		// adapt parameters in order to configure a valid destination
		Properties connectProperties = new Properties();
		connectProperties.setProperty(DestinationDataProvider.JCO_ASHOST, "192.168.35.150");
		connectProperties.setProperty(DestinationDataProvider.JCO_SYSNR, "20");
		connectProperties.setProperty(DestinationDataProvider.JCO_CLIENT, "160");
		connectProperties.setProperty(DestinationDataProvider.JCO_USER, "ct06170");
		connectProperties.setProperty(DestinationDataProvider.JCO_PASSWD, "Guilherme4*");
		connectProperties.setProperty(DestinationDataProvider.JCO_LANG, "en");

	}

	static void createDestinationDataFile(String destinationName, Properties connectProperties) {
		File destCfg = new File(destinationName + ".jcoDestination");
		try {
			FileOutputStream fos = new FileOutputStream(destCfg, false);
			connectProperties.store(fos, "for tests only !");
			fos.close();
		} catch (Exception e) {
			throw new RuntimeException("Unable to create the destination files", e);
		}
	}
}
