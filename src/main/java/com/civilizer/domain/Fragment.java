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
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

import com.civilizer.domain.SearchParams.Keyword;

@SuppressWarnings("serial")
@Entity
@Table(name = "FRAGMENT")
public final class Fragment implements Serializable {
    private Long id;
    private String title = "";
    private String content = "";
    private DateTime creationDatetime;
    private DateTime updateDatetime;
    private Set<Fragment> relatedOnes = Collections.emptySet();
    private Set<Tag> tags = Collections.emptySet();

    public Fragment() {
    }
    
    public Fragment(String title, String content, DateTime dt) {
    	setTitle(title);
    	setContent(content);
    	if (dt == null) {
    		dt = new DateTime();
    	}
    	setCreationDatetime(dt);
    	setUpdateDatetime(dt);
    }

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "fragment_id")
    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "title")
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Column(name = "content")
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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

    @OneToMany(fetch=FetchType.LAZY)
    @Cascade({
    	CascadeType.PERSIST
      , CascadeType.MERGE
      , CascadeType.REFRESH
      , CascadeType.SAVE_UPDATE
      , CascadeType.DETACH
    })
    @JoinTable(name = "FRAGMENT2FRAGMENT",
        joinColumns = @JoinColumn(name = "from_id"),
        inverseJoinColumns = @JoinColumn(name = "to_id"))
    public Set<Fragment> getRelatedOnes() {
        return relatedOnes;
    }

    public void setRelatedOnes(Set<Fragment> relatedOnes) {
        this.relatedOnes = relatedOnes;
    }
    public boolean isRelatedTo(Fragment frg) {
    	return relatedOnes.contains(frg);
    }

    @OneToMany(fetch=FetchType.LAZY)
    @Cascade({
        CascadeType.PERSIST
      , CascadeType.MERGE
      , CascadeType.REFRESH
      , CascadeType.SAVE_UPDATE
      , CascadeType.DETACH
    })
    @JoinTable(name = "TAG2FRAGMENT",
        joinColumns = @JoinColumn(name = "fragment_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id"))
    public Set<Tag> getTags() {
        return this.tags;
    }

    public void setTags(Set<Tag> tags) {
        this.tags = tags;
    }
    
    public void addTag(Tag tag) {
        if (tags.equals(Collections.emptySet())) {
            tags = new HashSet<Tag>();
        }
        this.tags.add(tag);
    }

    public void removeTag(Tag tag) {
        this.tags.remove(tag);
    }
    
    public boolean containsTagId(long tagId) {
    	return Tag.containsId(getTags(), tagId);
    }

    public boolean containsTagName(String tagName) {
    	return Tag.containsName(getTags(), tagName);
    }
    
    public String concatenatedTagNames() {
        return Tag.getTagNamesFrom(tags);
    }
    
    public static boolean containsId(Collection<Fragment> fragments, long id) {
    	if (fragments == null || fragments.isEmpty()) {
    		return false;
    	}
    	
    	for (Fragment f : fragments) {
    		if (f.getId().equals(id)) {
    			return true;
    		}
    	}
    	
    	return false;
    }
    
    public static Collection<String> getFragmentTitleCollectionFrom(Collection<Fragment> fragments) {
    	List<String> fragmentNames = new ArrayList<String>();
        for (Fragment f : fragments) {
            fragmentNames.add(f.getTitle());
        }
        return fragmentNames;
    }
    
    public static void applyExclusiveTagFilter(
    		Collection<Fragment> fragments
          , Collection<Long> tags
    ) {
    	if (fragments == null || tags == null || tags.isEmpty()) {
    		return;
    	}
    	Set<Long> setEx = new HashSet<Long>(tags);
    	Iterator<Fragment> itr = fragments.iterator();
    	while (itr.hasNext()) {
    	    Fragment f = itr.next();
    	    Collection<Long> tagIds = Tag.getTagIdCollectionFrom(f.getTags());
    	    if (!Collections.disjoint(tagIds, setEx)) {
    	        itr.remove();
    	    }
    	}
    }
    
    public static void sort(
            List<Fragment> fragments
          , FragmentOrder order
          , boolean ascending
    ) {
        final Comparator<Fragment> cmptr = FragmentOrder.getComparator(order, ascending);
        Collections.sort(fragments, cmptr);
    }

    public static List<Fragment> paginate(
            List<Fragment> fragments
            , int first
            , int count
            , FragmentOrder order
            , boolean ascending
            ) {
        Fragment.sort(fragments, order, ascending);
        final int fromIndex = first;
        final int toIndex = Math.min(fragments.size(), first + count);
        return fragments.subList(fromIndex, toIndex);
    }
    
    public boolean matchesTagKeywords(SearchParams.Keywords keywords) {
        final Set<Tag> tags = getTags();
        for (Keyword w : keywords.getWords()) {
            boolean match = true;
            if (w.isInverse()) {
                for (Tag t : tags) {
                    match &= w.matchesTagName(t);
                    if (!match) {
                        if (keywords.isAny())
                            break;
                        else
                            return false;
                    }
                }
                if (match && keywords.isAny())
                    return true;
            }
            else {
                match = false;
                for (Tag t : tags) {
                    match |= w.matchesTagName(t);
                }
                if (match && keywords.isAny())
                    return true;
                if (!match && !keywords.isAny())
                    return false;
            }
        }
        return !keywords.isAny();
    }

    @Override
    public int hashCode() {
        final int prime = 43;
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
        final Long otherId = ((Fragment) obj).getId();
        if (id == otherId)
            return true;
        if (id != null && id.equals(otherId))
            return true;
        return false;
    }

    @Override
    public String toString() {
        return    "id: " + id
                + ", title: "+ title
                ;
    }

}
