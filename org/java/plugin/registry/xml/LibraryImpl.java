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
package org.java.plugin.registry.xml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.java.plugin.registry.Library;
import org.java.plugin.registry.ManifestProcessingException;
import org.java.plugin.registry.Version;

/**
 * @version $Id$
 */
class LibraryImpl extends PluginElementImpl<Library> implements Library {
    private final ModelLibrary model;
    private List<String> exports;

    LibraryImpl(final PluginDescriptorImpl descr,
            final PluginFragmentImpl aFragment, final ModelLibrary aModel)
            throws ManifestProcessingException {
        super(descr, aFragment, aModel.getId(), aModel.getDocumentation());
        model = aModel;
        if ((model.getPath() == null)
                || (model.getPath().trim().length() == 0)) {
            throw new ManifestProcessingException(
                    PluginRegistryImpl.PACKAGE_NAME,
                    "libraryPathIsBlank"); //$NON-NLS-1$
        }
        exports = new ArrayList<String>(model.getExports().size());
        for (String exportPrefix : model.getExports()) {
            if ((exportPrefix == null) || (exportPrefix.trim().length() == 0)) {
                throw new ManifestProcessingException(
                        PluginRegistryImpl.PACKAGE_NAME,
                        "exportPrefixIBlank"); //$NON-NLS-1$
            }
            exportPrefix = exportPrefix.replace('\\', '.').replace('/', '.');
            if (exportPrefix.startsWith(".")) { //$NON-NLS-1$
                exportPrefix = exportPrefix.substring(1);
            }
            exports.add(exportPrefix);
        }
        exports = Collections.unmodifiableList(exports);
        if (log.isDebugEnabled()) {
            log.debug("object instantiated: " + this); //$NON-NLS-1$
        }
    }

    /**
     * @see org.java.plugin.registry.Library#getPath()
     */
    public String getPath() {
        return model.getPath();
    }

    /**
     * @see org.java.plugin.registry.Library#getExports()
     */
    public Collection<String> getExports() {
        return exports;
    }

    /**
     * @see org.java.plugin.registry.Library#isCodeLibrary()
     */
    public boolean isCodeLibrary() {
        return model.isCodeLibrary();
    }

    /**
     * @see org.java.plugin.registry.UniqueIdentity#getUniqueId()
     */
    public String getUniqueId() {
        return getDeclaringPluginDescriptor().getRegistry().makeUniqueId(
                getDeclaringPluginDescriptor().getId(), getId());
    }
    
    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "{Library: uid=" + getUniqueId() + "}"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * @see org.java.plugin.registry.Library#getVersion()
     */
    public Version getVersion() {
        return model.getVersion();
    }
}
