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
 * $Id: DnsNameViewCondition.java,v 1.1 2009-08-19 05:40:50 veiming Exp $
 */

package com.sun.identity.admin.model;

import com.sun.identity.entitlement.EntitlementCondition;
import com.sun.identity.entitlement.DNSNameCondition;
import java.io.Serializable;

public class DnsNameViewCondition
    extends ViewCondition
    implements Serializable {

    private String domainNameMask;

    public EntitlementCondition getEntitlementCondition() {
        DNSNameCondition dnsNameCondition = new DNSNameCondition();
        dnsNameCondition.setDisplayType(getConditionType().getName());
        dnsNameCondition.setDomainNameMask(getDomainNameMask());

        return dnsNameCondition;
    }

    public String getDomainNameMask() {
        return domainNameMask;
    }

    public void setDomainNameMask(String domainNameMask) {
        this.domainNameMask = domainNameMask;
    }

    @Override
    public String toString() {
        return getTitle() + ":{" + domainNameMask + "}";
    }
}
