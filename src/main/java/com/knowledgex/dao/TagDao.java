package com.knowledgex.dao;

import java.util.List;

import com.knowledgex.domain.Fragment;
import com.knowledgex.domain.Tag;

public interface TagDao {
    public List<Tag> findAll();

    public Tag findById(Long id);

    public Tag findByIdWithChildren(Long id);

    public Tag findByIdWithFragments(Long id);

    public List<Fragment> findFragments(Long id);
    
    public List<Tag> findParentTags(Long id);

    public Tag save(Tag tag);

    public void delete(Tag tag);
}
