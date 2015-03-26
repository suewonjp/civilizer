package com.civilizer.dao;

import java.util.*;

import com.civilizer.domain.FileEntity;

public interface FileEntityDao {

	public long countAll();
	
    public List<FileEntity> findAll();

    public FileEntity findById(Long id);

    public FileEntity findByName(String name);

    public void save(FileEntity fe);

    public void delete(FileEntity fe);
    
}
