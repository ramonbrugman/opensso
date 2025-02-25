<%--
   DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  
   Copyright (c) 2008 Sun Microsystems, Inc. All Rights Reserved.
  
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
  
   $Id: encode.jsp,v 1.13 2008-12-05 17:53:39 veiming Exp $
--%>

<%@page contentType="text/html; charset=UTF-8" %> 
<html>
<head>
    <title>OpenSSO</title>
    <link rel="stylesheet" type="text/css" href="com_sun_web_ui/css/css_ns6up.css" />
    <link rel="shortcut icon" href="com_sun_web_ui/images/favicon/favicon.ico" type="image/x-icon" />
</head>

<%@page import="com.iplanet.sso.SSOException" %>
<%@page import="com.iplanet.sso.SSOToken" %>
<%@page import="com.iplanet.sso.SSOTokenManager" %>
<%@page import="com.sun.identity.security.EncodeAction" %>
<%@page import="com.sun.identity.shared.locale.Locale" %>
<%@page import="com.sun.identity.sm.SMSEntry" %>
<%@page import="java.security.AccessController" %>
<%@page import="java.util.ResourceBundle" %>


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
        request.setCharacterEncoding("UTF-8");
        SSOTokenManager manager = SSOTokenManager.getInstance();
        SSOToken ssoToken = manager.createSSOToken(request);
        manager.validateToken(ssoToken);

        String ssoPropLocale = ssoToken.getProperty("Locale");
        ResourceBundle rb =
            ((ssoPropLocale != null) && (ssoPropLocale.length() > 0)) ?
            ResourceBundle.getBundle("encode", Locale.getLocale(ssoPropLocale)):
            ResourceBundle.getBundle("encode");

        if (ssoToken.getPrincipal().getName().equalsIgnoreCase(
            "id=amadmin,ou=user," + SMSEntry.getRootSuffix())
        ) {
            String strPwd = request.getParameter("password");

            if ((strPwd != null) && (strPwd.trim().length() > 0))  {
                out.println(rb.getString("result-encoded-pwd") + " ");
                    out.println((String) AccessController.doPrivileged(
                        new EncodeAction(strPwd.trim())));
                out.println("<br /><br /><a href=\"encode.jsp\">" +
                    rb.getString("encode-another-pwd") + "</a>");
            } else {
                out.println(
                   "<form name=\"frm\" action=\"encode.jsp\" method=\"post\">");
                out.println(rb.getString("prompt-pwd"));
                out.println("<input type=\"text\" name=\"password\" />");
                out.println("<input type=\"submit\" value=\"" +
			rb.getString("btn-encode") + "\" />");
                out.println("</form>");
            }
        } else {
            out.println(rb.getString("no.permission"));
        }
    } catch (SSOException e) {
        response.sendRedirect("UI/Login?goto=../encode.jsp");
    }
%>
</td></tr></table>

</body></html>
