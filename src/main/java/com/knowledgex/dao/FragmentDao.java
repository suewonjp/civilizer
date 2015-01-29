package com.knowledgex.dao;

import java.util.*;

import com.knowledgex.domain.Fragment;

public interface FragmentDao {
    public Collection<Fragment> findAll();
    
    public Collection<Fragment> findSome(int first, int count);

    public Fragment findById(Long id);

    public Fragment findByIdWithRelatedOnes(Long id);
    
    public Fragment findByIdWithTags(Long id);

    public Fragment save(Fragment frgm);

    public void delete(Fragment frgm);
}
