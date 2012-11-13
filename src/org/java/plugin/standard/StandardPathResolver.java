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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.java.plugin.PathResolver;
import org.java.plugin.registry.Identity;
import org.java.plugin.registry.PluginDescriptor;
import org.java.plugin.registry.PluginElement;
import org.java.plugin.registry.PluginFragment;
import org.java.plugin.util.ExtendedProperties;
import org.java.plugin.util.IoUtil;

/**
 * Standard simple implementation of path resolver. For resolving it uses
 * plug-in element registration (see {@link #registerContext(Identity, URL)})
 * procedure.
 * @version $Id$
 */
public class StandardPathResolver implements PathResolver {
    protected Log log = LogFactory.getLog(getClass());
    private Map<String, URL> urlMap = new HashMap<String, URL>();
    
    /**
     * This implementation accepts {@link PluginDescriptor} or
     * {@link PluginFragment} as valid plug-in elements.
     * @see org.java.plugin.PathResolver#registerContext(
     *      org.java.plugin.registry.Identity, java.net.URL)
     */
    public void registerContext(final Identity idt, final URL url) {
        if (!(idt instanceof PluginDescriptor)
                && !(idt instanceof PluginFragment)) {
            throw new IllegalArgumentException(
                    "unsupported identity class " //$NON-NLS-1$
                    + idt.getClass().getName());
        }
        final URL oldUrl = urlMap.put(idt.getId(), url);
        if (oldUrl != null) {
            log.warn("old context URL " + oldUrl //$NON-NLS-1$
                    + " has been replaced with new " + url //$NON-NLS-1$
                    + " for " + idt //$NON-NLS-1$
                    + " with key " + idt.getId()); //$NON-NLS-1$
        } else {
            if (log.isDebugEnabled()) {
                log.debug("context URL " + url //$NON-NLS-1$
                        + " registered for " + idt //$NON-NLS-1$
                        + " with key " + idt.getId()); //$NON-NLS-1$
            }
        }
    }

    /**
     * @see org.java.plugin.PathResolver#unregisterContext(java.lang.String)
     */
    public void unregisterContext(final String id) {
        final URL url = urlMap.remove(id);
        if (url == null) {
            log.warn("no context was registered with key " + id); //$NON-NLS-1$
        } else {
            if (log.isDebugEnabled()) {
                log.debug("context URL " + url //$NON-NLS-1$
                        + " un-registered for key " + id); //$NON-NLS-1$
            }
        }
    }

    /**
     * @see org.java.plugin.PathResolver#resolvePath(
     *      org.java.plugin.registry.Identity, java.lang.String)
     */
    public URL resolvePath(final Identity identity, final String path) {
        URL baseUrl;
        if ((identity instanceof PluginDescriptor)
                || (identity instanceof PluginFragment)) {
            baseUrl = getRegisteredContext(identity.getId());
        } else if (identity instanceof PluginElement) {
            PluginElement<?> element = (PluginElement) identity;
            if (element.getDeclaringPluginFragment() != null) {
                baseUrl = getRegisteredContext(
                        element.getDeclaringPluginFragment().getId());
            } else {
                baseUrl = getRegisteredContext(
                        element.getDeclaringPluginDescriptor().getId());
            }
        } else {
            throw new IllegalArgumentException("unknown identity class " //$NON-NLS-1$
                    + identity.getClass().getName());
        }
        return resolvePath(baseUrl, path);
    }
    
    /**
     * @see org.java.plugin.PathResolver#getRegisteredContext(java.lang.String)
     */
    public URL getRegisteredContext(final String id) {
        final URL result = urlMap.get(id);
        if (result == null) {
            throw new IllegalArgumentException("unknown plug-in or" //$NON-NLS-1$
                    + " plug-in fragment ID - " + id); //$NON-NLS-1$
        }
        return result;
    }
    
    /**
     * @see org.java.plugin.PathResolver#isContextRegistered(java.lang.String)
     */
    public boolean isContextRegistered(final String id) {
        return urlMap.containsKey(id);
    }

    /**
     * Resolves given path against given base URL.
     * @param baseUrl base URL to resolve given path
     * @param path path to be resolved
     * @return resolved URL
     */
    protected URL resolvePath(final URL baseUrl, final String path) {
        try {
            if ("".equals(path) || "/".equals(path)) { //$NON-NLS-1$ //$NON-NLS-2$
                return maybeJarUrl(baseUrl);
            }
            return maybeJarUrl(new URL(maybeJarUrl(baseUrl), path));
        } catch (MalformedURLException mue) {
            log.error("can't create URL in context of " + baseUrl //$NON-NLS-1$
                    + " and path " + path, mue); //$NON-NLS-1$
            throw new IllegalArgumentException("path " + path //$NON-NLS-1$
                    + " in context of " + baseUrl //$NON-NLS-1$
                    + " cause creation of malformed URL"); //$NON-NLS-1$
        }
    }
    
    protected URL maybeJarUrl(final URL url) throws MalformedURLException {
        if ("jar".equalsIgnoreCase(url.getProtocol())) { //$NON-NLS-1$
            return url;
        }
        File file = IoUtil.url2file(url);
        if ((file == null) || !file.isFile()) {
            return url;
        }
        String fileName =
            file.getName().toLowerCase(Locale.getDefault());
        if (fileName.endsWith(".jar") //$NON-NLS-1$
                || fileName.endsWith(".zip")) { //$NON-NLS-1$
            return new URL("jar:" //$NON-NLS-1$
                    + IoUtil.file2url(file).toExternalForm()
                    + "!/"); //$NON-NLS-1$
        }
        return url;
    }

    /**
     * No configuration parameters expected in this implementation.
     * @see org.java.plugin.PathResolver#configure(ExtendedProperties)
     */
    public void configure(final ExtendedProperties config) throws Exception {
        // no-op
    }
}
