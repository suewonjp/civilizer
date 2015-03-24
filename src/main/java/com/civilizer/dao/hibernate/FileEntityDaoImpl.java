package com.civilizer.dao.hibernate;

import java.util.List;

import javax.annotation.Resource;

import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.civilizer.dao.FileEntityDao;
import com.civilizer.domain.FileEntity;

@Repository("fileEntityDao")
@Transactional
public class FileEntityDaoImpl implements FileEntityDao {
	
	private SessionFactory sessionFactory;
    
    @Resource(name = "sessionFactory")
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

	@Override
	public long countAll() {
		return 0;
	}

	@Override
	public List<FileEntity> findAll() {
		return null;
	}

	@Override
	public FileEntity findById(Long id) {
		return null;
	}

	@Override
	public FileEntity save(FileEntity fe) {
		sessionFactory.getCurrentSession().saveOrUpdate(fe);
        return fe;
	}

	@Override
	public void delete(FileEntity fe) {
		sessionFactory.getCurrentSession().delete(fe);
	}

}
