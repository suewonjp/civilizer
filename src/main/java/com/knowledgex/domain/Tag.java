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
import javax.persistence.OneToMany;
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
public final class Tag implements Serializable {
	
	public static final String TAG_NAME_DELIMITER = ",";
	public static final long TRASH_TAG_ID = 0L;

    private Long id;
    private String tagName;
    private DateTime creationDatetime;
    private DateTime updateDatetime;
    private String creator;
    private String updater;
    private Long fragmentId;
    private Set<Fragment> fragments = Collections.emptySet();
    private Set<Tag> children = Collections.emptySet();

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
    
    @OneToMany(fetch=FetchType.LAZY)
    @Cascade({CascadeType.MERGE
        , CascadeType.REFRESH
        , CascadeType.SAVE_UPDATE
        , CascadeType.DETACH
    })
    @JoinTable(name = "TAG2FRAGMENT",
        joinColumns = @JoinColumn(name = "tag_id"),
        inverseJoinColumns = @JoinColumn(name = "fragment_id"))
    public Set<Fragment> getFragments() {
        return this.fragments;
    }

    public void setFragments(Set<Fragment> fragments) {
        this.fragments = fragments;
    }

    @OneToMany(fetch=FetchType.LAZY)
    @Cascade({CascadeType.MERGE
        , CascadeType.REFRESH
        , CascadeType.SAVE_UPDATE
        , CascadeType.DETACH
    })
    @JoinTable(name = "TAG2TAG",
        joinColumns = @JoinColumn(name = "parent_id"),
        inverseJoinColumns = @JoinColumn(name = "child_id"))
    public Set<Tag> getChildren() {
        return children;
    }

    public void setChildren(Set<Tag> children) {
        this.children = children;
    }
    
    public void addChild(Tag c) {
        if (children.equals(Collections.emptySet())) {
            children = new HashSet<Tag>();
        }
        children.add(c);
    }
    
    public static String getTagNamesFrom(Collection<Tag> tags) {
    	String result = new String();
    	for (Tag t : tags) {
    		result += t.tagName + TAG_NAME_DELIMITER;
    	}
    	return result;
    }
    
    public static Collection<String> getTagNameCollectionFrom(String names) {
    	if (names == null || names.trim().isEmpty()) {
    		return Collections.emptyList();
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

    public static Collection<Long> getTagIdCollectionFrom(Collection<Tag> tags) {
        List<Long> output = new ArrayList<Long>(tags.size());
        for (Tag t : tags) {
            output.add(t.getId());
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

    public static Tag getTagFromId(long id, Collection<Tag> tags) {
    	Tag tag = null;
    	for (Tag t : tags) {
    		if (t.getId() == id) {
    			tag = t;
    			break;
    		}
    	}
    	return tag;
    }
    
    public static Collection<Tag> getTopParentTags(Collection<Tag> tags) {
        if (tags  == null|| tags.isEmpty()) {
            return null;
        }
        Collection<Tag> output = new ArrayList<Tag>(tags);
        for (Tag t : tags) {
            for (Tag c : t.getChildren()) {
                if (output.contains(c)) {
                    output.remove(c);
                }
            }
        }
        return output;
    }
    
    public static boolean containsId(Collection<Tag> tags, long id) {
    	if (tags == null || tags.isEmpty()) {
    		return false;
    	}
    	
    	for (Tag t : tags) {
    		if (t.getId().equals(id)) {
    			return true;
    		}
    	}
    	
    	return false;
    }
    
    public static boolean containsName(Collection<Tag> tags, String name) {
    	if (name != null && name.isEmpty()) {
    		name = null;
    	}
    	if (tags != null && tags.isEmpty()) {
    		tags = null;
    	}
    	
    	boolean nameNull = (name == null);
    	if (nameNull ^ (tags == null)) {
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
    
    public static boolean isTrashTag(long id) {
    	return  (id == TRASH_TAG_ID);
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
        return  getTagName();
    }

}
