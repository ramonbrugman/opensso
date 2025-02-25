/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2007 Sun Microsystems Inc. All Rights Reserved
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * https://opensso.dev.java.net/public/CDDLv1.0.html or
 * opensso/legal/CDDLv1.0.txt
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at opensso/legal/CDDLv1.0.txt.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * $Id: AuthPropertiesModel.java,v 1.2 2008-06-25 05:42:46 qcheng Exp $
 *
 */

package com.sun.identity.console.authentication.model;

import com.sun.identity.console.base.model.AMConsoleException;
import com.sun.identity.console.base.model.AMModel;
import java.util.Map;
import java.util.Set;

/* - NEED NOT LOG - */

public interface AuthPropertiesModel extends AMModel
{
    /**
     * Returns all the values for the core authentication service. 
     *
     * @return Map of attribute values. Returns the EMPTY_SET if
     * the values could not be retrieved.
     */
    public Map getValues() throws AMConsoleException;

    /** 
     * @return map of auth-type/localized-auth-type pairs
     */
    public Map getAuthTypes();

    public void removeAuthInstance(Set names)
        throws AMConsoleException ;
        
    public void createAuthInstance(String name, String type) 
        throws AMConsoleException ;

    /**
    * Sets the specified attribute values in the service configuration. 
    * The service name is defined in the subclass. Each attribute passed
    * will be set even if the value has not changed. It is the responsibility 
    * of the invoking view bean to pass only those values that are to be set.
    * Each attribute will also cause one log record to be written for each
    * successful set operation.
    *
    * @param modifiedValues that will be set in the service configuration data.
    * @throws AMConsoleException on any error condition.
    */
    public void setValues(Map modifiedValues) throws AMConsoleException;   

    public Set getAuthInstances();

    public String getServiceName(String type);

    public Map getInstanceValues(String name);

    public void setInstanceValues(String name, Map value)
        throws AMConsoleException;

    public void setCurrentRealm(String realm);

    /**
     * Returns true if there are attributes for a authentication type.
     *
     * @param type Authtentication type.
     * @return true if there are attributes for a authentication type.
     */
    boolean hasAuthAttributes(String type);
}
