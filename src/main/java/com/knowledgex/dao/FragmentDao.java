package com.knowledgex.dao;

import java.util.*;

import com.knowledgex.domain.Fragment;
import com.knowledgex.domain.FragmentOrder;

public interface FragmentDao {
	public long countAll(boolean includeTrashed);
	
    public List<Fragment> findAll();

    public List<Fragment> findNonTrashed();
    
    public List<Fragment> findSome(int first, int count);

    public List<Fragment> findSomeNonTrashed(int first, int count);

    public List<Fragment> findSomeNonTrashed(int first, int count, FragmentOrder order, boolean asc);

    public Fragment findById(Long id);

    public Fragment findByIdWithAll(Long id);

    public Fragment findByIdWithRelatedOnes(Long id);
    
    public Fragment findByIdWithTags(Long id);

    public Fragment save(Fragment frgm);

    public void delete(Fragment frgm);
}
