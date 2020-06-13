package com.tech.dream.model;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class SessionDTO {
	
	private Long userId;
	private Long userGroupId;
	private Long companyId;
	private List<Long> companyBranchIdList;
	private Map<Long, UserGroupAccessMappingDTO> accessModuleList;
	private String token;
	private String companyType;

}
