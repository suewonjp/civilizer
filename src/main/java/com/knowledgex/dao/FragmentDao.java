package com.knowledgex.dao;

import java.util.List;

import com.knowledgex.domain.Fragment;

public interface FragmentDao {
    public List<Fragment> findAll();

    public Fragment findById(Long id);

    public Fragment findByIdWithRelatedOnes(Long id);
    
    public Fragment findByIdWithTags(Long id);

    public Fragment save(Fragment frgm);

    public void delete(Fragment frgm);
}
