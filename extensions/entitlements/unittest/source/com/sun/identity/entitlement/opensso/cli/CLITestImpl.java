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
 * $Id: CLITestImpl.java,v 1.1 2009-06-12 00:02:35 veiming Exp $
 */

package com.sun.identity.entitlement.opensso.cli;

import com.iplanet.sso.SSOToken;
import com.sun.identity.cli.CLIConstants;
import com.sun.identity.cli.CLIException;
import com.sun.identity.cli.CommandManager;
import com.sun.identity.entitlement.opensso.SubjectUtils;
import com.sun.identity.security.AdminTokenAction;
import java.security.AccessController;
import java.util.HashMap;
import java.util.Map;
import javax.security.auth.Subject;
import org.testng.annotations.BeforeClass;


public class CLITestImpl {
    protected CommandManager cmdManager;
    private static DevNullOutputWriter outputWriter = new DevNullOutputWriter();
    protected SSOToken adminToken = (SSOToken) AccessController.doPrivileged(
            AdminTokenAction.getInstance());
    protected Subject adminSubject = SubjectUtils.createSubject(adminToken);

    @BeforeClass
    public void setup() throws Exception {
        Map<String, Object> env = new HashMap<String, Object>();
        env.put(CLIConstants.SYS_PROPERTY_COMMAND_NAME, "testclifw");
        env.put(CLIConstants.SYS_PROPERTY_DEFINITION_FILES,
            "com.sun.identity.entitlement.opensso.cli.EntitlementManager");
        env.put(CLIConstants.SYS_PROPERTY_OUTPUT_WRITER, outputWriter);
        cmdManager = new CommandManager(env);
    }
}
