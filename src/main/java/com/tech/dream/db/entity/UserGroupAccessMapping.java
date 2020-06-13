package com.tech.dream.db.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

@Entity
@Table(name = "usergroupaccessmapping")
public class UserGroupAccessMapping extends BaseEntity{
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", columnDefinition = "bigint not null auto_increment")
	private Long id;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name = "user_group_id", columnDefinition = "bigint")
	private UserGroup userGroup;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name = "access_module_id", columnDefinition = "bigint")
	private AccessModule accessModule;
	
	@Column(name = "create_access", columnDefinition = "boolean not null default false")
	private Boolean createAccess;
	
	@Column(name = "read_access", columnDefinition = "boolean not null default false")
	private Boolean readAccess;
	
	@Column(name = "update_access", columnDefinition = "boolean not null default false")
	private Boolean updateAccess;
	
	@Column(name = "delete_access", columnDefinition = "boolean not null default false")
	private Boolean deleteAccess;
	
	public Boolean getCreateAccess() {
		return createAccess;
	}

	public void setCreateAccess(Boolean createAccess) {
		this.createAccess = createAccess;
	}

	public Boolean getReadAccess() {
		return readAccess;
	}

	public void setReadAccess(Boolean readAccess) {
		this.readAccess = readAccess;
	}

	public Boolean getUpdateAccess() {
		return updateAccess;
	}

	public void setUpdateAccess(Boolean updateAccess) {
		this.updateAccess = updateAccess;
	}

	public Boolean getDeleteAccess() {
		return deleteAccess;
	}

	public void setDeleteAccess(Boolean deleteAccess) {
		this.deleteAccess = deleteAccess;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public UserGroup getUserGroup() {
		return userGroup;
	}

	public void setUserGroup(UserGroup userGroup) {
		this.userGroup = userGroup;
	}

	public AccessModule getAccessModule() {
		return accessModule;
	}

	public void setAccessModule(AccessModule accessModule) {
		this.accessModule = accessModule;
	}
	
	@PrePersist
    protected void onCreate() {
		super.onCreate();
		this.createAccess = this.createAccess == null ? false : this.createAccess;
		this.readAccess = this.readAccess == null ? false : this.readAccess;
		this.updateAccess = this.updateAccess == null ? false : this.updateAccess;
		this.deleteAccess = this.deleteAccess == null ? false : this.deleteAccess;
	}

	@PreUpdate
    protected void onUpdate() {
		super.onUpdate();
		this.createAccess = this.createAccess == null ? false : this.createAccess;
		this.readAccess = this.readAccess == null ? false : this.readAccess;
		this.updateAccess = this.updateAccess == null ? false : this.updateAccess;
		this.deleteAccess = this.deleteAccess == null ? false : this.deleteAccess;
	}

}
