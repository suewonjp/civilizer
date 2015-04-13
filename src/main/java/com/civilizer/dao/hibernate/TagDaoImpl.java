package com.civilizer.dao.hibernate;

import java.util.*;

import javax.annotation.Resource;

import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.civilizer.dao.TagDao;
import com.civilizer.domain.*;

@Repository("tagDao")
@Transactional
public final class TagDaoImpl implements TagDao {

    private SessionFactory sessionFactory;
    
    @Resource(name = "sessionFactory")
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
    
    @Override
    public List<?> executeQueryForResult(String query) {
        return sessionFactory.getCurrentSession()
                .createQuery(query)
                .list();
    }
    
    @Override
    public void executeQuery(String query, boolean sql) {
    	final Session session = sessionFactory.getCurrentSession();
    	Query q = sql ? session.createSQLQuery(query) : session.createQuery(query);
    	q.executeUpdate();
    }
    
    @Override
    public long countAll() {
    	return (Long) sessionFactory.getCurrentSession()
    			.getNamedQuery("Tag.countAll")
    			.iterate().next();
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public List<Tag> findAll() {
        return sessionFactory.getCurrentSession()
                .createQuery("from Tag t")
                .list();
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public List<Tag> findAllWithChildren(boolean includeSpecialTags) {
    	final Session s = sessionFactory.getCurrentSession();
    	final String qs = includeSpecialTags ? 
    			"Tag.findIdsOrderByTagName" : "Tag.findIdsNonSpecialOrderByTagName";
    	final List<Long> ids = s.getNamedQuery(qs).list();
    	final List<Tag> output = new ArrayList<Tag>();
    	final Query q = s.getNamedQuery("Tag.findByIdWithChildren");
    	for (long id : ids) {
    		output.add((Tag) q.setParameter("id", id).uniqueResult());
		}
    	return output;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void findIdsOfAllDescendants(Long parentTagId, Session s, Set<Long> idsInOut) {
    	if (s == null) {
    		s = sessionFactory.getCurrentSession();
    	}
    	final List<Long> idsOfChildren = s.getNamedQuery("Tag.findIdsOfChildren").setParameter("id", parentTagId).list();
    	idsInOut.addAll(idsOfChildren);
    	for (Long cid : idsOfChildren) {
			findIdsOfAllDescendants(cid, s, idsInOut);
		}
    }
    
    @Override
    public long countAllDescendants(Long id) {
    	final Set<Long> descendantIds = new HashSet<Long>();
		findIdsOfAllDescendants(id, sessionFactory.getCurrentSession(), descendantIds);
		return descendantIds.size();
    }
    
    @Override
    public Tag findById(Long id) {
    	return (Tag) sessionFactory.getCurrentSession().get(Tag.class, id);
    }

    @Override
    public Tag findById(Long id, boolean withFragments, boolean withChildren) {
    	Tag output = findById(id);
    	if (withFragments) {
    		Hibernate.initialize(output.getFragments());
    	}
    	if (withChildren) {
    		Hibernate.initialize(output.getChildren());
    	}
    	return output;
    }
    
    @Override
    public void populate(Tag target, boolean withFragments, boolean withChildren) {
    	final Tag tmp = (Tag) sessionFactory.getCurrentSession().get(Tag.class, target.getId());
    	if (withFragments) {
    		Hibernate.initialize(tmp.getFragments());
    		target.setFragments(tmp.getFragments());
    	}
    	if (withChildren) {
    		Hibernate.initialize(tmp.getChildren());
    		target.setChildren(tmp.getChildren());
    	}
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Tag> findParentTags(Long id) {
        return sessionFactory.getCurrentSession()
                .getNamedQuery("Tag.findParentTags")
                .setParameter("id", id)
                .list();
    }

    @Override
    public void save(Tag tag) {
        sessionFactory.getCurrentSession().saveOrUpdate(tag);
    }
    
    @Override
    public void saveWithHierarchy(Tag tag, Collection<Tag> parents, Collection<Tag> children) {
    	final Session session = sessionFactory.getCurrentSession();
    	
    	if (parents.contains(tag)) {
    		throw new IllegalArgumentException("The target tag '" + tag.getTagName() +  "' exists in its parents list!");
    	}
    	if (children.contains(tag)) {
    		throw new IllegalArgumentException("The target tag '" + tag.getTagName() +  "' exists in its children list!");
    	}
    	if (Collections.disjoint(parents, children) == false) {
    		throw new IllegalArgumentException("The parents and children have one or more tags in common!");
    	}
    	
    	// Persist the target tag without its relationships touched;
    	save(tag);
    	
    	tag = (Tag) session.get(Tag.class, tag.getId());
    	final long tid = tag.getId();
    	
    	final String[] qs = {
    			// delete all existing parent relationships
    			"delete from tag2tag where child_id = " + tid,
    			// delete all existing child relationships
    			"delete from tag2tag where parent_id = " + tid,
    	};
    	
    	session.createSQLQuery(qs[0]).executeUpdate();
    	session.createSQLQuery(qs[1]).executeUpdate();
    	
    	// reconstruct parent relationships
    	for (Tag p : parents) {
    		final String s = "insert into tag2tag(parent_id, child_id) values (" +p.getId()+ ", " +tid+ ")";
    		session.createSQLQuery(s).executeUpdate();
    	}
    	
    	// reconstruct child relationships
    	for (Tag c : children) {
    		final String s = "insert into tag2tag(parent_id, child_id) values (" +tid+ ", " +c.getId()+ ")";
    		session.createSQLQuery(s).executeUpdate();
    	}
    }

    @Override
    public void delete(Tag tag) {
        sessionFactory.getCurrentSession().delete(tag);
    }

}
