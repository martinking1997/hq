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
package org.hyperic.hq.events.server.session;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.Query;
import org.hyperic.hq.dao.HibernateDAO;
import org.hyperic.hq.appdef.shared.AppdefEntityID;
import org.hyperic.util.jdbc.DBUtil;

public class AlertDAO extends HibernateDAO {
    public AlertDAO(Session session) {
        super(Alert.class, session);
    }

    public Alert findById(Integer id) {
        return (Alert)super.findById(id);
    }

    int deleteByIds(Integer[] ids) {
        String sql = "delete Alert where id in " + DBUtil.makeInSet(ids);

        return getSession().createQuery(sql)
            .executeUpdate();
    }

    public List findByCreateTime(long begin, long end) {
        String sql = "from Alert a where a.ctime between :timeStart and " +
            ":timeEnd order by a.ctime desc";
        
        return getSession().createQuery(sql)
            .setLong("timeStart", begin)
            .setLong("timeEnd", end)
            .list();
    }

    int deleteByCreateTime(long begin, long end) {
        String sql = "delete Alert a where a.ctime between :timeStart and " +
            ":timeEnd order by a.ctime desc";

        return getSession().createQuery(sql)
            .setLong("timeStart", begin)
            .setLong("timeEnd", end)
            .executeUpdate();
    }
    
    public List findByEntity(AppdefEntityID id) {
        return findByEntity(id, "a.ctime DESC");
    }

    public List findByCreateTimeAndPriority(long begin, long end, int priority){
        String sql = "from Alert a where a.ctime between :begin and :end " + 
            "and (a.alertDefinition.priority = :priority " +
            "     or a.alertDefinition.priority > :priority) " +
            "order by a.ctime desc"; 
        
        return getSession().createQuery(sql)
            .setLong("begin", begin)
            .setLong("end", end)
            .setInteger("priority", priority)
            .list();
    }
    
    public List findByAppdefEntityInRange(AppdefEntityID id, long begin,
                                          long end)
    {
        String sql = "from Alert a where a.alertDefinition.appdefType = :aType " +
            "and a.alertDefinition.appdefId = :aId and a.ctime between :begin " +
            "and :end order by a.ctime desc";
        
        return getSession().createQuery(sql)
            .setInteger("aType", id.getType())
            .setInteger("aId", id.getID())
            .setLong("begin", begin)
            .setLong("end", end)
            .list();
    }
    
    public List findByAppdefEntityInRangeSortByAlertDef(AppdefEntityID id,
                                                        long begin, long end)
    {
        String sql = "from Alert a WHERE a.alertDefinition.appdefType = :aType " +
            "and a.alertDefinition.appdefId = :aId and a.ctime between :begin " +
            "and :end order by a.alertDefinition.name DESC";
        
        return getSession().createQuery(sql)
            .setInteger("aType", id.getType())
            .setInteger("aId", id.getID())
            .setLong("begin", begin)
            .setLong("end", end)
            .list();
    }
    
    private List findByEntity(AppdefEntityID id, String orderBy) {
        String sql = "from Alert a WHERE a.alertDefinition.appdefType = :aType " +
            "and a.alertDefinition.appdefId = :aId ORDER BY " + orderBy;
        
        return getSession().createQuery(sql)
            .setInteger("aType", id.getType())
            .setInteger("aId", id.getId().intValue())
            .list();
    }

    int deleteByEntity(AppdefEntityID id) {
        String sql = "delete Alert a WHERE a.alertDefinition.appdefType = :aType " +
            "and a.alertDefinition.appdefId = :aId";

        return getSession().createQuery(sql)
            .setInteger("aType", id.getType())
            .setInteger("aId", id.getId().intValue())
            .executeUpdate();
    }

    public List findByAppdefEntitySortByAlertDef(AppdefEntityID id) {
        return findByEntity(id, "a.alertDefinition.name DESC");
    }
    
    public Alert findByAlertDefinitionAndCtime(AlertDefinition def, long ctime){
        String sql = "from Alert a WHERE a.alertDefinition = :alertDef " +
            "and a.ctime = :ctime";
        
        return (Alert)getSession().createQuery(sql)
            .setParameter("alertDef", def)
            .setLong("ctime", ctime)
            .uniqueResult();
    }

    public List findByAlertDefinition(AlertDefinition def) {
        String sql = "from Alert a WHERE a.alertDefinition = :alertDef";
        
        return getSession().createQuery(sql)
            .setParameter("alertDef", def)
            .list();
    }

    int deleteByAlertDefinition(Integer def) {
        String sql = "delete Alert a WHERE a.alertDefinition.id = :alertDef";

        return getSession().createQuery(sql)
            .setInteger("alertDef", def.intValue())
            .executeUpdate();
    }
    
    public int countAlerts(AlertDefinition def) {
        String sql = "select count(*) from Alert a " +
            "where a.alertDefinition = :alertDef";
        
        return ((Integer)getSession().createQuery(sql)
                                     .setParameter("alertDef", def)
                                     .uniqueResult()).intValue();
    }
    
    public List findBySubject(Integer userId) {
        String sql = "select a from Alert a " + 
            " join a.userAlertsBag ua " + 
            " where ua.userId = :userId";
        
        return getSession().createQuery(sql)
            .setInteger("userId", userId.intValue())
            .list();
    }
    
    void remove(Alert alert) {
        super.remove(alert);
    }
    
    void save(Alert alert) { 
        super.save(alert);
    }
}
