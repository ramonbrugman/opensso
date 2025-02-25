/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2006 Sun Microsystems Inc. All Rights Reserved
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
 * $Id: RemoveMember.java,v 1.4 2008-08-04 22:12:36 veiming Exp $
 *
 */

package com.sun.identity.cli.idrepo;


import com.iplanet.sso.SSOToken;
import com.iplanet.sso.SSOException;
import com.sun.identity.cli.CLIException;
import com.sun.identity.cli.ExitCodes;
import com.sun.identity.cli.IArgument;
import com.sun.identity.cli.IOutput;
import com.sun.identity.cli.LogWriter;
import com.sun.identity.cli.RequestContext;
import com.sun.identity.idm.AMIdentity;
import com.sun.identity.idm.AMIdentityRepository;
import com.sun.identity.idm.IdRepoException;
import com.sun.identity.idm.IdType;
import java.text.MessageFormat;
import java.util.Set;
import java.util.logging.Level;

/**
 * This command removes the membership of an identity from another.
 */
public class RemoveMember extends IdentityCommand {
    /**
     * Services a Commandline Request.
     *
     * @param rc Request Context.
     * @throw CLIException if the request cannot serviced.
     */
    public void handleRequest(RequestContext rc) 
        throws CLIException {
        super.handleRequest(rc);

        SSOToken adminSSOToken = getAdminSSOToken();
        IOutput outputWriter = getOutputWriter();
        String realm = getStringOptionValue(IArgument.REALM_NAME);
        String idName = getStringOptionValue(ARGUMENT_ID_NAME);
        String type = getStringOptionValue(ARGUMENT_ID_TYPE);
        IdType idType = convert2IdType(type);

        String memberIdName = getStringOptionValue(ARGUMENT_MEMBER_IDNAME);
        String memberType = getStringOptionValue(ARGUMENT_MEMBER_IDTYPE);
        IdType memberIdType = convert2IdType(memberType);

        String[] params = {realm, type, idName, memberIdName, memberType};

        try {
            AMIdentityRepository amir = new AMIdentityRepository(
                adminSSOToken, realm);
            Set memberOfs = memberIdType.canBeMemberOf();
            if (!memberOfs.contains(idType)) {
                String[] args = {type, memberType};
                throw new CLIException(MessageFormat.format(getResourceString(
                    "idrepo-cannot-be-member"), (Object[])args),
                    ExitCodes.REQUEST_CANNOT_BE_PROCESSED);
            }
        
            writeLog(LogWriter.LOG_ACCESS, Level.INFO,
                "ATTEMPT_IDREPO_REMOVE_MEMBER", params);
            AMIdentity amid = new AMIdentity(
                adminSSOToken, idName, idType, realm, null);
            AMIdentity memberAmid = new AMIdentity(
                adminSSOToken, memberIdName, memberIdType, realm, null); 
            String[] args = {memberIdName, idName};
            amid.removeMember(memberAmid);
            outputWriter.printlnMessage(MessageFormat.format(
                getResourceString("idrepo-get-removemember-succeed"), 
                (Object[])args));
            writeLog(LogWriter.LOG_ACCESS, Level.INFO, 
                "SUCCEED_IDREPO_REMOVE_MEMBER", params);
        } catch (IdRepoException e) {
            String[] args = {realm, type, idName, memberIdName, memberType,
                e.getMessage()};
            debugError("RemoveMember.handleRequest", e);
            writeLog(LogWriter.LOG_ERROR, Level.INFO,
                "FAILED_IDREPO_REMOVE_MEMBER", args);
            throw new CLIException(e, ExitCodes.REQUEST_CANNOT_BE_PROCESSED);
        } catch (SSOException e) {
            String[] args = {realm, type, idName, memberIdName, memberType,
                e.getMessage()};
            debugError("RemoveMember.handleRequest", e);
            writeLog(LogWriter.LOG_ERROR, Level.INFO,
                "FAILED_IDREPO_REMOVE_MEMBER", args);
            throw new CLIException(e, ExitCodes.REQUEST_CANNOT_BE_PROCESSED);
        }
    }
}
