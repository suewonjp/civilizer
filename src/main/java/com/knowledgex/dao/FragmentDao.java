package com.knowledgex.dao;

import java.util.*;

import com.knowledgex.domain.Fragment;
import com.knowledgex.domain.FragmentOrder;

public interface FragmentDao {
    
    public List<?> executeQuery(String query);
    
	public long countAll(boolean includeTrashed);
	
	public long countByTagId(long tagId, boolean includeTrashed);
	
    public List<Fragment> findAll(boolean includeTrashed);

    public List<Fragment> findByTagId(long tagId, boolean includeTrashed);

    public List<Fragment> findByTagIds(Collection<Long> idsIn, Collection<Long> idsEx);

    public List<Fragment> findSomeByTagId(long tagId, int first, int count, FragmentOrder order, boolean asc);

    public List<Fragment> findSomeNonTrashed(int first, int count, FragmentOrder order, boolean asc);

    public List<Fragment> findSomeNonTrashedByTagId(long tagId, int first, int count, FragmentOrder order, boolean asc, TagDao tagDao);

    public Fragment findById(Long id);

    public Fragment findById(Long id, boolean withTags, boolean withRelatedOnes);

    public List<Long> findIdsByTagId(long tagId);

    public Fragment save(Fragment frgm);

    public void delete(Fragment frgm);
    
}
