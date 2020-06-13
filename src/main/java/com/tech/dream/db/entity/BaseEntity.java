package com.tech.dream.db.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

import lombok.Data;

@Data
@MappedSuperclass
public class BaseEntity {
	
	@Column(name = "removed", columnDefinition = "boolean not null default false")
	public Boolean removed;
	
	@Column(name = "active", columnDefinition = "boolean not null default true")
	public Boolean active;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name = "created_by", columnDefinition = "bigint")
	public User createdBy;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name = "updated_by", columnDefinition = "bigint")
	public User updatedBy;
	
	@Column(name = "created_date")
	public Date createdDate;
	
	@Column(name = "updated_date")
	public Date updatedDate;
	
    protected void onCreate() {
		this.createdDate = new Date();
		this.updatedDate = new Date();
		this.removed = this.removed == null?false: this.removed;
		this.active = this.active == null?true: this.active;
    }

    protected void onUpdate() {
    	this.updatedDate = new Date();
    	this.removed = this.removed == null?false: this.removed;
    	this.active = this.active == null?true: this.active;
    }
    
}
