// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package org.hyperic.hq.inventory.domain;

import java.lang.String;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import org.hyperic.hq.inventory.domain.Platform;

privileged aspect Platform_Roo_Finder {
    
    public static TypedQuery<Platform> Platform.findPlatformsByFqdnEquals(String fqdn) {
        if (fqdn == null || fqdn.length() == 0) throw new IllegalArgumentException("The fqdn argument is required");
        EntityManager em = Platform.entityManager();
        TypedQuery<Platform> q = em.createQuery("SELECT Platform FROM Platform AS platform WHERE platform.fqdn = :fqdn", Platform.class);
        q.setParameter("fqdn", fqdn);
        return q;
    }
    
    public static TypedQuery<Platform> Platform.findPlatformsByFqdnLike(String fqdn) {
        if (fqdn == null || fqdn.length() == 0) throw new IllegalArgumentException("The fqdn argument is required");
        fqdn = fqdn.replace('*', '%');
        if (fqdn.charAt(0) != '%') {
            fqdn = "%" + fqdn;
        }
        if (fqdn.charAt(fqdn.length() -1) != '%') {
            fqdn = fqdn + "%";
        }
        EntityManager em = Platform.entityManager();
        TypedQuery<Platform> q = em.createQuery("SELECT Platform FROM Platform AS platform WHERE LOWER(platform.fqdn) LIKE LOWER(:fqdn)", Platform.class);
        q.setParameter("fqdn", fqdn);
        return q;
    }
    
}
