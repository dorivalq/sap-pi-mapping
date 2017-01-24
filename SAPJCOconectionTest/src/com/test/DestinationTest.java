package com.test;

import java.util.HashMap;
import java.util.Properties;

import com.sap.conn.jco.AbapException;
import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoDestinationManager;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.ext.DataProviderException;
import com.sap.conn.jco.ext.DestinationDataEventListener;
import com.sap.conn.jco.ext.DestinationDataProvider;

/**
 * Each application using Java Connector 3 deals with destinations. A
 * destination represents a logical address of an ABAP system and thus separates
 * the destination configuration from application logic. JCo retrieves the
 * destination parameters required at runtime from DestinationDataProvider and
 * ServerDataProvider registered in the runtime environment. If no provider is
 * registered, JCo uses the default implementation that reads the configuration
 * from a properties file. It is expected that each environment provides a
 * suitable implementation that meets security and other requirements.
 * Furthermore, only one DestinationDataProvider and only one ServerDataProvider
 * can be registered per process. The reason behind this design decision is the
 * following: the provider implementations are part of the environment
 * infrastructure. The implementation should not be application specific, and in
 * particular must be separated from application logic.
 * 
 * This example demonstrates a simple implementation of the
 * DestinationDataProvider interface and shows how to register it. A real world
 * implementation should save the configuration data in a secure way.
 */
public class DestinationTest {
	// The custom destination data provider implements DestinationDataProvider
	// and
	// provides an implementation for at least getDestinationProperties(String).
	// Whenever possible the implementation should support events and notify the
	// JCo runtime
	// if a destination is being created, changed, or deleted. Otherwise JCo
	// runtime
	// will check regularly if a cached destination configuration is still valid
	// which incurs
	// a performance penalty.
	static class MyDestinationDataProvider implements DestinationDataProvider {
		private DestinationDataEventListener eL;
		private HashMap<String, Properties> secureDBStorage = new HashMap<String, Properties>();

		public Properties getDestinationProperties(String destinationName) {
			try {
				// read the destination from DB
				Properties p = secureDBStorage.get(destinationName);

				if (p != null) {
					// check if all is correct, for example
					if (p.isEmpty())
						throw new DataProviderException(DataProviderException.Reason.INVALID_CONFIGURATION,
								"destination configuration is incorrect", null);

					return p;
				}

				return null;
			} catch (RuntimeException re) {
				throw new DataProviderException(DataProviderException.Reason.INTERNAL_ERROR, re);
			}
		}

		// An implementation supporting events has to retain the eventListener
		// instance provided
		// by the JCo runtime. This listener instance shall be used to notify
		// the JCo runtime
		// about all changes in destination configurations.
		public void setDestinationDataEventListener(DestinationDataEventListener eventListener) {
			this.eL = eventListener;
		}

		public boolean supportsEvents() {
			return true;
		}

		// implementation that saves the properties in a very secure way
		void changeProperties(String destName, Properties properties) {
			synchronized (secureDBStorage) {
				if (properties == null) {
					if (secureDBStorage.remove(destName) != null)
						eL.deleted(destName);
				} else {
					secureDBStorage.put(destName, properties);
					eL.updated(destName); // create or updated
				}
			}
		}
	} // end of MyDestinationDataProvider

	// business logic
	void executeCalls(String destName) {
		JCoDestination dest;
		try {
			dest = JCoDestinationManager.getDestination(destName);
			dest.ping();

			JCoFunction function = dest.getRepository().getFunction("STFC_CONNECTION");
			if (function == null)
				throw new RuntimeException("BAPI_COMPANYCODE_GETLIST not found in SAP.");

			// JCoFunction is container for function values. Each function
			// contains separate
			// containers for import, export, changing and table parameters.
			// To set or get the parameters use the APIS setValue() and
			// getXXX().
			function.getImportParameterList().setValue("REQUTEXT", "Hfala mano");

			try {
				// execute, i.e. send the function to the ABAP system addressed
				// by the specified destination, which then returns the function
				// result.
				// All necessary conversions between Java and ABAP data types
				// are done automatically.
				function.execute(dest);
			} catch (AbapException e) {
				System.out.println(e.toString());
				return;
			}

			System.out.println("STFC_CONNECTION finished:");
			System.out.println(" Echo: " + function.getExportParameterList().getString("ECHOTEXT"));
//            System.out.println(" Response: " + function.getExportParameterList().getString("RESPTEXT"));
			System.out.println(" Response: " + function.getExportParameterList().getString("RESPTEXT"));
			System.out.println();
			System.out.println("Destination " + destName + " works");
		} catch (JCoException e) {
			e.printStackTrace();
			System.out.println("Execution on destination " + destName + " failed");
		}
	}

	static Properties getDestinationPropertiesFromUI() {
		// adapt parameters in order to configure a valid destination
		Properties connectProperties = new Properties();
		connectProperties.setProperty(DestinationDataProvider.JCO_ASHOST, "192.168.35.150");
		connectProperties.setProperty(DestinationDataProvider.JCO_SYSNR, "20");
		connectProperties.setProperty(DestinationDataProvider.JCO_CLIENT, "160");
		connectProperties.setProperty(DestinationDataProvider.JCO_USER, "pirfcuser");
		connectProperties.setProperty(DestinationDataProvider.JCO_PASSWD, "rfcALL01");
		connectProperties.setProperty(DestinationDataProvider.JCO_LANG, "en");
		return connectProperties;
	}

	public static void main(String[] args) {

		MyDestinationDataProvider myProvider = new MyDestinationDataProvider();

		// register the provider with the JCo environment;
		// catch IllegalStateException if an instance is already registered
		try {
			com.sap.conn.jco.ext.Environment.registerDestinationDataProvider(myProvider);
		} catch (IllegalStateException providerAlreadyRegisteredException) {
			// somebody else registered its implementation,
			// stop the execution
			throw new Error(providerAlreadyRegisteredException);
		}

//		String destName = "ABAP_AS";
		String destName = "CCS_160_IDOC";
		
		
		DestinationTest test = new DestinationTest();

		// set properties for the destination and ...
		myProvider.changeProperties(destName, getDestinationPropertiesFromUI());
		// ... work with it
		test.executeCalls(destName);

//        //now remove the properties and ...
//        myProvider.changeProperties(destName, null);
//        //... and let the test fail
//        test.executeCalls(destName);
	}

}
