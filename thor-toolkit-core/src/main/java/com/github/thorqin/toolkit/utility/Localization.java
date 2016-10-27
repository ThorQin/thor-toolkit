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
import java.text.MessageFormat;
import java.util.*;

/**
 *
 * @author nuo.qin
 */
public class Localization {

    public class UTF8Control extends ResourceBundle.Control {
		@Override
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
    private final static Localization defaultLoc = Localization.getInstance(null, null);

    public static String[] splitLocalePart(String localeString) {
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
        if (localeString.matches("^[a-zA-Z]+(_[a-zA-Z]+){1,2}$")) {
            return localeString.split("_");
        } else if (localeString.matches("^[a-zA-Z]+(-[a-zA-Z]+){1,2}$")) {
            return localeString.split("-");
        } else {
            localeString = "en-US";
            return localeString.split("-");
        }
    }

    public static String standardize(String localeString) {
        String[] arr = splitLocalePart(localeString);
        return arr[0].toLowerCase() + "-" + arr[1].toUpperCase();
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
     * Construct by language &amp; country
	 * @param bundleName Bundle name
	 * @param language "zh"
	 * @param country "CN"
	 */
	public Localization(String bundleName, String language, String country) {
        if (language == null)
            language = "en";
		if (country == null)
            country = "US";
        else if (country.matches("(?i)hans|chs"))
            country = "CN";
        else if (country.matches("(?i)hant|cht"))
            country = "TW";
		locale = new Locale(language.toLowerCase(), country.toUpperCase());
        if (bundleName == null)
            bundle = null;
        else {
            try {
                bundle = ResourceBundle.getBundle(bundleName, locale, this.getClass().getClassLoader(), new UTF8Control());
            } catch (Exception e) {
                bundle = null;
                System.out.println("Initialize localization failed: " + e.getMessage());
            }
        }
	}

    public Localization() {
        this(null, null, null);
    }

    public Locale getLocale() {
        return locale;
    }

    /**
     * Translate message in specified locale.
     * @param key Message to be translated
     * @return Translated message
     */
	public String get(String key) {
        if (bundle == null)
            return key;
		try {
			if (bundle.keySet().contains(key)) {
                String msg = bundle.getString(key);
                return msg;
            } else
				return key;
		} catch (Exception e) {
			return key;
		}
	}

    /**
     * Translate message in specified locale.
     * @param key Message to be translated
     * @param defaultMessage If key not found then return the default message.
     * @return Translated message
     */
    public String get(String key, String defaultMessage) {
        if (bundle == null)
            return defaultMessage;
        try {
            if (bundle.keySet().contains(key))
                return bundle.getString(key);
            else
                return defaultMessage;
        } catch (Exception e) {
            return defaultMessage;
        }
    }

    /**
     * Translate message in specified locale.
     * @param key Message to be translated
     * @param defaultMessage If key not found then return the default message.
     * @param params Message will be formatted with those parameters
     * @return Translated message
     */
    public String getMessage(String key, String defaultMessage, Object... params) {
        String msg;
        try {
            if (bundle == null)
                msg = defaultMessage;
            else if (bundle.keySet().contains(key))
                msg = bundle.getString(key);
            else
                msg = defaultMessage;
        } catch (Exception e) {
            msg = defaultMessage;
        }
        return MessageFormat.format(msg, params);
    }

    public static synchronized String get(String bundle, String locale, String msg) {
        return getInstance(bundle, locale).get(msg);
    }

    public static synchronized String get(String bundle, String locale, String msg, String defaultMessage) {
        return getInstance(bundle, locale).get(msg, defaultMessage);
    }

    public static Localization getInstance() {
        return defaultLoc;
    }

    public static synchronized Localization getInstance(String bundle, String locale) {
        if (locale == null)
            locale = "en-US";
        if (bundle == null)
            bundle = "message";
        String key = bundle + "_" + locale;
        if (cache.containsKey(key)) {
            return cache.get(key);
        } else {
            Localization loc;
            try {
                loc = new Localization(bundle, locale);
            } catch (Exception ex) {
                throw new RuntimeException("Get localization instance failed", ex);
            }
            cache.put(key, loc);
            return loc;
        }
    }
}
