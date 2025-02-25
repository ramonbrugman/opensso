<%--
   DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  
   Copyright (c) 2007 Sun Microsystems Inc. All Rights Reserved
  
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

   $Id: FileChooser.jsp,v 1.2 2008-06-25 05:50:22 qcheng Exp $
   
--%>

<%@ page info="FileChooser" language="java" %>
<%@taglib uri="/WEB-INF/jato.tld" prefix="jato" %>
<%@taglib uri="/WEB-INF/cc.tld" prefix="cc" %>
<jato:useViewBean
    className="com.sun.identity.console.federation.FileChooserViewBean"
    fireChildDisplayEvents="true" >

<cc:i18nbundle baseName="amConsole" id="amConsole"
    locale="<%=((com.sun.identity.console.base.AMViewBeanBase)viewBean).getUserLocale()%>"/>

<cc:header name="hdrCommon" pageTitle="webconsole.title" bundleID="amConsole" copyrightYear="2007" fireDisplayEvents="true">

<script language="javascript" src="../console/js/am.js">
</script>
<cc:form name="FileChooser" method="post" defaultCommandChild="/button1">

<script language="javascript">    
    function selectFile() {
	var frm = document.forms[0];
	var file = frm.elements["FileChooser.chooseFile.fileName"].value;
        var directory = frm.elements["FileChooser.chooseFile.lookIn"].value;
        
	var parent = opener.document.forms[0];
	var field = parent.elements[parent.name + '.' + window.name];
	field.value = directory + directory.charAt(0) + file;
        self.close();
    }
</script>    

<cc:secondarymasthead name="secondaryMasthead" />

<table border="0" cellpadding="10" cellspacing="0" width="100%">
    <tr>
	<td>
	<cc:alertinline name="ialertCommon" bundleID="amConsole" />
	</td>
    </tr>
</table>

<%-- PAGE CONTENT --%>
<cc:pagetitle name="pgtitle" 
    bundleID="amConsole" 
    pageTitleText="file.chooser.title" 
    showPageTitleSeparator="true" 
    viewMenuLabel="" 
    pageTitleHelpMessage="" 
    showPageButtonsTop="false" 
    showPageButtonsBottom="true" >
    
    <ul>    
    <cc:filechooser name="chooseFile" />
    </ul>
    
</cc:pagetitle>

</cc:form>
<%-- END CONTENT --%>

</cc:header>
</jato:useViewBean>
