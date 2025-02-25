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
 * $Id: PatternViewFilter.java,v 1.2 2009-09-30 22:53:35 farble1670 Exp $
 */

package com.sun.identity.admin.model;

import com.sun.identity.entitlement.util.SearchFilter;
import java.util.Collections;
import java.util.List;

public abstract class PatternViewFilter extends ViewFilter {
    private String filter;

    public abstract String getPrivilegeAttributeName();

    private String getPattern(String filter) {
        String pattern;
        if (filter == null || filter.length() == 0) {
            pattern = "*";
        } else {
            pattern = "*" + filter + "*";
        }

        return pattern;
    }

    public List<SearchFilter> getSearchFilters() {
        String pattern = getPattern(getFilter());
        SearchFilter psf = new SearchFilter(getPrivilegeAttributeName(), pattern);
        return Collections.singletonList(psf);
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }
}
