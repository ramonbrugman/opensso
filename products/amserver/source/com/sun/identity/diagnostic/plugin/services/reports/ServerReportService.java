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
 * $Id: ServerReportService.java,v 1.2 2009-11-13 21:57:40 ak138937 Exp $
 *
 */

package com.sun.identity.diagnostic.plugin.services.reports;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.logging.Level;

import com.sun.identity.diagnostic.base.core.ToolContext;
import com.sun.identity.diagnostic.base.core.ToolLogWriter;
import com.sun.identity.diagnostic.base.core.common.ToolConstants;
import com.sun.identity.diagnostic.base.core.log.IToolOutput;
import com.sun.identity.diagnostic.base.core.service.ServiceRequest;
import com.sun.identity.diagnostic.base.core.service.ServiceResponse;
import com.sun.identity.diagnostic.base.core.service.ToolService;
import com.sun.identity.diagnostic.plugin.services.common.ClientConstants;
import com.sun.identity.shared.debug.Debug;


/**
 * This class contains the implementation of a reports service
 * that can be realized in the Diagnostic tool application.
 *
 */
public class ServerReportService implements ToolConstants,
    ReportConstants, ToolService {
    
    private ToolContext tContext;
    private static IToolOutput toolOutWriter;
    private ResourceBundle rb;
    
    /** Creates a new instance of ServerReportService */
    public ServerReportService() {
    }
    
    /**
     * This is called once during service activation.
     * It caches the given <code>ToolContext</code>.
     *
     * @param tContext ToolContext in which this service runs.
     */
    public void init(ToolContext tContext) {
        this.tContext = tContext;
        toolOutWriter = tContext.getOutputWriter();
        rb = ResourceBundle.getBundle(REPORT_RESOURCE_BUNDLE);
    }
    
    /**
     * This is called once during application start up.
     * Starts up the report service.
     */
    public void start() {
    }
    
    /**
     * This is method that processes the given request.
     *
     * @param sReq ServiceRequest object containing input params
     * @param sRes ServiceResponse object containg output results
     * @throw Exception if the exception occurs.
     */
    public void processRequest(
        ServiceRequest sReq,
        ServiceResponse sRes
    ) throws Exception {
        toolOutWriter.init(sRes, rb);
        toolOutWriter.printlnResult("service-start-msg");
        ToolLogWriter.log(rb,Level.INFO,"service-start-msg", null);
        HashSet commandSet = (HashSet)sReq.getCommandSet();
        Map params = (HashMap)sReq.getData();    
        try {
            Map validatorMap = (HashMap)getValidators();
            for (Iterator j = commandSet.iterator(); j.hasNext();) {
                String cmd = ((String)j.next()).toLowerCase();;
                String path = getBootFileName(
                    (String)params.get(CONFIG_DIR), cmd);
                if (!path.toLowerCase().contains("agent")) {
                    path = (String)params.get(CONFIG_DIR);
                }
                ((IGenerateReport)validatorMap.get(cmd)).generateReport(path);
            }
            toolOutWriter.printResult(sRes.getStatus());
            toolOutWriter.printResult("service-done-msg");
        } catch (Exception e) {
            throw new Exception(rb.getString("cannot-bootstrap-system") + "\n" +
                e.getMessage());
        }
    }
    
    private String getBootFileName(
        String path,
        String cmd
    ) throws Exception {
        String fName = null;
        try {
            if (cmd.toLowerCase().indexOf("agt") == -1 ) {
                //server bootfile
                fName = path + "/" + REPORT_SVR_BOOTFILE;
            } else {
                //agent bootfile
                fName = path + "/" + ClientConstants.AGENT_BOOTSTRAP_FILE;
            }
            File bFile = new File(fName);
            if (!bFile.exists())  {
                throw new Exception(
                    rb.getString("invalid-bootstrap-location") + path);
            }
        } catch (Exception e) {
            throw e;
        }
        return fName;
    }
    
    /**
     * This is called once during application shutdown.
     */
    public void stop() {
    }
    
    static IToolOutput getToolWriter() {
        return toolOutWriter;
    }
    
    private Map getValidators() {
        Map opCodeToClass = new HashMap();
        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(
                tContext.getApplicationHome() +
                "/services/reports/config/ReportConfiguration"));
            if (in.ready()) {
                String line;
                while ((line = in.readLine()) != null) {
                    if (line.indexOf("=") != -1) {
                        StringTokenizer st =
                            new StringTokenizer(line, "=");
                        String op = st.nextToken();
                        String className = st.nextToken();
                        if (op != null && op.trim().length() > 0) {
                            if (className != null &&
                                className.trim().length() > 0){
                                Class clazz = Class.forName(className);
                                opCodeToClass.put(op.toLowerCase(),
                                    (IGenerateReport)clazz.newInstance());
                            }
                        }
                    }
                }
            }
        } catch (Exception ioe) {
            Debug.getInstance(DEBUG_NAME).error(
                "ServerReportService.getValidators: " +
                "Unable to read opcode-to-class cofiguration file",
                ioe);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception ex) {
                }
            }
        }
        return opCodeToClass;
    }
}
