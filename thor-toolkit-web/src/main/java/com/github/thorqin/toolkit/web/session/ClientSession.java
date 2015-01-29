package com.github.thorqin.toolkit.web.session;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.github.thorqin.toolkit.utility.Encryptor;
import com.github.thorqin.toolkit.web.WebApplication;
import org.apache.commons.codec.binary.Base64;
import com.github.thorqin.toolkit.utility.Serializer;

/**
 * This tool class can let developer easy to save session data to client by use cookie storage.
 * since cookie storage have 4k size limit, so we shouldn't save much data to the session.
 * @author nuo.qin
 */
public class ClientSession extends WebSession {

	private final static String sessionName = "_Thor_Session";
	private final static String keyCode = "kyj1JEkLQ/5To0AF81vlmA==";
	private final static Logger logger = Logger.getLogger(ClientSession.class.getName());
	private final static ThreadLocal<Encryptor> encryptorLocalStore = new ThreadLocal<>();

	private boolean isSaved = false;

	private static class Data {
		public String sid;
		public long lastAccessedTime;
		public long creationTime;
		public int maxInterval = 0;
		public Map<String, Object> values = null;
	}
	private Data value;
	
	private static Encryptor getEncryptor() throws Exception {
		Encryptor obj = encryptorLocalStore.get();
		if (obj == null) {
			obj = Encryptor.create("aes", Base64.decodeBase64(importKey()));
			encryptorLocalStore.set(obj);
		}
		return obj;
	}
	
	private static String importKey() {
		String path = ClientSession.class.getClassLoader().getResource("/").getFile();
		path += "server.key";
		try {
			return new String(Files.readAllBytes(new File(path).toPath()));
		} catch (IOException ex) {
			logger.log(Level.WARNING, "server.key does not exists in classes folder, use default key instead.");
			return keyCode;
		}
	}

	private void newSession() {
		this.isSaved = false;
		value = new Data();
		value.sid = java.util.UUID.randomUUID().toString().replace("-", "");
		value.values = new HashMap<>();
		long now = new Date().getTime();
		value.creationTime = now;
		value.lastAccessedTime = now;
		if (application != null) {
			this.setMaxInactiveInterval(application.getSetting().maxInterval);
		} else {
			this.setMaxInactiveInterval(request.getSession().getMaxInactiveInterval());
		}
	}

	private void fromCookie(Cookie cookie) throws Exception {
		Encryptor enc = getEncryptor();
		this.isSaved = false;
		value = Serializer.fromKryo(enc.decrypt(
				Base64.decodeBase64(cookie.getValue())));
		value.lastAccessedTime = new Date().getTime();
	}

	public ClientSession(WebApplication application, HttpServletRequest request, HttpServletResponse response) {
		super(application, request, response);
		Cookie[] cookies = request.getCookies();
		if (cookies == null) {
			newSession();
		} else {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals(sessionName)) {
					try {
						fromCookie(cookie);
						return;
					} catch (Exception ex) {
						logger.log(Level.SEVERE, "Invalid session: " + cookie.getValue(), ex);
					}
				}
			}
			newSession();
		}
	}

	@Override
	public String getId() {
		return value.sid;
	}

	@Override
	public void set(String key, Object value) {
		this.value.values.put(key, value);
		touch();
	}

	@Override
	public Object get(String key) {
		Object findValue = this.value.values.get(key);
		return findValue;
	}

	@Override
	public void remove(String key) {
		this.value.values.remove(key);
		touch();
	}

	@Override
	public void clear() {
		this.value.values.clear();
		touch();
	}

	@Override
	public void touch() {
		value.lastAccessedTime = new Date().getTime();
		this.isSaved = false;
	}

	@Override
	public boolean isSaved() {
		return this.isSaved;
	}

	@Override
	public String toString() {
		try {
			Encryptor enc = getEncryptor();
			return Base64.encodeBase64String(enc.encrypt(Serializer.toKryo(value)));
		} catch (Exception ex) {
			logger.log(Level.SEVERE, "Export session failed", ex);
			return null;
		}
	}

	public void save(String path, String domain, Integer maxAge, boolean httpOnly, boolean secureOnly) {
		try {
			touch();
			String sessionContent = toString();
			if (sessionContent == null)
				return;
			Cookie cookie = new Cookie(sessionName, sessionContent);
			if (maxAge != null)
				cookie.setMaxAge(maxAge);
			if (domain != null && !domain.isEmpty())
				cookie.setDomain(domain);
			if (path != null && !path.isEmpty())
				cookie.setPath(path);
			cookie.setHttpOnly(httpOnly);
			cookie.setSecure(secureOnly);
			cookie.setVersion(1);
			if (response != null)
				response.addCookie(cookie);
			this.isSaved = true;
		} catch (Exception ex) {
			logger.log(Level.SEVERE, "Save client session failed!", ex);
		}
	}
	
	public void save(String path, String domain) {
		save(path, domain, null, true, false);
	}
	
	public void save(String path) {
		save(path, null, null, true, false);
	}
	
	private static String getRootPath(HttpServletRequest req) {
		String path = req.getContextPath();
		if (path == null || path.isEmpty())
			return "/";
		else
			return path;
	}

	@Override
	public void save() {
		save(getRootPath(request), null, null, true, false);
	}
	
	@Override
	public long getCreationTime() {
		return value.creationTime;
	}

	@Override
	public long getLastAccessedTime() {
		return value.lastAccessedTime;
	}

	@Override
	public void setMaxInactiveInterval(int interval) {
		value.maxInterval = interval;
	}

	@Override
	public int getMaxInactiveInterval() {
		return value.maxInterval;
	}

	public static class IteratorEnumeration<E> implements Enumeration<E> {
		private final Iterator<E> iterator;
		public IteratorEnumeration(Iterator<E> iterator){
			this.iterator = iterator;
		}
		@Override
		public E nextElement() {
			return iterator.next();
		}
		@Override
		public boolean hasMoreElements() {
			return iterator.hasNext();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Enumeration<String> getKeys() {
		return new IteratorEnumeration(value.values.keySet().iterator());
	}
}
