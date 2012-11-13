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
 * This interface abstracts a plug-in element - a thing that is declared in
 * plug-in or plug-in fragment descriptor.
 * @param <T> type of plug-in element
 * @version $Id$
 */
public interface PluginElement<T extends PluginElement<T>>
        extends Identity, Documentable<T> {
    /**
     * Returns plug-in descriptor, this element belongs to. This method
     * should never return <code>null</code>.
     * @return plug-in descriptor, this element belongs to
     */
    PluginDescriptor getDeclaringPluginDescriptor();
    
    /**
     * Returns descriptor of plug-in fragment that contributes this element.
     * This method may return <code>null</code>, if element is contributed by
     * plug-in directly.
     * @return descriptor of plug-in fragment that contributes this element
     */
    PluginFragment getDeclaringPluginFragment();
}
