package com.knowledgex.domain;

import static javax.persistence.GenerationType.IDENTITY;

import java.io.Serializable;
import java.util.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

@SuppressWarnings("serial")
@Entity
@Table(name = "TAG")
@NamedQueries({
    @NamedQuery(name = "Tag.findById",
            query = "select distinct t from Tag t where t.id = :id"),
    @NamedQuery(name = "Tag.findByIdWithChildren",
            query = "select distinct t from Tag t left join fetch t.children where t.id = :id"),
    @NamedQuery(name = "Tag.findByIdWithFragments",
            query = "select distinct t from Tag t left join fetch t.fragments where t.id = :id"),
    @NamedQuery(name = "Tag.findFragments",
            query = "select t.fragments from Tag t where t.id = :id"),
    @NamedQuery(name = "Tag.findParentTags",
            query = "select distinct t from Tag t inner join t.children child where child.id = :id"),
})
public class Tag implements Serializable {
	
	public static final String TAG_NAME_DELIMITER = ",";

    private Long id;
    private String tagName;
    private DateTime creationDatetime;
    private DateTime updateDatetime;
    private String creator;
    private String updater;
    private Long fragmentId;
    private Collection<Fragment> fragments = new ArrayList<Fragment>();
    private Collection<Tag> children = new ArrayList<Tag>();

    public Tag() {
    }

    public Tag(String name) {
    	setTagName(name);
    	DateTime dt = new DateTime();
    	setCreationDatetime(dt);
        setUpdateDatetime(dt);
    }

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "tag_id")
    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "tag_name", unique = true)
    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    @Column(name = "creation_datetime")
    @Type(type="org.joda.time.contrib.hibernate.PersistentDateTime")
    @DateTimeFormat(iso=ISO.DATE)
    public DateTime getCreationDatetime() {
        return creationDatetime;
    }

    public void setCreationDatetime(DateTime creationDatetime) {
        this.creationDatetime = creationDatetime;
    }

    @Column(name = "update_datetime")
    @Type(type="org.joda.time.contrib.hibernate.PersistentDateTime")
    @DateTimeFormat(iso=ISO.DATE)
    public DateTime getUpdateDatetime() {
        return updateDatetime;
    }

    public void setUpdateDatetime(DateTime updateDatetime) {
        this.updateDatetime = updateDatetime;
    }

    @Column(name = "creator")
    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    @Column(name = "updater")
    public String getUpdater() {
        return updater;
    }

    public void setUpdater(String updater) {
        this.updater = updater;
    }

    @Column(name = "fragment_id")
    public Long getFragmentId() {
        return fragmentId;
    }

    public void setFragmentId(Long fragmentId) {
        this.fragmentId = fragmentId;
    }
    
    @ManyToMany(fetch=FetchType.LAZY)
    @Cascade({CascadeType.MERGE
        , CascadeType.REFRESH
        , CascadeType.SAVE_UPDATE
        , CascadeType.DETACH
    })
    @JoinTable(name = "TAG2FRAGMENT",
        joinColumns = @JoinColumn(name = "tag_id"),
        inverseJoinColumns = @JoinColumn(name = "fragment_id"))
    public Collection<Fragment> getFragments() {
        return this.fragments;
    }

    public void setFragments(Collection<Fragment> fragments) {
        this.fragments = fragments;
    }

    @ManyToMany(fetch=FetchType.LAZY)
    @Cascade({CascadeType.MERGE
        , CascadeType.REFRESH
        , CascadeType.SAVE_UPDATE
        , CascadeType.DETACH
    })
    @JoinTable(name = "TAG2TAG",
        joinColumns = @JoinColumn(name = "parent_id"),
        inverseJoinColumns = @JoinColumn(name = "child_id"))
    public Collection<Tag> getChildren() {
        return children;
    }

    public void setChildren(Collection<Tag> children) {
        this.children = children;
    }
    
    public void addChild(Tag c) {
        this.children.add(c);
    }
    
    public static String getTagNamesFrom(Collection<Tag> tags) {
    	String result = new String();
    	for (Tag t : tags) {
    		result += t.tagName + TAG_NAME_DELIMITER;
    	}
    	return result;
    }
    
    public static Collection<String> getTagNameCollectionFrom(String names) {
    	if (null == names || names.trim().isEmpty()) {
    		return new ArrayList<String>();
    	}
    	String[] arr = names.split("\\s*[" + TAG_NAME_DELIMITER + "]+\\s*");
    	List<String> output = new ArrayList<String>();
    	for (String s : arr) {
    		if (!s.isEmpty()) {
    			output.add(s);
    		}
    	}
    	return output;
    }

    public static Collection<String> getTagNameCollectionFrom(Collection<Tag> tags) {
    	List<String> output = new ArrayList<String>(tags.size());
    	for (Tag t : tags) {
    		output.add(t.getTagName());
    	}
    	return output;
    }
    
    public static Tag getTagFromName(String tagName, Collection<Tag> tags) {
    	Tag tag = null;
    	for (Tag t : tags) {
    		if (t.getTagName().equals(tagName)) {
    			tag = t;
    			break;
    		}
    	}
    	return tag;
    }
    
    public static boolean containsName(Collection<Tag> tags, String name) {
    	if (null != name && name.isEmpty()) {
    		name = null;
    	}
    	if (null != tags && tags.isEmpty()) {
    		tags = null;
    	}
    	
    	boolean nameNull = (null == name);
    	if (nameNull ^ (null == tags)) {
    		return false;
    	}
    	else {
    		if (nameNull) {
    			return true;
    		}    		
    	}
    	
    	for (Tag t : tags) {
    		if (t.getTagName().equals(name)) {
    			return true;
    		}
    	}
    	
    	return false;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = prime + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Tag other = (Tag) obj;
        final Long id = getId();
        final Long otherId = other.getId();
        if (id == null) {
            if (otherId != null)
                return false;
        } else if (!id.equals(otherId))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return  "Tag - id: " + id
                + ", name: " + tagName
                + ", created at: "+ creationDatetime
                + ", updated at: "+ updateDatetime
                ;
    }

}
