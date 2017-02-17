package br.com.cpfl.mapping;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class PIMessages {
	private static final String BUNDLE_NAME = "br.com.cpfl.mapping.messages"; //$NON-NLS-1$

	private static ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

	private PIMessages() {
	}
	
//	private static String path = "/interf/pi-essages.properties";
	private static String path = "/interf";
	public static String getString(String key) {
		try {
			
			File file = new File(path);

			URL resourceURL;

			resourceURL = file.toURI().toURL();
			URLClassLoader urlLoader = new URLClassLoader(new java.net.URL[] { resourceURL });
//			RESOURCE_BUNDLE = java.util.ResourceBundle.getBundle(path, java.util.Locale.getDefault(),urlLoader);
			RESOURCE_BUNDLE = java.util.ResourceBundle.getBundle("pi-messages", java.util.Locale.getDefault(),urlLoader);
			//RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			e.printStackTrace();
			throw new RuntimeException(
					"[PROPERTIES NOT FOUND]: Arquivo de properties nao encontrato em: " + path + " " + e);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			throw new RuntimeException(
					"[PROPERTIES NOT FOUND]: Arquivo de properties nao encontrato em: " + path + " " + e);
		}
	}
}
