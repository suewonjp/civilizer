package com.knowledgex.dao;

import java.util.*;

import com.knowledgex.domain.Fragment;
import com.knowledgex.domain.Tag;

public interface TagDao {
    public Collection<Tag> findAll();

    public Tag findById(Long id);

    public Tag findByIdWithChildren(Long id);

    public Tag findByIdWithFragments(Long id);

    public Collection<Fragment> findFragments(Long id);
    
    public Collection<Tag> findParentTags(Long id);

    public Tag save(Tag tag);

    public void delete(Tag tag);
}
