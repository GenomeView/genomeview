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
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;

import org.java.plugin.registry.PluginDescriptor;

/**
 * Extension to Java class loader API. One instance of this class should be
 * created by {@link org.java.plugin.PluginManager plug-in manager} for every
 * available plug-in.
 * 
 * @version $Id$
 */
public abstract class PluginClassLoader extends URLClassLoader {
    private final PluginManager manager;
    private final PluginDescriptor descriptor;

    /**
     * @param aManager plug-in manager
     * @param descr plug-in descriptor
     * @param urls resources "managed" by this class loader
     * @param parent parent class loader
     * @param factory URL stream handler factory
     * @see URLClassLoader#URLClassLoader(java.net.URL[], java.lang.ClassLoader,
     *      java.net.URLStreamHandlerFactory)
     */
    protected PluginClassLoader(final PluginManager aManager,
            final PluginDescriptor descr, final URL[] urls,
            final ClassLoader parent, final URLStreamHandlerFactory factory) {
        super(urls, parent, factory);
        manager = aManager;
        descriptor = descr;
    }

    /**
     * @param aManager plug-in manager
     * @param descr plug-in descriptor
     * @param urls resources "managed" by this class loader
     * @param parent parent class loader
     * @see URLClassLoader#URLClassLoader(java.net.URL[], java.lang.ClassLoader)
     */
    protected PluginClassLoader(final PluginManager aManager,
            final PluginDescriptor descr, final URL[] urls,
            final ClassLoader parent) {
        super(urls, parent);
        manager = aManager;
        descriptor = descr;
    }

    /**
     * @param aManager plug-in manager
     * @param descr plug-in descriptor
     * @param urls resources "managed" by this class loader
     * @see URLClassLoader#URLClassLoader(java.net.URL[])
     */
    protected PluginClassLoader(final PluginManager aManager,
            final PluginDescriptor descr, final URL[] urls) {
        super(urls);
        manager = aManager;
        descriptor = descr;
    }
    
    /**
     * @return returns the plug-in manager
     */
    public PluginManager getPluginManager() {
        return manager;
    }
    
    /**
     * @return returns the plug-in descriptor
     */
    public PluginDescriptor getPluginDescriptor() {
        return descriptor;
    }
    
    /**
     * Should release all resources acquired by this class loader instance.
     */
    protected abstract void dispose();
    
    /**
     * Registry data change notification.
     */
    protected abstract void pluginsSetChanged();

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "{PluginClassLoader: uid=" //$NON-NLS-1$
            + System.identityHashCode(this) + "; " //$NON-NLS-1$
            + descriptor + "}"; //$NON-NLS-1$
    }
}
