package org.hyperic.hq.notifications.filtering;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyperic.hq.notifications.model.BaseNotification;
import org.hyperic.hq.notifications.model.NotificationGroup;

public abstract class DestinationEvaluator {
    private final Log log = LogFactory.getLog(DestinationEvaluator.class);
    // TODO~ change to write through versioning (each node would have versioning - write on one version, read another, then sync between them), o/w will pose problems in scale
    protected Map<Integer,FilterChain<BaseNotification>> destToFilter = new ConcurrentHashMap<Integer,FilterChain<BaseNotification>>();

    /**
     * append filters
     * 
     * @param dest
     * @param filters
     */
    public Integer register(Collection<? extends Filter<? extends BaseNotification,? extends FilteringCondition<?>>> filters) {
        if (filters==null || filters.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("no filters were passed to be registered");
            }
        }
        Integer regID = createRegID();
        FilterChain<BaseNotification> filterChain = this.destToFilter.get(regID);
        if (filterChain==null) {
            filterChain = new FilterChain<BaseNotification>(filters);
            this.destToFilter.put(regID,filterChain);
            
            if (log.isDebugEnabled()) {
                log.debug("registering the following filters to destination " + regID + " (no previous filters were assigned to it):\n" + filters);
            }
        } else {
            filterChain.addAll(filters);
            
            if (log.isDebugEnabled()) {
                log.debug("appending the following filters to destination " + regID + ":\n" + filters);
            }
        }
        return regID;
    }
    /**
     * unregister all filters assigned to this destination
     * @param dest
     */
    public void unregisterAll(Integer regID) {
        FilterChain<? extends BaseNotification> filterChain = this.destToFilter.remove(regID);
        if (log.isDebugEnabled()) {
            if (filterChain==null) {
                log.debug("no filters were previously registered with registration " + regID);
            } else {
                // TODO~ remove all filter chain filters from it
                log.debug("un-registering all previously regitered filters from registration " + regID + ":\n" + filterChain);
            }
        }
//        removeRegistrationFromDB(regID);
    }
    /**
     * 
     * @param dest
     * @param filters
     */
    public void unregister(Destination dest, List<Filter<? extends BaseNotification,? extends FilteringCondition<?>>> filters) {
        if (filters==null || filters.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("no filters were passed to be un-registered from destination " + dest);
            }
            return;
        }
        FilterChain<? extends BaseNotification> filterChain = this.destToFilter.get(dest);
        if (filterChain==null) {
            if (log.isDebugEnabled()) {
                log.debug("no filters were previously registered with destination " + dest);
            }
        } else {
            filterChain.removeAll(filters);
            if (log.isDebugEnabled()) {
                log.debug("un-registering the following filters from destination " + dest + ":\n" + filters);
            }
            if (filterChain.isEmpty()) {
                this.destToFilter.remove(dest);
                if (log.isDebugEnabled()) {
                    log.debug("un-registering the following destination " + dest);
                }
            }
        }
    }
    public List<NotificationGroup> evaluate(final List<? extends BaseNotification> entities) throws JMSException {
        List<NotificationGroup> nsGrpList = new ArrayList<NotificationGroup>();
        Set<Entry<Integer,FilterChain<BaseNotification>>> regToFilterESet = destToFilter.entrySet();
         
        for(Entry<Integer,FilterChain<BaseNotification>> regToFilterE:regToFilterESet) {
            FilterChain<BaseNotification> filterChain = regToFilterE.getValue();
            List<? extends BaseNotification> filteredEntities = ((List<? extends BaseNotification>) filterChain.filter(entities));
            if (filteredEntities!=null) {
                Integer regID = regToFilterE.getKey();
                NotificationGroup nsGrp = new NotificationGroup(regID, filteredEntities);
                nsGrpList.add(nsGrp);
            }
        }
        return nsGrpList;
    }
    
    //TODO~ replace with DB persisted reg ID
    static Integer regID = 0; 
    private Integer createRegID() {
        return ++regID;
    }
}
