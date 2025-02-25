/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2007 Sun Microsystems Inc. All Rights Reserved
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
 * $Id: TemplatedForm.java,v 1.4 2008-06-25 05:42:41 qcheng Exp $
 *
 */
package com.sun.identity.config.util;

import net.sf.click.control.Field;
import net.sf.click.control.Form;

import java.util.Iterator;

/**
 * @author Jeffrey Bermudez
 */
public class TemplatedForm extends Form {

    public TemplatedForm(String name) {
        super(name);
    }

    public TemplatedForm() {
        super();
    }

    public boolean doProcess() {
        // We can overwrite this method to put some specific login here
        return true;
    }

    public final boolean onProcess() {
        super.onProcess();

        Iterator it = getFieldList().iterator();
        while (it.hasNext()) {
            if (!((Field) it.next()).onProcess()) {
                return false;
            }
        }

        return doProcess();
    }

}
