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

import java.net.URL;

import org.java.plugin.registry.Documentation;
import org.java.plugin.registry.Identity;
import org.java.plugin.registry.ManifestProcessingException;
import org.java.plugin.registry.MatchingRule;
import org.java.plugin.registry.PluginDescriptor;
import org.java.plugin.registry.PluginFragment;
import org.java.plugin.registry.PluginRegistry;
import org.java.plugin.registry.Version;

/**
 * @version $Id: PluginFragmentImpl.java,v 1.4 2007/03/03 17:16:26 ddimon Exp $
 */
class PluginFragmentImpl extends IdentityImpl implements PluginFragment {
    private final PluginRegistry registry;
    private final ModelPluginFragment model;
    private Documentation<PluginFragment> doc;

    PluginFragmentImpl(final PluginRegistry aRegistry,
            final ModelPluginFragment aModel)
            throws ManifestProcessingException {
        super(aModel.getId());
        registry = aRegistry;
        model = aModel;
        if (model.getVendor() == null) {
            model.setVendor(""); //$NON-NLS-1$
        }
        if ((model.getPluginId() == null)
                || (model.getPluginId().trim().length() == 0)) {
            throw new ManifestProcessingException(
                    PluginRegistryImpl.PACKAGE_NAME,
                    "fragmentPliginIdIsBlank", getId()); //$NON-NLS-1$
        }
        if (getId().equals(model.getPluginId())) {
            throw new ManifestProcessingException(
                    PluginRegistryImpl.PACKAGE_NAME,
                    "invalidFragmentPluginId", getId()); //$NON-NLS-1$
        }
        if ((model.getDocsPath() == null)
                || (model.getDocsPath().trim().length() == 0)) {
            model.setDocsPath("docs"); //$NON-NLS-1$
        }
        if (model.getDocumentation() != null) {
            doc = new DocumentationImpl<PluginFragment>(this,
                    model.getDocumentation());
        }
        if (log.isDebugEnabled()) {
            log.debug("object instantiated: " + this); //$NON-NLS-1$
        }
    }
    
    ModelPluginFragment getModel() {
        return model;
    }

    /**
     * @see org.java.plugin.registry.UniqueIdentity#getUniqueId()
     */
    public String getUniqueId() {
        return registry.makeUniqueId(getId(), model.getVersion());
    }

    /**
     * @see org.java.plugin.registry.PluginFragment#getVendor()
     */
    public String getVendor() {
        return model.getVendor();
    }

    /**
     * @see org.java.plugin.registry.PluginFragment#getVersion()
     */
    public Version getVersion() {
        return model.getVersion();
    }

    /**
     * @see org.java.plugin.registry.PluginFragment#getPluginId()
     */
    public String getPluginId() {
        return model.getPluginId();
    }

    /**
     * @see org.java.plugin.registry.PluginFragment#getPluginVersion()
     */
    public Version getPluginVersion() {
        return model.getPluginVersion();
    }

    /**
     * @see org.java.plugin.registry.PluginFragment#getRegistry()
     */
    public PluginRegistry getRegistry() {
        return registry;
    }

    /**
     * @see org.java.plugin.registry.PluginFragment#matches(
     *      org.java.plugin.registry.PluginDescriptor)
     */
    public boolean matches(final PluginDescriptor descr) {
        return PluginPrerequisiteImpl.matches(model.getPluginVersion(),
                descr.getVersion(), model.getMatchingRule());
    }
    
    /**
     * @see org.java.plugin.registry.PluginFragment#getMatchingRule()
     */
    public MatchingRule getMatchingRule() {
        return model.getMatchingRule();
    }

    /**
     * @see org.java.plugin.registry.Documentable#getDocumentation()
     */
    public Documentation<PluginFragment> getDocumentation() {
        return doc;
    }

    /**
     * @see org.java.plugin.registry.PluginFragment#getDocsPath()
     */
    public String getDocsPath() {
        return model.getDocsPath();
    }

    /**
     * @see org.java.plugin.registry.PluginFragment#getLocation()
     */
    public URL getLocation() {
        return model.getLocation();
    }

    /**
     * @see org.java.plugin.registry.xml.IdentityImpl#isEqualTo(
     *      org.java.plugin.registry.Identity)
     */
    @Override
    protected boolean isEqualTo(final Identity idt) {
        if (!(idt instanceof PluginFragmentImpl)) {
            return false;
        }
        PluginFragmentImpl other = (PluginFragmentImpl) idt;
        return getUniqueId().equals(other.getUniqueId())
            && getLocation().toExternalForm().equals(
                    other.getLocation().toExternalForm());
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "{PluginFragment: uid=" + getUniqueId() + "}"; //$NON-NLS-1$ //$NON-NLS-2$
    }
}
