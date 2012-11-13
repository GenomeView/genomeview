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
 * This interface provides access to information about resource or code
 * contributed by plug-in.
 * <p>
 * Library UID is a combination of declaring plug-in ID and library ID that is
 * unique within whole set of registered plug-ins.
 * </p>
 *
 * @version $Id$
 */
public interface Library extends UniqueIdentity, PluginElement<Library> {
    /**
     * @return path to resource
     */
    String getPath();
    
    /**
     * @return <code>true</code> if this is "code" library
     */
    boolean isCodeLibrary();
    
    /**
     * This method should return collection of {@link String} objects that
     * represent resource name prefixes or package name patterns that are
     * available to other plug-ins.
     * <br>
     * For code library, prefix is a package name, for resource library,
     * the same rules applied to relative resource path calculated against
     * library path (you can replace slash characters in path with dots).
     * <br>
     * Example prefixes are:<br>
     * <code>
     * "*", "package.name.*", "package.name.ClassName", "resource/path/*
     * </code>
     * @return collection of exported resource name patterns
     */
    Collection<String> getExports();

    /**
     * @return library version identifier as specified in manifest file or
     *         <code>null</code>
     */
    Version getVersion();
}
