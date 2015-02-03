package com.knowledgex.dao.hibernate;

import java.util.*;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.knowledgex.dao.TagDao;
import com.knowledgex.domain.Fragment;
import com.knowledgex.domain.Tag;

@Repository("tagDao")
@Transactional
public class TagDaoImpl implements TagDao {

    final private Log log = LogFactory.getLog(TagDaoImpl.class);

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
    public List<Tag> findAll() {
        return sessionFactory.getCurrentSession()
                .createQuery("from Tag t")
                .list();
    }
    
    @Override
    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public List<Tag> findAllWithChildren() {
        return sessionFactory.getCurrentSession()
                .getNamedQuery("Tag.findAllWithChildren")
                .list();
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
    public List<Fragment> findFragments(Long id) {
        List<Fragment> output = (List<Fragment>)
                sessionFactory.getCurrentSession()
                .getNamedQuery("Tag.findFragments")
                .setParameter("id", id)
                .list();
        return output;
    }

	@Override
	@SuppressWarnings("unchecked")
	public List<Fragment> findFragments(Long id, int first, int count) {
	    List<Fragment> output = (List<Fragment>)
	            sessionFactory.getCurrentSession()
                .getNamedQuery("Tag.findFragments")
                .setParameter("id", id)
                .setFirstResult(first)
                .setMaxResults(count)
                .list();
	    return output;
	}
    
    @Override
    @SuppressWarnings("unchecked")
	public List<Fragment> findFragments(
			Collection<Long> idsIn
			, Collection<Long> idsEx
			) {
    	if (null == idsIn || idsIn.isEmpty()) {
    		// Empty inclusion filter, empty results
    		return null;
    	}
    	Set<Long> setIn = new HashSet<Long>(idsIn); // needed for removing duplications
    	List<Fragment> output =
    	        sessionFactory.getCurrentSession()
                .getNamedQuery("Tag.findFragmentsWithIdFilter")
                .setParameterList("ids", setIn)
                .list();
    	if (null == idsEx || idsEx.isEmpty()) {
    		// We have an inclusive filter only
    		return output;
    	}
    	// We have an exclusive filter
    	Fragment.applyExclusiveTagFilter(output, idsEx);
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
