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
 * $Id: FilteredRoleGetNumOfUserReq.java,v 1.2 2008-06-25 05:52:27 qcheng Exp $
 *
 */

package com.iplanet.am.admin.cli;

import com.iplanet.am.sdk.AMException;
import com.iplanet.am.sdk.AMFilteredRole;
import com.iplanet.am.sdk.AMStoreConnection;
import com.iplanet.sso.SSOException;

class FilteredRoleGetNumOfUserReq extends RoleGetNumOfUserReq {

    FilteredRoleGetNumOfUserReq(String targetDN) {
        super(targetDN);
    }
    
    protected long getNumberOfUsers(AMStoreConnection dpConnection)
        throws AdminException
    {
        try {
            AMFilteredRole role = dpConnection.getFilteredRole(targetDN);
            return role.getNumberOfUsers();
        } catch (AMException dpe) {
            throw new AdminException(dpe);
        } catch (SSOException ssoe) {
            throw new AdminException(ssoe);
        }
    }
}
