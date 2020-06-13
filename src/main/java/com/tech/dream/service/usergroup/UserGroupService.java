package com.tech.dream.service.usergroup;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.tech.dream.db.entity.Company;
import com.tech.dream.db.entity.UserGroup;
import com.tech.dream.db.repository.UserGroupMappingRepository;
import com.tech.dream.db.repository.UserGroupRepository;
import com.tech.dream.model.ErrorDTO;
import com.tech.dream.model.PagingSortSearchDTO;
import com.tech.dream.model.ResultDTO;
import com.tech.dream.model.SearchQueryDTO;
import com.tech.dream.model.SessionDTO;
import com.tech.dream.model.UserGroupAccessMappingDTO;
import com.tech.dream.model.UserGroupDTO;
import com.tech.dream.service.usermapping.UserGroupAccessMappingService;
import com.tech.dream.util.Constants.CompanyType;
import com.tech.dream.util.Constants.DataType;
import com.tech.dream.util.Constants.DisplayModuleNames;

@Service
@Transactional
public class UserGroupService {

	@Autowired
	private UserGroupRepository repository;
	
	@Autowired
	private UserGroupMappingRepository userGroupMappingRepository;
	
	@Autowired
	private UserGroupAccessMappingService userGroupAccessMappingService;

	@Autowired
	private LocalContainerEntityManagerFactoryBean emf;

	public Object create(UserGroupDTO dto, SessionDTO session) {
		ErrorDTO error = createValidation(dto, session);
		if (error != null) {
			return error;
		}
		UserGroup entity = convertToEntity(dto);
		repository.save(entity);
		dto.setId(entity.getId());
		dto.setActive(entity.getActive());
		
		if(dto.getAccessModuleList()!=null && dto.getAccessModuleList().size()>0) {
			for(UserGroupAccessMappingDTO accessModuleDTO: dto.getAccessModuleList()) {
				accessModuleDTO.setUserGroupId(dto.getId());
			}
		}
		userGroupAccessMappingService.createList(dto.getAccessModuleList(), dto.getId(), session);
		
		//dto = convertToDTO(entity);
		return dto;
	}

	public Object update(UserGroupDTO dto, SessionDTO session) {
		ErrorDTO error = updateValidation(dto, session);
		if (error != null) {
			return error;
		}
		repository.update(dto.getId(), dto.getCode(), dto.getName(), dto.getActive()!=null?dto.getActive():true, dto.getDesc());

		if (repository.isAdminUserGroup(dto.getId())<=0) {
			if(dto.getAccessModuleList()!=null && dto.getAccessModuleList().size()>0) {
				for(UserGroupAccessMappingDTO accessModuleDTO: dto.getAccessModuleList()) {
					accessModuleDTO.setUserGroupId(dto.getId());
				}
			}
			userGroupAccessMappingService.createList(dto.getAccessModuleList(), dto.getId(), session);
		}
		//UserGroup entity = convertToEntity(dto);
		//repository.save(entity);
		//dto = convertToDTO(entity);
		return dto;
	}

	public Object delete(UserGroupDTO dto, SessionDTO session) {
		ErrorDTO error = deleteValidation(dto, session);
		if (error != null) {
			return error;
		}
		repository.delete(dto.getId());
		return true;
	}

	public UserGroup convertToEntity(UserGroupDTO dto) {
		UserGroup e = new UserGroup();
		e.setId(dto.getId());
		e.setCode(dto.getCode());
		e.setName(dto.getName());
		e.setDesc(dto.getDesc());
		e.setActive(dto.getActive()!=null?dto.getActive():true);
		e.setIsAdmin(dto.getIsAdmin()!=null?dto.getIsAdmin():false);
		e.setCompany(new Company(dto.getCompanyId()));
		return e;
	}

	public UserGroupDTO convertToDTO(UserGroup e) {
		UserGroupDTO dto = new UserGroupDTO();
		dto.setId(e.getId());
		dto.setCode(e.getCode());
		dto.setName(e.getName());
		dto.setDesc(e.getDesc());
		dto.setCompanyId(e.getCompany().getId());
		dto.setActive(e.getActive());
		return dto;
	}
	
	private ErrorDTO deleteValidation(UserGroupDTO dto, SessionDTO session) {
		if (dto.getId()==null) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "Id is mandatory and cannot be null.");
		}
		if(!CompanyType.MARKETPLACE.equalsIgnoreCase(session.getCompanyType()) && repository.existsByIdAndCompanyId(dto.getId(), session.getCompanyId())<=0) {
			return new ErrorDTO(HttpStatus.FORBIDDEN, DisplayModuleNames.USER+" does not have access to create "+DisplayModuleNames.USERGROUP+" for this "+DisplayModuleNames.COMPANY);
		}
		if (repository.existsByUserGroupId(dto.getId())<=0) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, DisplayModuleNames.USERGROUP+" does not exists for given id.");
		}
		if (userGroupMappingRepository.existsUsersForUserGroup(dto.getId())>0) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, DisplayModuleNames.USERGROUP+" already has "+DisplayModuleNames.USER+" mapped to it. Please remove them and try again.");
		}
		if (repository.isAdminUserGroup(dto.getId())>0) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "Admin " + DisplayModuleNames.USERGROUP+" cannot be deleted.");
		}
		return null;
	}

	private ErrorDTO createValidation(UserGroupDTO dto, SessionDTO session) {
		if (StringUtils.isEmpty(dto.getName())) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "Name is mandatory parameter and cannot be null.");
		}
		if (dto.getCompanyId()==null) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "CompanyId is mandatory parameter and cannot be null.");
		}
		if(!CompanyType.MARKETPLACE.equalsIgnoreCase(session.getCompanyType()) && session.getCompanyId().longValue()!=dto.getCompanyId().longValue()) {
			return new ErrorDTO(HttpStatus.FORBIDDEN, DisplayModuleNames.USER+" does not have access to create "+DisplayModuleNames.USERGROUP+" for this "+DisplayModuleNames.COMPANY);
		}
		if (repository.existsByNameAndCompanyId(dto.getName(), dto.getCompanyId())>0) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, DisplayModuleNames.USERGROUP+" already exists for give branch name and "+DisplayModuleNames.COMPANY);
		}
		return null;
	}

	private ErrorDTO updateValidation(UserGroupDTO dto, SessionDTO session) {
		if (dto.getId()==null) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "Id is mandatory and cannot be null.");
		}
		if(!CompanyType.MARKETPLACE.equalsIgnoreCase(session.getCompanyType()) && session.getCompanyId().longValue()!=dto.getCompanyId().longValue()) {
			return new ErrorDTO(HttpStatus.FORBIDDEN, DisplayModuleNames.USER+" does not have access to update "+ DisplayModuleNames.USERGROUP +" for this "+DisplayModuleNames.COMPANY);
		}
		if (StringUtils.isEmpty(dto.getName())) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "Name is mandatory parameter and cannot be null.");
		}
		if (dto.getCompanyId()==null) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "CompanyId is mandatory parameter and cannot be null.");
		}
		if (repository.existsByUserGroupId(dto.getId())<=0) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, DisplayModuleNames.USERGROUP+" does not exists for given id.");
		}
		if (repository.existsByNameAndCompanyIdAndNotId(dto.getName(), dto.getCompanyId(),
				dto.getId())>0) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, DisplayModuleNames.USERGROUP+" already exists for give name and "+DisplayModuleNames.COMPANY);
		}
		return null;
	}

	public ResultDTO list(SessionDTO session, Long companyId, PagingSortSearchDTO filters) {
		ResultDTO resultDTO = new ResultDTO();
		Long totalCount = 0L; 
		ErrorDTO error = listValidation(session);
		if(error!=null) {
			resultDTO.setErrorDTO(error);
			return resultDTO;
		}
		if(!CompanyType.MARKETPLACE.equalsIgnoreCase(session.getCompanyType())) {
			companyId = session.getCompanyId();
		}

		if (filters == null) filters = new PagingSortSearchDTO();

		EntityManager em = emf.getNativeEntityManagerFactory().createEntityManager();
		List<Object[]> datalistResult = null;

		try {
			String query = "select ug.id, ug.name, ug.code, ug.`desc`, ug.active, ug.company_id, ug.is_admin "
			+ " from usergroup ug "
			+ " where ug.removed = FALSE and ug.company_id = " + companyId; 

			String totalCountQuery = "select count(1)"
			+ " from usergroup ug "
			+ " where ug.removed = FALSE and ug.company_id = " + companyId; 
			
			// searching
			String searchQuery = "";
			if (filters.getSearch() != null){
				for (SearchQueryDTO sDto: filters.getSearch()){
					String dbField = getDBField(sDto.getSearchField());
					String fieldDatatype = getUserGroupFieldDataType(sDto.getSearchField());

					switch (fieldDatatype) {
						case DataType.TYPE_STRING: 
							searchQuery += " and " + dbField + " like " + "'%" + sDto.getSearchText() + "%'";
							break;
						case DataType.TYPE_INT:
							searchQuery += " and " + dbField + " = " + sDto.getSearchText();
							break;
						default:
							searchQuery += "";
					}
				}
			}

			query = query + searchQuery; 
			totalCountQuery = totalCountQuery + searchQuery;

			// sorting 
			if (filters.getSortField() != null) {
				String dbField = getDBField(filters.getSortField());
				query += " order by " + dbField;
			} else{
				query += " order by ug.id";
			}
	
			if (filters.getSortOrder() != null) {
				query += " " + filters.getSortOrder();
			} else {
				query += " desc";
			}

			// pagination
			if (filters.getPageNumber() == null) { 
				filters.setPageNumber(Long.parseLong(String.valueOf(0)));
			}
			if (filters.getPageSize() == null) { 
				filters.setPageNumber(Long.parseLong(String.valueOf(0)));
			}
			query += " limit " + (filters.getPageNumber() * filters.getPageSize()) + "," + filters.getPageSize();
			query += ";";

			Query q = em.createNativeQuery(query);
			datalistResult = q.getResultList();

			q = em.createNativeQuery(totalCountQuery);
			totalCount = Long.parseLong(String.valueOf(q.getSingleResult()));
		}finally{
			em.close();
		}

		List<UserGroupDTO> dtoList = new ArrayList<>();
		if(datalistResult!=null && datalistResult.size()>0) {
			for(Object[] dataResult : datalistResult) {
				UserGroupDTO dto = this.fillUserGroupDTO(dataResult);
				dto.setAccessModuleList(getUserGroupAccessModuleIdList(dto.getId()));
				dtoList.add(dto);
			}
		}
		resultDTO.setData(dtoList);
		resultDTO.setErrorDTO(error);
		resultDTO.setTotalCount(totalCount);
		resultDTO.setPageNumber(filters.getPageNumber());
		resultDTO.setPageSize(filters.getPageSize());
		return resultDTO;
	}
	
	
	private UserGroupDTO fillUserGroupDTO(Object[] result) {
		UserGroupDTO dto = new UserGroupDTO();
		dto.setId(((BigInteger)result[0]).longValue());
		dto.setName((String)result[1]);
		dto.setCode((String)result[2]);
		dto.setDesc((String)result[3]);
		dto.setActive(result[4]!=null?(Boolean)result[4]:null);
		dto.setCompanyId(result[5]!=null?((BigInteger)result[5]).longValue():null);
		dto.setIsAdmin((Boolean)result[6]);
		return dto;
	}

	private ErrorDTO listValidation(SessionDTO session) {
		return null;
	}

	public Object get(SessionDTO session, Long id) {
		ErrorDTO error = getValidation(session, id);
		if(error!=null) {
			return error;
		}
		List<Object[]> datalistResult = repository.findUserGroupDataByIdAndCompanyId(id, session.getCompanyId());
		if(datalistResult!=null && datalistResult.size()>0) {
			Object[] dataResult = datalistResult.get(0);
			UserGroupDTO dto = this.fillUserGroupDTO(dataResult);
			dto.setAccessModuleList(getUserGroupAccessModuleIdList(dto.getId()));
			return dto;
		}
		return null;
	}

	private ErrorDTO getValidation(SessionDTO session, Long id) {
		//check if user is superadmin  or usergroupid belongs to same company of session company id
		return null;
	}
	
	private List<UserGroupAccessMappingDTO> getUserGroupAccessModuleIdList(Long userGroupId) {
		List<UserGroupAccessMappingDTO> accessModuleIdList = userGroupAccessMappingService.findUserGroupAccessModuleIdList(userGroupId);
		return accessModuleIdList;
	}

	private String getUserGroupFieldDataType(String column) {
		switch (column) {
			case "id":
			case "active":
				return DataType.TYPE_INT;
			case "code": 
			case "desc": 
			case "name":
				return DataType.TYPE_STRING;
		}
		return "";
	}

	private String getDBField(String UIField) {
		switch(UIField) {
			case "id":
			case "name": 
			case "code":	
			case "active":
				return "ug." + UIField;
			case "desc":
				return "ug.`desc`";
		}
		return "";
	}
}
