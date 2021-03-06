package com.civilizer.domain;

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

@SuppressWarnings("serial")
@Entity
@Table(name = "TAG")
public final class Tag implements Serializable {
	
    // [RULE] tag names are separable only with commas
	public static final String TAG_NAME_DELIMITER = ",";

	public static final String[] SPECIAL_TAG_NAMES = {
		"#trash",
		"#bookmark",
		"#untagged",
	};
	
	// [RULE] all special tags have predefined id numbers which are zero or minus
	public static final int TRASH_TAG_ID = 0;
	public static final int BOOKMARK_TAG_ID = -1;
	public static final int UNTAGGED_TAG_ID = -2;

    private Long id;
    private String tagName = "";
    private Set<Fragment> fragments = Collections.emptySet();
    private Set<Tag> children = Collections.emptySet();

    public Tag() {
    }

    public Tag(String name) {
    	setTagName(name);
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
        if (tagName != null)
            this.tagName = tagName.intern();
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
    
    public List<Tag> listOfChildren() {
    	return new ArrayList<>(children);
    }
    
    public static String stripDoubleQuotes(String name) {
        if (name.startsWith("\"") || name.endsWith("\"")) {
            return name.replace("\"", "");
        }
        return name;
    }
    
    public static char findInvalidCharFromName(String name) {
        // [RULE] these characters are not accepted to be used in tag names
        final char[] invalidChar = { '\"', ',', '\\', ':' };
        for (char c : invalidChar) {
            if (name.indexOf(c) != -1) {
                return c;
            }
        }
        return (char)0;
    }
    
    public static String getTagNamesFrom(Collection<Tag> tags) {
    	String result = new String();
    	for (Tag t : tags) {
    		result += t.tagName + TAG_NAME_DELIMITER;
    	}
    	return result;
    }
    
    public static String trimName(String src) {
        if (src == null)
            return "";
        src = src.trim();
        final int si = src.startsWith("\"") ? 1 : 0;
        final int ei = src.endsWith("\"") ? src.length()-1 : src.length();
        src = src.substring(si, ei).trim();
        return src;
    }
    
    public static List<String> getTagNameCollectionFrom(String names) {
    	if (names == null || names.trim().isEmpty()) {
    		return Collections.emptyList();
    	}
    	String[] arr = names.split("\\s*[" + TAG_NAME_DELIMITER + "]+\\s*");
    	List<String> output = new ArrayList<String>();
    	for (String s : arr) {
    		s = Tag.trimName(s);
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

    public static int getIndexOf(long id, List<Tag> tags) {
    	final int c = tags.size();
    	int output = -1;
    	for (int i=0; i<c; ++i) {
    		if (tags.get(i).getId() == id) {
    			output = i;
    			break;
    		}
    	}
    	return output;
    }
    
    public static Collection<Tag> getTopParentTags(Collection<Tag> tags) {
        if (tags  == null|| tags.isEmpty()) {
            return null;
        }
        List<Tag> output = new ArrayList<Tag>(tags);
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
        if (tags == null || tags.isEmpty()) {
            return false;
        }
        
    	for (Tag t : tags) {
    		if (t.getTagName().equals(name)) {
    			return true;
    		}
    	}
    	
    	return false;
    }
    
    private void getIdsOfDescendants(Set<Long> idSet) {
        for (Tag t : getChildren()) {
            idSet.add(t.getId());
        }
        for (Tag t : getChildren()) {
            t.getIdsOfDescendants(idSet);
        }
    }

    public Set<Long> getIdsOfDescendants(boolean includeSelf) {
        Set<Long> output = new HashSet<>();
        if (includeSelf)
            output.add(getId());
        getIdsOfDescendants(output);
        if (output.isEmpty())
            output = Collections.emptySet();
        return output;
    }
    
    public static boolean isSpecialTag(String name) {
    	for (String n : SPECIAL_TAG_NAMES) {
			if (n.equals(name)) {
				return true;
			}
		}
    	return false;
    }

    public static int getSpecialTagId(String name) {
        for (int i=0; i<SPECIAL_TAG_NAMES.length; ++i) {
            if (SPECIAL_TAG_NAMES[i].equals(name)) {
                return -i;
            }
        }
        return Integer.MAX_VALUE;
    }
    
    public static String getSpecialTagName(long id) {
        return SPECIAL_TAG_NAMES[Math.abs((int) id)];
    }
    
    public static boolean isTrashTag(long id) {
    	return  (id == TRASH_TAG_ID);
    }

    public static boolean isTrivialTag(long id) {
    	return  (id > TRASH_TAG_ID);
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
        final Long id = getId();
        final Long otherId = ((Tag) obj).getId();
        if (id == otherId)
            return true;
        if (id != null && id.equals(otherId))
            return true;
        return false;
    }

    @Override
    public String toString() {
        return  getTagName();
    }

}
