/*
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
 * $Id: StsManageServiceSecuritySummary.java,v 1.3 2009-10-21 16:46:04 ggennaro Exp $
 */

package com.sun.identity.admin.model;

import java.util.ArrayList;

import com.sun.identity.admin.ListFormatter;
import com.sun.identity.admin.Resources;

public class StsManageServiceSecuritySummary extends StsManageWizardSummary {

    public StsManageServiceSecuritySummary(StsManageWizardBean wizardBean) {
        super(wizardBean);
    }

    @Override
    public String getLabel() {
        Resources r = new Resources();
        String label = r.getString(this, "label");
        return label;
    }

    @Override
    public String getValue() {
        StsManageWizardBean wizardBean = getStsManageWizardBean();
        StsProfileBean profileBean = wizardBean.getStsProfileBean();
        ArrayList<String> a = new ArrayList<String>();
        
        for(SecurityMechanismPanelBean panelBean 
                : profileBean.getSecurityMechanismPanels() ) {
            
            if( panelBean.isChecked() ) {
                a.add(" " + panelBean.getSecurityMechanism().toLocaleString());
            }
        }

        ListFormatter lf = new ListFormatter(a);
        return lf.toString();
   }

    @Override
    public String getTemplate() {
        return null;
    }

    @Override
    public String getIcon() {
        return "../image/edit.png";
    }

    @Override
    public boolean isExpandable() {
        return false;
    }

    @Override
    public int getGotoStep() {
        return StsManageWizardStep.SECURITY.toInt();
    }

}
