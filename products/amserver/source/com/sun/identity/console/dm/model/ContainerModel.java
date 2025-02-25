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
 * $Id: ContainerModel.java,v 1.2 2008-06-25 05:42:56 qcheng Exp $
 *
 */

package com.sun.identity.console.dm.model;

import com.sun.identity.console.base.model.AMConsoleException;
import java.util.Set;
import java.util.Map;

/* - NEED NOT LOG - */

/**
 * <code>ContainerModel</code> defines a set of methods required by container
 * navigation view bean.
 */
public interface ContainerModel
    extends DMModel
{
    /**
     * Gets the containers based on the specified filter and location.
     *
     * @param location Search filter for fetching containers.
     * @param filter used to search for containers.
     * @return Set of containers.
     */
    public Set getContainers(String location, String filter);
    
    /**
     * Returns Map of attribute name to empty set of values.
     *
     * @return attribute values.
     */
    public Map getDataMap();

    /**
     * Returns container creation property XML.
     *
     * @return container creation property XML.
     */
    public String getCreateContainerXML();

    /**
     * Creates a new container entry.
     *
     * @param location Name of the parent entry where container will be created.
     * @param data Map of attribute values used to create container.
     *
     * @throws AMConsoleException if container cannot be created.
     */
    public void createContainer(String location, Map data) 
        throws AMConsoleException;
    
    /**
     * Returns true if the container has properties to display.
     *
     * @return true if there are properties to display, false otherwise.
     */
    public boolean hasDisplayProperties();
    
    public Map getAssignedServices(String location);
}
