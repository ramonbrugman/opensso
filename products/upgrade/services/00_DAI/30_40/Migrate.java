/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2008 Sun Microsystems Inc. All Rights Reserved
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
 * $Id: Migrate.java,v 1.2 2008-06-25 05:53:12 qcheng Exp $
 *
 */

import com.sun.identity.upgrade.MigrateTasks;
import com.sun.identity.upgrade.UpgradeException;
import com.sun.identity.upgrade.UpgradeUtils;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Migration for the DAI Service.
 * This class is invoked during migration from older versions
 * of Access Manager to the latest version.
 */
public class Migrate implements MigrateTasks {

    static final String SERVICE_NAME = "DAI";
    static final String SERVICE_DIR = "00_DAI/30_40/";
    static final String LDIF_FILE = "DAI_ds_remote_schema.ldif";
    static final String SUBCONFIG_NAME = 
            "/templates/CreationTemplates/BasicUser";
    static final String ATTR_NAME = "required";
    static final String ATTR_VAL1 = "objectClass=sunFederationManagerDataStore";
    static final String ATTR_VAL2 =
            "objectClass=sunIdentityServerLibertyPPService";
    static final String ATTR_VAL3 =
            "objectClass=sunFMSAML2NameIdentifier";

    /**
     * Loads the ldif and service changes for the DAI Service
     *
     * @return true if successful otherwise false.
     */
    public boolean migrateService() {
        boolean isSuccess = false;
        try {
            //load ldif file 
            String ldifPath =
                    UpgradeUtils.getAbsolutePath(SERVICE_DIR, LDIF_FILE);
            UpgradeUtils.loadLdif(ldifPath);
            Map attrValues = new HashMap();
            HashSet attrValSet = new HashSet();
            attrValSet.add(ATTR_VAL1);
            attrValSet.add(ATTR_VAL2);
            attrValSet.add(ATTR_VAL3);

            UpgradeUtils.addAttributeToSubConfiguration(
                    SERVICE_NAME, SUBCONFIG_NAME, attrValues);
            isSuccess = true;
        } catch (UpgradeException e) {
            UpgradeUtils.debug.error(SERVICE_DIR + "Error loading data :" +
                    SERVICE_NAME + e.getMessage());
        }
        return isSuccess;
    }

    /**
     * Post Migration operations.
     *
     * @return true if successful else error.
     */
    public boolean postMigrateTask() {
        return true;
    }

    /**
     * Pre Migration operations.
     *
     * @return true if successful else error.
     */
    public boolean preMigrateTask() {
        return true;
    }
}
