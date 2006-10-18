/*
 * NOTE: This copyright does *not* cover user programs that use HQ
 * program services by normal system calls through the application
 * program interfaces provided as part of the Hyperic Plug-in Development
 * Kit or the Hyperic Client Development Kit - this is merely considered
 * normal use of the program, and does *not* fall under the heading of
 * "derived work".
 * 
 * Copyright (C) [2004, 2005, 2006], Hyperic, Inc.
 * This file is part of HQ.
 * 
 * HQ is free software; you can redistribute it and/or modify
 * it under the terms version 2 of the GNU General Public License as
 * published by the Free Software Foundation. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 */

package org.hyperic.hq.appdef.server.session;

import org.hyperic.hq.appdef.shared.ServerLocal;
import org.hyperic.hq.appdef.shared.ServerTypeLocal;
import org.hyperic.hq.appdef.shared.ServerVOHelperLocal;
import org.hyperic.hq.appdef.shared.ServerVOHelperUtil;
import org.hyperic.hq.appdef.ServerType;
import org.hyperic.hq.appdef.Server;
import org.hyperic.util.pager.PagerProcessor;

public class PagerProcessor_server implements PagerProcessor {
    private ServerVOHelperLocal helperLocal = null;
    
    public PagerProcessor_server () {}

    public Object processElement ( Object o ) {
        if ( o == null ) return null;
        try {
            if (helperLocal == null)
                helperLocal = ServerVOHelperUtil.getLocalHome().create();
            
            if ( o instanceof ServerLocal ) {
                return helperLocal.getServerValue((ServerLocal)o);
            }
            if ( o instanceof ServerTypeLocal ) {
                return helperLocal.getServerTypeValue((ServerTypeLocal) o);
            }
            
            if ( o instanceof Server) {
                return helperLocal.getServerValue((Server)o);
            }
            if ( o instanceof ServerType) {
                return helperLocal.getServerTypeValue((ServerType) o);
            }
        } catch ( Exception e ) {
            IllegalStateException ex =
                new IllegalStateException("Error converting to ServerValue: " + e);
            ex.initCause(e); 
            throw ex;
        }
        return o;
    }
}
