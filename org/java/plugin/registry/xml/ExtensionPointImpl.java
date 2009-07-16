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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.java.plugin.registry.Extension;
import org.java.plugin.registry.ExtensionMultiplicity;
import org.java.plugin.registry.ExtensionPoint;
import org.java.plugin.registry.Identity;
import org.java.plugin.registry.IntegrityCheckReport;
import org.java.plugin.registry.ManifestProcessingException;
import org.java.plugin.registry.ParameterMultiplicity;
import org.java.plugin.registry.ParameterType;
import org.java.plugin.registry.PluginDescriptor;
import org.java.plugin.registry.PluginRegistry;
import org.java.plugin.registry.IntegrityCheckReport.ReportItem;

/**
 * @version $Id$
 */
class ExtensionPointImpl extends PluginElementImpl<ExtensionPoint> implements ExtensionPoint {
    private final ModelExtensionPoint model;
    private Map<String, Extension> connectedExtensions;
    private Map<String, Extension> availableExtensions;
    private List<ParameterDefinition> parameterDefinitions;
    private Boolean isValid;
    private boolean paramDefsMerged = false;
    private List<ExtensionPoint> descendants;

    ExtensionPointImpl(final PluginDescriptorImpl descr,
            final PluginFragmentImpl aFragment,
            final ModelExtensionPoint aModel)
            throws ManifestProcessingException {
        super(descr, aFragment, aModel.getId(), aModel.getDocumentation());
        model = aModel;
        if ((model.getParentPointId() != null)
                && (model.getParentPluginId() == null)) {
            log.warn("parent plug-in ID not specified together with parent" //$NON-NLS-1$
                    + " extension point ID, using declaring plug-in ID," //$NON-NLS-1$
                    + " extension point is " + getUniqueId()); //$NON-NLS-1$
            model.setParentPluginId(descr.getId());
        }
        parameterDefinitions = new ArrayList<ParameterDefinition>(model.getParamDefs().size());
        Set<String> names = new HashSet<String>();
        ParameterDefinitionImpl def;
        for (ModelParameterDef modelParameterDef : model.getParamDefs()) {
            def = new ParameterDefinitionImpl(null, modelParameterDef);
            if (names.contains(def.getId())) {
                throw new ManifestProcessingException(
                        PluginRegistryImpl.PACKAGE_NAME,
                        "duplicateParameterDefinition", //$NON-NLS-1$
                        new Object[] {def.getId(), getId(), descr.getId()});
            }
            names.add(def.getId());
            parameterDefinitions.add(def);
        }
        parameterDefinitions =
            Collections.unmodifiableList(parameterDefinitions);
        if (log.isDebugEnabled()) {
            log.debug("object instantiated: " + this); //$NON-NLS-1$
        }
    }

    /**
     * @see org.java.plugin.registry.UniqueIdentity#getUniqueId()
     */
    public String getUniqueId() {
        return getDeclaringPluginDescriptor().getRegistry().makeUniqueId(
                getDeclaringPluginDescriptor().getId(), getId());
    }
    
    /**
     * @see org.java.plugin.registry.ExtensionPoint#getMultiplicity()
     */
    public ExtensionMultiplicity getMultiplicity() {
        return model.getExtensionMultiplicity();
    }

    private void updateExtensionsLists() {
        connectedExtensions = new HashMap<String, Extension>();
        availableExtensions = new HashMap<String, Extension>();
        for (PluginDescriptor descr : getDeclaringPluginDescriptor().getRegistry()
                .getPluginDescriptors()) {
            for (Extension ext : descr.getExtensions()) {
                if (getDeclaringPluginDescriptor().getId().equals(
                        ext.getExtendedPluginId())
                        && getId().equals(ext.getExtendedPointId())) {
                    availableExtensions.put(ext.getUniqueId(), ext);
                    if (ext.isValid()) {
                        if (log.isDebugEnabled()) {
                            log.debug("extension " + ext //$NON-NLS-1$
                                    + " connected to point " + this); //$NON-NLS-1$
                        }
                        connectedExtensions.put(ext.getUniqueId(), ext);
                    } else {
                        log.warn("extension " + ext.getUniqueId() //$NON-NLS-1$
                                + " is invalid and doesn't connected to" //$NON-NLS-1$
                                + " extension point " + getUniqueId()); //$NON-NLS-1$
                    }
                }
            }
        }
    }

    /**
     * @see org.java.plugin.registry.ExtensionPoint#getAvailableExtensions()
     */
    public Collection<Extension> getAvailableExtensions() {
        if (availableExtensions == null) {
            updateExtensionsLists();
        }
        return Collections.unmodifiableCollection(availableExtensions.values());
    }

    /**
     * @see org.java.plugin.registry.ExtensionPoint#getAvailableExtension(
     *      java.lang.String)
     */
    public Extension getAvailableExtension(final String uniqueId) {
        if (availableExtensions == null) {
            updateExtensionsLists();
        }
        Extension result = availableExtensions.get(uniqueId);
        if (result == null) {
            throw new IllegalArgumentException("extension " + uniqueId //$NON-NLS-1$
                + " not available in point " + getUniqueId()); //$NON-NLS-1$
        }
        return result;
    }

    /**
     * @see org.java.plugin.registry.ExtensionPoint#isExtensionAvailable(
     *      java.lang.String)
     */
    public boolean isExtensionAvailable(final String uniqueId) {
        if (availableExtensions == null) {
            updateExtensionsLists();
        }
        return availableExtensions.containsKey(uniqueId);
    }

    /**
     * @see org.java.plugin.registry.ExtensionPoint#getConnectedExtensions()
     */
    public Collection<Extension> getConnectedExtensions() {
        if (connectedExtensions == null) {
            updateExtensionsLists();
        }
        return Collections.unmodifiableCollection(connectedExtensions.values());
    }

    /**
     * @see org.java.plugin.registry.ExtensionPoint#getConnectedExtension(
     *      java.lang.String)
     */
    public Extension getConnectedExtension(final String uniqueId) {
        if (connectedExtensions == null) {
            updateExtensionsLists();
        }
        Extension result = connectedExtensions.get(uniqueId);
        if (result == null) {
            throw new IllegalArgumentException("extension " + uniqueId //$NON-NLS-1$
                + " not connected to point " + getUniqueId()); //$NON-NLS-1$
        }
        return result;
    }

    /**
     * @see org.java.plugin.registry.ExtensionPoint#isExtensionConnected(
     *      java.lang.String)
     */
    public boolean isExtensionConnected(final String uniqueId) {
        if (connectedExtensions == null) {
            updateExtensionsLists();
        }
        return connectedExtensions.containsKey(uniqueId);
    }

    /**
     * @see org.java.plugin.registry.ExtensionPoint#isValid()
     */
    public boolean isValid() {
        if (isValid == null) {
            validate();
        }
        return isValid.booleanValue();
    }
    
    Collection<ReportItem> validate() {
        if ((model.getParentPluginId() != null)
                && (model.getParentPointId() != null)) {
            try {
                if (!isExtensionPointAvailable(model.getParentPluginId(),
                        model.getParentPointId())) {
                    isValid = Boolean.FALSE;
                    return Collections.singletonList((ReportItem)
                            new IntegrityChecker.ReportItemImpl(
                                    IntegrityCheckReport.Severity.ERROR, this,
                                    IntegrityCheckReport.Error.INVALID_EXTENSION_POINT,
                                    "parentExtPointNotAvailable", //$NON-NLS-1$
                                    new Object[] {
                                            getDeclaringPluginDescriptor()
                                            .getRegistry().makeUniqueId(
                                                    model.getParentPluginId(),
                                                    model.getParentPointId()),
                                                    getUniqueId()}));
                }
            } catch (Throwable t) {
                isValid = Boolean.FALSE;
                if (log.isDebugEnabled()) {
                    log.debug("failed checking availability of extension point " //$NON-NLS-1$
                            + getDeclaringPluginDescriptor().getRegistry()
                                .makeUniqueId(model.getParentPluginId(),
                                        model.getParentPointId()), t);
                }
                return Collections.singletonList((ReportItem)
                        new IntegrityChecker.ReportItemImpl(
                                IntegrityCheckReport.Severity.ERROR, this,
                                IntegrityCheckReport.Error.INVALID_EXTENSION_POINT,
                                "parentExtPointAvailabilityCheckFailed", //$NON-NLS-1$
                                new Object[] {
                                        getDeclaringPluginDescriptor()
                                        .getRegistry().makeUniqueId(
                                                model.getParentPluginId(),
                                                model.getParentPointId()),
                                                getUniqueId(), t}));
            }
        }
        switch (getMultiplicity()) {
        case ANY:
            isValid = Boolean.TRUE;
            return Collections.emptyList();
        case ONE:
            isValid = Boolean.valueOf(getAvailableExtensions().size() == 1);
            if (!isValid.booleanValue()) {
                return Collections.singletonList((ReportItem)
                        new IntegrityChecker.ReportItemImpl(
                                IntegrityCheckReport.Severity.ERROR, this,
                                IntegrityCheckReport.Error.INVALID_EXTENSION_POINT,
                                "toManyOrFewExtsConnected", getUniqueId())); //$NON-NLS-1$
            }
            break;
        case NONE:
            isValid = Boolean.valueOf(getAvailableExtensions().size() == 0);
            if (!isValid.booleanValue()) {
                return Collections.singletonList((ReportItem)
                        new IntegrityChecker.ReportItemImpl(
                                IntegrityCheckReport.Severity.ERROR, this,
                                IntegrityCheckReport.Error.INVALID_EXTENSION_POINT,
                                "extsConnectedToAbstractExtPoint", getUniqueId())); //$NON-NLS-1$
            }
            break;
        case ONE_PER_PLUGIN:
            isValid = Boolean.TRUE;
            final Set<String> foundPlugins = new HashSet<String>();
            String pluginId;
            for (Extension extension : getAvailableExtensions()) {
                pluginId = extension.getDeclaringPluginDescriptor().getId();
                if (!foundPlugins.add(pluginId)) {
                    isValid = Boolean.FALSE;
                    return Collections.singletonList((ReportItem)
                            new IntegrityChecker.ReportItemImpl(
                                    IntegrityCheckReport.Severity.ERROR, this,
                                    IntegrityCheckReport.Error.INVALID_EXTENSION_POINT,
                                    "toManyExtsConnected", getUniqueId())); //$NON-NLS-1$
                }
            }
            break;
        }
        return Collections.emptyList();
    }

    private boolean isExtensionPointAvailable(final String pluginId,
            final String pointId) {
        PluginRegistry registry = getDeclaringPluginDescriptor().getRegistry();
        if (!registry.isPluginDescriptorAvailable(pluginId)) {
            return false;
        }
        for (ExtensionPoint extensionPoint : registry.getPluginDescriptor(pluginId)
                .getExtensionPoints()) {
            if (extensionPoint.getId().equals(pointId)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * @see org.java.plugin.registry.ExtensionPoint#getParameterDefinitions()
     */
    public Collection<ParameterDefinition> getParameterDefinitions() {
        if ((model.getParentPluginId() == null)
                || (model.getParentPointId() == null) || paramDefsMerged) {
            return parameterDefinitions;
        }
        final Set<String> names = new HashSet<String>();
        final Collection<ParameterDefinition> parentParamDefs =
            getDeclaringPluginDescriptor().getRegistry().getExtensionPoint(
                    model.getParentPluginId(), model.getParentPointId())
                    .getParameterDefinitions();
        final List<ParameterDefinition> newParamDefs = new ArrayList<ParameterDefinition>(parameterDefinitions.size()
                + parentParamDefs.size());
        for (ParameterDefinition def : parameterDefinitions) {
            names.add(def.getId());
            newParamDefs.add(def);
        }
        for (ParameterDefinition def : parentParamDefs) {
            if (names.contains(def.getId()))
                continue;
            newParamDefs.add(def);
        }
        paramDefsMerged = true;
        parameterDefinitions = Collections.unmodifiableList(newParamDefs);
        return parameterDefinitions;
    }
    
    /**
     * @see org.java.plugin.registry.ExtensionPoint#getParameterDefinition(
     *      java.lang.String)
     */
    public ParameterDefinition getParameterDefinition(final String id) {
        for (ParameterDefinition parameterDefinition : getParameterDefinitions()) {
            ParameterDefinitionImpl def = (ParameterDefinitionImpl) parameterDefinition;
            if (def.getId().equals(id)) {
                return def;
            }
        }
        throw new IllegalArgumentException("parameter definition with ID " + id //$NON-NLS-1$
            + " not found in extension point " + getUniqueId() //$NON-NLS-1$
            + " and all it parents"); //$NON-NLS-1$
    }

    /**
     * @see org.java.plugin.registry.ExtensionPoint#getParentPluginId()
     */
    public String getParentPluginId() {
        return model.getParentPluginId();
    }

    /**
     * @see org.java.plugin.registry.ExtensionPoint#getParentExtensionPointId()
     */
    public String getParentExtensionPointId() {
        return model.getParentPointId();
    }

    /**
     * @see org.java.plugin.registry.ExtensionPoint#isSuccessorOf(
     *      org.java.plugin.registry.ExtensionPoint)
     */
    public boolean isSuccessorOf(final ExtensionPoint extensionPoint) {
        if ((model.getParentPluginId() == null)
                || (model.getParentPointId() == null)) {
            return false;
        }
        if (model.getParentPluginId().equals(
                extensionPoint.getDeclaringPluginDescriptor().getId())
                && model.getParentPointId().equals(extensionPoint.getId())) {
            return true;
        }
        try {
            return getDeclaringPluginDescriptor().getRegistry()
                .getExtensionPoint(model.getParentPluginId(),
                        model.getParentPointId()).isSuccessorOf(extensionPoint);
        } catch (IllegalArgumentException iae) {
            return false;
        }
    }

    private void collectDescendants() {
        descendants = new LinkedList<ExtensionPoint>();
        for (PluginDescriptor descr : getDeclaringPluginDescriptor().getRegistry()
                .getPluginDescriptors()) {
            for (ExtensionPoint extp : descr.getExtensionPoints()) {
                if (extp.isSuccessorOf(this)) {
                    if (log.isDebugEnabled()) {
                        log.debug("extension point " + extp //$NON-NLS-1$
                                + " is descendant of point " + this); //$NON-NLS-1$
                    }
                    descendants.add(extp);
                }
            }
        }
        descendants = Collections.unmodifiableList(descendants);
    }

    /**
     * @see org.java.plugin.registry.ExtensionPoint#getDescendants()
     */
    public Collection<ExtensionPoint> getDescendants() {
        if (descendants == null) {
            collectDescendants();
        }
        return descendants;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "{ExtensionPoint: uid=" + getUniqueId() + "}"; //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    void registryChanged() {
        isValid = null;
        connectedExtensions = null;
        availableExtensions = null;
        descendants = null;
    }
    
    class ParameterDefinitionImpl extends PluginElementImpl<ParameterDefinition>
            implements ParameterDefinition {
        private List<ParameterDefinition> subDefinitions;
        private final ParameterDefinitionImpl superDefinition;
        private final ModelParameterDef modelParamDef;
        private final ParameterValueParser valueParser;

        ParameterDefinitionImpl(final ParameterDefinitionImpl aSuperDefinition,
                final ModelParameterDef aModel)
                throws ManifestProcessingException {
            super(ExtensionPointImpl.this.getDeclaringPluginDescriptor(),
                    ExtensionPointImpl.this.getDeclaringPluginFragment(),
                    aModel.getId(), aModel.getDocumentation());
            superDefinition = aSuperDefinition;
            modelParamDef = aModel;
            valueParser = new ParameterValueParser(
                    getDeclaringPluginDescriptor().getRegistry(), this,
                    modelParamDef.getDefaultValue());
            if (!valueParser.isParsingSucceeds()) {
                log.warn("parsing default value for parameter definition " //$NON-NLS-1$
                        + this + " failed, message is: " //$NON-NLS-1$
                        + valueParser.getParsingMessage());
                throw new ManifestProcessingException(
                        PluginRegistryImpl.PACKAGE_NAME,
                        "invalidDefaultValueAttribute", //$NON-NLS-1$
                        new Object[] {modelParamDef.getDefaultValue(),
                            ExtensionPointImpl.this.getId(),
                            ExtensionPointImpl.this
                                .getDeclaringPluginDescriptor().getId()});
            }
            if (ParameterType.ANY == modelParamDef.getType()) {
                subDefinitions = Collections.emptyList();
            } else {
                subDefinitions = new ArrayList<ParameterDefinition>(
                        modelParamDef.getParamDefs().size());
                final Set<String> names = new HashSet<String>();
                for (ModelParameterDef modelParameterDef : modelParamDef.getParamDefs()) {
                    ParameterDefinitionImpl def = new ParameterDefinitionImpl(this, modelParameterDef);
                    if (names.contains(def.getId())) {
                        throw new ManifestProcessingException(
                                PluginRegistryImpl.PACKAGE_NAME,
                                "duplicateParameterDefinition", //$NON-NLS-1$
                                new Object[] {def.getId(),
                                    ExtensionPointImpl.this.getId(),
                                    ExtensionPointImpl.this.
                                    getDeclaringPluginDescriptor().getId()});
                    }
                    names.add(def.getId());
                    subDefinitions.add(def);
                }
                subDefinitions = Collections.unmodifiableList(subDefinitions);
            }
            if (log.isDebugEnabled()) {
                log.debug("object instantiated: " + this); //$NON-NLS-1$
            }
        }
        
        ParameterValueParser getValueParser() {
            return valueParser;
        }
        
        /**
         * @see org.java.plugin.registry.ExtensionPoint.ParameterDefinition
         *      #getDeclaringExtensionPoint()
         */
        public ExtensionPoint getDeclaringExtensionPoint() {
            return ExtensionPointImpl.this;
        }

        /**
         * @see org.java.plugin.registry.ExtensionPoint.ParameterDefinition
         *      #getMultiplicity()
         */
        public ParameterMultiplicity getMultiplicity() {
            return modelParamDef.getMultiplicity();
        }

        /**
         * @see org.java.plugin.registry.ExtensionPoint.ParameterDefinition
         *      #getSubDefinitions()
         */
        public Collection<ParameterDefinition> getSubDefinitions() {
            return subDefinitions;
        }

        /**
         * @see org.java.plugin.registry.ExtensionPoint.ParameterDefinition
         *      #getSuperDefinition()
         */
        public ParameterDefinition getSuperDefinition() {
            return superDefinition;
        }
        
        /**
         * @see org.java.plugin.registry.ExtensionPoint.ParameterDefinition
         *      #getSubDefinition(java.lang.String)
         */
        public ParameterDefinition getSubDefinition(final String id) {
            for (ParameterDefinition parameterDefinition : subDefinitions) {
                ParameterDefinitionImpl def = (ParameterDefinitionImpl) parameterDefinition;
                if (def.getId().equals(id)) {
                    return def;
                }
            }
            throw new IllegalArgumentException(
                    "parameter definition with ID " + id //$NON-NLS-1$
                    + " not found in extension point " + getUniqueId()); //$NON-NLS-1$
        }

        /**
         * @see org.java.plugin.registry.ExtensionPoint.ParameterDefinition#getType()
         */
        public ParameterType getType() {
            return modelParamDef.getType();
        }

        /**
         * @see org.java.plugin.registry.ExtensionPoint.ParameterDefinition
         *      #getCustomData()
         */
        public String getCustomData() {
            return modelParamDef.getCustomData();
        }
        
        /**
         * @see org.java.plugin.registry.ExtensionPoint.ParameterDefinition
         *      #getDefaultValue()
         */
        public String getDefaultValue() {
            return modelParamDef.getDefaultValue();
        }
        
        /**
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return "{PluginExtensionPoint.ParameterDefinition: extPointUid=" //$NON-NLS-1$
                + getDeclaringExtensionPoint().getUniqueId() + "; id=" + getId() //$NON-NLS-1$
                + "}"; //$NON-NLS-1$
        }
        
        /**
         * @see org.java.plugin.registry.xml.IdentityImpl#isEqualTo(
         *      org.java.plugin.registry.Identity)
         */
        @Override
        protected boolean isEqualTo(final Identity idt) {
            if (!super.isEqualTo(idt)) {
                return false;
            }
            ParameterDefinitionImpl other = (ParameterDefinitionImpl) idt;
            if ((getSuperDefinition() == null)
                    && (other.getSuperDefinition() == null)) {
                return true;
            }
            if ((getSuperDefinition() == null)
                    || (other.getSuperDefinition() == null)) {
                return false;
            }
            return getSuperDefinition().equals(other.getSuperDefinition());
        }
    }
}
