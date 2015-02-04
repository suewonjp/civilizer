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

@Repository("fragmentDao")
@Transactional
public final class FragmentDaoImpl implements FragmentDao {

    private Log log = LogFactory.getLog(FragmentDaoImpl.class);

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
    public List<Fragment> findAll() {
        return sessionFactory.getCurrentSession()
                .createQuery("from Fragment f")
                .list();
    }

	@Override
	@SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
	public List<Fragment> findSome(int first, int count) {
	    first = Math.max(0, first);
	    count = Math.max(0, count);
	    Session s = sessionFactory.getCurrentSession();
	    List<Long> ids = s.getNamedQuery("Fragment.findIdsOrderByUpdateDatetime")
	            .setFirstResult(first)
                .setMaxResults(count)
                .list();
	    List<Fragment> output = new ArrayList<Fragment>(count);
	    count = Math.min(count, ids.size());
	    Query q = s.getNamedQuery("Fragment.findByIdWithTags");
	    for (int i = 0; i < count; ++i) {
            output.add((Fragment) q.setParameter("id", ids.get(i)).uniqueResult());
        }
	    return output;
//		return sessionFactory.getCurrentSession()
//                .createQuery("from Fragment f order by f.updateDatetime desc")
//                .setFirstResult(first)
//                .setMaxResults(count)
//                .list();
	}

    @Override
    public Fragment findById(Long id) {
        return (Fragment) sessionFactory.getCurrentSession()
                .getNamedQuery("Fragment.findById")
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
