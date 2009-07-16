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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.java.plugin.registry.PluginRegistry;
import org.java.plugin.standard.StandardObjectFactory;
import org.java.plugin.util.ExtendedProperties;
import org.java.plugin.util.IoUtil;

/**
 * Factory class to help creating base Framework objects: plug-in registry, path
 * resolver and plug-in manager.
 * 
 * @version $Id$
 */
public abstract class ObjectFactory {
    /**
     * Creates and configures new instance of object factory.
     * 
     * @return configured instance of object factory
     * 
     * @see #newInstance(ExtendedProperties)
     */
    public static ObjectFactory newInstance() {
        return newInstance(null);
    }

    /**
     * Creates and configures new instance of object factory. Factory
     * implementation class discovery procedure is following:
     * <ul>
     * <li>Use the <code>org.java.plugin.ObjectFactory</code> property from
     * the given properties collection (if it is provided).</li>
     * <li>Use the <code>org.java.plugin.ObjectFactory</code> system
     * property.</li>
     * <li>Use the properties file "jpf.properties" in the JRE "lib" directory
     * or in the CLASSPATH. This configuration file is in standard
     * <code>java.util.Properties</code> format and contains among others the
     * fully qualified name of the implementation class with the key being the
     * system property defined above.</li>
     * <li>Use the Services API (as detailed in the JAR specification), if
     * available, to determine the class name. The Services API will look for a
     * class name in the file
     * <code>META-INF/services/org.java.plugin.ObjectFactory</code> in jars
     * available to the runtime.</li>
     * <li>Framework default <code>ObjectFactory</code> implementation.</li>
     * </ul>
     * 
     * @param config
     *            factory configuration data, may be <code>null</code>
     * @return configured instance of object factory
     */
    public static ObjectFactory newInstance(final ExtendedProperties config) {
        Log log = LogFactory.getLog(ObjectFactory.class);
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = ObjectFactory.class.getClassLoader();
        }
        ExtendedProperties props;
        if (config != null) {
            props = config;
        } else {
            props = loadProperties(cl);
        }
        String className = findProperty(cl, props);
        ObjectFactory result;
        try {
            if (className == null) {
                className = "org.java.plugin.standard.StandardObjectFactory"; //$NON-NLS-1$
            }
            result = (ObjectFactory) loadClass(cl, className).newInstance();
        } catch (ClassNotFoundException cnfe) {
            log.fatal("failed instantiating object factory " //$NON-NLS-1$
                    + className, cnfe);
            throw new Error("failed instantiating object factory " //$NON-NLS-1$
                    + className, cnfe);
        } catch (IllegalAccessException iae) {
            log.fatal("failed instantiating object factory " //$NON-NLS-1$
                    + className, iae);
            throw new Error("failed instantiating object factory " //$NON-NLS-1$
                    + className, iae);
        } catch (SecurityException se) {
            log.fatal("failed instantiating object factory " //$NON-NLS-1$
                    + className, se);
            throw new Error("failed instantiating object factory " //$NON-NLS-1$
                    + className, se);
        } catch (InstantiationException ie) {
            log.fatal("failed instantiating object factory " //$NON-NLS-1$
                    + className, ie);
            throw new Error("failed instantiating object factory " //$NON-NLS-1$
                    + className, ie);
        }
        result.configure(props);
        log.debug("object factory instance created - " + result); //$NON-NLS-1$
        return result;
    }

    private static Class<?> loadClass(final ClassLoader cl,
            final String className) throws ClassNotFoundException {
        if (cl != null) {
            try {
                return cl.loadClass(className);
            } catch (ClassNotFoundException cnfe) {
                // ignore
            }
        }
        ClassLoader cl2 = ObjectFactory.class.getClassLoader();
        if (cl2 != null) {
            try {
                return cl2.loadClass(className);
            } catch (ClassNotFoundException cnfe) {
                // ignore
            }
        }
        return ClassLoader.getSystemClassLoader().loadClass(className);
    }

    private static ExtendedProperties loadProperties(final ClassLoader cl) {
        Log log = LogFactory.getLog(ObjectFactory.class);
        File file = new File(System.getProperty("java.home") //$NON-NLS-1$
                + File.separator + "lib" + File.separator //$NON-NLS-1$
                + "jpf.properties"); //$NON-NLS-1$
        URL url = null;
        if (file.canRead()) {
            try {
                url = IoUtil.file2url(file);
            } catch (MalformedURLException mue) {
                log.error("failed converting file " + file //$NON-NLS-1$
                        + " to URL", mue); //$NON-NLS-1$
            }
        }
        if (url == null) {
            if (cl != null) {
                url = cl.getResource("jpf.properties"); //$NON-NLS-1$
                if (url == null) {
                    url = ClassLoader.getSystemResource("jpf.properties"); //$NON-NLS-1$
                }
            } else {
                url = ClassLoader.getSystemResource("jpf.properties"); //$NON-NLS-1$
            }
            if (url == null) {
                log.debug("no jpf.properties file found in ${java.home}/lib (" //$NON-NLS-1$
                        + file
                        + ") nor in CLASSPATH, using standard properties"); //$NON-NLS-1$
                url = StandardObjectFactory.class.getResource("jpf.properties"); //$NON-NLS-1$
            }
        }
        try {
            InputStream strm = IoUtil.getResourceInputStream(url);
            try {
                ExtendedProperties props = new ExtendedProperties();
                props.load(strm);
                log.debug("loaded jpf.properties from " + url); //$NON-NLS-1$
                return props;
            } finally {
                try {
                    strm.close();
                } catch (IOException ioe) {
                    // ignore
                }
            }
        } catch (Exception e) {
            log.error("failed loading jpf.properties from CLASSPATH", //$NON-NLS-1$
                    e);
        }
        return null;
    }

    private static String findProperty(final ClassLoader cl,
            final ExtendedProperties props) {
        Log log = LogFactory.getLog(ObjectFactory.class);
        String name = ObjectFactory.class.getName();
        String result = System.getProperty(name);
        if (result != null) {
            log.debug("property " + name //$NON-NLS-1$
                    + " found as system property"); //$NON-NLS-1$
            return result;
        }
        if (props != null) {
            result = props.getProperty(name);
            if (result != null) {
                log.debug("property " + name //$NON-NLS-1$
                        + " found in properties file"); //$NON-NLS-1$
                return result;
            }
        }
        String serviceId = "META-INF/services/" //$NON-NLS-1$
                + ObjectFactory.class.getName();
        InputStream strm;
        if (cl == null) {
            strm = ClassLoader.getSystemResourceAsStream(serviceId);
        } else {
            strm = cl.getResourceAsStream(serviceId);
        }
        if (strm != null) {
            try {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(strm, "UTF-8")); //$NON-NLS-1$
                try {
                    result = reader.readLine();
                } finally {
                    try {
                        reader.close();
                    } catch (IOException ioe) {
                        // ignore
                    }
                }
            } catch (IOException ioe) {
                try {
                    strm.close();
                } catch (IOException ioe2) {
                    // ignore
                }
            }
        }
        if (result != null) {
            log.debug("property " + name //$NON-NLS-1$
                    + " found as service"); //$NON-NLS-1$
            return result;
        }
        log.debug("no property " + name //$NON-NLS-1$
                + " found"); //$NON-NLS-1$
        return result;
    }

    /**
     * Configures this factory instance. This method is called from
     * {@link #newInstance(ExtendedProperties)}.
     * 
     * @param config
     *            factory configuration data, may be <code>null</code>
     */
    protected abstract void configure(ExtendedProperties config);

    /**
     * Creates new instance of plug-in manager using new instances of registry
     * and path resolver.
     * 
     * @return new plug-in manager instance
     * 
     * @see #createRegistry()
     * @see #createPathResolver()
     */
    public final PluginManager createManager() {
        return createManager(createRegistry(), createPathResolver());
    }

    /**
     * Creates new instance of plug-in manager.
     * 
     * @param registry
     * @param pathResolver
     * @return new plug-in manager instance
     */
    public abstract PluginManager createManager(PluginRegistry registry,
            PathResolver pathResolver);

    /**
     * Creates new instance of plug-in registry implementation class using
     * standard discovery algorithm to determine which registry implementation
     * class should be instantiated.
     * 
     * @return new registry instance
     */
    public abstract PluginRegistry createRegistry();

    /**
     * Creates new instance of path resolver implementation class using standard
     * discovery algorithm to determine which resolver implementation class
     * should be instantiated.
     * 
     * @return new path resolver instance
     */
    public abstract PathResolver createPathResolver();
}
