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

/**
 * This interface abstracts inter plug-ins dependencies.
 * <p>
 * Plug-in prerequisite UID is a combination of declaring plug-in ID and
 * prerequisite ID (may be auto-generated) that is unique within whole set of
 * registered plug-ins.
 * </p>
 * 
 * @version $Id$
 */
public interface PluginPrerequisite
        extends UniqueIdentity, PluginElement<PluginPrerequisite> {
    /**
     * @return ID of plug-in, this plug-in depends on
     */
    String getPluginId();
    
    /**
     * @return desired plug-in version identifier or <code>null</code>
     *         if not specified
     */
    Version getPluginVersion();
    
    /**
     * @return <code>true</code> if this prerequisite is propagated
     *         on depending plug-ins
     */
    boolean isExported();
    
    /**
     * @return <code>true</code> if this prerequisite is not required
     */
    boolean isOptional();
    
    /**
     * @return <code>true</code> if this prerequisite allows reverse look up of
     *         classes in imported plug-in
     */
    boolean isReverseLookup();
    
    /**
     * @return <code>true</code> if this prerequisite is fulfilled
     */
    boolean matches();
    
    /**
     * @return the match rule as it specified in manifest
     */
    MatchingRule getMatchingRule();
}