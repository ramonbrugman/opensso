<%--
   DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  
   Copyright (c) 2008 Sun Microsystems Inc. All Rights Reserved
  
   The contents of this file are subject to the terms
   of the Common Development and Distribution License
   (the License). You may not use this file except in
   compliance with the License.

   You can obtain a copy of the License at
   https://opensso.dev.java.net/public/CDDLv1.0.html or
   opensso/legal/CDDLv1.0.txt
   See the License for the specific language governing
   permission and limitations under the License.

   When distributing Covered Code, include this CDDL
   Header Notice in each file and include the License file
   at opensso/legal/CDDLv1.0.txt.
   If applicable, add the following below the CDDL Header,
   with the fields enclosed by brackets [] replaced by
   your own identifying information:
   "Portions Copyrighted [year] [name of copyright owner]"

   $Id: showServerConfig.jsp,v 1.11 2008-11-25 18:16:57 veiming Exp $

--%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%@ page import="com.iplanet.sso.SSOException" %>
<%@ page import="com.iplanet.sso.SSOToken" %>
<%@ page import="com.iplanet.sso.SSOTokenManager" %>
<%@ page import="com.sun.identity.common.configuration.ServerConfiguration" %>
<%@ page import="com.sun.identity.security.EncodeAction" %>
<%@ page import="com.sun.identity.idm.IdConstants" %>
<%@ page import="com.sun.identity.sm.SMSEntry" %>
<%@ page import="com.sun.identity.sm.ServiceConfig" %>
<%@ page import="com.sun.identity.sm.ServiceConfigManager" %>
<%@ page import="java.security.AccessController" %>
<%@ page import="java.io.File" %>
<%@ page import="java.net.URL" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="java.util.Properties" %>
<%@ page import="java.util.Enumeration" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.StringTokenizer" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="com.iplanet.am.util.SystemProperties" %>


<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>OpenSSO</title>
    <link rel="stylesheet" type="text/css" href="com_sun_web_ui/css/css_ns6up.css" />
    <link rel="shortcut icon" href="com_sun_web_ui/images/favicon/favicon.ico" type="image/x-icon" />
</head>

<body class="DefBdy">
    <div class="SkpMedGry1"><a href="#SkipAnchor3860"><img src="com_sun_web_ui/images/other/dot.gif" alt="Jump to End of Masthead" border="0" height="1" width="1" /></a></div><div class="MstDiv">
    <table class="MstTblBot" title="" border="0" cellpadding="0" cellspacing="0" width="100%">
        <tr>
        <td class="MstTdTtl" width="99%">
        <div class="MstDivTtl"><img name="AMConfig.configurator.ProdName" src="console/images/PrimaryProductName.png" alt="OpenSSO" border="0" /></div>
        </td>
        <td class="MstTdLogo" width="1%"><img name="AMConfig.configurator.BrandLogo" src="com_sun_web_ui/images/other/javalogo.gif" alt="Java(TM) Logo" border="0" height="55" width="31" /></td>
        </tr>
    </table>
    <table class="MstTblEnd" border="0" cellpadding="0" cellspacing="0" width="100%"><tr><td><img name="RMRealm.mhCommon.EndorserLogo" src="com_sun_web_ui/images/masthead/masthead-sunname.gif" alt="Sun(TM) Microsystems, Inc." align="right" border="0" height="10" width="108" /></td></tr></table>
    </div>
    <table class="SkpMedGry1" border="0" cellpadding="5" cellspacing="0" width="100%"><tr><td><img src="com_sun_web_ui/images/other/dot.gif" alt="Jump to End of Masthead" border="0" height="1" width="1" /></a></td></tr></table>
    <table border="0" cellpadding="10" cellspacing="0" width="100%"><tr><td></td></tr></table>
    <table border="0" cellpadding="10" cellspacing="0" width="100%"><tr><td>

<%
    try {
        SSOTokenManager manager = SSOTokenManager.getInstance();
        SSOToken ssoToken = manager.createSSOToken(request);
        manager.validateToken(ssoToken);
        String strURL = request.getRequestURL().toString();

        if (ssoToken.getPrincipal().getName().equals(
            "id=amadmin,ou=user," + SMSEntry.getRootSuffix())
        ) {
              out.println("<B>SYSTEM PROPERTIES</B>");
              %>
                  <table border="1">
                  <tr>
                  <td>
              <%
                  Properties propDef = ServerConfiguration.getDefaults(ssoToken);
                  out.println("<B>OpenSSO Version</B>");
              %>
                  </td>
                  <td>
              <%
                  out.println(propDef.getProperty("com.iplanet.am.version"));
              %>
                  </td>
                  </tr>
                  <tr>
                  <td>
              <%
                  Properties prop = null;
                  try {
                      URL url = new URL(strURL);
                      int port = url.getPort();
                      String protocol = url.getProtocol();
                      String host = url.getHost();
                      String path = url.getPath();

                      if (port == -1) {
                          port = protocol.equals("https") ? 443 : 80;
                      }

                      int idx = path.indexOf("/showServerConfig.jsp");
                      if (idx != -1) {
                          path = path.substring(0, idx);
                      }

                      prop = ServerConfiguration.getServerInstance(ssoToken,
                          protocol + "://" + host + ":" + port + path);
                  } catch (java.net.MalformedURLException e) {
                      //ignore
                  }

                  out.println("<B>Server Name</B>");
              %>
                  </td>
                  <td>
              <%
                  out.println(prop.getProperty("com.iplanet.am.server.protocol").trim() + "://" +
                  prop.getProperty("com.iplanet.am.server.host").trim() + ":" +
                  prop.getProperty("com.iplanet.am.server.port").trim() +
                  prop.getProperty("com.iplanet.am.services.deploymentDescriptor"));
              %>
                  </td>
                  </tr>
                  <tr>
                  <td>
              <%
                  ServletContext sctx = getServletConfig().getServletContext();
                  out.println("<B>Container</B>");
              %>
                  </td>
                  <td>
              <%
                  out.println(sctx.getServerInfo());
              %>
                  </td>
                  </tr>
                  <tr>
                  <td>
              <%
                  out.println("<B>Operating System</B>");
              %>
                  </td>
                  <td>
              <%
                  out.println(System.getProperty("os.name"));
                  out.println(" ");
                  out.println(System.getProperty("os.version"));
                  out.println(" ");
                  out.println(System.getProperty("os.arch"));
              %>
                  </td>
                  </tr>
                  <tr>
                  <td>
              <%
                  out.println("<B>Java Version</B>");
              %>
                  </td>
                  <td>
              <%
                  out.println(System.getProperty("java.version"));
              %>
                  </td>
                  </tr>
                  <tr>
                  <td>
              <%
                  out.println("<B>Browser Version</B>");
              %>
                  </td>
                  <td>
              <%
                  out.println(request.getHeader("user-agent"));
              %>
                  </td>
                  </tr>
                  <tr>
                  <td>
              <%
                  out.println("<B>Server Install Mode</B>");
              %>
                  </td>
                  <td>
              <%
                  if (!ServerConfiguration.isLegacy(ssoToken))
                      out.println("Realm");
                  else
                      out.println("Legacy");
              %>
                  </td>
                  </tr>
                  <tr>
                  <td>
              <%
                  out.println("<B>Service Management Configuration Datastore Type</B>");
              %>
                  </td>
                  <td>
              <%
                  String strConfigDir = prop.getProperty("com.iplanet.services.configpath");
                  boolean isEmbeddedDS = (new File(strConfigDir + "/opends")).exists();
                  if (isEmbeddedDS) {
                      out.println("Embedded");
                  } else {
                      out.println("Remote");
                  }
              %>
                  </td>
                  </tr>
                  <tr>
                  <td>
              <%
                  out.println("<B>Java Home</B>");
              %>
                  </td>
                  <td>
              <%
                  out.println(System.getProperty("java.home"));
              %>
                  </td>
                  </tr>
                  <tr>
                  <td>
              <% 
                  out.println("<B>Server Names (Configuration->Sites and Servers)</B>");
              %>
                  </td>
                  <td>
              <%
                  out.println(ServerConfiguration.getServers(ssoToken));
              %>
                  </td>
                  </tr>
                  <tr>
                  <td>
              <%
                  out.println("<B>Configuration Directory</B>");
              %>
                  </td>
                  <td>
              <%
                  out.println(strConfigDir);
              %>
                  </td>
                  </tr>
                  <tr>
                  <td>
              <%
                  out.println("<B>User management datastore names under root realm (Datastore plugin type)</B>");
              %>
                  </td>
                  <td>
              <%
                  ServiceConfigManager scm = new ServiceConfigManager(IdConstants.REPO_SERVICE, ssoToken);
                  ServiceConfig svcfg = scm.getOrganizationConfig("/", null);
                  Set dsSet = svcfg.getSubConfigNames();
                  int j = 1;
                  for (Iterator i = dsSet.iterator(); i.hasNext();) {
                      String dsname = (String)i.next();
                      ServiceConfig subConfig = svcfg.getSubConfig(dsname);
                      if (j == dsSet.size())
                          out.println(dsname + " (" + subConfig.getSchemaID() + ")");
                      else
                          out.println(dsname + " (" + subConfig.getSchemaID() + "), ");
                      j++;
                  }
              %>
                  </td>
                  </tr>
                  <tr>
                  <td>
              <%
                  out.println("<B>Java Runtime Name</B>");
              %>
                  </td>
                  <td>
              <%
                  out.println(System.getProperty("java.runtime.name"));
              %>
                  </td>
                  </tr>
                  <tr>
                  <td>
              <%
                  out.println("<B>Java VM Name</B>");
              %>
                  </td>
                  <td>
              <%
                  out.println(System.getProperty("java.vm.name"));
              %>
                  </td>
                  </tr>
                  <tr>
                  <td>
              <%
                  out.println("<B>Java VM Version</B>");
              %>
                  </td>
                  <td>
              <%
                  out.println(System.getProperty("java.vm.version"));
              %>
                  </td>
                  </tr>
                  <tr>
                  <td>
              <%
                  out.println("<B>Java Arch Data Model</B>");
              %>
                  </td>
                  <td>
              <%
                  out.println(System.getProperty("sun.arch.data.model") + " bit");
              %>
                  </td>
                  </tr>
                  <tr>
                  <td>
              <%
                  out.println("<B>System Locale</B>");
              %>
                  </td>
                  <td>
              <%
                  out.println(System.getProperty("user.language"));
              %>
                  </td>
                  </tr>
                  <tr>
                  <td>
              <%
                  out.println("<B>Java Classpath</B>");
              %>
                  </td>
                  <td>
              <%
                  String strCP = System.getProperty("java.class.path");
                  StringTokenizer st = new StringTokenizer(strCP, ":");
                  while (st.hasMoreTokens()) {
                      String token = st.nextToken();
                      out.println(token + "\n");
                  }
              %>
                  </td>
                  </tr>
                  <tr>
                  <td>
              <%
                  out.println("<B>Java VM Vendor</B>");
              %>
                  </td>
                  <td>
              <%
                  out.println(System.getProperty("java.vm.specification.vendor"));
              %>
                  </td>
                  </tr>
                  </table>
              <% 
              out.println("<br/>");
              out.println("<a href=" + strURL + ">The data above is generated using the following url</a>");
	} else { 
            out.println("<B>User does not have permission to access this page</B>");
        }
    } catch (SSOException e) {
        response.sendRedirect("UI/Login?goto=../showServerConfig.jsp");
    }
%>
</td></tr></table>

</body></html>
