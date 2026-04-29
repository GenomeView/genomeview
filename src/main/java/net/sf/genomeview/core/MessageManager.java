package net.sf.genomeview.core;

import java.lang.module.Configuration;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Uses a languagepack resource bundle (in /lang/MessageXXX.properties) to
 * translate messages
 * 
 * @author David Roldan Martinez
 * @author Thomas Abeel
 * 
 *
 */
public class MessageManager {
	private final Locale loc;
	private final ResourceBundle rb;

	/**
	 * Typically the lang is fetched with {@link Configuration#get()} using
	 * "lang.current"
	 * 
	 * @param lang the language, or "automatic"/null to use
	 *             {@link Locale#getDefault()}
	 */
	public MessageManager(String lang) {

		loc = lang == null || lang.equals("automatic") ? Locale.getDefault()
				: new Locale(lang);
		/* Localize Java dialogs */
		Locale.setDefault(loc);
		/* Getting messages for GV */
		// log.info("Getting messages for lang: " + loc);
		rb = ResourceBundle.getBundle("lang.Messages", loc);
		// log.debug("Language keys: " + rb.keySet());
	}

	/**
	 * 
	 * @param key the key to get the value for
	 * @return the value for given key
	 * @throws MissingResourceException if no such key
	 */
	public String getString(String key) {
		return rb.getString(key);
	}

	public Locale getLocale() {
		return loc;
	}

	/**
	 * 
	 * @param key    the message key , used to fetch the actual message from the
	 *               database.
	 * @param params the params. Length is expected to match the number of {X}
	 *               items in the message.
	 * @return the message stored under key in the Message database, with all
	 *         indices like <code>{0}</code> replaced with the corresponding
	 *         item in the params.
	 */
	public String formatMessage(String key, Object[] params) {
		return MessageFormat.format(rb.getString(key), params);
	}
}
