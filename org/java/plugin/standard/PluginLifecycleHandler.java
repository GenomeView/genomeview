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
package org.java.plugin.standard;

import org.java.plugin.ObjectFactory;
import org.java.plugin.Plugin;
import org.java.plugin.PluginClassLoader;
import org.java.plugin.PluginLifecycleException;
import org.java.plugin.PluginManager;
import org.java.plugin.registry.PluginDescriptor;
import org.java.plugin.util.ExtendedProperties;


/**
 * Manager class that handles plug-in life cycle related logic. This class is
 * part of standard implementation of plug-in manager, other implementations may
 * not use it at all. The main purpose of this class is to simplify
 * customization of plug-in manager behavior.
 * @version $Id$
 */
public abstract class PluginLifecycleHandler {
    private PluginManager manager;

    /**
     * Initializes this handler instance. This method called once during this
     * handler instance life cycle.
     * @param aManager a plug-in manager, this handler is "connected" to
     */
    protected void init(PluginManager aManager) {
        manager = aManager;
    }
    
    /**
     * @return instance of plug-in manager, this handler is "connected" to
     */
    protected PluginManager getPluginManager() {
        return manager;
    }
    
    /**
     * Configures this handler instance. Note that this method should be called
     * once before {@link #init(PluginManager)}, usually this is done in
     * {@link ObjectFactory object factory} implementation.
     * @param config handler configuration data
     */
    protected abstract void configure(ExtendedProperties config);
    
    /**
     * This method should create new instance of class loader for given plug-in. 
     * @param descr plug-in descriptor
     * @return class loader instance for given plug-in
     */
    protected abstract PluginClassLoader createPluginClassLoader(
            PluginDescriptor descr);

    /**
     * This method should create new instance of plug-in class. No initializing
     * logic should be executed in new class instance during this method call.
     * <br>
     * Note that this method will NOT be called for those plug-ins that have NO
     * class declared in plug-in descriptor i.e., method
     * {@link PluginDescriptor#getPluginClassName()} returns blank string or
     * <code>null</code>.
     * @param descr plug-in descriptor
     * @return new not initialized instance of plug-in class
     * @throws PluginLifecycleException if plug-in class can't be instantiated
     *         for some reason
     */
    protected abstract Plugin createPluginInstance(PluginDescriptor descr)
        throws PluginLifecycleException;
    
    /**
     * This method will be called by {@link PluginManager} just before starting
     * plug-in. Put here any "initializing" logic that should be executed before
     * plug-in start.
     * @param plugin plug-in being starting
     * @throws PluginLifecycleException if plug-in can't be "initialized"
     */
    protected abstract void beforePluginStart(final Plugin plugin)
        throws PluginLifecycleException;
    
    /**
     * This method will be called by {@link PluginManager} just after stopping
     * plug-in. Put here any "un-initializing" logic that should be executed
     * after plug-in stop.
     * @param plugin plug-in being stopping
     * @throws PluginLifecycleException if plug-in can't be "un-initialized"
     */
    protected abstract void afterPluginStop(final Plugin plugin)
        throws PluginLifecycleException;
    
    /**
     * Should dispose all resources allocated by this handler instance. No
     * methods will be called for this class instance after executing this
     * method.
     */
    protected abstract void dispose();
}
