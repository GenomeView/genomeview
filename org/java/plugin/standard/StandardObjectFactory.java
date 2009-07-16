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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.java.plugin.ObjectFactory;
import org.java.plugin.PathResolver;
import org.java.plugin.PluginManager;
import org.java.plugin.registry.PluginRegistry;
import org.java.plugin.util.ExtendedProperties;

/**
 * Standard object factory implementation.
 * @version $Id$
 */
public class StandardObjectFactory extends ObjectFactory {
    static final String PACKAGE_NAME = "org.java.plugin.standard"; //$NON-NLS-1$
    
    protected Log log = LogFactory.getLog(getClass());
    protected ExtendedProperties config;

    /**
     * @see org.java.plugin.ObjectFactory#configure(ExtendedProperties)
     */
    @Override
    protected void configure(final ExtendedProperties configuration) {
        config = (configuration != null) ? configuration
                : new ExtendedProperties();
    }
    
    protected String getImplClassName(final Class<?> cls) {
        String result = config.getProperty(cls.getName(), null);
        if (log.isDebugEnabled()) {
            log.debug("implementation class for " + cls.getName() //$NON-NLS-1$
                    + " is " + result); //$NON-NLS-1$
        }
        return result;
    }
    
    protected Object createClassInstance(final String className)
            throws InstantiationException, IllegalAccessException,
            ClassNotFoundException {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl != null) {
            try {
                return cl.loadClass(className).newInstance();
            } catch (ClassNotFoundException cnfe) {
                // ignore
            }
        }
        cl = getClass().getClassLoader();
        if (cl != null) {
            try {
                return cl.loadClass(className).newInstance();
            } catch (ClassNotFoundException cnfe) {
                // ignore
            }
        }
        return ClassLoader.getSystemClassLoader().loadClass(
                className).newInstance();
    }

    /**
     * @see org.java.plugin.ObjectFactory#createRegistry()
     */
    @Override
    public PluginRegistry createRegistry() {
        String className = getImplClassName(PluginRegistry.class);
        PluginRegistry result;
        if (className == null) {
            className = "org.java.plugin.registry.xml.PluginRegistryImpl"; //$NON-NLS-1$
        }
        try {
            result = (PluginRegistry) createClassInstance(className);
        } catch (Exception e) {
            log.fatal("failed creating registry instance " //$NON-NLS-1$
                    + className, e);
            throw new Error("failed creating registry instance " //$NON-NLS-1$
                    + className, e);
        }
        result.configure(config.getSubset(className + ".")); //$NON-NLS-1$
        log.debug("registry instance created - " + result); //$NON-NLS-1$
        return result;
    }

    /**
     * @see org.java.plugin.ObjectFactory#createPathResolver()
     */
    @Override
    public PathResolver createPathResolver() {
        String className = getImplClassName(PathResolver.class);
        PathResolver result;
        if (className == null) {
            className = "org.java.plugin.standard.StandardPathResolver"; //$NON-NLS-1$
        }
        try {
            result = (PathResolver) createClassInstance(className);
        } catch (Exception e) {
            log.fatal("failed creating path resolver instance " //$NON-NLS-1$
                    + className, e);
            throw new Error("failed creating path resolver instance " //$NON-NLS-1$
                    + className, e);
        }
        try {
            result.configure(config.getSubset(className + ".")); //$NON-NLS-1$
        } catch (Exception e) {
            log.fatal("failed configuring path resolver instance " //$NON-NLS-1$
                    + result, e);
            throw new Error("failed configuring path resolver instance " //$NON-NLS-1$
                    + result, e);
        }
        log.debug("path resolver instance created - " + result); //$NON-NLS-1$
        return result;
    }

    /**
     * Creates new instance of plug-in life cycle handler implementation class
     * using standard discovery algorithm to determine which handler
     * implementation class should be instantiated.
     * @return new plug-in life cycle handler instance
     */
    protected PluginLifecycleHandler createLifecycleHandler() {
        String className = getImplClassName(PluginLifecycleHandler.class);
        PluginLifecycleHandler result;
        if (className == null) {
            className =
                "org.java.plugin.standard.StandardPluginLifecycleHandler"; //$NON-NLS-1$
        }
        try {
            result = (PluginLifecycleHandler) createClassInstance(className);
        } catch (Exception e) {
            log.fatal("failed creating plug-in life cycle handler instance " //$NON-NLS-1$
                    + className, e);
            throw new Error(
                    "failed creating plug-in life cycle handler instance " //$NON-NLS-1$
                    + className, e);
        }
        result.configure(config.getSubset(className + ".")); //$NON-NLS-1$
        log.debug("life cycle handler instance created - " + result); //$NON-NLS-1$
        return result;
    }

    /**
     * @see org.java.plugin.ObjectFactory#createManager(
     *      org.java.plugin.registry.PluginRegistry,
     *      org.java.plugin.PathResolver)
     */
    @Override
    public PluginManager createManager(final PluginRegistry registry,
            final PathResolver pathResolver) {
        return new StandardPluginManager(registry, pathResolver,
                createLifecycleHandler());
    }
}
