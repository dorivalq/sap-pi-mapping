package br.com.cpfl.mapping.mem_massa;

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
import br.com.cpfl.mapping.PIMessages;

/**
 * Classe responsavel por executar funncoes JCO. Configurar conforme abaixo.
 * Configurar como no exemplo<br>
 * <code>
List<CpflZCcsXiT001> lista = executarMetodoRemoto(CONSULTAR_CCS_XI);
		wZCcsXit001 = lista.get(0);

		functionBuilder = new FunctionBuilder();
		functionBuilder.setupDestinationProperties(wZCcsXit001);
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
		</code>
 * 
 * @param ccsXi
 * @return FunctionBuilder
 * @author Dorival Querino da Silva - 1 de fev de 2017 - CSC
 * 
 */
public class FunctionBuilder {
	private FunctionBuilderInner builder;
	
	public FunctionBuilder() {
		this.builder = new FunctionBuilderInner();
	}
	
	/**
	 * Configurar como no exemplo
	 * functionBuilder = new FunctionBuilder();
	 * functionBuilder.setupDestinationProperties(Entidade_wZCcsXit001);
	 * functionBuilder.setupFunction("NOME_FUNCAO", importParamMap);
	 * 
	 * @param ccsXi
	 * @return FunctionBuilder
	 */
	public FunctionBuilder setupDestinationProperties(CpflZCcsXiT001 ccsXi) {
		builder.setupDestinationProperties(ccsXi);
		return this;
	}
	
	/**
	 * Configurar como no exemplo <code>
	List<CpflZCcsXiT001> lista = executarMetodoRemoto(CONSULTAR_CCS_XI);
			wZCcsXit001 = lista.get(0);
	
			functionBuilder = new FunctionBuilder();
			functionBuilder.setupDestinationProperties(wZCcsXit001);
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
			</code>
	 * 
	 * @param ccsXi
	 * @return FunctionBuilder
	 */
	public FunctionBuilder setupFunction(String functionName, Map<String, String> importParamMap){
		
		try {
			builder.setupFunction(functionName, importParamMap);
		} catch (JCoException e) {
			throw new RuntimeException(e);
		}
		return this;
	}
	
	/**
	 * Configurar como no exemplo: <code>
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
			</code>
	 * 
	 * @param tableName
	 * @param fieldMap
	 * @return
	 */
	public FunctionBuilder addTableValue(String tableName, Map fieldMap ){
		builder.addTableValue(tableName, fieldMap);
		return this;
	}
	
	/**
	 * @param <code>ccsXi Entidade CpflZCcsXiT001 com os dados para a
	 *            conexao Jco Exemplo: wZCcsXit001 =
	 *            classe.executarMetodoRemoto(CONSULTAR_CCS_XI) Map<String,
	 *            String> importParamMap = new HashMap<String, String>();
	 *            importParamMap.put("QUERY_TABLE", "EQUI");
	 *            FunctionUtil.executeCalls(zProv.wZCcsXit001,
	 *            "RFC_READ_TABLE", importParamMap); </code>
	 * @param functionName Nome da função
	 * @param importParamMap Mapa com todos os import parameters da funcao
	 *            Exemplo de uso: Map<String, String> importParamMap = new
	 *            HashMap<String, String>();
	 *            importParamMap.put("QUERY_TABLE", "EQUI");
	 * @return JCoFunction com o resultado da execução.
	 */
	public JCoFunction executeCalls(){
		return builder.executeCalls();
	}
	
	public static class FunctionBuilderInner {
		private JCoFunction function;
		private JCoDestination dest;
		private Properties connectProperties;
		private JCoTable table;
		private CpflZCcsXiT001 ccsXi;

		public class MyDestinationDataProvider implements DestinationDataProvider {
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
		 *            Exemplo: wZCcsXit001 =
		 *            classe.executarMetodoRemoto(CONSULTAR_CCS_XI) Map<String,
		 *            String> importParamMap = new HashMap<String, String>();
		 *            importParamMap.put("QUERY_TABLE", "EQUI");
		 *            FunctionUtil.executeCalls(zProv.wZCcsXit001,
		 *            "RFC_READ_TABLE", importParamMap);
		 * @param functionName Nome da função
		 * @param importParamMap Mapa com todos os import parameters da funcao
		 *            Exemplo de uso: Map<String, String> importParamMap = new
		 *            HashMap<String, String>();
		 *            importParamMap.put("QUERY_TABLE", "EQUI");
		 * @return JCoFunction com o resultado da execução.
		 */
		public JCoFunction executeCalls() {
//			setupDestinationProperties(ccsXi);
			
			try {
				function.execute(dest);
			} catch (JCoException e) {
				e.printStackTrace();
				System.out.println("Execution on destination " + dest.getDestinationName() + ", Function: " + function.getName() + " failed");
				throw new RuntimeException(e);
			}
			return function;
		}

		private void addTableValue(String tableName, Map<String, String> fieldMap) {
			table = function.getTableParameterList().getTable(tableName);
			table.appendRow();
			Set<String> keySet = fieldMap.keySet();
			for (String key : keySet) {
				table.setValue(key, fieldMap.get(key));
			}
			
		}

		public JCoFunction setupFunction(String functionName, Map<String, String> importParamMap) throws JCoException {
			if (dest==null ||dest.getRepository() == null ) {
				setupDestinationProperties(ccsXi);
			}
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

		private void setupDestinationProperties(CpflZCcsXiT001 ccsXi) {
			this.ccsXi = ccsXi;
			MyDestinationDataProvider myProvider = new MyDestinationDataProvider();
			
			System.out.println(PIMessages.getString("pi.destination.name"));
			try {
				//Dentro do PI, este if nao sera executado, irá cais na clausula else.
				if (!com.sap.conn.jco.ext.Environment.isDestinationDataProviderRegistered()) {
					com.sap.conn.jco.ext.Environment.registerDestinationDataProvider(myProvider);

					connectProperties = new Properties();
					connectProperties.setProperty(DestinationDataProvider.JCO_ASHOST, ccsXi.getJcoAshost());
					connectProperties.setProperty(DestinationDataProvider.JCO_SYSNR, ccsXi.getJcoSysnr());
					connectProperties.setProperty(DestinationDataProvider.JCO_CLIENT, ccsXi.getJcoClient());
					connectProperties.setProperty(DestinationDataProvider.JCO_USER, ccsXi.getJcoUser());
					connectProperties.setProperty(DestinationDataProvider.JCO_PASSWD, ccsXi.getJcoPasswd());
					connectProperties.setProperty(DestinationDataProvider.JCO_LANG, ccsXi.getJcoLang());

					myProvider.changeProperties(ccsXi.getDest(), connectProperties);

					dest = JCoDestinationManager.getDestination(ccsXi.getDest());			
				}else{
					ccsXi.setDest(PIMessages.getString("pi.destination.name"));
					dest = JCoDestinationManager.getDestination( ccsXi.getDest());
				}
			} catch (IllegalStateException e) {
				System.err.println("Destination already registered. Proceed with the previous one");
				throw new RuntimeException(e);
			} catch (JCoException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}

	}
}
