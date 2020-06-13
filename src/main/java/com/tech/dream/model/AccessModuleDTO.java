package com.tech.dream.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class AccessModuleDTO {
	
	private Long id;
	private String name;
	private String code;
	private String desc;
	private String type;
	private Boolean isAdminModule;
	private Boolean isClientModule;
	
	public Boolean getIsClientModule() {
		return isClientModule;
	}
	public void setIsClientModule(Boolean isClientModule) {
		this.isClientModule = isClientModule;
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
	
}
