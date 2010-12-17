package org.hyperic.hq.inventory.dao;

import java.util.List;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hyperic.hq.inventory.domain.ResourceType;
import org.springframework.datastore.graph.neo4j.finder.FinderFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class ResourceTypeDaoImpl implements ResourceTypeDao {
	@PersistenceContext
	private EntityManager entityManager;

    @Resource
    private FinderFactory finderFactory;
	
    @Transactional(readOnly = true)
	public ResourceType findById(Integer id) {
        if (id == null) return null;
        
        ResourceType result = entityManager.find(ResourceType.class, id);
        
        // TODO workaround to trigger Neo4jNodeBacking's around advice for the getter
        if(result != null) {
            result.getId();
        }
        
        return result;
    }
    
    @Transactional(readOnly = true)
    public List<ResourceType> findAll() {
		List<ResourceType> result =  entityManager.createQuery("select o from ResourceType o",ResourceType.class).getResultList();
		
		// TODO workaround to trigger Neo4jNodeBacking's around advice for the getter
		for (ResourceType resourceType : result) {
			resourceType.getId();
		}
		
		return result;
    }

	
    @Transactional(readOnly = true)
    public List<ResourceType> find(Integer firstResult, Integer maxResults) {
		List<ResourceType> result = entityManager.createQuery("select o from ResourceType o",ResourceType.class)
        	.setFirstResult(firstResult)
        	.setMaxResults(maxResults)
        	.getResultList();
		
		// TODO workaround to trigger Neo4jNodeBacking's around advice for the getter
		for (ResourceType resourceType : result) {
			resourceType.getId();
		}
		
		return result;
    }

    @Transactional(readOnly = true)
	public Long count() {
        return (Long) entityManager.createQuery("select count(o) from ResourceType o").getSingleResult();
    }
    
    @Transactional(readOnly = true)
	public ResourceType findByName(String name) {
        // Can't do JPA-style queries on property values that are only in graph
        ResourceType type = finderFactory.getFinderForClass(ResourceType.class)
            .findByPropertyValue("name", name);
        
        if (type != null) {
            type.getId();
        }
        
        return type;
    }
    
    @Transactional(readOnly = true)
	public ResourceType findRoot() {
    	return findById(1);
    }
}