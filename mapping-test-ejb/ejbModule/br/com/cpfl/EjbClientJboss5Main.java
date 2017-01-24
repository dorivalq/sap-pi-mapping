package br.com.cpfl;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import br.com.cpfl.daoejb.MappingDataAccessRemote;

public class EjbClientJboss5Main {
	/*
	 * location of JBoss JNDI Service provider the client will use. It should be
	 * URL string.
	 */
	private static final String PROVIDER_URL = "jnp://localhost:1099";

	/*
	 * specifying the list of package prefixes to use when loading in URL
	 * context factories. colon separated
	 */
	private static final String JNP_INTERFACES = "org.jboss.naming:org.jnp.interfaces";

	/*
	 * Factory that creates initial context objects. fully qualified class name.
	 */
	private static final String INITIAL_CONTEXT_FACTORY = "org.jnp.interfaces.NamingContextFactory";

	private static Context context;

	public static void main(String[] args) {

		MappingDataAccessRemote bean = null;
		// Properties extends HashTable
		Properties prop = new Properties();
		prop.put(Context.INITIAL_CONTEXT_FACTORY, INITIAL_CONTEXT_FACTORY);
		prop.put(Context.URL_PKG_PREFIXES, JNP_INTERFACES);
		prop.put(Context.PROVIDER_URL, PROVIDER_URL);
		try {
			context = new InitialContext(prop);
			bean = (MappingDataAccessRemote) context.lookup("MappingDataAccessImpl/remote");

//		ZModelObisc model = (ZModelObisc) bean.consultarModelo("123").get(0);
//		System.out.println("Modelo recuperado de ejb remoto : " + model.getId().getModel());

		} catch (NamingException e) {
			e.printStackTrace();
		}

	}
}
