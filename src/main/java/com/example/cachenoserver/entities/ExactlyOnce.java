package com.example.cachenoserver.entities;

import java.io.Serializable;
import java.sql.Date;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.domain.Persistable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "EXACTLYONCE")
public class ExactlyOnce implements Serializable, Persistable<String> {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "eid")
	private String id;

	@CreationTimestamp
	@Column(name = "createdon")
	private Date createdOn;

	@UpdateTimestamp
	@Column(name = "updatedon")
	private Date updatedOn;
	@Column(name = "recpartition")
	private int recPartition;
	@Column(name = "rectopic")
	private String recTopic;
	@Column(name = "kafkagroup")
	private String group;
	@Column(name = "\"offset\"")
	private Long offset;

	public ExactlyOnce() {
	}

	public void generateId() {
		this.id = this.group + "#" + this.recTopic + "#" + this.recPartition + "#" + this.offset;

	}

	public Date getCreatedOn() {
		return createdOn;
	}

	public String getGroup() {
		return group;
	}

	@Transient
	@Override
	public String getId() {
		return id;
	}

	public Long getOffset() {
		return offset;
	}

	public int getRecPartition() {
		return recPartition;
	}

	public String getRecTopic() {
		return recTopic;
	}

	public Date getUpdatedOn() {
		return updatedOn;
	}

	@Transient
	@Override
	public boolean isNew() {
		return true;
	}

	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setOffset(Long offset) {
		this.offset = offset;
	}

	public void setRecPartition(int recPartition) {
		this.recPartition = recPartition;
	}

	public void setRecTopic(String recTopic) {
		this.recTopic = recTopic;
	}

	public void setUpdatedOn(Date updatedOn) {
		this.updatedOn = updatedOn;
	}

}
