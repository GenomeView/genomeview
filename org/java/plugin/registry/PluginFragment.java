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


/**
 * Interface to get access to main information about plug-in fragment. This
 * does not include information about libraries, extensions and extension
 * points, defined in this fragment, such information is available as part of
 * plug-in, to which this fragment contributes.
 * <p>
 * Plug-in fragment UID is a combination of plug-in fragment ID and version
 * identifier that is unique within whole set of registered plug-ins and
 * fragments.
 * </p>
 *
 * @version $Id$
 */
public interface PluginFragment
        extends UniqueIdentity, Documentable<PluginFragment> {
    /**
     * @return vendor as specified in manifest file or empty string
     */
    String getVendor();
    
    /**
     * @return plug-in fragment version identifier as specified in manifest file
     */
    Version getVersion();

    /**
     * @return ID of plug-in to which this fragment may contribute
     */
    String getPluginId();

    /**
     * @return version identifier of plug-in to which this fragment may
     *         contribute or <code>null</code> if no version specified in
     *         manifest
     */
    Version getPluginVersion();

    /**
     * @return plug-ins registry
     */
    PluginRegistry getRegistry();
    
    /**
     * Checks is this fragment may contribute to given plug-in.
     * @param descr plug-in descriptor
     * @return <code>true</code> if this fragment may contribute to given
     *         plug-in
     */
    boolean matches(PluginDescriptor descr);
    
    /**
     * @return the match rule as it specified in manifest
     */
    MatchingRule getMatchingRule();
    
    /**
     * @return location from which this fragment was registered
     */
    URL getLocation();
}
