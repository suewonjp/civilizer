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

import org.hibernate.Hibernate;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

@SuppressWarnings("serial")
@Entity
@Table(name = "FRAGMENT")
@NamedQueries({
    @NamedQuery(name = "Fragment.findById",
            query = "select distinct f from Fragment f where f.id = :id"),
    @NamedQuery(name = "Fragment.findByIdWithRelatedOnes",
            query = "select distinct f from Fragment f left join fetch f.relatedOnes where f.id = :id"),
    @NamedQuery(name = "Fragment.findByIdWithTags",
            query = "select distinct f from Fragment f left join fetch f.tags where f.id = :id"),
})
public class Fragment implements Serializable {
    private Long id;
    private String title;
    private String content;
    private DateTime creationDatetime;
    private DateTime updateDatetime;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private Integer childrenOrderedBy = 1;
    private Boolean childrenOrderedInAsc = false;
    private String password;
    private String creator;
    private String updater;
    private Long tagId;
    private Collection<Fragment> relatedOnes = new ArrayList<Fragment>();
    private Collection<Tag> tags = new ArrayList<Tag>();

    public Fragment() {
    }
    
    public Fragment(String title, String content, DateTime dt) {
    	setTitle(title);
    	setContent(content);
    	if (null == dt) {
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

    @Column(name = "file_name")
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Column(name = "file_type")
    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    @Column(name = "file_size")
    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    @Column(name = "children_ordered_by")
    public Integer getChildrenOrderedBy() {
        return childrenOrderedBy;
    }

    public void setChildrenOrderedBy(Integer childrenOrderedBy) {
        this.childrenOrderedBy = childrenOrderedBy;
    }

    @Column(name = "children_ordered_in_asc")
    public Boolean getChildrenOrderedInAsc() {
        return childrenOrderedInAsc;
    }

    public void setChildrenOrderedInAsc(Boolean childrenOrderedInAsc) {
        this.childrenOrderedInAsc = childrenOrderedInAsc;
    }

    @Column(name = "password")
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    @Column(name = "tag_id")
    public Long getTagId() {
        return tagId;
    }

    public void setTagId(Long tagId) {
        this.tagId = tagId;
    }

    @ManyToMany(fetch=FetchType.LAZY)
    @Cascade({CascadeType.MERGE
        , CascadeType.REFRESH
        , CascadeType.SAVE_UPDATE
        , CascadeType.DETACH
    })
    @JoinTable(name = "FRAGMENT2FRAGMENT",
        joinColumns = @JoinColumn(name = "from_id"),
        inverseJoinColumns = @JoinColumn(name = "to_id"))
    public Collection<Fragment> getRelatedOnes() {
        return relatedOnes;
    }

    public void setRelatedOnes(Collection<Fragment> relatedOnes) {
        this.relatedOnes = relatedOnes;
    }

    @ManyToMany(fetch=FetchType.EAGER)
    @Cascade({CascadeType.PERSIST
        , CascadeType.MERGE
        , CascadeType.REFRESH
        , CascadeType.SAVE_UPDATE
        , CascadeType.DETACH
    })
    @JoinTable(name = "TAG2FRAGMENT",
        joinColumns = @JoinColumn(name = "fragment_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id"))
    public Collection<Tag> getTags() {
        return this.tags;
    }

    public void setTags(Collection<Tag> tags) {
        this.tags = tags;
    }
    
    public void addTag(Tag tag) {
        this.tags.add(tag);
    }
    
    public static Collection<String> getFragmentTitleCollectionFrom(Collection<Fragment> fragments) {
    	List<String> fragmentNames = new ArrayList<String>();
        for (Fragment f : fragments) {
            fragmentNames.add(f.getTitle());
        }
        return fragmentNames;
    }

    public String toString() {
        return "Fragment - id: " + id
                + ", title: "+ title
                + ", content: "+ content
                + ", tag count: "+ (Hibernate.isInitialized(tags) ? tags.size() : 0)
                + ", created at: "+ creationDatetime
                + ", updated at: "+ updateDatetime
                ;
    }

}
