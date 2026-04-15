package net.sf.genomeview.gui;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import net.sf.genomeview.core.Configuration;

/**
 * 
 * @author David Roldan Martinez
 * @author Thomas Abeel
 * 
 *
 */
public class MessageManager {

	private static ResourceBundle rb;

//	private static Logger log = LoggerFactory
//			.getLogger(MessageManager.class.getCanonicalName());

	private static Locale loc = Configuration.instance().get("lang:current")
			.equals("automatic") ? Locale.getDefault()
					: new Locale(Configuration.instance().get("lang:current"));

	static {
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
	public static String getString(String key) {
		return rb.getString(key);
	}

	public static Locale getLocale() {
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
	public static String formatMessage(String key, Object[] params) {
		return MessageFormat.format(rb.getString(key), params);
	}
}
