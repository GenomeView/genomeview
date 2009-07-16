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

import org.java.plugin.registry.Documentation;
import org.java.plugin.registry.Identity;
import org.java.plugin.registry.ManifestProcessingException;
import org.java.plugin.registry.PluginDescriptor;
import org.java.plugin.registry.PluginElement;
import org.java.plugin.registry.PluginFragment;

/**
 * @version $Id$
 */
abstract class PluginElementImpl<T extends PluginElement<T>>
        extends IdentityImpl implements PluginElement<T> {
    private final PluginDescriptor descriptor;
    private final PluginFragment fragment;
    private DocumentationImpl<T> doc;

    protected PluginElementImpl(final PluginDescriptor descr,
            final PluginFragment aFragment, final String id,
            final ModelDocumentation modelDoc)
            throws ManifestProcessingException {
        super(id);
        descriptor = descr;
        fragment = aFragment;
        if (modelDoc != null) {
            doc = new DocumentationImpl(this, modelDoc);
        }
    }

    /**
     * @see org.java.plugin.registry.xml.IdentityImpl#isEqualTo(
     *      org.java.plugin.registry.Identity)
     */
    @Override
    protected boolean isEqualTo(final Identity idt) {
        if (!getClass().getName().equals(idt.getClass().getName())) {
            return false;
        }
        return getDeclaringPluginDescriptor().equals(
                ((PluginElementImpl) idt).getDeclaringPluginDescriptor())
                && getId().equals(idt.getId());
    }

    /**
     * @see org.java.plugin.registry.PluginElement#getDeclaringPluginDescriptor()
     */
    public PluginDescriptor getDeclaringPluginDescriptor() {
        return descriptor;
    }

    /**
     * @see org.java.plugin.registry.PluginElement#getDeclaringPluginFragment()
     */
    public PluginFragment getDeclaringPluginFragment() {
        return fragment;
    }

    /**
     * @see org.java.plugin.registry.Documentable#getDocumentation()
     */
    public Documentation<T> getDocumentation() {
        return doc;
    }

    /**
     * @see org.java.plugin.registry.Documentable#getDocsPath()
     */
    public String getDocsPath() {
        return (fragment != null) ? fragment.getDocsPath()
                : descriptor.getDocsPath();
    }
}
