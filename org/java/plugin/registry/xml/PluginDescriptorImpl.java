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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.java.plugin.registry.Documentation;
import org.java.plugin.registry.Extension;
import org.java.plugin.registry.ExtensionPoint;
import org.java.plugin.registry.Identity;
import org.java.plugin.registry.Library;
import org.java.plugin.registry.ManifestProcessingException;
import org.java.plugin.registry.PluginAttribute;
import org.java.plugin.registry.PluginDescriptor;
import org.java.plugin.registry.PluginFragment;
import org.java.plugin.registry.PluginPrerequisite;
import org.java.plugin.registry.PluginRegistry;
import org.java.plugin.registry.Version;

/**
 * @version $Id: PluginDescriptorImpl.java,v 1.4 2007/03/03 17:16:25 ddimon Exp $
 */
class PluginDescriptorImpl extends IdentityImpl implements PluginDescriptor {
    private final PluginRegistry registry;
    private final ModelPluginDescriptor model;
    private Map<String, PluginPrerequisite> pluginPrerequisites;
    private Map<String, Library> libraries;
    private Map<String, ExtensionPoint> extensionPoints;
    private Map<String, Extension> extensions;
    private Documentation<PluginDescriptor> doc;
    private List<PluginFragment> fragments;
    private List<PluginAttribute> attributes;

    PluginDescriptorImpl(final PluginRegistry aRegistry,
            final ModelPluginDescriptor aModel)
            throws ManifestProcessingException {
        super(aModel.getId());
        registry = aRegistry;
        model = aModel;
        if (model.getVendor() == null) {
            model.setVendor(""); //$NON-NLS-1$
        }
        if ((model.getClassName() != null)
                && (model.getClassName().trim().length() == 0)) {
            model.setClassName(null);
        }
        if ((model.getDocsPath() == null)
                || (model.getDocsPath().trim().length() == 0)) {
            model.setDocsPath("docs"); //$NON-NLS-1$
        }
        if (model.getDocumentation() != null) {
            doc = new DocumentationImpl<PluginDescriptor>(this,
                    model.getDocumentation());
        }
        
        attributes = new LinkedList<PluginAttribute>();
        fragments = new LinkedList<PluginFragment>();
        pluginPrerequisites = new HashMap<String, PluginPrerequisite>();
        libraries = new HashMap<String, Library>();
        extensionPoints = new HashMap<String, ExtensionPoint>();
        extensions = new HashMap<String, Extension>();
        
        processAttributes(null, model);
        processPrerequisites(null, model);
        processLibraries(null, model);
        processExtensionPoints(null, model);
        processExtensions(null, model);
        
        if (log.isDebugEnabled()) {
            log.debug("object instantiated: " + this); //$NON-NLS-1$
        }
    }
    
    void registerFragment(final PluginFragmentImpl fragment)
            throws ManifestProcessingException {
        fragments.add(fragment);
        processAttributes(fragment, fragment.getModel());
        processPrerequisites(fragment, fragment.getModel());
        processLibraries(fragment, fragment.getModel());
        processExtensionPoints(fragment, fragment.getModel());
        processExtensions(fragment, fragment.getModel());
    }
    
    void unregisterFragment(final PluginFragmentImpl fragment) {
        // removing attributes
        for (Iterator<PluginAttribute> it = attributes.iterator(); it.hasNext();) {
            if (fragment.equals(it.next()
                    .getDeclaringPluginFragment())) {
                it.remove();
            }
        }
        // removing prerequisites
        for (Iterator<Entry<String, PluginPrerequisite>> it =
            pluginPrerequisites.entrySet().iterator(); it.hasNext();) {
            Entry<String, PluginPrerequisite> entry = it.next();
            if (fragment.equals(entry.getValue()
                    .getDeclaringPluginFragment())) {
                it.remove();
            }
        }
        // removing libraries
        for (Iterator<Entry<String, Library>> it = libraries.entrySet().iterator();
                it.hasNext();) {
            Entry<String, Library> entry = it.next();
            if (fragment.equals(entry.getValue()
                    .getDeclaringPluginFragment())) {
                it.remove();
            }
        }
        // removing extension points
        for (Iterator<Entry<String, ExtensionPoint>> it = extensionPoints.entrySet().iterator();
                it.hasNext();) {
            Entry<String, ExtensionPoint> entry = it.next();
            if (fragment.equals(entry.getValue()
                    .getDeclaringPluginFragment())) {
                it.remove();
            }
        }
        // removing extensions
        for (Iterator<Entry<String, Extension>> it = extensions.entrySet().iterator();
                it.hasNext();) {
            Entry<String, Extension> entry = it.next();
            if (fragment.equals(entry.getValue()
                    .getDeclaringPluginFragment())) {
                it.remove();
            }
        }
        fragments.remove(fragment);
    }
    
    private void processAttributes(final PluginFragmentImpl fragment,
            final ModelPluginManifest modelManifest)
            throws ManifestProcessingException {
        for (ModelAttribute modelAttribute : modelManifest.getAttributes()) {
            attributes.add(new PluginAttributeImpl(this, fragment, modelAttribute, null));
        }
    }
    
    private void processPrerequisites(final PluginFragmentImpl fragment,
            final ModelPluginManifest modelManifest)
            throws ManifestProcessingException {
        for (ModelPrerequisite modelPrerequisite : modelManifest.getPrerequisites()) {
            PluginPrerequisiteImpl pluginPrerequisite =
                new PluginPrerequisiteImpl(this, fragment, modelPrerequisite);
            if (pluginPrerequisites.containsKey(
                    pluginPrerequisite.getPluginId())) {
                throw new ManifestProcessingException(
                        PluginRegistryImpl.PACKAGE_NAME,
                        "duplicateImports", new Object[] { //$NON-NLS-1$
                            pluginPrerequisite.getPluginId(), getId()});
            }
            pluginPrerequisites.put(pluginPrerequisite.getPluginId(),
                pluginPrerequisite);
        }
    }
    
    private void processLibraries(final PluginFragmentImpl fragment,
            final ModelPluginManifest modelManifest)
            throws ManifestProcessingException {
        for (ModelLibrary modelLibrary : modelManifest.getLibraries()) {
            LibraryImpl lib = new LibraryImpl(this, fragment, modelLibrary);
            if (libraries.containsKey(lib.getId())) {
                throw new ManifestProcessingException(
                        PluginRegistryImpl.PACKAGE_NAME,
                        "duplicateLibraries", new Object[] { //$NON-NLS-1$
                            lib.getId(), getId()});
            }
            libraries.put(lib.getId(), lib);
        }
    }
    
    private void processExtensionPoints(final PluginFragmentImpl fragment,
            final ModelPluginManifest modelManifest)
            throws ManifestProcessingException {
        for (ModelExtensionPoint modelExtensionPoint : modelManifest.getExtensionPoints()) {
            ExtensionPointImpl extensionPoint =
                new ExtensionPointImpl(this, fragment, modelExtensionPoint);
            if (extensionPoints.containsKey(extensionPoint.getId())) {
                throw new ManifestProcessingException(
                        PluginRegistryImpl.PACKAGE_NAME,
                        "duplicateExtensionPoints", new Object[] { //$NON-NLS-1$
                            extensionPoint.getId(), getId()});
            }
            extensionPoints.put(extensionPoint.getId(), extensionPoint);
        }
    }
    
    private void processExtensions(final PluginFragmentImpl fragment,
            final ModelPluginManifest modelManifest)
            throws ManifestProcessingException {
        for (ModelExtension modelExtension : modelManifest.getExtensions()) {
            ExtensionImpl extension = new ExtensionImpl(this, fragment, modelExtension);
            if (extensions.containsKey(extension.getId())) {
                throw new ManifestProcessingException(
                        PluginRegistryImpl.PACKAGE_NAME,
                        "duplicateExtensions", new Object[] { //$NON-NLS-1$
                            extension.getId(), getId()});
            }
            if (!getId().equals(extension.getExtendedPluginId())
                    && !pluginPrerequisites.containsKey(
                            extension.getExtendedPluginId())) {
                throw new ManifestProcessingException(
                        PluginRegistryImpl.PACKAGE_NAME,
                        "pluginNotDeclaredInPrerequisites", new Object[] { //$NON-NLS-1$
                            extension.getExtendedPluginId(), extension.getId(),
                            getId()});
            }
            extensions.put(extension.getId(), extension);
        }
        //extensions = Collections.unmodifiableMap(extensions);
    }

    /**
     * @see org.java.plugin.registry.UniqueIdentity#getUniqueId()
     */
    public String getUniqueId() {
        return registry.makeUniqueId(getId(), model.getVersion());
    }

    /**
     * @see org.java.plugin.registry.PluginDescriptor#getVendor()
     */
    public String getVendor() {
        return model.getVendor();
    }

    /**
     * @see org.java.plugin.registry.PluginDescriptor#getVersion()
     */
    public Version getVersion() {
        return model.getVersion();
    }

    /**
     * @see org.java.plugin.registry.PluginDescriptor#getPrerequisites()
     */
    public Collection<PluginPrerequisite> getPrerequisites() {
        return Collections.unmodifiableCollection(pluginPrerequisites.values());
    }

    /**
     * @see org.java.plugin.registry.PluginDescriptor#getPrerequisite(java.lang.String)
     */
    public PluginPrerequisite getPrerequisite(final String id) {
        return pluginPrerequisites.get(id);
    }

    /**
     * @see org.java.plugin.registry.PluginDescriptor#getExtensionPoints()
     */
    public Collection<ExtensionPoint> getExtensionPoints() {
        return Collections.unmodifiableCollection(extensionPoints.values());
    }

    /**
     * @see org.java.plugin.registry.PluginDescriptor#getExtensionPoint(java.lang.String)
     */
    public ExtensionPoint getExtensionPoint(final String id) {
        return extensionPoints.get(id);
    }

    /**
     * @see org.java.plugin.registry.PluginDescriptor#getExtensions()
     */
    public Collection<Extension> getExtensions() {
        return Collections.unmodifiableCollection(extensions.values());
    }

    /**
     * @see org.java.plugin.registry.PluginDescriptor#getExtension(java.lang.String)
     */
    public Extension getExtension(final String id) {
        return extensions.get(id);
    }

    /**
     * @see org.java.plugin.registry.PluginDescriptor#getLibraries()
     */
    public Collection<Library> getLibraries() {
        return Collections.unmodifiableCollection(libraries.values());
    }

    /**
     * @see org.java.plugin.registry.PluginDescriptor#getLibrary(java.lang.String)
     */
    public Library getLibrary(final String id) {
        return libraries.get(id);
    }

    /**
     * @see org.java.plugin.registry.PluginDescriptor#getRegistry()
     */
    public PluginRegistry getRegistry() {
        return registry;
    }

    /**
     * @see org.java.plugin.registry.PluginDescriptor#getPluginClassName()
     */
    public String getPluginClassName() {
        return model.getClassName();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "{PluginDescriptor: uid=" + getUniqueId() + "}"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * @see org.java.plugin.registry.Documentable#getDocumentation()
     */
    public Documentation<PluginDescriptor> getDocumentation() {
        return doc;
    }

    /**
     * @see org.java.plugin.registry.PluginDescriptor#getFragments()
     */
    public Collection<PluginFragment> getFragments() {
        return Collections.unmodifiableCollection(fragments);
    }

    /**
     * @see org.java.plugin.registry.PluginDescriptor#getAttribute(java.lang.String)
     */
    public PluginAttribute getAttribute(final String id) {
        PluginAttributeImpl result = null;
        for (PluginAttribute attribute : attributes) {
            PluginAttributeImpl attr = (PluginAttributeImpl) attribute;
            if (attr.getId().equals(id)) {
                if (result == null) {
                    result = attr;
                } else {
                    throw new IllegalArgumentException(
                        "more than one attribute with ID " + id //$NON-NLS-1$
                        + " defined in plug-in " + getUniqueId()); //$NON-NLS-1$
                }
            }
        }
        return result;
    }
    
    /**
     * @see org.java.plugin.registry.PluginDescriptor#getAttributes()
     */
    public Collection<PluginAttribute> getAttributes() {
        return Collections.unmodifiableCollection(attributes);
    }
    
    /**
     * @see org.java.plugin.registry.PluginDescriptor#getAttributes(java.lang.String)
     */
    public Collection<PluginAttribute> getAttributes(final String id) {
        List<PluginAttribute> result = new LinkedList<PluginAttribute>();
        for (PluginAttribute attribute : attributes) {
            PluginAttributeImpl param = (PluginAttributeImpl) attribute;
            if (param.getId().equals(id)) {
                result.add(param);
            }
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * @see org.java.plugin.registry.PluginDescriptor#getDocsPath()
     */
    public String getDocsPath() {
        return model.getDocsPath();
    }

    /**
     * @see org.java.plugin.registry.PluginDescriptor#getLocation()
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
        if (!(idt instanceof PluginDescriptorImpl)) {
            return false;
        }
        PluginDescriptorImpl other = (PluginDescriptorImpl) idt;
        return getUniqueId().equals(other.getUniqueId())
            && getLocation().toExternalForm().equals(
                    other.getLocation().toExternalForm());
    }
}
