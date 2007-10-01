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

package org.hyperic.hq.ui.action.portlet.availsummary;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.hyperic.hq.appdef.shared.AppdefEntityID;
import org.hyperic.hq.appdef.shared.AppdefEntityNotFoundException;
import org.hyperic.hq.appdef.shared.AppdefResourceTypeValue;
import org.hyperic.hq.appdef.shared.AppdefResourceValue;
import org.hyperic.hq.bizapp.shared.AppdefBoss;
import org.hyperic.hq.bizapp.shared.MeasurementBoss;
import org.hyperic.hq.bizapp.server.session.DashboardConfig;
import org.hyperic.hq.measurement.MeasurementConstants;
import org.hyperic.hq.measurement.server.session.DerivedMeasurement;
import org.hyperic.hq.product.MetricValue;
import org.hyperic.hq.ui.Constants;
import org.hyperic.hq.ui.WebUser;
import org.hyperic.hq.ui.action.BaseAction;
import org.hyperic.hq.ui.exception.ParameterNotFoundException;
import org.hyperic.hq.ui.util.ContextUtils;
import org.hyperic.hq.ui.util.DashboardUtils;
import org.hyperic.hq.ui.util.RequestUtils;
import org.hyperic.util.config.ConfigResponse;
import org.json.JSONObject;

/**
 * This action class is used by the Availability Summary portlet.  It's main
 * use is to generate the JSON objects required for display into the UI.
 */
public class ViewAction extends BaseAction {

    private static Log _log = LogFactory.getLog(ViewAction.class);

    public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
        throws Exception
    {
        ServletContext ctx = getServlet().getServletContext();
        MeasurementBoss mBoss = ContextUtils.getMeasurementBoss(ctx);
        HttpSession session = request.getSession();
        DashboardConfig dashConfig = (DashboardConfig) session.getAttribute(Constants.SELECTED_DASHBOARD);
        ConfigResponse dashPrefs = dashConfig.getConfig();
        WebUser user = (WebUser)
            request.getSession().getAttribute(Constants.WEBUSER_SES_ATTR);
        String token;
        long ts = System.currentTimeMillis();

        try {
            token = RequestUtils.getStringParameter(request, "token");
        } catch (ParameterNotFoundException e) {
            token = null;
        }

        String resKey = PropertiesForm.RESOURCES;
        String numKey = PropertiesForm.NUM_TO_SHOW;
        String titleKey = PropertiesForm.TITLE;
        
        if (token != null) {
            resKey += token;
            numKey += token;
            titleKey += token;
        }

        List entityIds = DashboardUtils.preferencesAsEntityIds(resKey, dashPrefs);

        AppdefEntityID[] arrayIds =
            (AppdefEntityID[])entityIds.toArray(new AppdefEntityID[0]);
        int count = Integer.parseInt(dashPrefs.getValue(numKey, "10"));
        int sessionId = user.getSessionId().intValue();

        CacheEntry[] ents = new CacheEntry[arrayIds.length];
        Integer[] mids = new Integer[arrayIds.length];
        Map res = new HashMap();
        long interval = 0;
        ArrayList toRemove = new ArrayList();
        for (int i = 0; i < arrayIds.length; i++) {
            AppdefEntityID id = arrayIds[i];
            try {
                ents[i] = loadData(sessionId, id);
            } catch (AppdefEntityNotFoundException e) {
                toRemove.add(id.getAppdefKey());
            }

            if (ents[i] != null) {
                mids[i] = ents[i].getMetricId();
                if (ents[i].getInterval() > interval) {
                    interval = ents[i].getInterval();
                }
            } else {
                mids[i] = null;
            }
        }

        MetricValue[] vals = mBoss.getLastMetricValue(sessionId, mids,
                                                      interval);

        for (int i = 0; i < ents.length; i++) {
            CacheEntry ent = ents[i];
            MetricValue val = vals[i];
            if (val == null) {
                // If we don't have measurement data for this resource, assume
                // that it is down.
                val = new MetricValue(MeasurementConstants.AVAIL_DOWN);
            }

            // If no avail measurement is scheduled, skip this resource
            if (ent != null) {
                String name = ent.getType().getName();
                AvailSummary summary = (AvailSummary) res.get(name);
                if (summary == null) {
                    summary = new AvailSummary(ent.getType());
                    res.put(name, summary);
                }
                summary.setAvailability(val.getValue());
            }
        }

        JSONObject availSummary = new JSONObject();
        List types = new ArrayList();

        TreeSet sortedSet = new TreeSet(new AvailSummaryComparator());
        sortedSet.addAll(res.values());

        for (Iterator i = sortedSet.iterator(); i.hasNext() && count-- > 0; ) {
            AvailSummary summary = (AvailSummary)i.next();
            JSONObject typeSummary = new JSONObject();
            typeSummary.put("resourceTypeName", summary.getTypeName());
            typeSummary.put("numUp", summary.getNumUp());
            typeSummary.put("numDown", summary.getNumDown());
            typeSummary.put("appdefType", summary.getAppdefType());
            typeSummary.put("appdefTypeId", summary.getAppdefTypeId());

            types.add(typeSummary);
        }

        availSummary.put("availSummary", types);

        if (token != null) {
            availSummary.put("token", token);
        } else {
            availSummary.put("token", JSONObject.NULL);
        }
        availSummary.put("title", dashPrefs.getValue(titleKey, ""));
        
        response.getWriter().write(availSummary.toString());

        _log.debug("Availability summary loaded in " +
                   (System.currentTimeMillis() - ts) + " ms");

        if (toRemove.size() > 0) {
            _log.debug("Removing " + toRemove.size() + " missing resources.");
            DashboardUtils.removeResources((String[])toRemove.toArray(new String[0]),
                                           resKey, dashPrefs);
        }

        return null;
    }

    private class AvailSummary {
        private AppdefResourceTypeValue _type;
        private int _numUp = 0;
        private int _numDown = 0;

        public AvailSummary(AppdefResourceTypeValue type) {
            _type = type;
        }

        public void setAvailability(double avail) {
            if (avail == MeasurementConstants.AVAIL_UP) {
                _numUp++;
            } else {
                _numDown++;
            }
        }

        public String getTypeName() {
            return _type.getName();
        }

        public int getAppdefType() {
            return _type.getAppdefType();
        }

        public Integer getAppdefTypeId() {
            return _type.getId();
        }

        public int getNumUp() {
            return _numUp;
        }

        public int getNumDown() {
            return _numDown;
        }

        public double getAvailPercentage() {
            return (double)_numUp/(_numDown + _numUp);
        }
    }

    private class AvailSummaryComparator implements Comparator {

        public int compare(Object o1, Object o2) {
            AvailSummary s1 = (AvailSummary)o1;
            AvailSummary s2 = (AvailSummary)o2;

            if (s1.getAvailPercentage() == s2.getAvailPercentage()) {
                // Sort on the actual number
                if (s1.getNumDown() != s2.getNumDown()) {
                    return s1.getNumDown() < s2.getNumDown() ? 1 : -1;
                }
                // Sort on type name if equal avail percentage
                return s1.getTypeName().compareTo(s2.getTypeName());
            } else if (s1.getAvailPercentage() < s2.getAvailPercentage()) {
                return -1;
            } else {
                return 1;
            }
        }
    }

    private CacheEntry loadData(int sessionId, AppdefEntityID id)
        throws AppdefEntityNotFoundException
    {
        Cache cache = CacheManager.getInstance().getCache("AvailabilitySummary");
        CacheKey key = new CacheKey(sessionId, id);
        Element e = cache.get(key);

        if (e != null) {
            return (CacheEntry)e.getObjectValue();
        }

        // Otherwise, load from the backend
        ServletContext ctx = getServlet().getServletContext();
        AppdefBoss      aBoss = ContextUtils.getAppdefBoss(ctx);
        MeasurementBoss mBoss = ContextUtils.getMeasurementBoss(ctx);

        try {
            AppdefResourceValue val = aBoss.findById(sessionId, id);
            DerivedMeasurement m = mBoss.findAvailabilityMetric(sessionId, id);
            if (m == null) {
                return null;
            }

            CacheEntry res = new CacheEntry(val.getAppdefResourceTypeValue(),
                                            m.getId(), m.getInterval());
            cache.put(new Element(key, res));
            return res;
        } catch (AppdefEntityNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            _log.debug("Caught exception loading data: " + ex, ex);
            return null;
        }
    }

    // Classes for caching dashboard data

    public class CacheKey {
        private AppdefEntityID _id;
        private int _sessionId;

        public CacheKey(int sessionId, AppdefEntityID id) {
            _sessionId = sessionId;
            _id = id;
        }

        public boolean equals(Object o) {
            return (o instanceof CacheKey) &&
                ((CacheKey)o)._id.equals(_id) &&
                ((CacheKey)o)._sessionId == _sessionId;
        }

        public int hashCode() {
            return _id.hashCode() + _sessionId;
        }
    }

    public class CacheEntry {
        private AppdefResourceTypeValue _type;
        private Integer _metricId;
        private long _interval;

        public CacheEntry(AppdefResourceTypeValue tval, Integer metricId,
                          long interval) {
            _type = tval;
            _metricId = metricId;
            _interval = interval;
        }

        public AppdefResourceTypeValue getType() {
            return _type;
        }

        public Integer getMetricId() {
            return _metricId;
        }

        public long getInterval() {
            return _interval;
        }
    }
}
