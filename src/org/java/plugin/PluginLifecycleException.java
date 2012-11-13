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
package org.java.plugin;

/**
 * Exception class that indicates errors during plug-in life cycle.
 * @version $Id$
 */
public class PluginLifecycleException extends JpfException {
    private static final long serialVersionUID = -4019294858687542301L;

    /**
     * @param packageName package to load resources from
     * @param messageKey resource key
     */
    public PluginLifecycleException(final String packageName,
            final String messageKey) {
        super(packageName, messageKey, null, null);
    }

    /**
     * @param packageName package to load resources from
     * @param messageKey resource key
     * @param data parameters substitution data
     */
    public PluginLifecycleException(final String packageName,
            final String messageKey, final Object data) {
        super(packageName, messageKey, data, null);
    }

    /**
     * @param packageName package to load resources from
     * @param messageKey resource key
     * @param data parameters substitution data
     * @param cause nested exception
     */
    public PluginLifecycleException(final String packageName,
            final String messageKey, final Object data, final Throwable cause) {
        super(packageName, messageKey, data, cause);
    }
}
