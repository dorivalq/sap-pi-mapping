package br.com.cpfl.mapping.load_data.client;

import static javax.naming.Context.URL_PKG_PREFIXES;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

import com.sap.engine.services.configuration.appconfiguration.ApplicationConfigHandlerFactory;

import br.com.cpfl.daoejb.MappingPODataAccessRemote;
import br.com.cpfl.daoejb.load.LoadDataPORemote;
import br.com.cpfl.mapping.PIMessages;

public class LoadDataEjbClient {
	public static void main(String[] args) throws Exception {

		LoadDataPORemote service = getMappingService();
		
		service.carragarDadosZFlag();
		service.carregarDadosZModel();
//     (String dest, String flag, String jcoAshost, String jcoSysnr, String jcoClient, String jcoUser, String jcoPasswd, String jcoLang)
		service.carregarDestinationCcsXi("CCS_160_IDOC" ,null ,"192.168.35.150" ,"20" ,"160" ,"pirfcuser" ,"rfcALL01" ,"en");
		service.carregarDadosZProvGrupo();
		
		System.out.println(" Load Data finalizado com sucesso !!");
	}

//	public static LoadDataPORemote getService() throws Exception {
//
//
//		Properties props = new Properties();
//
//		props.put(Context.INITIAL_CONTEXT_FACTORY,
//
//				"com.sap.engine.services.jndi.InitialContextFactoryImpl");
//
//		props.put(Context.PROVIDER_URL, "10.50.152.35:50004");
//
//		props.put(Context.URL_PKG_PREFIXES, "com.sap.engine.services");
//
//		InitialContext ctx = new InitialContext(props);
//
//		ApplicationConfigHandlerFactory cfgHandler = (ApplicationConfigHandlerFactory) ctx
//				.lookup("ApplicationConfiguration");
//
//		java.util.Properties appProps = cfgHandler.getApplicationProperties();
//
//		LoadDataPORemote ejb = (LoadDataPORemote) ctx.lookup(""
//				+ "ejb:/appName=demo.sap.com/dao-ejb-3-ear, jarName=demo.sap.com~dao-ejb-3.jar, beanName=LoadDataPOImpl, interfaceName=br.com.cpfl.daoejb.load.LoadDataPORemote");
//		
//		return ejb;
//	}
	
	private static LoadDataPORemote getMappingService() throws Exception {

		Properties props = new Properties();

		props.put(Context.INITIAL_CONTEXT_FACTORY,

				PIMessages.getString("ejbclient.context"));
		props.put(Context.PROVIDER_URL, PIMessages.getString("ejbclient.host_port"));

		props.put(URL_PKG_PREFIXES, PIMessages.getString("ejbclient.pkg_prefix"));

		InitialContext ctx = new InitialContext(props);

//		ApplicationConfigHandlerFactory cfgHandler = (ApplicationConfigHandlerFactory) ctx
//		.lookup("ApplicationConfiguration");

//		java.util.Properties appProps = cfgHandler.getApplicationProperties();
//		System.out.println(appProps.toString());
		Object ejb = ctx.lookup(PIMessages.getString("loadData.po.remote_url"));

		return (LoadDataPORemote) ejb;
	}
}
