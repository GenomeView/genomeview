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
package org.java.plugin.registry;

import java.util.Collection;
import java.util.Locale;


/**
 * Result of validation performed by registry on all registered plug-ins. This
 * includes dependencies check, parameters check (against parameter definitions)
 * and any other kind of validation.
 * 
 * @version $Id$
 */
public interface IntegrityCheckReport {
    /**
     * @return number of items with severity {@link Severity#ERROR}
     *         in this report
     */
    int countErrors();
    
    /**
     * @return number of items with severity {@link Severity#WARNING}
     *         in this report
     */
    int countWarnings();
    
    /**
     * @return collection of {@link ReportItem} objects
     */
    Collection<ReportItem> getItems();
    
    /**
     * Integrity check report item severity constants.
     * 
     * @version $Id$
     */
    enum Severity {
        /**
         * Integrity check report item severity constant.
         */
        ERROR,
        
        /**
         * Integrity check report item severity constant.
         */
        WARNING,
        
        /**
         * Integrity check report item severity constant.
         */
        INFO
    }
    
    /**
     * Integrity check error constants.
     *
     * @version $Id$
     */
    enum Error {
        /**
         * Integrity check error constant.
         */
        NO_ERROR,
        
        /**
         * Integrity check error constant.
         */
        CHECKER_FAULT,
        
        /**
         * Integrity check error constant.
         */
        MANIFEST_PROCESSING_FAILED,
        
        /**
         * Integrity check error constant.
         */
        UNSATISFIED_PREREQUISITE,
        
        /**
         * Integrity check error constant.
         */
        BAD_LIBRARY,
        
        /**
         * Integrity check error constant.
         */
        INVALID_EXTENSION_POINT,
        
        /**
         * Integrity check error constant.
         */
        INVALID_EXTENSION
    }
    
    /**
     * Integrity check report element. Holds all information about particular
     * check event.
     * @version $Id$
     */
    interface ReportItem {
        /**
         * @return severity code for this report item
         */
        Severity getSeverity();
        
        /**
         * @return source for this report item, can be <code>null</code>
         */
        Identity getSource();
        
        /**
         * @return error code for this report item
         */
        Error getCode();
        
        /**
         * @return message, associated with this report item for the system
         *         default locale
         */
        String getMessage();
        
        /**
         * @param locale locale to get message for
         * @return message, associated with this report item for given locale
         */
        String getMessage(Locale locale);
    }
}
