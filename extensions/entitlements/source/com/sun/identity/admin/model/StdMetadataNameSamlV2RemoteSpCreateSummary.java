/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009 Sun Microsystems Inc. All Rights Reserved
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
 * $Id: StdMetadataNameSamlV2RemoteSpCreateSummary.java,v 1.3 2009-06-27 01:52:14 asyhuang Exp $
 */

package com.sun.identity.admin.model;

import com.sun.identity.admin.Resources;

public class StdMetadataNameSamlV2RemoteSpCreateSummary extends SamlV2RemoteSpCreateSummary {

    public StdMetadataNameSamlV2RemoteSpCreateSummary(SamlV2RemoteSpCreateWizardBean samlV2RemoteSpCreateWizardBean) {
        super(samlV2RemoteSpCreateWizardBean);
    }

    public String getLabel() {
        Resources r = new Resources();
        String label = r.getString(this, "label");
        return label;
    }

    public String getValue() {
        String filename;
        boolean hasMeta = getSamlV2RemoteSpCreateWizardBean().isMeta();
        if(hasMeta){
            filename = getSamlV2RemoteSpCreateWizardBean().getStdMetaFilename();
        } else {
            filename = new String();
        }

        return filename;
    }

    public boolean isExpandable() {
        return false;
    }

    public String getIcon() {
        return "../image/xml.png";
    }

    public String getTemplate() {
        return null;
    }

    public int getGotoStep() {
        return SamlV2RemoteSpCreateWizardStep.METADATA.toInt();
    }
}