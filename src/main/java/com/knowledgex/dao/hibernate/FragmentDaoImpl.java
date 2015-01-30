package com.knowledgex.dao.hibernate;

import java.util.Collection;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.knowledgex.dao.FragmentDao;
import com.knowledgex.domain.Fragment;

@Repository("fragmentDao")
@Transactional
public class FragmentDaoImpl implements FragmentDao {

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
    public Collection<Fragment> findAll() {
        return sessionFactory.getCurrentSession()
                .createQuery("from Fragment f").list();
    }

	@Override
	@SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
	public Collection<Fragment> findSome(int first, int count) {
	    first = Math.max(0, first);
	    count = Math.max(0, count);
		return sessionFactory.getCurrentSession()
                .createQuery("from Fragment f")
                .setFirstResult(first)
                .setMaxResults(count)
                .list();
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
