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
 * $Id: ELogRecord.java,v 1.2 2009-05-04 20:57:07 veiming Exp $
 */

package com.sun.identity.entitlement.log;

import com.sun.identity.log.ILogRecord;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import javax.security.auth.Subject;

/**
 * This class encapsulates the items in a log record.
 */
public class ELogRecord extends java.util.logging.LogRecord
    implements ILogRecord {
    private Map<String, Object> logInfoMap = new HashMap<String, Object>();
    private Subject logBy;
    private Subject logFor;

    /**
     * Constructor.
     *
     * @param level Log level.
     * @param message Log message.
     * @param logBy log by subject.
     * @param logFor log for subject.
     */
    public ELogRecord(
        Level level,
        String message,
        Subject logBy,
        Subject logFor
    ) {
        super(level, message);
        this.logBy = logBy;
        this.logFor = logFor;
    }

    /**
     * Returns log information map.
     *
     * @return log information map.
     */
    public Map<String, Object> getLogInfoMap() {
        return logInfoMap;
    }

    /**
     * Adds to the log information map, the field key and its corresponding
     * value.
     *
     * @param key The key which will be used by the formatter to determine if
     *        this piece of info is supposed to be added to the log string
     *        according to the selected log fields.
     * @param value The value which may form a part of the actual log-string.
     */
    public void addLogInfo(String key, Object value) {
        logInfoMap.put(key, value);
    }

    /**
     * Returns log by subject.
     *
     * @return log by subject.
     */
    public Object getLogBy() {
        return logBy;
    }

    /**
     * Returns log for subject.
     *
     * @return log for subject.
     */
    public Object getLogFor() {
        return logFor;
    }
}
