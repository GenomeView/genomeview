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
import java.util.Locale;

import org.java.plugin.PluginManager.PluginLocation;
import org.java.plugin.util.IoUtil;

/**
 * A standard implementation of plug-in location interface. It may be used to
 * create plug-in locations from JAR or ZIP files of plug-in folders, or from
 * any URL pointers.
 * <p>
 * Inspired by Per Cederberg.
 * 
 * @version $Id$
 */
public class StandardPluginLocation implements PluginLocation {
    /**
     * Creates plug-in location from a given file and checks that all required
     * resources are available. Before creating location object, this method
     * probes given ZIP file of folder for presence of any of the following
     * files:
     * <ul>
     *   <li>/plugin.xml</li>
     *   <li>/plugin-fragment.xml</li>
     *   <li>/META-INF/plugin.xml</li>
     *   <li>/META-INF/plugin-fragment.xml</li>
     * </ul>
     * If any of those files present, a new plug-in location object created and
     * returned.
     * @param file plug-in JAR or ZIP file or plug-in folder
     * @return created new plug-in location or <code>null</code> if given file
     *         doesn't points to a valid plug-in file or folder
     * @throws MalformedURLException if the plug-in URL's couldn't be created
     */
    public static PluginLocation create(final File file)
            throws MalformedURLException {
        if (file.isDirectory()) {
            URL manifestUrl = getManifestUrl(file);
            return (manifestUrl == null) ? null
                    : new StandardPluginLocation(
                            IoUtil.file2url(file), manifestUrl);
        }
        String fileName = file.getName().toLowerCase(Locale.getDefault());
        if (!fileName.endsWith(".jar") //$NON-NLS-1$
                && !fileName.endsWith(".zip")) { //$NON-NLS-1$
            return null;
        }
        URL manifestUrl = getManifestUrl(file);
        return (manifestUrl == null) ? null
                : new StandardPluginLocation(new URL("jar:" //$NON-NLS-1$
                        + IoUtil.file2url(file).toExternalForm()
                        + "!/"), manifestUrl); //$NON-NLS-1$
    }

    private static URL getManifestUrl(final File file)
            throws MalformedURLException {
        if (file.isDirectory()) {
            File result = new File(file, "plugin.xml"); //$NON-NLS-1$
            if (result.isFile()) {
                return IoUtil.file2url(result);
            }
            result = new File(file, "plugin-fragment.xml"); //$NON-NLS-1$
            if (result.isFile()) {
                return IoUtil.file2url(result);
            }
            result = new File(file, "META-INF" + File.separator //$NON-NLS-1$
                    + "plugin.xml"); //$NON-NLS-1$
            if (result.isFile()) {
                return IoUtil.file2url(result);
            }
            result = new File(file, "META-INF" + File.separator //$NON-NLS-1$
                    + "plugin-fragment.xml"); //$NON-NLS-1$
            if (result.isFile()) {
                return IoUtil.file2url(result);
            }
            return null;
        }
        if (!file.isFile()) {
            return null;
        }
        URL url = new URL("jar:" //$NON-NLS-1$
                + IoUtil.file2url(file).toExternalForm()
                + "!/plugin.xml"); //$NON-NLS-1$
        if (IoUtil.isResourceExists(url)) {
            return url;
        }
        url = new URL("jar:" //$NON-NLS-1$
                + IoUtil.file2url(file).toExternalForm()
                + "!/plugin-fragment.xml"); //$NON-NLS-1$
        if (IoUtil.isResourceExists(url)) {
            return url;
        }
        url = new URL("jar:" //$NON-NLS-1$
                + IoUtil.file2url(file).toExternalForm()
                + "!/META-INF/plugin.xml"); //$NON-NLS-1$
        if (IoUtil.isResourceExists(url)) {
            return url;
        }
        url = new URL("jar:" //$NON-NLS-1$
                + IoUtil.file2url(file).toExternalForm()
                + "!/META-INF/plugin-fragment.xml"); //$NON-NLS-1$
        if (IoUtil.isResourceExists(url)) {
            return url;
        }
        return null;
    }
    
    private final URL context;
    private final URL manifest;

    /**
     * Creates a new plug-in location from a given context an manifest URL's.
     * @param aContext plug-in context URL
     * @param aManifest plug-in manifest URL
     */
    public StandardPluginLocation(final URL aContext, final URL aManifest) {
        if (aContext == null) {
            throw new NullPointerException("context"); //$NON-NLS-1$
        }
        if (aManifest == null) {
            throw new NullPointerException("manifest"); //$NON-NLS-1$
        }
        context = aContext;
        manifest = aManifest;
    }
    
    /**
     * Creates a new plug-in location from a jar or a zip file or a folder. This
     * plug-in manifest file path specified is relative to the root directory of
     * the jar or zip file or given folder.
     * @param file the plug-in zip file or plug-in folder
     * @param manifestPath the relative manifest path
     * @throws MalformedURLException if the plug-in URL's couldn't be created
     */
    public StandardPluginLocation(final File file, final String manifestPath)
            throws MalformedURLException {
        if (file.isDirectory()) {
            context = IoUtil.file2url(file);
        } else {
            context = new URL("jar:" //$NON-NLS-1$
                    + IoUtil.file2url(file).toExternalForm() + "!/"); //$NON-NLS-1$
        }
        manifest = new URL(context, manifestPath.startsWith("/") //$NON-NLS-1$
                ? manifestPath.substring(1) : manifestPath);
    }

    /**
     * @see org.java.plugin.PluginManager.PluginLocation#getManifestLocation()
     */
    public URL getManifestLocation() {
        return manifest;
    }

    /**
     * @see org.java.plugin.PluginManager.PluginLocation#getContextLocation()
     */
    public URL getContextLocation() {
        return context;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return context.toString();
    }
}
