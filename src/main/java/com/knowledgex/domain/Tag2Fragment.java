package com.knowledgex.domain;

import static javax.persistence.GenerationType.IDENTITY;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@SuppressWarnings("serial")
@Entity
@Table(name = "TAG2FRAGMENT")
public final class Tag2Fragment implements Serializable {
	
	private Long id;
	private Long tagId;
	private Long fragmentId;
	
	@Id
    @GeneratedValue(strategy = IDENTITY)
	@Column(name = "tag2fragment_id")
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	@Column(name = "tag_id")
	public Long getTagId() {
		return tagId;
	}
	
	public void setTagId(Long tagId) {
		this.tagId = tagId;
	}
	
	@Column(name = "fragment_id")
	public Long getFragmentId() {
		return fragmentId;
	}
	
	public void setFragmentId(Long fragmentId) {
		this.fragmentId = fragmentId;
	}

}
