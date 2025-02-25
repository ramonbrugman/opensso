/* The contents of this file are subject to the terms
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
 * $Id: SAMLv2AutoFederationTests.java,v 1.10 2009-01-27 00:14:08 nithyas Exp $
 *
 * Copyright 2007 Sun Microsystems Inc. All Rights Reserved
 */

package com.sun.identity.qatest.samlv2;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.sun.identity.qatest.common.FederationManager;
import com.sun.identity.qatest.common.MultiProtocolCommon;
import com.sun.identity.qatest.common.SAMLv2Common;
import com.sun.identity.qatest.common.TestCommon;
import com.sun.identity.qatest.common.TestConstants;
import com.sun.identity.qatest.common.webtest.DefaultTaskHandler;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * This class tests the following: 
 * 1. First it sets the autofederation attribute to true. 
 * Sets the autofederation attribute & attribute map in the sp & idp extended 
 * metadata. It creates the users with same mail address. 
 * 2. Tests cover sp init SSO
 * 3. SP init SSO with POST/SOAP profiles.
 * 4. IDP init SSO
 * 5. IDP init SSO with POST/SOAP profiles. 
 */
public class SAMLv2AutoFederationTests extends TestCommon {
    
    public WebClient webClient;
    private DefaultTaskHandler task;
    private Map<String, String> configMap;
    private Map<String, String> usersMap;
    ArrayList spuserlist = new ArrayList();
    ArrayList idpuserlist = new ArrayList();
    private String  baseDir;
    private HtmlPage page;
    private URL url;
    private String spmetadata;
    private String idpmetadata;
    private String AUTO_FED_ENABLED_FALSE = "<Attribute name=\""
            + "autofedEnabled" + "\">\n"
            + "            <Value>false</Value>\n"
            + "        </Attribute>\n";
    private  String AUTO_FED_ENABLED_TRUE = "<Attribute name=\""
            + "autofedEnabled" + "\">\n"
            + "            <Value>true</Value>\n"
            + "        </Attribute>\n";
    
    private String AUTO_FED_ATTRIB_DEFAULT = "<Attribute name=\""
            + "autofedAttribute\">\n"
            + "            <Value/>\n"
            + "        </Attribute>";
    private String AUTO_FED_ATTRIB_VALUE = "<Attribute name=\""
            + "autofedAttribute\">\n"
            + "            <Value>mail</Value>\n"
            + "        </Attribute>";
    
    private String ATTRIB_MAP_DEFAULT = "<Attribute name=\""
            + "attributeMap\"/>\n";
    private String ATTRIB_MAP_VALUE = "<Attribute name=\""
            + "attributeMap\">\n"
            + "            <Value>mail=mail</Value>\n"
            + "        </Attribute>";
    
    
    /** Creates a new instance of SAMLv2AutoFederation */
    public SAMLv2AutoFederationTests() {
        super("SAMLv2AutoFederation");
    }
    
    /**
     * Create the webClient which should be run before each test.
     */
    @BeforeMethod(groups={"ldapv3", "ldapv3_sec", "s1ds", "s1ds_sec", "ad", "ad_sec", "amsdk", "amsdk_sec", "jdbc", "jdbc_sec"})
    private void getWebClient() 
    throws Exception {
        try {
            webClient = new WebClient(BrowserVersion.MOZILLA_1_0);
        } catch (Exception e) {
            log(Level.SEVERE, "getWebClient", e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    /**
     * This is setup method. It creates required users for test
     */
    @BeforeClass(groups={"ldapv3", "ldapv3_sec", "s1ds", "s1ds_sec", "ad", 
      "ad_sec", "amsdk", "amsdk_sec", "jdbc", "jdbc_sec"})
    public void setup() 
    throws Exception {
        List<String> list;
        String spurl;
        String idpurl;
        try {
            ResourceBundle rb_amconfig = ResourceBundle.getBundle(
                    TestConstants.TEST_PROPERTY_AMCONFIG);
            baseDir = getBaseDir() + System.getProperty("file.separator")
                    + rb_amconfig.getString(TestConstants.KEY_ATT_SERVER_NAME)
                    + System.getProperty("file.separator") + "built"
                    + System.getProperty("file.separator") + "classes"
                    + System.getProperty("file.separator");
            //Upload global properties file in configMap
            configMap = new HashMap<String, String>();
            configMap = getMapFromResourceBundle("samlv2" + fileseparator +
                    "samlv2TestConfigData");
            configMap.putAll(getMapFromResourceBundle("samlv2" + fileseparator
                    + "samlv2TestData"));
            log(Level.FINEST, "setup: Config Map is ", configMap);
            spurl = configMap.get(TestConstants.KEY_SP_PROTOCOL) +
                    "://" + configMap.get(TestConstants.KEY_SP_HOST) + ":" +
                    configMap.get(TestConstants.KEY_SP_PORT) +
                    configMap.get(TestConstants.KEY_SP_DEPLOYMENT_URI);
            idpurl = configMap.get(TestConstants.KEY_IDP_PROTOCOL) +
                    "://" + configMap.get(TestConstants.KEY_IDP_HOST) + ":" +
                    configMap.get(TestConstants.KEY_IDP_PORT) +
                    configMap.get(TestConstants.KEY_IDP_DEPLOYMENT_URI);
            } catch (Exception e) {
                log(Level.SEVERE, "setup", e.getMessage());
                e.printStackTrace();
                throw e;
            }
        try {
            getWebClient();
            list = new ArrayList();
            consoleLogin(webClient, spurl + "/UI/Login",
                    configMap.get(TestConstants.KEY_SP_AMADMIN_USER),
                    configMap.get(TestConstants.KEY_SP_AMADMIN_PASSWORD));
            
            FederationManager fmSP = new FederationManager(spurl);
            consoleLogin(webClient, idpurl + "/UI/Login", configMap.get(
                    TestConstants.KEY_IDP_AMADMIN_USER),
                    configMap.get(TestConstants.KEY_IDP_AMADMIN_PASSWORD));
            FederationManager fmIDP = new FederationManager(idpurl);
            
            usersMap = new HashMap<String, String>();
            usersMap = getMapFromResourceBundle("samlv2" + fileseparator +
                    "samlv2autofederationtests");
            Integer totalUsers = new Integer(
                    (String)usersMap.get("totalUsers"));
            for (int i = 1; i < totalUsers + 1; i++) {
                //create sp user first
                list.clear();
                list.add("mail=" + usersMap.get(TestConstants.KEY_SP_USER_MAIL +
                        i));
                list.add("sn=" + usersMap.get(TestConstants.KEY_SP_USER + i));
                list.add("cn=" + usersMap.get(TestConstants.KEY_SP_USER + i));
                list.add("userpassword=" + usersMap.get(
                        TestConstants.KEY_SP_USER_PASSWORD + i));
                list.add("inetuserstatus=Active");
                log(Level.FINEST, "setup", "SP user to be created is " +
                        list);
                if (FederationManager.getExitCode(fmSP.createIdentity(webClient,
                        configMap.get(
                        TestConstants.KEY_SP_EXECUTION_REALM),
                        usersMap.get(TestConstants.KEY_SP_USER + i), "User",
                        list)) != 0) {
                    log(Level.SEVERE, "setup", "createIdentity famadm command" +
                            " failed");
                    assert false;
                }
                spuserlist.add(usersMap.get(TestConstants.KEY_SP_USER + i));
                
                //create idp user
                list.clear();
                list.add("mail=" + usersMap.get(
                        TestConstants.KEY_IDP_USER_MAIL + i));
                list.add("sn=" + usersMap.get(TestConstants.KEY_IDP_USER + i));
                list.add("cn=" + usersMap.get(TestConstants.KEY_IDP_USER + i));
                list.add("userpassword=" + usersMap.get(
                        TestConstants.KEY_IDP_USER_PASSWORD + i));
                list.add("inetuserstatus=Active");
                log(Level.FINEST, "setup", "IDP user to be created is " + list,
                        null);
                if (FederationManager.getExitCode(fmIDP.createIdentity(
                        webClient, configMap.get(TestConstants.KEY_IDP_EXECUTION_REALM),
                        usersMap.get(TestConstants.KEY_IDP_USER + i), "User",
                        list)) != 0) {
                    log(Level.SEVERE, "setup", "createIdentity famadm command" +
                            " failed");
                    assert false;
                }
                idpuserlist.add(usersMap.get(TestConstants.KEY_IDP_USER + i));
                list.clear();
            }
        } catch (Exception e) {
            log(Level.SEVERE, "setup", e.getMessage());
            e.printStackTrace();
            throw e;
        } finally {
            consoleLogout(webClient, spurl);
            consoleLogout(webClient, idpurl);
        }
        exiting("setup");
    }
    
    /**
     * Change SAMLv2 ext metadata on SP & IDP side to configure autofederation
     * based on mail attribute
     */
    @BeforeClass(groups={"ldapv3", "ldapv3_sec", "s1ds", "s1ds_sec", "ad", 
      "ad_sec", "amsdk", "amsdk_sec", "jdbc", "jdbc_sec"})
    public void autoFedSetup()
    throws Exception {
        entering("autoFedSetup", null);
        String spurl;
        String idpurl;
        try {
            log(Level.FINEST, "autoFedSetup",
                    "\nRunning: samlv2AutoFedSPInit\n",
                    null);
            getWebClient();
            configMap = new HashMap<String, String>();
            configMap = getMapFromResourceBundle("samlv2" + fileseparator +
                    "samlv2TestConfigData");
            configMap.putAll(getMapFromResourceBundle("samlv2" + fileseparator
                    + "samlv2TestData"));
            log(Level.FINEST, "autoFedSetup", "Map:" + configMap);
            
            spurl = configMap.get(TestConstants.KEY_SP_PROTOCOL) +
                    "://" + configMap.get(TestConstants.KEY_SP_HOST) + ":" +
                    configMap.get(TestConstants.KEY_SP_PORT) +
                    configMap.get(TestConstants.KEY_SP_DEPLOYMENT_URI);
            idpurl = configMap.get(TestConstants.KEY_IDP_PROTOCOL) +
                    "://" + configMap.get(TestConstants.KEY_IDP_HOST) + ":" +
                    configMap.get(TestConstants.KEY_IDP_PORT) +
                    configMap.get(TestConstants.KEY_IDP_DEPLOYMENT_URI);
            } catch (Exception e) {
                log(Level.SEVERE, "autoFedSetup", e.getMessage());
                e.printStackTrace();
                throw e;
            }
        try {
            //get sp & idp extended metadata
            FederationManager spfm = new FederationManager(spurl);
            FederationManager idpfm = new FederationManager(idpurl);
            consoleLogin(webClient, spurl + "/UI/Login", configMap.get(
                    TestConstants.KEY_SP_AMADMIN_USER),
                    configMap.get(TestConstants.KEY_SP_AMADMIN_PASSWORD));
            
            HtmlPage spmetaPage = spfm.exportEntity(webClient,
                    configMap.get(TestConstants.KEY_SP_ENTITY_NAME),
                    configMap.get(TestConstants.KEY_SP_EXECUTION_REALM),
                    false, false, true, "saml2");
            if (FederationManager.getExitCode(spmetaPage) != 0) {
               log(Level.SEVERE, "autoFedSetup", "exportEntity famadm command" +
                       " failed");
               assert false;
            }
            spmetadata = MultiProtocolCommon.getExtMetadataFromPage(spmetaPage);
            String spmetadataMod = spmetadata.replaceAll(AUTO_FED_ENABLED_FALSE,
                    AUTO_FED_ENABLED_TRUE);
            spmetadataMod = spmetadataMod.replaceAll(AUTO_FED_ATTRIB_DEFAULT,
                    AUTO_FED_ATTRIB_VALUE);
            spmetadataMod = spmetadataMod.replaceAll(ATTRIB_MAP_DEFAULT,
                    ATTRIB_MAP_VALUE);
            log(Level.FINEST, "autoFedSetup: Modified metadata:",
                    spmetadataMod);
            if (FederationManager.getExitCode(spfm.deleteEntity(webClient,
                    configMap.get(TestConstants.KEY_SP_ENTITY_NAME),
                    configMap.get(TestConstants.KEY_SP_EXECUTION_REALM),
                    true, "saml2")) != 0) {
                log(Level.SEVERE, "autoFedSetup", "Deletion of Extended " +
                        "entity failed");
                log(Level.SEVERE, "autoFedSetup", "deleteEntity famadm" +
                        " command failed");
                assert(false);
            }
            
            if (FederationManager.getExitCode(spfm.importEntity(webClient,
                    configMap.get(TestConstants.KEY_SP_EXECUTION_REALM), "",
                    spmetadataMod, "", "saml2")) != 0) {
                log(Level.FINEST, "autoFedSetup", "Failed to import extended " +
                        "metadata");
                log(Level.FINEST, "autoFedSetup", "importEntity famadm" +
                        " command failed");
                assert(false);
            }
            consoleLogin(webClient, idpurl + "/UI/Login", configMap.get(
                    TestConstants.KEY_IDP_AMADMIN_USER),
                    configMap.get(TestConstants.KEY_IDP_AMADMIN_PASSWORD));
            
            HtmlPage idpmetaPage = idpfm.exportEntity(webClient,
                    configMap.get(TestConstants.KEY_IDP_ENTITY_NAME),
                    configMap.get(TestConstants.KEY_IDP_EXECUTION_REALM), false, false,
                    true, "saml2");
            if (FederationManager.getExitCode(idpmetaPage) != 0) {
               log(Level.SEVERE, "autoFedSetup", "exportEntity famadm command" +
                       " failed");
               assert false;
            }
            idpmetadata = MultiProtocolCommon.getExtMetadataFromPage(
                    idpmetaPage);
            String idpmetadataMod = idpmetadata.replaceAll(
                    AUTO_FED_ENABLED_FALSE, AUTO_FED_ENABLED_TRUE);
            idpmetadataMod = idpmetadataMod.replaceAll(
                    AUTO_FED_ATTRIB_DEFAULT, AUTO_FED_ATTRIB_VALUE);
            idpmetadataMod = idpmetadataMod.replaceAll(ATTRIB_MAP_DEFAULT,
                    ATTRIB_MAP_VALUE);
            log(Level.FINEST, "samlv2AutoFedSPInit: Modified metadata:",
                    idpmetadataMod);
            
            if (FederationManager.getExitCode(idpfm.deleteEntity(webClient,
                    configMap.get(TestConstants.KEY_IDP_ENTITY_NAME),
                    configMap.get(TestConstants.KEY_IDP_EXECUTION_REALM), true, "saml2"))
                    != 0) {
                log(Level.SEVERE, "autoFedSetup", "Deletion of idp Extended " +
                        "entity failed");
                log(Level.SEVERE, "autoFedSetup", "deleteEntity famadm" +
                        " command failed");
                assert(false);
            }
            
            if (FederationManager.getExitCode(idpfm.importEntity(webClient,
                    configMap.get(TestConstants.KEY_IDP_EXECUTION_REALM), "",
                    idpmetadataMod, "", "saml2")) != 0) {
                log(Level.SEVERE, "autoFedSetup", "Failed to import idp " +
                        "extended metadata");
                log(Level.SEVERE, "autoFedSetup", "importEntity famadm" +
                        " command failed");
                assert(false);
            }
        } catch (Exception e) {
            log(Level.SEVERE, "autoFedSetup", e.getMessage());
            e.printStackTrace();
            throw e;
        } finally {
            consoleLogout(webClient, spurl + "/UI/Logout");
            consoleLogout(webClient, idpurl + "/UI/Logout");
        }
        exiting("autoFedSetup");
    }
    
    /**
     * Run SP initiated auto federation
     * @DocTest: SAML2| SP initiated Autofederation.
     */
    @Test(groups={"ldapv3", "ldapv3_sec", "s1ds", "s1ds_sec", "ad", "ad_sec", 
      "amsdk", "amsdk_sec", "jdbc", "jdbc_sec"})
    public void autoFedSPInitArt()
    throws Exception {
        entering("autoFedSPInitArt", null);
        try {
            configMap.put(TestConstants.KEY_SP_USER,
                    usersMap.get(TestConstants.KEY_SP_USER + 1));
            configMap.put(TestConstants.KEY_SP_USER_PASSWORD,
                    usersMap.get(TestConstants.KEY_SP_USER_PASSWORD + 1));
            configMap.put(TestConstants.KEY_IDP_USER, usersMap.
                    get(TestConstants.KEY_IDP_USER + 1));
            configMap.put(TestConstants.KEY_IDP_USER_PASSWORD,
                    usersMap.get(TestConstants.KEY_IDP_USER_PASSWORD + 1));
            //Now perform SSO
            String[] arrActions = {"autofedspinit_ssoinit",
            "autofedspinit_slo"};
            String ssoxmlfile = baseDir + arrActions[0] + ".xml";
            SAMLv2Common.getxmlSPInitSSO(ssoxmlfile, configMap, "artifact",
                    true, false);
            String sloxmlfile = baseDir + arrActions[1] + ".xml";
            SAMLv2Common.getxmlSPSLO(sloxmlfile, configMap, "http", false);
            
            for (int i = 0; i < arrActions.length; i++) {
                log(Level.FINEST, "autoFedSPInitArt",
                        "Inside for loop. value of i is " + arrActions[i]);
                task = new DefaultTaskHandler(baseDir + arrActions[i]
                        + ".xml");
                page = task.execute(webClient);
            }
        } catch (Exception e) {
            log(Level.SEVERE, "autoFedSPInitArt", e.getMessage());
            e.printStackTrace();
            throw e;
        }
        exiting("autoFedSPInitArt");
    }
    
    /**
     * Run IDP initiated auto federation
     * @DocTest: SAML2| IDP initiated Autofederation.
     */
    @Test(groups={"ldapv3", "ldapv3_sec", "s1ds", "s1ds_sec", "ad", "ad_sec", 
      "amsdk", "amsdk_sec", "jdbc", "jdbc_sec"})
    public void autoFedIDPInitArt()
    throws Exception {
        entering("autoFedIDPInitArt", null);
        try {
            configMap.put(TestConstants.KEY_SP_USER,
                    usersMap.get(TestConstants.KEY_SP_USER + 2));
            configMap.put(TestConstants.KEY_SP_USER_PASSWORD,
                    usersMap.get(TestConstants.KEY_SP_USER_PASSWORD + 2));
            configMap.put(TestConstants.KEY_IDP_USER,
                    usersMap.get(TestConstants.KEY_IDP_USER + 2));
            configMap.put(TestConstants.KEY_IDP_USER_PASSWORD,
                    usersMap.get(TestConstants.KEY_IDP_USER_PASSWORD + 2));
            //Now perform SSO
            String[] arrActions = {"autofedidpinit_idplogin",
            "autofedidpinit_sso", "autofedidpinit_slo"};
            String loginxmlfile = baseDir + arrActions[0] + ".xml";
            SAMLv2Common.getxmlIDPLogin(loginxmlfile, configMap);
            String ssoxmlfile = baseDir + arrActions[1] + ".xml";
            SAMLv2Common.getxmlIDPInitSSO(ssoxmlfile, configMap, "artifact",
                    true);
            String sloxmlfile = baseDir + arrActions[2] + ".xml";
            SAMLv2Common.getxmlIDPSLO(sloxmlfile, configMap, "http");
            
            for (int i = 0; i < arrActions.length; i++) {
                log(Level.FINEST, "autoFedIDPInitArt",
                        "Inside for loop. value of i is " + arrActions[i]);
                task = new DefaultTaskHandler(baseDir + arrActions[i]
                        + ".xml");
                page = task.execute(webClient);
            }
        } catch (Exception e) {
            log(Level.SEVERE, "autoFedIDPInitArt", e.getMessage());
            e.printStackTrace();
            throw e;
        }
        exiting("autoFedIDPInitArt");
    }
    
    /**
     * Run SP initiated auto federation
     * @DocTest: SAML2| SP initiated Autofederation.
     */
    @Test(groups={"ldapv3_sec", "s1ds_sec", "ad_sec", "amsdk_sec"})
    public void autoFedSPInitPost()
    throws Exception {
        entering("autoFedSPInitPost", null);
        try {
            configMap.put(TestConstants.KEY_SP_USER,
                    usersMap.get(TestConstants.KEY_SP_USER + 3));
            configMap.put(TestConstants.KEY_SP_USER_PASSWORD,
                    usersMap.get(TestConstants.KEY_SP_USER_PASSWORD + 3));
            configMap.put(TestConstants.KEY_IDP_USER,
                    usersMap.get(TestConstants.KEY_IDP_USER + 3));
            configMap.put(TestConstants.KEY_IDP_USER_PASSWORD,
                    usersMap.get(TestConstants.KEY_IDP_USER_PASSWORD + 3));
            //Now perform SSO
            String[] arrActions = {"autofedspinitpost_sso",
            "autofedspinitpost_slo"};
            String ssoxmlfile = baseDir + arrActions[0] + ".xml";
            SAMLv2Common.getxmlSPInitSSO(ssoxmlfile, configMap, "post", true, 
                    false);
            String sloxmlfile = baseDir + arrActions[1] + ".xml";
            SAMLv2Common.getxmlSPSLO(sloxmlfile, configMap, "soap", false);
            
            for (int i = 0; i < arrActions.length; i++) {
                log(Level.FINEST, "samlv2AutoFed",
                        "Inside for loop. value of i is " + arrActions[i]);
                task = new DefaultTaskHandler(baseDir + arrActions[i]
                        + ".xml");
                page = task.execute(webClient);
            }
        } catch (Exception e) {
            log(Level.SEVERE, "autoFedSPInitPost", e.getMessage());
            e.printStackTrace();
            throw e;
        }
        exiting("autoFedSPInitPost");
    }
    
    /**
     * Run IDP initiated auto federation
     * @DocTest: SAML2| IDP initiated Autofederation.
     */
    @Test(groups={"ldapv3_sec", "s1ds_sec", "ad_sec", "amsdk_sec"})
    public void autoFedIDPInitPost()
    throws Exception {
        entering("autoFedIDPInitPost", null);
        try {
            configMap.put(TestConstants.KEY_SP_USER,
                    usersMap.get(TestConstants.KEY_SP_USER + 4));
            configMap.put(TestConstants.KEY_SP_USER_PASSWORD,
                    usersMap.get(TestConstants.KEY_SP_USER_PASSWORD + 4));
            configMap.put(TestConstants.KEY_IDP_USER,
                    usersMap.get(TestConstants.KEY_IDP_USER + 4));
            configMap.put(TestConstants.KEY_IDP_USER_PASSWORD,
                    usersMap.get(TestConstants.KEY_IDP_USER_PASSWORD + 4));
            //Now perform SSO
            String[] arrActions = {"autofedidpinitpost_idplogin",
            "autofedidpinitpost_sso", "autofedidpinitpost_slo"};
            String loginxmlfile = baseDir + arrActions[0] + ".xml";
            SAMLv2Common.getxmlIDPLogin(loginxmlfile, configMap);
            String ssoxmlfile = baseDir + arrActions[1] + ".xml";
            SAMLv2Common.getxmlIDPInitSSO(ssoxmlfile, configMap, "post", true);
            String sloxmlfile = baseDir + arrActions[2] + ".xml";
            SAMLv2Common.getxmlIDPSLO(sloxmlfile, configMap, "soap");
            
            for (int i = 0; i < arrActions.length; i++) {
                log(Level.FINEST, "autoFedIDPInitPost",
                        "Inside for loop. value of i is " + arrActions[i]);
                task = new DefaultTaskHandler(baseDir + arrActions[i]
                        + ".xml");
                page = task.execute(webClient);
            }
        } catch (Exception e) {
            log(Level.SEVERE, "autoFedIDPInitPost", e.getMessage());
            e.printStackTrace();
            throw e;
        }
        exiting("autoFedIDPInitPost");
    }
    
    /**
     * This methods deletes all the users as part of cleanup
     */
    @AfterClass(groups={"ldapv3", "ldapv3_sec", "s1ds", "s1ds_sec", "ad", 
      "ad_sec", "amsdk", "amsdk_sec", "jdbc", "jdbc_sec"})
    public void cleanup()
    throws Exception {
        entering("cleanup", null);
        String spurl;
        String idpurl;
        
            configMap = new HashMap<String, String>();
            getWebClient();
            configMap = getMapFromResourceBundle("samlv2" + fileseparator +
                    "samlv2TestConfigData");
            configMap.putAll(getMapFromResourceBundle("samlv2" + fileseparator
                    + "samlv2TestData"));
            
            spurl = configMap.get(TestConstants.KEY_SP_PROTOCOL) +
                    "://" + configMap.get(TestConstants.KEY_SP_HOST) + ":" +
                    configMap.get(TestConstants.KEY_SP_PORT) +
                    configMap.get(TestConstants.KEY_SP_DEPLOYMENT_URI);
            idpurl = configMap.get(TestConstants.KEY_IDP_PROTOCOL) +
                    "://" + configMap.get(TestConstants.KEY_IDP_HOST) + ":" +
                    configMap.get(TestConstants.KEY_IDP_PORT)
                    + configMap.get(TestConstants.KEY_IDP_DEPLOYMENT_URI);
        try {
            //get sp & idp extended metadata
            FederationManager spfm = new FederationManager(spurl);
            FederationManager idpfm = new FederationManager(idpurl);
            consoleLogin(webClient, spurl + "/UI/Login", configMap.get(
                    TestConstants.KEY_SP_AMADMIN_USER),
                    configMap.get(TestConstants.KEY_SP_AMADMIN_PASSWORD));
            if (FederationManager.getExitCode(spfm.deleteIdentities(webClient,
                    configMap.get(TestConstants.KEY_SP_EXECUTION_REALM), spuserlist,
                    "User")) != 0) {
                log(Level.SEVERE, "cleanup", "deleteIdentities famadm" +
                        " command failed");
                assert false;
            }
            
            if (FederationManager.getExitCode(spfm.deleteEntity(webClient,
                    configMap.get(TestConstants.KEY_SP_ENTITY_NAME),
                    configMap.get(TestConstants.KEY_SP_EXECUTION_REALM),
                    true, "saml2")) != 0) {
                log(Level.SEVERE, "cleanup", "Deletion of Extended " +
                        "entity failed");
                log(Level.SEVERE, "cleanup", "deleteEntity famadm command " +
                        "failed");
                assert(false);
            }
            
            if (FederationManager.getExitCode(spfm.importEntity(webClient,
                    configMap.get(TestConstants.KEY_SP_EXECUTION_REALM), "",
                    spmetadata, "", "saml2")) != 0) {
                log(Level.SEVERE, "cleanup", "Failed to import extended " +
                        "metadata");
                log(Level.SEVERE, "cleanup", "importEntity famadm command" +
                        " failed");
                assert(false);
            }
            consoleLogin(webClient, idpurl + "/UI/Login", configMap.get(
                    TestConstants.KEY_IDP_AMADMIN_USER),
                    configMap.get(TestConstants.KEY_IDP_AMADMIN_PASSWORD));
            if (FederationManager.getExitCode(idpfm.deleteIdentities(webClient,
                    configMap.get(TestConstants.KEY_IDP_EXECUTION_REALM),idpuserlist,
                    "User")) != 0) {
                log(Level.SEVERE, "cleanup", "deleteIdentities famadm command" +
                        " failed");
                assert false;
            }
            
            if (FederationManager.getExitCode(idpfm.deleteEntity(webClient,
                    configMap.get(TestConstants.KEY_IDP_ENTITY_NAME),
                    configMap.get(TestConstants.KEY_IDP_EXECUTION_REALM), true, "saml2"))
                    != 0) {
                log(Level.SEVERE, "cleanup", "Deletion of idp Extended " +
                        "entity failed");
                log(Level.SEVERE, "cleanup", "deleteEntity famadm command" +
                        " failed");
                assert(false);
            }
            
            if (FederationManager.getExitCode(idpfm.importEntity(webClient,
                    configMap.get(TestConstants.KEY_IDP_EXECUTION_REALM), "",
                    idpmetadata, "", "saml2")) != 0) {
                log(Level.FINEST, "cleanup", "Failed to import idp " +
                        "extended metadata");
                log(Level.SEVERE, "cleanup", "importEntity famadm command" +
                        " failed");
                assert(false);
            }
        } catch (Exception e) {
            log(Level.SEVERE, "cleanup", e.getMessage());
            e.printStackTrace();
            throw e;
        } finally {
            consoleLogout(webClient, spurl);
            consoleLogout(webClient, idpurl);
        }
        exiting("cleanup");
    }
}
