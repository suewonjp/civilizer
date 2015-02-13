package com.knowledgex.dao;

import java.util.*;

import com.knowledgex.domain.*;

public interface TagDao {
    
    public List<?> executeQuery(String query);
    
	public long countAll();
	
    public List<Tag> findAll();

    public List<Tag> findAllWithChildren();

    public Tag findById(Long id);

    public Tag findByIdWithChildren(Long id);

    public Tag findByIdWithFragments(Long id);
    
//    public List<Long> findFragmentIds(Long id);
//    
//    public List<Fragment> findFragments(Long id);
//
//    public List<Fragment> findFragments(Long id, int first, int count);
//
//    public List<Fragment> findNonTrashedFragments(Long id, int first, int count);
//    
//    public List<Fragment> findFragments(
//    		Collection<Long> idsIn
//    		, Collection<Long> idsEx
//    		);
    
    public List<Tag> findParentTags(Long id);

    public Tag save(Tag tag);

    public void delete(Tag tag);
    
}
