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
 * $Id: AttributeSubject.java,v 1.4 2009-07-02 15:53:15 veiming Exp $
 */
package com.sun.identity.entitlement;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.security.auth.Subject;

public class AttributeSubject extends EntitlementSubjectImpl {
    /**
     * Constructor.
     */
    public AttributeSubject() {
        super();
    }

    /**
     * Constructor.
     *
     * @param name attribute name
     * @param value attibute value
     */
    public AttributeSubject(String name, String value) {
        super(name, value);
    }

    /**
     * Returns attibute value.
     *
     * @return attibute value
     */
    public String getValue() {
        return getPSubjectName();
    }

    /**
     * Sets attibute value.
     *
     * @param value attibute value
     */
    public void setValue(String value) {
        setPSubjectName(value);
    }

    /**
     * Returns <code>SubjectDecision</code> of
     * <code>EntitlementSubject</code> evaluation
     *
     * @param realm Realm name.
     * @param subject EntitlementSubject who is under evaluation.
     * @param resourceName Resource name.
     * @param environment Environment parameters.
     * @return <code>SubjectDecision</code> of
     * <code>EntitlementSubject</code> evaluation
     * @throws com.sun.identity.entitlement,  EntitlementException in case
     * of any error
     */
    public SubjectDecision evaluate(
        String realm,
        SubjectAttributesManager mgr,
        Subject subject,
        String resourceName,
        Map<String, Set<String>> environment)
        throws EntitlementException {

        boolean satified = false;
        Set publicCreds = subject.getPublicCredentials();
        if ((publicCreds != null) && !publicCreds.isEmpty()) {
            Map<String, Set<String>> attributes = (Map<String, Set<String>>)
                publicCreds.iterator().next();
            Set<String> values = attributes.get(
                SubjectAttributesCollector.NAMESPACE_ATTR + getID());
            satified = (values != null) ? values.contains(getValue()) : false;
        }
        satified = satified ^ isExclusive();
        return new SubjectDecision(satified, Collections.EMPTY_MAP);
    }

    /**
     * Returns search index attributes.
     *
     * @return search index attributes.
     */
    public Map<String, Set<String>> getSearchIndexAttributes() {
        Map<String, Set<String>> map = new HashMap<String, Set<String>>(4);
        Set<String> set = new HashSet<String>();
        set.add(getValue());
        map.put(SubjectAttributesCollector.NAMESPACE_ATTR + getID(), set);
        
        return map;
    }

    /**
     * Returns required attribute names.
     * 
     * @return required attribute names.
     */
    public Set<String> getRequiredAttributeNames() {
        Set<String> set = new HashSet<String>(2);
        set.add(SubjectAttributesCollector.NAMESPACE_ATTR + getID());
        return set;
    }

    /**
     * Returns <code>true</code> is this subject is an identity object.
     *
     * @return <code>true</code> is this subject is an identity object.
     */
    public boolean isIdentity() {
        return true;
    }
}
