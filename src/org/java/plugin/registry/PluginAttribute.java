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
package org.java.plugin.registry;

import java.util.Collection;

/**
 * This interface abstracts plug-in attribute, a &lt;ID,VALUE&gt; pair. Plug-in
 * attributes are not involved into JPF runtime internal logic and intended
 * to be used by plug-in developers. 
 * @version $Id$
 */
public interface PluginAttribute extends PluginElement<PluginAttribute> {
    /**
     * @return attribute value as it is specified in manifest
     */
    String getValue();

    /**
     * @return collection of all sub-attributes of this attribute
     */
    Collection<PluginAttribute> getSubAttributes();

    /**
     * @param id ID of sub-attribute to look for
     * @return sub-attribute with given ID
     */
    PluginAttribute getSubAttribute(String id);

    /**
     * @param id ID of sub-attribute to look for
     * @return collection of all sub-attributes with given ID
     */
    Collection<PluginAttribute> getSubAttributes(String id);
    
    /**
     * @return attribute, of which this one is child or <code>null</code> if
     *         this is top level attribute
     */
    PluginAttribute getSuperAttribute();
}
