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

import java.net.URL;

import org.java.plugin.registry.Identity;
import org.java.plugin.util.ExtendedProperties;

/**
 * This interface is intended to establish correspondence between relative path
 * and absolute URL in context of plug-in or plug-in fragment.
 * 
 * @see org.java.plugin.ObjectFactory#createPathResolver()
 * 
 * @version $Id$
 */
public interface PathResolver {
    /**
     * Configures this resolver instance. Usually this method is called from
     * {@link ObjectFactory object factory} implementation.
     * @param config path resolver configuration data
     * @throws Exception if any error has occurred
     */
    void configure(ExtendedProperties config) throws Exception;

    /**
     * Registers "home" URL for given plug-in element.
     * @param idt plug-in element
     * @param url "home" URL for a given plug-in element
     */
    void registerContext(Identity idt, URL url);
    
    /**
     * Unregisters plug-in element from this path resolver.
     * @param id plug-in element identifier
     */
    void unregisterContext(String id);
    
    /**
     * Returns URL of {@link #registerContext(Identity, URL) registered} plug-in
     * element context. If context for plug-in element with given ID not
     * registered, this method should throw an {@link IllegalArgumentException}.
     * In other words, this method shouldn't return <code>null</code>.
     * @param id plug-in element identifier
     * @return registered context "home" location
     */
    URL getRegisteredContext(String id);
    
    /**
     * @param id plug-in element identifier
     * @return <code>true</code> if context for plug-in element with given ID
     *         registered
     */
    boolean isContextRegistered(String id);

    /**
     * Should resolve given path to URL for a given identity.
     * @param identity plug-in element for which to resolve path
     * @param path path to be resolved
     * @return resolved absolute URL
     */
    URL resolvePath(Identity identity, String path);
}