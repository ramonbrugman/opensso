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
 * $Id: ResourcesDelegationSummary.java,v 1.2 2009-10-30 17:33:54 farble1670 Exp $
 */

package com.sun.identity.admin.model;

import com.sun.identity.admin.Resources;
import java.util.List;

public class ResourcesDelegationSummary extends DelegationSummary {

    public ResourcesDelegationSummary(DelegationWizardBean wizardBean) {
        super(wizardBean);
    }

    public String getLabel() {
        Resources r = new Resources();
        String label = r.getString(this, "label");
        return label;
    }

    private int getSize() {
        List<Resource> resources = getDelegationWizardBean().getDelegationBean().getResources();
        if (resources == null) {
            return 0;
        }

        return resources.size();
    }

    public String getValue() {
        return Integer.toString(getSize());
    }

    public boolean isExpandable() {
        return getSize() > 0;

    }
    public String getIcon() {
        return "../image/application.png";
    }

    @Override
    public String getTemplate() {
        return "/admin/facelet/template/delegation-summary-resources.xhtml";
    }

    public int getGotoStep() {
        return DelegationWizardStep.RESOURCES.toInt();
    }

}
