package com.knowledgex.dao.hibernate;

import java.util.Collection;

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

    private Log log = LogFactory.getLog(TagDaoImpl.class);

    private SessionFactory sessionFactory;

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    @Resource(name = "sessionFactory")
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
    
    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public Collection<Tag> findAll() {
        return sessionFactory.getCurrentSession()
                .createQuery("from Tag t").list();
    }

    public Tag findById(Long id) {
        return (Tag) sessionFactory.getCurrentSession()
                .getNamedQuery("Tag.findById")
                .setParameter("id", id)
                .uniqueResult();
    }

    public Tag findByIdWithChildren(Long id) {
        return (Tag) sessionFactory.getCurrentSession()
                .getNamedQuery("Tag.findByIdWithChildren")
                .setParameter("id", id)
                .uniqueResult();
    }

    public Tag findByIdWithFragments(Long id) {
        return (Tag) sessionFactory.getCurrentSession()
                .getNamedQuery("Tag.findByIdWithFragments")
                .setParameter("id", id)
                .uniqueResult();
    }
    
    @SuppressWarnings("unchecked")
    public Collection<Fragment> findFragments(Long id) {
        return sessionFactory.getCurrentSession()
                .getNamedQuery("Tag.findFragments")
                .setParameter("id", id)
                .list();
    }
    
    @SuppressWarnings("unchecked")
    public Collection<Tag> findParentTags(Long id) {
        return sessionFactory.getCurrentSession()
                .getNamedQuery("Tag.findParentTags")
                .setParameter("id", id)
                .list();
    }

    public Tag save(Tag tag) {
        sessionFactory.getCurrentSession().saveOrUpdate(tag);
        log.info("Tag saved with id: " + tag.getId());
        return tag;
    }

    public void delete(Tag tag) {
        sessionFactory.getCurrentSession().delete(tag);
        log.info("Tag deleted with id: " + tag.getId());
    }

}
