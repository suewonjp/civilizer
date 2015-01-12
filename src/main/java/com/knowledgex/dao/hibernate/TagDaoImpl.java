package com.knowledgex.dao.hibernate;

import java.util.*;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.knowledgex.dao.TagDao;
import com.knowledgex.domain.Fragment;
import com.knowledgex.domain.Tag;

@Repository("tagDao")
@Transactional
public class TagDaoImpl implements TagDao {

    private Log log = LogFactory.getLog(TagDaoImpl.class);

    private SessionFactory sessionFactory;

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    @Resource(name = "sessionFactory")
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public Collection<Tag> findAll() {
        return sessionFactory.getCurrentSession()
                .createQuery("from Tag t").list();
    }

    @Override
    public Tag findById(Long id) {
        return (Tag) sessionFactory.getCurrentSession()
                .getNamedQuery("Tag.findById")
                .setParameter("id", id)
                .uniqueResult();
    }

    @Override
    public Tag findByIdWithChildren(Long id) {
        return (Tag) sessionFactory.getCurrentSession()
                .getNamedQuery("Tag.findByIdWithChildren")
                .setParameter("id", id)
                .uniqueResult();
    }

    @Override
    public Tag findByIdWithFragments(Long id) {
        return (Tag) sessionFactory.getCurrentSession()
                .getNamedQuery("Tag.findByIdWithFragments")
                .setParameter("id", id)
                .uniqueResult();
    }    
    
    @Override
    @SuppressWarnings("unchecked")
    public Collection<Fragment> findFragments(Long id) {
        return sessionFactory.getCurrentSession()
                .getNamedQuery("Tag.findFragments")
                .setParameter("id", id)
                .list();
    }
    
    @Override
    @SuppressWarnings("unchecked")
	public Collection<Fragment> findFragments(
			Collection<Long> idsIn
			, Collection<Long> idsEx
			) {
    	if (null == idsIn || idsIn.isEmpty()) {
    		// Empty inclusion filter, empty results
    		return null;
    	}
    	Set<Long> setIn = new HashSet<Long>(idsIn); // needed for removing duplications
    	if (null == idsEx || idsEx.isEmpty()) {
    		// We an have inclusion filter only
    		return sessionFactory.getCurrentSession()
		            .getNamedQuery("Tag.findFragmentsWithIdFilterIn")
		            .setParameterList("idsIn", setIn)
		            .list();
    	}
    	else {    
    		// We have an exclusion filter.
    		Set<Long> setEx = new HashSet<Long>(idsEx);
    		return sessionFactory.getCurrentSession()
    				.getNamedQuery("Tag.findFragmentsWithIdFilterInEx")
    				.setParameterList("idsIn", setIn)
    				.setParameterList("idsEx", setEx)
    				.list();
    	}
	}
    
    @Override
    @SuppressWarnings("unchecked")
    public Collection<Tag> findParentTags(Long id) {
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
