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

public class FunctionUtil {

	private JCoFunction function;
	private JCoDestination dest;

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
	 * 	Exemplo:
	 * 		wZCcsXit001 = classe.executarMetodoRemoto(CONSULTAR_CCS_XI)
	 * 		Map<String, String> importParamMap = new HashMap<String, String>();
			importParamMap.put("QUERY_TABLE", "EQUI");
	 * 		FunctionUtil.executeCalls(zProv.wZCcsXit001, "RFC_READ_TABLE", importParamMap);
	 * @param functionName Nome da função
	 * @param importParamMap Mapa com todos os import parameters da funcao
	 * 		Exemplo de uso:
	 * 			Map<String, String> importParamMap = new HashMap<String, String>();
				importParamMap.put("QUERY_TABLE", "EQUI");
	 * @return JCoFunction com o resultado da execução.
	 */
	public JCoFunction executeCalls(CpflZCcsXiT001 ccsXi, String functionName,
			Map<String, String> importParamMap) {

		try {

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

	private void addTableValue(String tableName, String tableField, String tableFieldValue ){
		JCoTable table = null;
		table = function.getTableParameterList().getTable(tableName);
		table.appendRow();
		table.setValue(tableField,tableFieldValue);
	}
	
	private static JCoFunction getFunction(String functionName, Map<String, String> importParamMap, JCoDestination dest)
			throws JCoException {
		JCoFunction function;
		function = dest.getRepository().getFunction(functionName);
		if (function == null)
			throw new RuntimeException(functionName + " not found in SAP.");

		Set<String> keySet = importParamMap.keySet();
		for (String key : keySet) {
			System.out.println(importParamMap.get(key));
			function.getImportParameterList().setValue(key, importParamMap.get(key));
		}
		return function;
	}

	private static void setProperties(CpflZCcsXiT001 ccsXi) {
		MyDestinationDataProvider myProvider = new MyDestinationDataProvider();
		try {
			com.sap.conn.jco.ext.Environment.registerDestinationDataProvider(myProvider);
		} catch (IllegalStateException providerAlreadyRegisteredException) {
			throw new RuntimeException(providerAlreadyRegisteredException);
		}
		Properties properties = getDestinationPropertiesFromUI(ccsXi);
		myProvider.changeProperties(ccsXi.getDest(), properties);
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
}
