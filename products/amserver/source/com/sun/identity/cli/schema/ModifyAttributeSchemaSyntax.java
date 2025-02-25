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
 * $Id: ModifyAttributeSchemaSyntax.java,v 1.3 2008-12-04 06:32:06 veiming Exp $
 *
 */

package com.sun.identity.cli.schema;


import com.sun.identity.cli.CLIException;
import com.sun.identity.cli.ExitCodes;
import com.sun.identity.cli.IArgument;
import com.sun.identity.cli.IOutput;
import com.sun.identity.cli.LogWriter;
import com.sun.identity.cli.RequestContext;
import com.iplanet.sso.SSOException;
import com.sun.identity.sm.AttributeSchema;
import com.sun.identity.sm.SMSException;
import com.sun.identity.sm.ServiceSchema;
import java.text.MessageFormat;
import java.util.logging.Level;

/**
 * Modifies attribute schema's syntax.
 */
public class ModifyAttributeSchemaSyntax extends SchemaCommand {
    static final String ARGUMENT_SYNTAX = "syntax";
    
    /**
     * Services a Commandline Request.
     *
     * @param rc Request Context.
     * @throw CLIException if the request cannot serviced.
     */
    public void handleRequest(RequestContext rc) 
        throws CLIException {
        super.handleRequest(rc);
        ldapLogin();

        String schemaType = getStringOptionValue(IArgument.SCHEMA_TYPE);
        String serviceName = getStringOptionValue(IArgument.SERVICE_NAME);
        String subSchemaName = getStringOptionValue(IArgument.SUBSCHEMA_NAME);
        String attributeSchemaName = getStringOptionValue(
            IArgument.ATTRIBUTE_SCHEMA);
        String syntax = getStringOptionValue(ARGUMENT_SYNTAX);
        
        ServiceSchema ss = getServiceSchema();
        IOutput outputWriter = getOutputWriter();

        try {
            String[] params = {serviceName, schemaType, subSchemaName,
                attributeSchemaName, syntax};
            writeLog(LogWriter.LOG_ACCESS, Level.INFO,
                "ATTEMPT_MODIFY_ATTRIBUTE_SCHEMA_SYNTAX", params);
            AttributeSchema attrSchema = ss.getAttributeSchema(
                attributeSchemaName);

            if (attrSchema == null) {
                String[] args = {serviceName, schemaType, subSchemaName,
                    attributeSchemaName, syntax,
                    "attribute schema does not exist"};
                attributeSchemaNoExist(attributeSchemaName,
                    "FAILED_MODIFY_ATTRIBUTE_SCHEMA_SYNTAX", args);
            }

            attrSchema.setSyntax(syntax);
            writeLog(LogWriter.LOG_ACCESS, Level.INFO,
                "SUCCEED_MODIFY_ATTRIBUTE_SCHEMA_SYNTAX", params);
            outputWriter.printlnMessage(MessageFormat.format(
                getResourceString(
                    "attribute-schema-modify-syntax-succeed"),
                (Object[])params));
        } catch (SSOException e) {
            String[] args = {serviceName, schemaType, subSchemaName,
                attributeSchemaName, syntax, e.getMessage()};
            debugError("ModifyAttributeSchemaSyntax.handleRequest",e);
            writeLog(LogWriter.LOG_ERROR, Level.INFO,
                "FAILED_MODIFY_ATTRIBUTE_SCHEMA_SYNTAX", args);
            throw new CLIException(e, ExitCodes.REQUEST_CANNOT_BE_PROCESSED);
        } catch (SMSException e) {
            String[] args = {serviceName, schemaType, subSchemaName,
                attributeSchemaName, syntax, e.getMessage()};
            debugError("ModifyAttributeSchemaSyntax.handleRequest",e);
            writeLog(LogWriter.LOG_ERROR, Level.INFO,
                "FAILED_MODIFY_ATTRIBUTE_SCHEMA_SYNTAX", args);
            throw new CLIException(e, ExitCodes.REQUEST_CANNOT_BE_PROCESSED);
        }
    }
}
