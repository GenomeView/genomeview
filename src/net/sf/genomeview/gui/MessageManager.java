package net.sf.genomeview.gui;

import java.util.Locale;
import java.util.ResourceBundle;

public class MessageManager {

	private static ResourceBundle rb;    
    static{
        rb = ResourceBundle.getBundle("lang/Messages", Locale.ENGLISH);
    }
    
    public static String getString(String key){
    	return rb.getString(key);
    }
}
