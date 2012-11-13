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

import java.net.URL;
import java.util.Collection;

/**
 * Main interface to get access to all meta-information for particular
 * plug-in, described in plug-in manifest file.
 * <p>
 * Plug-in UID is a combination of plug-in ID and version identifier that is
 * unique within whole set of registered plug-ins.
 * </p>
 * @see <a href="{@docRoot}/../plugin_1_0.dtd">plug-in DTD for standard
 *      registry implementation</a>
 * @see org.java.plugin.registry.PluginRegistry
 * @version $Id$
 */
public interface PluginDescriptor
        extends UniqueIdentity, Documentable<PluginDescriptor> {
    /**
     * @return vendor as specified in manifest file or empty string
     */
    String getVendor();

    /**
     * @return plug-in version identifier as specified in manifest file
     */
    Version getVersion();

    /**
     * Returns collection of all top level attributes defined in manifest.
     * @return collection of {@link PluginAttribute} objects
     */
    Collection<PluginAttribute> getAttributes();
    
    /**
     * @param id ID of attribute to look for
     * @return top level attribute with given ID
     */
    PluginAttribute getAttribute(String id);

    /**
     * @param id ID of attribute to look for
     * @return collection of all top level attributes with given ID
     */
    Collection<PluginAttribute> getAttributes(String id);

    /**
     * Returns collection of all prerequisites defined in manifest.
     * @return collection of {@link PluginPrerequisite} objects
     */
    Collection<PluginPrerequisite> getPrerequisites();
    
    /**
     * @param id prerequisite ID
     * @return plug-in prerequisite object instance or <code>null</code>
     */
    PluginPrerequisite getPrerequisite(String id);

    /**
     * Returns collection of all extension points defined in manifest.
     * @return collection of {@link ExtensionPoint} objects
     */
    Collection<ExtensionPoint> getExtensionPoints();
    
    /**
     * @param id extension point ID
     * @return extension point object or <code>null</code>
     */
    ExtensionPoint getExtensionPoint(String id);

    /**
     * Returns collection of all extensions defined in manifest.
     * @return collection of {@link Extension} objects
     */
    Collection<Extension> getExtensions();
    
    /**
     * @param id extension ID
     * @return extension object or <code>null</code>
     */
    Extension getExtension(String id);

    /**
     * Returns collection of all libraries defined in manifest.
     * @return collection of {@link Library} objects
     */
    Collection<Library> getLibraries();
    
    /**
     * @param id library ID
     * @return library object or <code>null</code>
     */
    Library getLibrary(String id);

    /**
     * @return plug-ins registry
     */
    PluginRegistry getRegistry();
    
    /**
     * @return plug-in class name as specified in manifest file or
     *         <code>null</code>
     */
    String getPluginClassName();
    
    /**
     * Returns collection of plug-in fragments which contributes to this
     * plug-in. One plug-in fragment may contribute to several versions of the
     * same plug-in, according to it's manifest.
     * @return collection of {@link PluginFragment} objects
     */
    Collection<PluginFragment> getFragments();
    
    /**
     * @return location from which this plug-in was registered
     */
    URL getLocation();
}