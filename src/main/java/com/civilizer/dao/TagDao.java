package com.civilizer.dao;

import java.util.*;

import org.hibernate.Session;

import com.civilizer.domain.*;

public interface TagDao {
    
    public List<?> executeQueryForResult(String query);

    public void executeQuery(String query, boolean sql);
    
	public long countAll();
	
    public List<Tag> findAll();

    public List<Tag> findAllWithChildren(boolean includeSpecialTags);

    public void findIdsOfAllDescendants(Long parentTagId, Session s, Set<Long> idsInOut);

    public long countAllDescendants(Long id);

    public Tag findById(Long id);

    public Tag findById(Long id, boolean withFragments, boolean withChildren);
    
    public void populate(Tag target, boolean withFragments, boolean withChildren);

    public List<Tag> findParentTags(Long id);

    public void save(Tag tag);

    public void saveWithParents(Tag tag, Collection<Tag> parents);

    public void delete(Tag tag);
    
}
