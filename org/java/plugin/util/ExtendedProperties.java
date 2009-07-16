/*****************************************************************************
 * Java Plug-in Framework (JPF)
 * Copyright (C) 2004-2007 Dmitry Olshansky
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *****************************************************************************/
package org.java.plugin.util;

import java.util.Properties;

/**
 * This implementation supports parameters substitution in property value.
 * @see #getProperty(String)
 * @version $Id$
 */
public class ExtendedProperties extends Properties {
    private static final long serialVersionUID = 8904709563073950956L;

    /**
     * @see java.util.Properties#Properties()
     */
    public ExtendedProperties() {
        super();
    }

    /**
     * @see java.util.Properties#Properties(java.util.Properties)
     */
    public ExtendedProperties(Properties defs) {
        super(defs);
    }

    /**
     * Any parameter like <code>${propertyName}</code> in property value will
     * be replaced with the value of property with name
     * <code>propertyName</code>.
     * <p>For example, for the following set of
     * properties:
     * <pre>
     * param1=abcd
     * param2=efgh
     * param3=Alphabet starts with: ${param1}${param2}
     * </pre>
     * The call <code>props.getProperty("param3")</code> returns:
     * <pre>Alphabet starts with: abcdefgh</pre>
     * Note also that call <code>props.get("param3")</code> returns:
     * <pre>Alphabet starts with: ${param1}${param2}</pre>
     * So the {@link java.util.Map#get(java.lang.Object)} works as usual and
     * returns raw (not expanded with substituted parameters) property value.
     * </p>
     * @see java.util.Properties#getProperty(java.lang.String)
     */
    @Override
    public String getProperty(String key) {
        String result = super.getProperty(key);
        return (result == null) ? null : expandValue(result);
    }
    
    /**
     * @see java.util.Properties#getProperty(java.lang.String, java.lang.String)
     */
    @Override
    public String getProperty(String key, String defaultValue) {
        String result = getProperty(key);
        return (result == null) ? expandValue(defaultValue) : result;
    }
    
    /**
     * @param prefix string, each property key should start with (this prefix
     *               will NOT be included into new key)
     * @return sub-properties
     */
    public ExtendedProperties getSubset(final String prefix) {
        return getSubset(prefix, ""); //$NON-NLS-1$
    }
    
    /**
     * @param prefix string, each property key should start with
     * @param newPrefix new prefix to be added to each key instead of existing
     *                  prefix
     * @return sub-properties
     */
    public ExtendedProperties getSubset(final String prefix,
            final String newPrefix) {
        ExtendedProperties result = new ExtendedProperties();
        for (Object object : keySet()) {
            String key = object.toString();
            if (!key.startsWith(prefix) || key.equals(prefix)) {
                continue;
            }
            result.put(key.substring(prefix.length()) + newPrefix,
                    getProperty(key));
        }
        return result;
    }
    
    private String expandValue(final String value) {
        if ((value == null) || (value.length() < 4)) {
            return value;
        }
        StringBuilder result = new StringBuilder(value.length());
        result.append(value);
        int p1 = result.indexOf("${"); //$NON-NLS-1$
        int p2 = result.indexOf("}", p1 + 2); //$NON-NLS-1$
        while ((p1 >= 0) && (p2 > p1)) {
            String paramName = result.substring(p1 + 2, p2);
            String paramValue = getProperty(paramName);
            if (paramValue != null) {
                result.replace(p1, p2 + 1, paramValue);
                p1 += paramValue.length();
            } else {
                p1 = p2 + 1;
            }
            p1 = result.indexOf("${", p1); //$NON-NLS-1$
            p2 = result.indexOf("}", p1 + 2); //$NON-NLS-1$
        }
        return result.toString();
    }
}
