package com.knowledgex.dao.hibernate;

import java.util.*;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.knowledgex.dao.TagDao;
import com.knowledgex.domain.*;

@Repository("tagDao")
@Transactional
public final class TagDaoImpl implements TagDao {

    private final Log log = LogFactory.getLog(TagDaoImpl.class);

    private SessionFactory sessionFactory;
    
    @Resource(name = "sessionFactory")
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
    
    @Override
    public List<?> executeQuery(String query) {
        return sessionFactory.getCurrentSession()
                .createQuery(query)
                .list();
    }
    
    @Override
    public long countAll() {
    	return (Long) sessionFactory.getCurrentSession()
    			.getNamedQuery("Tag.countAll")
    			.iterate().next();
    }
    
    @Override
    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public List<Tag> findAll() {
        return sessionFactory.getCurrentSession()
                .createQuery("from Tag t")
                .list();
    }
    
    @Override
    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public List<Tag> findAllWithChildren() {
    	final Session s = sessionFactory.getCurrentSession();
    	final List<Long> ids = s.getNamedQuery("Tag.findIdsOrderByTagName").list();
    	final List<Tag> output = new ArrayList<Tag>();
    	final Query q = s.getNamedQuery("Tag.findByIdWithChildren");
    	for (long id : ids) {
    		output.add((Tag) q.setParameter("id", id).uniqueResult());
		}
    	return output;
    }

    @Override
    public Tag findById(Long id) {
        return (Tag) sessionFactory.getCurrentSession()
                .getNamedQuery("Tag.findById")
                .setParameter("id", id)
                .uniqueResult();
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
    @SuppressWarnings("unchecked")
    public List<Tag> findParentTags(Long id) {
        return sessionFactory.getCurrentSession()
                .getNamedQuery("Tag.findParentTags")
                .setParameter("id", id)
                .list();
    }

    @Override
    public Tag save(Tag tag) {
        sessionFactory.getCurrentSession().saveOrUpdate(tag);
        log.info("Tag saved with id: " + tag.getId());
        return tag;
    }

    @Override
    public void delete(Tag tag) {
        sessionFactory.getCurrentSession().delete(tag);
        log.info("Tag deleted with id: " + tag.getId());
    }

}
