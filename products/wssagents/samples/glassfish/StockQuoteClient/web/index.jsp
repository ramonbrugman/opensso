<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%--
   DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  
   Copyright (c) 2005 Sun Microsystems, Inc. All Rights Reserved.
  
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
  
   $Id: index.jsp,v 1.8 2008-08-19 19:15:10 veiming Exp $
--%>
<%--
The taglib directive below imports the JSTL library. If you uncomment it,
you must also add the JSTL library to the project. The Add Library... action
on Libraries node in Projects view can be used to add the JSTL 1.1 library.
--%>
<%--
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%> 
--%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <title>Stock Quote Client Sample</title>
    </head>
    <body>

    <h1>Stock Quote Client Sample</h1>
    
    <form name="GetQuote" action="GetQuote" method="GET">
        Stock Symbol: <input type="text" name="symbol" value="JAVA" size="12" />
        <p><input type="submit" value="GetQuote" name="quote" />
    </form>
<!--
    <p><hr>
    <form name="FAMConsole" action="/opensso/console" method="GET">
        Click <a href="/opensso/console">here</a> to view OpenSSO Console
        <p><input type="submit" value="FAMConsole"/>
    </form>
-->
    
    </body>
</html>
