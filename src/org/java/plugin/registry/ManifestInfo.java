/*****************************************************************************
 * Java Plug-in Framework (JPF)
 * Copyright (C) 2007 Dmitry Olshansky
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
 * Manifest info holder interface.
 * 
 * @see PluginRegistry#readManifestInfo(URL)
 * @version $Id: ManifestInfo.java,v 1.2 2007/02/06 16:25:16 ddimon Exp $
 */
public interface ManifestInfo {
    /**
     * @return plug-in or plug-in fragment identifier
     */
    String getId();

    /**
     * @return plug-in or plug-in fragment version identifier
     */
    Version getVersion();

    /**
     * @return plug-in or plug-in fragment vendor
     */
    String getVendor();

    /**
     * @return plug-in identifier this, fragment contributes to or
     *         <code>null</code> if this info is for plug-in manifest
     */
    String getPluginId();

    /**
     * @return plug-in version identifier, this fragment contributes to or
     *         <code>null</code> if this info is for plug-in manifest
     */
    Version getPluginVersion();

    /**
     * @return plug-in version matching rule or <code>null</code> if this
     *         info is for plug-in manifest
     */
    MatchingRule getMatchingRule();
}