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
 * $Id: SubRealmGroupTest.java,v 1.3 2009-11-19 01:02:04 veiming Exp $
 */

package com.sun.identity.entitlement;

import com.iplanet.sso.SSOException;
import com.iplanet.sso.SSOToken;
import com.sun.identity.authentication.internal.server.AuthSPrincipal;
import com.sun.identity.entitlement.opensso.OpenSSOGroupSubject;
import com.sun.identity.entitlement.opensso.SubjectUtils;
import com.sun.identity.entitlement.util.IdRepoUtils;
import com.sun.identity.idm.AMIdentity;
import com.sun.identity.idm.IdRepoException;
import com.sun.identity.security.AdminTokenAction;
import com.sun.identity.sm.OrganizationConfigManager;
import com.sun.identity.sm.SMSException;
import java.security.AccessController;
import java.security.Principal;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.security.auth.Subject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class SubRealmGroupTest {
    private static final String APPL_NAME = "SubRealmGroupAppl";
    private static final String SUB_REALM1 = "/SubRealmGroupTest1";
    private static final String SUB_REALM2 = "/SubRealmGroupTest2";
    private static final String REFERRAL1_NAME = "subRealmGroupReferral1";
    private static final String REFERRAL2_NAME = "subRealmGroupReferral2";
    private static final String PRIVILEGE1_NAME = "subRealmGroupPrivilege1";
    private static final String PRIVILEGE2_NAME = "subRealmGroupPrivilege2";
    private static final String USER1_NAME = "subRealmGroupTestUser1";
    private static final String GROUP1_NAME = "subRealmGroupTestGroup1";
    private static final String RESOURCE1 = "http://www.subrealmgrouptest1.com:80/*";
    private static final String RESOURCE2 = "http://www.subrealmgrouptest2.com:80/*";
    private static final String URL1 = "http://www.subrealmgrouptest1.com:80/private";
    private static final String URL2 = "http://www.subrealmgrouptest2.com:80/private";

    private AMIdentity user1;
    private AMIdentity group1;
    private Subject adminSubject;
    private String origGroupMembershipSearchIndexEnabled = null;

    @BeforeClass
    public void setup() throws Exception {
        SSOToken adminToken = (SSOToken) AccessController.doPrivileged(
            AdminTokenAction.getInstance());
        adminSubject = SubjectUtils.createSubject(adminToken);

        Application appl = new Application("/", APPL_NAME,
            ApplicationTypeManager.getAppplicationType(adminSubject,
            ApplicationTypeManager.URL_APPLICATION_TYPE_NAME));

        Set<String> avaliableResources = new HashSet<String>();
        avaliableResources.add(RESOURCE1);
        avaliableResources.add(RESOURCE2);
        appl.addResources(avaliableResources);
        appl.setEntitlementCombiner(DenyOverride.class);
        ApplicationManager.saveApplication(adminSubject, "/", appl);

        user1 = IdRepoUtils.createUser("/", USER1_NAME);
        group1 = IdRepoUtils.createGroup("/", GROUP1_NAME);
        group1.addMember(user1);

        EntitlementConfiguration ec = EntitlementConfiguration.getInstance(
            adminSubject, "/");

        Map<String, Set<String>> saccMap =
            ec.getSubjectAttributesCollectorConfiguration("OpenSSO");
        Set<String> tmpSet = saccMap.get("groupMembershipSearchIndexEnabled");
        origGroupMembershipSearchIndexEnabled = tmpSet.iterator().next();
        tmpSet.clear();
        tmpSet.add("true");
        ec.setSubjectAttributesCollectorConfiguration("OpenSSO", saccMap);

        createReferral(adminToken, adminSubject, SUB_REALM1, REFERRAL1_NAME,
            PRIVILEGE1_NAME, RESOURCE1, URL1);

        tmpSet.clear();
        tmpSet.add("false");
        ec.setSubjectAttributesCollectorConfiguration("OpenSSO", saccMap);

        createReferral(adminToken, adminSubject, SUB_REALM2, REFERRAL2_NAME,
            PRIVILEGE2_NAME, RESOURCE2, URL2);
    }

    private void createReferral(SSOToken adminToken, Subject adminSubject,
        String subRealm, String referralName, String privilegeName,
        String resource, String url)
        throws SMSException, EntitlementException, SSOException,
            IdRepoException, InterruptedException {

        OrganizationConfigManager orgMgr = new OrganizationConfigManager(
            adminToken, "/");
        orgMgr.createSubOrganization(subRealm.substring(1),
            Collections.EMPTY_MAP);

        Map<String, Set<String>> map = new HashMap<String, Set<String>>();
        Set<String> set = new HashSet<String>();
        map.put(APPL_NAME, set);
        set.add(resource);

        Set<String> realms = new HashSet<String>();
        realms.add(subRealm);

        ReferralPrivilege referral1 =
            new ReferralPrivilege(referralName, map, realms);
        ReferralPrivilegeManager mgr = new ReferralPrivilegeManager("/",
            adminSubject);
        mgr.add(referral1);

        PrivilegeManager pm = PrivilegeManager.getInstance(subRealm,
            adminSubject);
        Map<String, Boolean> actions = new HashMap<String, Boolean>();
        actions.put("GET", Boolean.TRUE);
        Entitlement ent = new Entitlement(APPL_NAME, url, actions);

        Set<EntitlementSubject> esSet = new HashSet<EntitlementSubject>();
        EntitlementSubject es1 = new OpenSSOGroupSubject(
            group1.getUniversalId());
        esSet.add(es1);

        EntitlementSubject eSubject = new OrSubject(esSet);
        Privilege privilege = Privilege.getNewInstance();
        privilege.setName(privilegeName);
        privilege.setEntitlement(ent);
        privilege.setSubject(eSubject);
        pm.addPrivilege(privilege);
    }

    @AfterClass
    public void cleanup() throws Exception {
        ApplicationManager.deleteApplication(adminSubject, "/", APPL_NAME);
    }

    private void removeOrganization() throws Exception {
        SSOToken adminToken = (SSOToken) AccessController.doPrivileged(
            AdminTokenAction.getInstance());
        ReferralPrivilegeManager mgr = new ReferralPrivilegeManager("/",
            adminSubject);
        mgr.delete(REFERRAL1_NAME);
        mgr.delete(REFERRAL2_NAME);

        Set<AMIdentity> identities = new HashSet<AMIdentity>();
        identities.add(user1);
        identities.add(group1);
        IdRepoUtils.deleteIdentities("/", identities);

        OrganizationConfigManager orgMgr = new OrganizationConfigManager(
            adminToken, "/");
        orgMgr.deleteSubOrganization(SUB_REALM1, true);
        orgMgr.deleteSubOrganization(SUB_REALM2, true);

        EntitlementConfiguration ec = EntitlementConfiguration.getInstance(
            adminSubject, "/");

        Map<String, Set<String>> saccMap =
            ec.getSubjectAttributesCollectorConfiguration("OpenSSO");
        Set<String> tmpSet = saccMap.get("groupMembershipSearchIndexEnabled");
        tmpSet.clear();
        tmpSet.add(origGroupMembershipSearchIndexEnabled);
        ec.setSubjectAttributesCollectorConfiguration("OpenSSO", saccMap);

    }

    @Test
    public void positiveTest()
        throws Exception {
        Thread.sleep(1000);
        if (!evaluate(URL1)) {
            throw new Exception("SubRealmGroupTest.postiveTest failed");
        }
        if (!evaluate(URL2)) {
            throw new Exception("SubRealmGroupTest.postiveTest failed");
        }
    }

    @Test(dependsOnMethods = {"positiveTest"})
    public void negativeTest()
        throws Exception {
        removeOrganization();
        Thread.sleep(1000);

        //this should return false since privileges are already deleted.
        try {
            if (evaluate(URL1)) {
                throw new Exception("SubRealmGroupTest.negativeTest failed");
            }
        } catch (EntitlementException e) {
            if (e.getErrorCode() != 248) {
                throw e;
            }
        }
        //this should return false since privileges are already deleted.
        try {
            if (evaluate(URL2)) {
                throw new Exception("SubRealmGroupTest.negativeTest failed");
            }
        } catch (EntitlementException e) {
            if (e.getErrorCode() != 248) {
                throw e;
            }
        }
    }

    private boolean evaluate(String res)
        throws EntitlementException {
        Subject subject = createSubject(user1.getUniversalId());
        Set actions = new HashSet();
        actions.add("GET");
        SSOToken adminToken = (SSOToken) AccessController.doPrivileged(
            AdminTokenAction.getInstance());
        Evaluator evaluator = new Evaluator(
            SubjectUtils.createSubject(adminToken), APPL_NAME);
        return evaluator.hasEntitlement("/", subject,
            new Entitlement(res, actions), Collections.EMPTY_MAP);
    }

    public static Subject createSubject(String uuid) {
        Set<Principal> userPrincipals = new HashSet<Principal>(2);
        userPrincipals.add(new AuthSPrincipal(uuid));
        return new Subject(false, userPrincipals, new HashSet(),
            new HashSet());
    }

}
