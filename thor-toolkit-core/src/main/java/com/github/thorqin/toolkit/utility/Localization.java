/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.thorqin.toolkit.utility;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.security.InvalidParameterException;
import java.util.*;

/**
 *
 * @author nuo.qin
 */
public class Localization {

    public class UTF8Control extends ResourceBundle.Control {
        public ResourceBundle newBundle
                (String baseName, Locale locale, String format, ClassLoader loader, boolean reload)
                throws IllegalAccessException, InstantiationException, IOException
        {
            // The below is a copy of the default implementation.
            String bundleName = toBundleName(baseName, locale);
            String resourceName = toResourceName(bundleName, "properties");
            ResourceBundle bundle = null;
            InputStream stream = null;
            if (reload) {
                URL url = loader.getResource(resourceName);
                if (url != null) {
                    URLConnection connection = url.openConnection();
                    if (connection != null) {
                        connection.setUseCaches(false);
                        stream = connection.getInputStream();
                    }
                }
            } else {
                stream = loader.getResourceAsStream(resourceName);
            }
            if (stream != null) {
                try {
                    // Only this line is changed to make it to read properties files as UTF-8.
                    bundle = new PropertyResourceBundle(new InputStreamReader(stream, "UTF-8"));
                } finally {
                    stream.close();
                }
            }
            return bundle;
        }
    }

	private Locale locale;
	private ResourceBundle bundle;

    private static Map<String, Localization> cache = new HashMap<>();

    private static String[] splitLocalePart(String localeString) {
        if (localeString == null) {
            localeString = "en-US";
        }
        int pos = localeString.indexOf(",");
        if (pos >= 0) {
            localeString = localeString.substring(0, pos);
        } else {
            pos = localeString.indexOf(";");
            if (pos >= 0) {
                localeString = localeString.substring(0, pos);
            }
        }
        if (localeString.matches("^[a-zA-Z]+_[a-zA-Z]+$")) {
            return localeString.split("_");
        } else if (localeString.matches("^[a-zA-Z]+-[a-zA-Z]+$")) {
            return localeString.split("-");
        }
        throw new InvalidParameterException("Invalid locale name.");
    }

	/**
	 * Construct by locale string
     * @param bundleName Bundle name
	 * @param localeString For example: "zh_CN"
	 */
	public Localization(String bundleName, String localeString) {
		this(bundleName, splitLocalePart(localeString)[0], splitLocalePart(localeString)[1]);
	}

	/**
     * Construct by language & country
	 * @param bundleName Bundle name
	 * @param language "zh"
	 * @param country "CN"
	 */
	public Localization(String bundleName, String language, String country) {
		if (language == null || country == null)
			throw new InvalidParameterException("Invalid loacle.");
		locale = new Locale(language.toLowerCase(), country.toUpperCase());
		bundle = ResourceBundle.getBundle(bundleName, locale, this.getClass().getClassLoader(), new UTF8Control());
	}

    /**
     * Translate message in specified locale.
     * @param msg Message to be translated
     * @return Translated message
     */
	public String get(String msg) {
		try {
			if (bundle.keySet().contains(msg))
				return bundle.getString(msg);
			else
				return msg;
		} catch (Exception e) {
			return msg;
		}
	}

    public static synchronized String get(String bundle, String locale, String msg) {
        return getInstance(bundle, locale).get(msg);
    }

    public static synchronized Localization getInstance(String bundle, String locale) {
        String key = bundle + "_" + locale;
        if (cache.containsKey(key)) {
            return cache.get(key);
        } else {
            Localization loc;
            try {
                loc = new Localization(bundle, locale);
            } catch (Exception ex) {
                ex.printStackTrace();
                loc = new Localization(bundle, "en-US");
            }
            cache.put(key, loc);
            return loc;
        }
    }
}
