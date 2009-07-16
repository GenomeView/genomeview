/*****************************************************************************
 * Java Plug-in Framework (JPF)
 * Copyright (C) 2004-2005 Dmitry Olshansky
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
package org.java.plugin;

import java.util.Locale;

import org.java.plugin.util.ResourceManager;

/**
 * Base JPF exception class that supports localized error messages.
 * @version $Id$
 */
public abstract class JpfException extends Exception {
    private final String packageName;
    private final String messageKey;
    private final Object data;
    
    protected JpfException(final String aPackageName,
            final String aMessageKey, final Object aData,
            final Throwable cause) {
        super(ResourceManager.getMessage(aPackageName, aMessageKey, aData),
                cause);
        packageName = aPackageName;
        messageKey = aMessageKey;
        data = aData;
    }

    /**
     * @param locale locale
     * @return error message for the given locale
     */
    public String getMessage(final Locale locale) {
        return ResourceManager.getMessage(packageName, messageKey, locale,
                data);
    }
}
