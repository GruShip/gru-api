package com.tech.dream.db.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

@Entity
@Table(name = "accessmodule")
public class AccessModule extends BaseEntity{
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", columnDefinition = "bigint not null auto_increment")
	private Long id;
	
	@Column(name = "name")
	private String name;
	
	@Column(name = "code")
	private String code;
	
	@Column(name = "type")
	private String type;
	
	@Column(name = "is_admin_module", columnDefinition = "boolean not null default false")
	public Boolean isAdminModule;
	
	@Column(name = "is_client_module", columnDefinition = "boolean not null default false")
	public Boolean isClientModule;
	
	@Column(name = "`desc`")
	private String desc;
	
	public AccessModule() {}
	public AccessModule(Long id) {
		super();
		this.id = id;
	}

	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Boolean getIsAdminModule() {
		return isAdminModule;
	}
	public void setIsAdminModule(Boolean isAdminModule) {
		this.isAdminModule = isAdminModule;
	}
	
	public Boolean getIsClientModule() {
		return isClientModule;
	}
	public void setIsClientModule(Boolean isClientModule) {
		this.isClientModule = isClientModule;
	}
	@PrePersist
    protected void onCreate() {
		super.onCreate();
	}

	@PreUpdate
    protected void onUpdate() {
		super.onUpdate();
	}

}

