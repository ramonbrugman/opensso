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
 * $Id: ViewFilter.java,v 1.2 2009-09-30 22:53:35 farble1670 Exp $
 */

package com.sun.identity.admin.model;

import com.sun.identity.admin.Resources;
import com.sun.identity.entitlement.util.SearchFilter;
import java.io.Serializable;
import java.util.List;

public abstract class ViewFilter implements Serializable {
    private String name;
    private ViewFilterType viewFilterType;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ViewFilterType getViewFilterType() {
        return viewFilterType;
    }

    public void setViewFilterType(ViewFilterType viewFilterType) {
        this.viewFilterType = viewFilterType;
    }

    public String getTitle() {
        Resources r = new Resources();
        String title = r.getString(this, "title");
        if (title != null) {
            return title;
        }
        return getName();
    }

    public abstract List<SearchFilter> getSearchFilters();
}
