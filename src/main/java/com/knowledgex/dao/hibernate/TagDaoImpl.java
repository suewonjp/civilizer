package com.knowledgex.dao.hibernate;

import java.util.*;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
    
//    @Override
//    @SuppressWarnings("unchecked")
//    public List<Long> findFragmentIds(Long id) {
//    	return sessionFactory.getCurrentSession()
//                .getNamedQuery("Tag.findFragmentIds")
//                .setParameter("id", id)
//                .list();
//    }
//    
//    @Override
//    @SuppressWarnings("unchecked")
//    public List<Fragment> findFragments(Long id) {
//        return sessionFactory.getCurrentSession()
//                .getNamedQuery("Tag.findFragments")
//                .setParameter("id", id)
//                .list();
//    }
//
//	@Override
//	@SuppressWarnings("unchecked")
//	public List<Fragment> findFragments(Long id, int first, int count) {
//	    return sessionFactory.getCurrentSession()
//                .getNamedQuery("Tag.findFragments")
//                .setParameter("id", id)
//                .setFirstResult(first)
//                .setMaxResults(count)
//                .list();
//	}
//	
//	@Override
//	@SuppressWarnings("unchecked")
//	public List<Fragment> findNonTrashedFragments(Long id, int first, int count) {
//		return sessionFactory.getCurrentSession()
//				.getNamedQuery("Tag.findNonTrashedFragments")
//				.setParameter("id", id)
//				.setFirstResult(first)
//				.setMaxResults(count)
//				.list();
//	}
//    
//    @Override
//    @SuppressWarnings("unchecked")
//	public List<Fragment> findFragments(
//			Collection<Long> idsIn
//			, Collection<Long> idsEx
//			) {
//    	if (idsIn == null || idsIn.isEmpty()) {
//    		// Empty inclusion filter, empty results
//    		return null;
//    	}
//    	
//    	final Set<Long> setIn = new HashSet<Long>(idsIn); // needed for removing duplications
//    	final List<Fragment> output =
//    	        sessionFactory.getCurrentSession()
//                .getNamedQuery("Tag.findFragmentsWithIdFilter")
//                .setParameterList("ids", setIn)
//                .list();
//    	
//    	if (idsEx == null || idsEx.isEmpty()) {
//    		// We have an inclusive filter only
//    		return output;
//    	}
//    	
//    	// We have an exclusive filter
//    	Fragment.applyExclusiveTagFilter(output, idsEx);
//    	return output;
//	}
    
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
