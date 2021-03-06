/**
 * %HEADER%
 */
package net.sf.genomeview.data;

/**
 * Types of notifications that can be send around between observables and
 * observers. This is useful to let some observers only react to certain
 * notification types.
 * 
 * @author Thomas
 * 
 */
public enum NotificationTypes {
	/**
	 * Notification to indicate that there is a large change in the model which
	 * may affect all observers. Generally observers should react on this type
	 * of notification.
	 */
	GENERAL,
	
	/**
	 * Indicates the event where a user selects another translation table.
	 */
	TRANSLATIONTABLECHANGE,
	
	/**
	 * Indicates that updateTracks was called.
	 */
	UPDATETRACKS,
	
	/**
	 * Configuration changed
	 */
	CONFIGURATION_CHANGE,
	
	/**
	 * The entry selection changed
	 */
	ENTRYCHANGED,
	
	/**
	 * Indicates that a feature was edited, added or removed
	 * 
	 */
	JANNOTCHANGE, 
	
	/**
	 * Indicates that there has been a change to a dialog window, either opened or closed.
	 */
	DIALOGCHANGE,
	
	/**
	 * Indicates an exception has occurred somewhere.
	 */
	EXCEPTION
	;

}
