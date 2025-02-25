<!--
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

   $Id: readme.txt,v 1.17 2008-08-19 19:14:50 veiming Exp $

-->

------------------------------------
J2EE Policy Agent Sample Application
------------------------------------

This document describes how to use the agent sample application in conjunction 
with the Weblogic 10 Application Server and the J2EE Agent. Please note that 
the agent needs to be installed first before deploying this sample application.

    * Overview
    * Configure the OpenSSO server
    * Configure the agent properties
    * Deploying the Sample Application
    * Running the Sample Application
    * Troubleshooting
    * Optional Steps
    ** Compiling and Assembling the Application


Overview
--------
The sample application is a collection of servlets, JSPs and EJB's that 
demonstrate the salient features of the J2EE policy Agent. These features 
include SSO, web-tier declarative security, programmatic security, URL policy 
evaluation and session/policy/profile attribute fetch. The web.xml deployment
descriptor has already been edited to include the Agent Filter. The 
deployment descriptors and source code are available in the sampleapp/src 
directory.

The sample application is supported for Policy Agent 3.0.

The application is already built and ready to be deployed. It is available at
sampleapp/dist/agentsample.ear.

Note, the instructions here assume that you have installed the agent 
successfully and have followed the steps outlined in the OpenSSO 
Policy Agent 3.0 Guide for BEA WebLogic Server/Portal 10.0, including the 
post-installation steps.




Configure the OpenSSO server
----------------------------------------------
This agent sample application requires that the OpenSSO server is configured 
with the subjects and policies required by the sample application.

On OpenSSO admin console, do the following configuration.
1.  Create the following users:
    Here is the following list of users with username/password :

    * andy/andy
    * bob/bob
    * chris/chris
    * dave/dave
    * ellen/ellen
    * frank/frank
    * gina/gina


2. Assign Users to Groups
   Create new groups for employee, manager, everyone, and customer. Then assign 
   the users to the groups as follows:

    * employee:
          o andy, bob, chris, dave, ellen, frank
    * manager:
          o andy, bob, chris
    * everyone:
          o andy, bob, chris, dave, ellen, frank, gina
    * customer:
          o chris, ellen
    
3. Create the following URL Policies:
   In the following URLs, replace the <hostname> and <port> with the 
   actual fully qualified host name and port on which the sample 
   application will be running.

    * Policy 1:
          o allow:
                + http://<hostname>:<port>/agentsample/jsp/*
                + http://<hostname>:<port>/agentsample/invokerservlet
                + http://<hostname>:<port>/agentsample/protectedservlet
                + http://<hostname>:<port>/agentsample/securityawareservlet
                + http://<hostname>:<port>/agentsample/unprotectedservlet
          o Subject: all authenticated users.                     
    * Policy 2:
          o allow:
                + http://<hostname>:<port>/agentsample/urlpolicyservlet
          o Subject: Group: customer




Configure the agent properties
--------------------------------------------

   If the agent configuration is centralized, then do the following steps.
   1). login to Opensso console as amadmin user
   2). navigate to Access Control/realm/Agents/J2EE, and click on the agent 
       instance link (assume the agent instance is already created, otherwise 
       refer to the agent doc to create the agent instance).
   3). in tab "Application", section "Access Denied URI Processing", property 
       "Resource Access Denied URI", enter agentsample in the Map Key field, 
       /agentsample/authentication/accessdenied.html in the Map Value field, and
       SAVE the change.
   4). in tab "Application", section "Login Processing", property "Login Form URI",
       add /agentsample/authentication/login.html, and SAVE the change.
   5). in tab "Application", section "Not Enforced URI Processing", property 
       "Not Enforced URIs", add the following entries:
          /agentsample/public/*
          /agentsample/images/*
          /agentsample/styles/*
          /agentsample/index.html
          /agentsample
       and SAVE the change. 
   6). in tab "Application", section "Privilege Attributes Processing", property
       "Privileged Attribute Mapping",
       add the following entries:
       a) add the UUID of group employee in the Map Key field of the UI
          add am_employee_role in the Corresponding Map Value field of the UI 
          and click the Add button.
       b) add the UUID of group manager in the Map Key field of the UI
          add am_manager_role in the Corresponding Map Value field of the UI 
          and click the Add button.
       c) then SAVE the change.
       Note you can find the UUID (Universal User ID) of the groups employee 
       and manager from the OpenSSO console group page.
       For example, if the UUID of employee is id=employee,ou=group,dc=opensso,dc=java,dc=net 
       and the UUID of manager is id=manager,ou=group,dc=opensso,dc=java,dc=net
       then you need to add the following entries in the "Privileged Attribute Mapping".
       a) add id=employee,ou=group,dc=opensso,dc=java,dc=net in the Map Key 
          field of the UI.
          add am_employee_role in the Corresponding Map Value field of the UI
          and click the Add button.
       b) add id=manager,ou=group,dc=opensso,dc=java,dc=net in the Map Key 
          field of the UI.
          add am_manager_role in the Corresponding Map Value field of the UI
          and click the Add button.


   If the agent configuration is local, then edit the local agent configuration
   file OpenSSOAgentConfiguration.properties located at the directory 
   <agent_install_root>/Agent_<instance_number>/config with following changes: 

    * Not enforced List:
      com.sun.identity.agents.config.notenforced.uri[0] = /agentsample/public/*
      com.sun.identity.agents.config.notenforced.uri[1] = /agentsample/images/*
      com.sun.identity.agents.config.notenforced.uri[2] = /agentsample/styles/*
      com.sun.identity.agents.config.notenforced.uri[3] = /agentsample/index.html
      com.sun.identity.agents.config.notenforced.uri[4] = /agentsample

    * Access Denied URI:
      com.sun.identity.agents.config.access.denied.uri[agentsample] = /agentsample/authentication/accessdenied.html
    * Form List:
      com.sun.identity.agents.config.login.form[0] = /agentsample/authentication/login.html

    * Privileged Attribute Mapping: 
      com.sun.identity.agents.config.privileged.attribute.mapping[id\=employee,ou\=group,dc\=opensso,dc\=java,dc\=net] = am_employee_role.
      com.sun.identity.agents.config.privileged.attribute.mapping[id\=manager,ou\=group,dc\=opensso,dc\=java,dc\=net] = am_manager_role.
      Note the above settings assume the UUIDs of group employee and manager are
      "id=employee,ou=group,dc=opensso,dc=java,dc=net" and 
      "id=manager,ou=group,dc=opensso,dc=java,dc=net" respectively. Change to 
      the appropriate UUIDs accordingly.



   Optionally, you can try out the fetch mode features that allow the agent to
   fetch some values and make them available to your application. For example, 
   you can fetch user profile values(like email or full name) from the user data
   store of your opensso setup and make them available to your application code
   (through cookies, headers, or request attributes) for application 
   customization. See the Policy Agent 3.0 for details about the fetching 
   attributes for details on using this feature. If you change the agent's 
   configuartion for the attribute fetching, the showHttpHeaders.jsp page of the
   sample application will show all the attributes being fetched. You can choose
   to try this later after you have already installed and deployed the agent and
   sample application in order to learn about this feature.

Deploying the Sample Application
--------------------------------
Note, before deploying the sample application, please be sure that you have
deployed the Agent Application, which should have been done after installing 
the agent. This is explained in the OpenSSO Policy Agent 3.0 Guide for 
BEA WebLogic Server/Portal 10.0 in the chapter which 
outlined the post-installation tasks.

To deploy the application, do the following:

Go to BEA WebLogic server console and deploy the sample application.




Running the Sample Application
----------------------------
You can run the application through the following URL:

http://<hostname>:<port>/agentsample

Traverse the various links to understand each agent feature.




Troubleshooting
----------------------------
If you encounter problems when running the application, review the log files to 
learn what exactly went wrong. J2EE Agent logs can be found at 
<agent_install_root>/Agent_<instance_number>/logs/debug directory.

Also, see the OpenSSO Policy Agent 3.0 Guide for BEA WebLogic 
Server/Portal 10.0.




Optional Steps
----------------------------

Compiling and Assembling the Application (Optional)
---------------------------------------------------

The application is already built and ready to be deployed, so you could skip 
this section. If you want to change something or get familiar with the build
details, then this section is useful.

This section contains instructions to build and assemble the sample application 
using a Command Line Interface (CLI). 

To rebuild the entire application from scratch, follow these steps:

   1. This requires that you have downloaded and installed Apache Ant, at least 
      Ant version 1.6.5. Also be sure to have ant/bin in your path.

   1. Set your JAVA_HOME  to include JDK1.5 or above. Also, include the 
      JAVA_HOME/bin directory in your path.

   2. Replace 'APPSERV_LIB_DIR' in build.xml with the directory where 
      weblogic.jar and api.jar are located.  
      For Example: Replace APPSERV_LIB_DIR with 
      /usr/local/bea/wlserver_10.0/server/lib where /usr/local/bea is 
      where you installed WebLogic BEA 10

   3. Compile and assemble the application. 
      For example: execute the command ant 
      under <agent_install_root>/sampleapp/ to execute the default target build 
      and rebuild the EAR file. 
      The build target creates a built and dist directory with the EAR file.

      Note that you can also run 'ant rebuild' to clean the application project 
      area and run a new build.

Now you are ready to use the dist/agentsample.ear file for deployment.


