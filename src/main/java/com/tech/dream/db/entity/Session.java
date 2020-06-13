package com.tech.dream.db.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;

@Entity
@Table(name = "session")
public class Session{

	@Id
	@Column(name = "id")
	private String id;
	
	@Column(name = "value", columnDefinition="TEXT")
	private String value;
	
	@Column(name = "user_id")
	private Long userId;
	
	@Column(name = "expiry_date")
	private Date expiryDate;
	
	@Column(name = "removed", columnDefinition = "boolean not null default false")
	private Boolean removed;
	
	@Column(name = "created_date")
	private Date createdDate;
	
	public Session() {}
	public Session(String id) {
		this.id = id;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public Date getExpiryDate() {
		return expiryDate;
	}
	public void setExpiryDate(Date expiryDate) {
		this.expiryDate = expiryDate;
	}
	public Boolean getRemoved() {
		return removed;
	}
	public void setRemoved(Boolean removed) {
		this.removed = removed;
	}
	public Date getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	@PrePersist
    protected void onCreate() {
		this.createdDate = new Date();
		this.removed = this.removed == null ? false : this.removed;
    }
}
