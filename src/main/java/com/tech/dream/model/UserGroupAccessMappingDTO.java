package com.tech.dream.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class UserGroupAccessMappingDTO {

	private Long id;
	private Long userGroupId;
	private Long accessModuleId;
	private Boolean createAccess = false;
	private Boolean updateAccess = false;
	private Boolean readAccess = false;
	private Boolean deleteAccess = false;
	private String userGroupName;
	private String accessModuleName;
	private String accessModuleCode;
	private String accessModuleType;
	
}
