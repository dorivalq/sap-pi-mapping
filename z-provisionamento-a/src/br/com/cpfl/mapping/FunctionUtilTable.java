package br.com.cpfl.mapping;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.sap.conn.jco.AbapException;
import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoDestinationManager;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoTable;
import com.sap.conn.jco.ext.DataProviderException;
import com.sap.conn.jco.ext.DestinationDataEventListener;
import com.sap.conn.jco.ext.DestinationDataProvider;

import br.com.cpfl.daoejb.entidades.CpflZCcsXiT001;

public class FunctionUtilTable {

	private static JCoFunction function;
	private JCoTable table;

	public static class MyDestinationDataProvider implements DestinationDataProvider {
		private DestinationDataEventListener eL;
		private HashMap<String, Properties> secureDBStorage = new HashMap<String, Properties>();

		public Properties getDestinationProperties(String destinationName) {
			try {
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
	/**
	 * @param ccsXi Entidade CpflZCcsXiT001 com os dados para a conexao Jco
	 * @param functionName Nome da função
	 * @param importParamMap Mapa com todos os import parameters da funcao
	 * @return JCoFunction com o resultado da execução.
	 */
	public static JCoFunction executeCalls(CpflZCcsXiT001 ccsXi, String functionName,
			Map<String, String> importParamMap) {
		function = null;
		MyDestinationDataProvider myProvider = new MyDestinationDataProvider();
		try {
			com.sap.conn.jco.ext.Environment.registerDestinationDataProvider(myProvider);
		} catch (IllegalStateException providerAlreadyRegisteredException) {
			//throw new RuntimeException(providerAlreadyRegisteredException);
			System.err.println("Provider already registered.");
		}
		Properties properties = getDestinationPropertiesFromUI(ccsXi);
		myProvider.changeProperties(ccsXi.getDest(), properties);

		JCoDestination dest;

		try {
			dest = JCoDestinationManager.getDestination(ccsXi.getDest());

			function = dest.getRepository().getFunction(functionName);
			if (function == null)
				throw new RuntimeException(functionName + " not found in SAP.");

			Set<String> keySet = importParamMap.keySet();
			for (String key : keySet) {
				function.getImportParameterList().setValue(key, importParamMap.get(key));
			}

			try {
				function.execute(dest);
			} catch (AbapException e) {
				e.printStackTrace();
				throw e;
			}

		} catch (JCoException e) {
			e.printStackTrace();
			System.out.println("Execution on destination " + ccsXi + ", Function: " + functionName + " failed");
			throw new RuntimeException(e);
		}
		return function;
	}

	public static Properties getDestinationPropertiesFromUI(CpflZCcsXiT001 ccsXi) {
		// adapt parameters in order to configure a valid destination
		Properties connectProperties = new Properties();
		connectProperties.setProperty(DestinationDataProvider.JCO_ASHOST, ccsXi.getJcoAshost());
		connectProperties.setProperty(DestinationDataProvider.JCO_SYSNR, ccsXi.getJcoSysnr());
		connectProperties.setProperty(DestinationDataProvider.JCO_CLIENT, ccsXi.getJcoClient());
		connectProperties.setProperty(DestinationDataProvider.JCO_USER, ccsXi.getJcoUser());
		connectProperties.setProperty(DestinationDataProvider.JCO_PASSWD, ccsXi.getJcoPasswd());
		connectProperties.setProperty(DestinationDataProvider.JCO_LANG, ccsXi.getJcoLang());
		return connectProperties;
	}
	
	private void addTableValue(String tableName, Map<String, String> fieldMap) {
		table = function.getTableParameterList().getTable(tableName);
		table.appendRow();
		Set<String> keySet = fieldMap.keySet();
		for (String key : keySet) {
			table.setValue(key, fieldMap.get(key));
		}
		
	}
}
