/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2008 Sun Microsystems Inc. All Rights Reserved
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
 * $Id: AgentExportPolicyModel.java,v 1.1 2009-12-19 00:08:14 asyhuang Exp $
 *
 */

package com.sun.identity.console.agentconfig.model;

import com.sun.identity.console.base.model.AMModel;
import com.sun.identity.console.base.model.AMConsoleException;
import java.util.Map;

/**
 * Agent Configuration Model.
 */
public interface AgentExportPolicyModel extends AMModel
{
    Map getAttributeValues(String agentName,String agentType) throws AMConsoleException;
    String getPolicyAttributeValues(String agentName,String agentType) throws AMConsoleException;
    String getInputPolicyAttributeValues(String agentName,String agentType) throws AMConsoleException;
    String getOutputPolicyAttributeValues(String agentName,String agentType) throws AMConsoleException;
    String getDisplayName(String universalId) throws AMConsoleException;
}
