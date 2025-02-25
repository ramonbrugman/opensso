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
 * $Id: MAPDeviceProfileModel.java,v 1.2 2008-06-25 05:43:18 qcheng Exp $
 *
 */

package com.sun.identity.console.service.model;

import com.sun.identity.console.base.model.AMConsoleException;
import com.sun.identity.console.base.model.AMModel;
import java.util.Map;
import java.util.Set;

/* - NEED NOT LOG - */

public interface MAPDeviceProfileModel extends AMModel {
    /**
     * Default classification.
     */
    String DEFAULT_CLASSIFICATION = "generalPropertyNames";

    /**
     * Returns an array of classifications for device attributes.
     *
     * @param clientType Client Type
     * @return an array of classifications for device attributes.
     */
    public String[] getAttributeClassification(String clientType);

    /**
     * Returns localized labels for device attribute classification.
     *
     * @param classifications Array of classifications for device attributes.
     * @return localized labels for device attribute classification.
     */
    public Map getLocalizedClassificationLabels(String[] classifications);

    /**
     * Returns the property XML for profile view.
     *
     * @param clientType Client Type.
     * @param classification Device attribute classification.
     * @throws AMConsoleException if there are no attribute to display.
     * @return the property XML for profile view.
     */
    public String getProfilePropertyXML(
        String clientType,
        String classification)
        throws AMConsoleException;

    /**
     * Returns attribute values of a device.
     *
     * @param clientType Client Type.
     * @param classification Profile attribute classification.
     * @return attribute values of a device.
     */
    public Map getAttributeValues(String clientType, String classification);

    /**
     * Returns readonly attribute names.
     *
     * @param clientType Client Type.
     * @param attributeNames Attribute Names
     * @return readonly attribute names.
     */
    public Set getReadOnlyAttributeNames(String clientType, Set attributeNames);

    /**
     * Modifies device profile.
     *
     * @param clientType Client Type.
     * @param attributeValues Map of attribute name to set of attribute values.
     * @throws AMConsoleException if updates fails.
     */
    public void modifyProfile(String clientType, Map attributeValues)
        throws AMConsoleException;
}
