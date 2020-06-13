package com.tech.dream.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class UserGroupDTO {
	private Long id;
	private String name;
	private String code;
	private Long companyId;
	private String desc;
	private Boolean active;
	private Boolean isAdmin;
	private List<UserGroupAccessMappingDTO> accessModuleList;
}
