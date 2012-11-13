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

import java.security.AccessController;
import java.security.PrivilegedAction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.java.plugin.Plugin;
import org.java.plugin.PluginLifecycleException;
import org.java.plugin.PluginManager;
import org.java.plugin.registry.PluginDescriptor;
import org.java.plugin.util.ExtendedProperties;

/**
 * Standard implementation of plug-in life cycle handler.
 * <p>
 * <b>Configuration parameters</b>
 * </p>
 * <p>
 * This life cycle handler implementation supports following configuration
 * parameters:
 * <dl>
 *   <dt>probeParentLoaderLast</dt>
 *   <dd>If <code>true</code>, plug-in classloader will try loading classes from
 *     system (boot) classpath <b>after</b> trying to load them from plug-in
 *     classpath. Otherwise system classpath will be used <b>first</b>. Default
 *     value is <code>false</code> that corresponds to standard delegation model
 *     for classloaders hierarchy that corresponds to JLS.</dd>
 *   <dt>stickySynchronizing</dt>
 *   <dd>Allows advanced configuring of classloaders synchronization in
 *     multy-threaded environment. If <code>true</code> then class loading will
 *     be synchronized with initial plug-in classloader instance. Otherwise
 *     <code>this</code> instance will be used as synchronizing monitor. Default
 *     value is <code>false</code>.</dd>
 *   <dt>localClassLoadingOptimization</dt>
 *   <dd>If <code>true</code> then plug-in classloader will collect local
 *     packages statistics to predict class location. This allow to optimize
 *     class look-up procedure for classes that belong to the requested plug-in.
 *     Default value is <code>true</code>.</dd>
 *   <dt>foreignClassLoadingOptimization</dt>
 *   <dd>If <code>true</code> then plug-in classloader will collect statistics
 *     for "foreign" classes - those which belong to depending plug-ins. This
 *     allow to optimize class look-up procedure when enumerating depending
 *     plug-ins. Default value is <code>true</code>.</dd>
 * </dl>
 * </p>
 * @version $Id: StandardPluginLifecycleHandler.java,v 1.5 2007/04/07 12:39:50 ddimon Exp $
 */
public class StandardPluginLifecycleHandler extends PluginLifecycleHandler {
    private final Log log = LogFactory.getLog(getClass());
    private boolean probeParentLoaderLast;
    private boolean stickySynchronizing;
    private boolean localClassLoadingOptimization;
    private boolean foreignClassLoadingOptimization;

    /**
     * Creates standard implementation of plug-in class loader.
     * @see org.java.plugin.standard.PluginLifecycleHandler#createPluginClassLoader(
     *      org.java.plugin.registry.PluginDescriptor)
     */
    @Override
    protected org.java.plugin.PluginClassLoader createPluginClassLoader(
            final PluginDescriptor descr) {
        /*StandardPluginClassLoader result = new StandardPluginClassLoader(
                getPluginManager(), descr, getClass().getClassLoader());*/
        StandardPluginClassLoader result = AccessController.doPrivileged(
                    new PrivilegedAction<StandardPluginClassLoader>() {
            public StandardPluginClassLoader run() {
                return new StandardPluginClassLoader(getPluginManager(), descr,
                        StandardPluginLifecycleHandler.this.getClass()
                        .getClassLoader());
            }
        });
        result.setProbeParentLoaderLast(probeParentLoaderLast);
        result.setStickySynchronizing(stickySynchronizing);
        result.setLocalClassLoadingOptimization(localClassLoadingOptimization);
        result.setForeignClassLoadingOptimization(
                foreignClassLoadingOptimization);
        return result;
    }

    /**
     * Creates instance of plug-in class calling it's default (no-arguments)
     * constructor. Class look-up is done with
     * {@link PluginManager#getPluginClassLoader(PluginDescriptor) plug-in's class loader}.
     * @see org.java.plugin.standard.PluginLifecycleHandler#createPluginInstance(
     *      org.java.plugin.registry.PluginDescriptor)
     */
    @Override
    protected Plugin createPluginInstance(final PluginDescriptor descr)
            throws PluginLifecycleException {
        String className = descr.getPluginClassName();
        Class<?> pluginClass;
        try {
            pluginClass =
                getPluginManager().getPluginClassLoader(descr).loadClass(
                        className);
        } catch (ClassNotFoundException cnfe) {
            throw new PluginLifecycleException(
                    StandardObjectFactory.PACKAGE_NAME,
                    "pluginClassNotFound", className, cnfe); //$NON-NLS-1$
        }
        try {
            return (Plugin) pluginClass.newInstance();
        } catch (InstantiationException ie) {
            throw new PluginLifecycleException(
                    StandardObjectFactory.PACKAGE_NAME,
                    "pluginClassInstantiationFailed", descr.getId(), ie); //$NON-NLS-1$
        } catch (IllegalAccessException iae) {
            throw new PluginLifecycleException(
                    StandardObjectFactory.PACKAGE_NAME,
                    "pluginClassInstantiationFailed", descr.getId(), iae); //$NON-NLS-1$
        }
    }
    
    /**
     * This method does nothing in this implementation.
     * @see org.java.plugin.standard.PluginLifecycleHandler#beforePluginStart(
     *      org.java.plugin.Plugin)
     */
    @Override
    protected void beforePluginStart(final Plugin plugin) {
        // no-op
    }

    /**
     * This method does nothing in this implementation.
     * @see org.java.plugin.standard.PluginLifecycleHandler#afterPluginStop(
     *      org.java.plugin.Plugin)
     */
    @Override
    protected void afterPluginStop(final Plugin plugin) {
        // no-op
    }

    /**
     * This method does nothing in this implementation.
     * @see org.java.plugin.standard.PluginLifecycleHandler#dispose()
     */
    @Override
    protected void dispose() {
        // no-op
    }

    /**
     * @see org.java.plugin.standard.PluginLifecycleHandler#configure(
     *      ExtendedProperties)
     */
    @Override
    public void configure(ExtendedProperties config) {
        probeParentLoaderLast = "true".equalsIgnoreCase( //$NON-NLS-1$
                config.getProperty("probeParentLoaderLast", "false")); //$NON-NLS-1$ //$NON-NLS-2$
        log.debug("probeParentLoaderLast parameter value is " //$NON-NLS-1$
                + probeParentLoaderLast);
        stickySynchronizing = "true".equalsIgnoreCase( //$NON-NLS-1$
                config.getProperty("stickySynchronizing", "false")); //$NON-NLS-1$ //$NON-NLS-2$
        log.debug("stickySynchronizing parameter value is " //$NON-NLS-1$
                + stickySynchronizing);
        localClassLoadingOptimization = !"false".equalsIgnoreCase( //$NON-NLS-1$
                config.getProperty("localClassLoadingOptimization", //$NON-NLS-1$
                        "true")); //$NON-NLS-1$
        log.debug("localLoadingClassOptimization parameter value is " //$NON-NLS-1$
                + localClassLoadingOptimization);
        foreignClassLoadingOptimization = !"false".equalsIgnoreCase( //$NON-NLS-1$
                config.getProperty("foreignClassLoadingOptimization", //$NON-NLS-1$
                        "true")); //$NON-NLS-1$
        log.debug("foreignClassLoadingOptimization parameter value is " //$NON-NLS-1$
                + foreignClassLoadingOptimization);
    }
}
