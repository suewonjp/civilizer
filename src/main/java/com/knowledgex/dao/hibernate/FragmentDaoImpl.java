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

import com.knowledgex.dao.FragmentDao;
import com.knowledgex.domain.Fragment;
import com.knowledgex.domain.FragmentOrder;

@Repository("fragmentDao")
@Transactional
public final class FragmentDaoImpl implements FragmentDao {

    private final Log log = LogFactory.getLog(FragmentDaoImpl.class);

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
    public long countAll(boolean includeTrashed) {
    	final String nq = includeTrashed ?
    			"Fragment.countAll" : "Fragment.countAllNonTrashed";
    	return (Long) sessionFactory.getCurrentSession()
    			.getNamedQuery(nq)
    			.iterate().next();
    }
    
    @Override
    public long countByTagId(long tagId, boolean includeTrashed) {
        final String nq = includeTrashed ?
                "Fragment.countByTagId" : "Fragment.countNonTrashedByTagId";
        return (Long) sessionFactory.getCurrentSession()
                .getNamedQuery(nq)
                .setParameter("tagId", tagId)
                .iterate().next();
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public List<Fragment> findAll() {
        return sessionFactory.getCurrentSession()
                .createQuery("from Fragment f")
                .list();
    }

    @Override
    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public List<Fragment> findNonTrashed() {
        return sessionFactory.getCurrentSession()
                .createQuery(
                          "select distinct f "
                        + "from Fragment f "
                        + "where f.id not in ( "
                        + "  select t2f.fragmentId "
                        + "  from Tag2Fragment t2f "
                        + "  where t2f.tagId = 0 "
                        + ") "
                        )
                .list();
    }
    
    @Override
    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public List<Fragment> findByTagId(long tagId, boolean includeTrashed) {
        final String nq = includeTrashed ?
                "Fragment.findByTagId" : "Fragment.findNonTrashedByTagId";
        return sessionFactory.getCurrentSession()
                .getNamedQuery(nq)
                .setParameter("tagId", tagId)
                .list();
    }

	@Override
	@SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
	public List<Fragment> findSomeByTagId(long tagId, int first, int count, FragmentOrder order, boolean asc) {
	    
	    first = Math.max(0, first);
        count = Math.max(0, count);
        final Session s = sessionFactory.getCurrentSession();
        final String[] namedQueries = {
                "Fragment.findIdsByTagIdOrderByUpdateDatetime"
                , "Fragment.findIdsByTagIdOrderByCreationDatetime"
                , "Fragment.findIdsByTagIdOrderByTitle"
                , "Fragment.findIdsByTagIdOrderById"
        };
        final List<Long> ids = s.getNamedQuery(namedQueries[order.ordinal()])
                .setParameter("tagId", tagId)
                .list();
        if (asc) {
            // default sort direction is descending
            Collections.reverse(ids);
        }
        final List<Fragment> output = new ArrayList<Fragment>(count);
        count = Math.min(count, ids.size()-first);
        Query q = s.getNamedQuery("Fragment.findByIdWithAll");
        for (int i = 0; i < count; ++i) {
            output.add((Fragment) q.setParameter("id", ids.get(i + first)).uniqueResult());
        }
        return output;
	}
	
	@Override
	@SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
	public List<Fragment> findSomeNonTrashed(int first, int count, FragmentOrder order, boolean asc) {
		first = Math.max(0, first);
	    count = Math.max(0, count);
	    final Session s = sessionFactory.getCurrentSession();
	    final String[] namedQueries = {
	    		"Fragment.findIdsNonTrashedOrderByUpdateDatetime"
	    		, "Fragment.findIdsNonTrashedOrderByCreationDatetime"
	    		, "Fragment.findIdsNonTrashedOrderByTitle"
	    		, "Fragment.findIdsNonTrashedOrderById"
	    };
	    final List<Long> ids = s.getNamedQuery(namedQueries[order.ordinal()]).list();
	    if (asc) {
	    	// default sort direction is descending
	    	Collections.reverse(ids);
	    }
	    final List<Fragment> output = new ArrayList<Fragment>(count);
	    count = Math.min(count, ids.size()-first);
	    Query q = s.getNamedQuery("Fragment.findByIdWithAll");
	    for (int i = 0; i < count; ++i) {
            output.add((Fragment) q.setParameter("id", ids.get(i + first)).uniqueResult());
        }
	    return output;
	}
	
	@Override
	@SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
	public List<Fragment> findSomeNonTrashedByTagId(long tagId, int first, int count, FragmentOrder order, boolean asc) {
	    first = Math.max(0, first);
        count = Math.max(0, count);
        final Session s = sessionFactory.getCurrentSession();
        final String[] namedQueries = {
                "Fragment.findIdsNonTrashedByTagIdOrderByUpdateDatetime"
                , "Fragment.findIdsNonTrashedByTagIdOrderByCreationDatetime"
                , "Fragment.findIdsNonTrashedByTagIdOrderByTitle"
                , "Fragment.findIdsNonTrashedByTagIdOrderById"
        };
        final List<Long> ids = s.getNamedQuery(namedQueries[order.ordinal()])
                .setParameter("tagId", tagId)
                .list();
        if (asc) {
            // default sort direction is descending
            Collections.reverse(ids);
        }
        final List<Fragment> output = new ArrayList<Fragment>(count);
        count = Math.min(count, ids.size()-first);
        Query q = s.getNamedQuery("Fragment.findByIdWithAll");
        for (int i = 0; i < count; ++i) {
            output.add((Fragment) q.setParameter("id", ids.get(i + first)).uniqueResult());
        }
        return output;
	}

    @Override
    public Fragment findById(Long id) {
        return (Fragment) sessionFactory.getCurrentSession()
                .getNamedQuery("Fragment.findById")
                .setParameter("id", id)
                .uniqueResult();
    }

    @Override
    public Fragment findByIdWithAll(Long id) {
        return (Fragment) sessionFactory.getCurrentSession()
                .getNamedQuery("Fragment.findByIdWithAll")
                .setParameter("id", id)
                .uniqueResult();
    }
    
    @Override
    public Fragment findByIdWithRelatedOnes(Long id) {
        return (Fragment) sessionFactory.getCurrentSession()
                .getNamedQuery("Fragment.findByIdWithRelatedOnes")
                .setParameter("id", id)
                .uniqueResult();
    }

    @Override
    public Fragment findByIdWithTags(Long id) {
        return (Fragment) sessionFactory.getCurrentSession()
                .getNamedQuery("Fragment.findByIdWithTags")
                .setParameter("id", id)
                .uniqueResult();
    }

    @Override
    public Fragment save(Fragment fragment) {
        sessionFactory.getCurrentSession().saveOrUpdate(fragment);
        log.info("Fragment saved with id: " + fragment.getId());
        return fragment;
    }

    @Override
    public void delete(Fragment fragment) {
        sessionFactory.getCurrentSession().delete(fragment);
        log.info("Fragment deleted with id: " + fragment.getId());
    }

}
