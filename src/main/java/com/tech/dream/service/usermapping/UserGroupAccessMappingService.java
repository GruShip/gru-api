package com.tech.dream.service.usermapping;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.tech.dream.db.entity.AccessModule;
import com.tech.dream.db.entity.UserGroup;
import com.tech.dream.db.entity.UserGroupAccessMapping;
import com.tech.dream.db.repository.UserGroupAccessMappingRepository;
import com.tech.dream.model.ErrorDTO;
import com.tech.dream.model.SessionDTO;
import com.tech.dream.model.UserGroupAccessMappingDTO;
import com.tech.dream.util.Constants.CompanyType;

@Service
@Transactional
public class UserGroupAccessMappingService {
	
	@Autowired
	private UserGroupAccessMappingRepository repository;
	
	public Object create(UserGroupAccessMappingDTO dto, SessionDTO session) {
		ErrorDTO error = createValidation(dto, session);
		if(error!=null) {
			return error;
		}
		List<Long> accessModuleIdList = new ArrayList<>();
		accessModuleIdList.add(dto.getAccessModuleId());
		repository.removeUserGroupAccessMappingApartFromGivenAccessModuleIdList(dto.getUserGroupId(), accessModuleIdList);
		
		List<Object[]> datalistResult = repository.getIdByUserGroupIdAndAccessModuleIdAndSession(dto.getUserGroupId(), dto.getAccessModuleId(), session.getCompanyId());
		
		if(datalistResult!=null && datalistResult.size()>0) {
			Object[] result = datalistResult.get(0);
			if(result!=null && result.length>0) {
				Long id = ((BigInteger)result[0]).longValue();
				dto.setId(id);
			}
			
		}
		
		UserGroupAccessMapping entity = convertToEntity(dto);
		repository.save(entity);
		dto = convertToDTO(entity);
		return dto;
	}
	
	public void removeAllMappings(Long userGroupId) {
		repository.removeAllUserGroupAccessMapping(userGroupId);
	}
	
	public Object createList(List<UserGroupAccessMappingDTO> dtoList, Long userGroupId, SessionDTO session) {
		if(dtoList==null || dtoList.size()==0) {
			this.removeAllMappings(userGroupId);
			return true;
		}
		List<ErrorDTO> errors = new ArrayList<ErrorDTO>();
		int index = 0;
		List<Long> accessModuleIdList = new ArrayList<>();
		for(UserGroupAccessMappingDTO dto:dtoList) {
			ErrorDTO error = createValidation(dto, session);
			if(error!=null) {
				error.setIndex(index);
				errors.add(error);
			}
			accessModuleIdList.add(dto.getAccessModuleId());
			index++;
		}
		if(errors.size()>0) {
			return errors;
		}
		
		List<UserGroupAccessMapping> entityList = new ArrayList<>();
		repository.removeUserGroupAccessMappingApartFromGivenAccessModuleIdList(userGroupId, accessModuleIdList);
		for(UserGroupAccessMappingDTO dto:dtoList) {
			List<Object[]> datalistResult = repository.getIdByUserGroupIdAndAccessModuleIdAndSession(dto.getUserGroupId(), dto.getAccessModuleId(), session.getCompanyId());
			
			if(datalistResult!=null && datalistResult.size()>0) {
				Object[] result = datalistResult.get(0);
				if(result!=null && result.length>0) {
					Long id = ((BigInteger)result[0]).longValue();
					dto.setId(id);
				}
				
			}
			
			UserGroupAccessMapping entity = convertToEntity(dto);
			entityList.add(entity);
		}
		if(entityList.size()>0) {
			repository.saveAll(entityList);
		}
		//dto = convertToDTO(entity);
		return true;
	}
	
	public Object delete(UserGroupAccessMappingDTO dto, SessionDTO session) {
		ErrorDTO error = deleteValidation(dto, session);
		if (error != null) {
			return error;
		}
		repository.delete(dto.getId());
		return true;
	}
	
	private ErrorDTO deleteValidation(UserGroupAccessMappingDTO dto, SessionDTO session) {
		if (StringUtils.isEmpty(dto.getId())) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "Id is mandatory and cannot be null.");
		}
		if (repository.existsByUserGroupAccessMappingIdAndSession(dto.getId(), session.getCompanyId())<=0) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST,
					"UserGroupAccessMapping does not exists for given id. id: " + dto.getId());
		}
		return null;
	}

	public UserGroupAccessMapping convertToEntity(UserGroupAccessMappingDTO dto) {
		UserGroupAccessMapping e = new UserGroupAccessMapping();
		e.setId(dto.getId());
		e.setAccessModule(new AccessModule(dto.getAccessModuleId()));
		e.setUserGroup(new UserGroup(dto.getUserGroupId()));
		e.setCreateAccess(dto.getCreateAccess());
		e.setReadAccess(dto.getReadAccess());
		e.setUpdateAccess(dto.getUpdateAccess());
		e.setDeleteAccess(dto.getDeleteAccess());
		return e;
	}
	
	public UserGroupAccessMappingDTO convertToDTO(UserGroupAccessMapping e) {
		UserGroupAccessMappingDTO dto = new UserGroupAccessMappingDTO();
		dto.setId(e.getId());
		dto.setAccessModuleId(e.getAccessModule().getId());
		dto.setUserGroupId(e.getUserGroup().getId());
		dto.setCreateAccess(e.getCreateAccess());
		dto.setReadAccess(e.getReadAccess());
		dto.setUpdateAccess(e.getUpdateAccess());
		dto.setDeleteAccess(e.getDeleteAccess());
		return dto;
	}

	private ErrorDTO createValidation(UserGroupAccessMappingDTO dto, SessionDTO session) {
		if(dto.getAccessModuleId()==null) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "AccessModuleId is mandatory parameter and cannot be null.");
		}
		if(dto.getUserGroupId()==null) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "UserGroupId is mandatory parameter and cannot be null.");
		}
		
//		if(repository.existsByUserGroupIdAndAccessModuleIdAndSession(dto.getUserGroupId(), dto.getAccessModuleId(), session.getCompanyId())>0) {
//			return new ErrorDTO(HttpStatus.BAD_REQUEST, "UserGroupAccessMapping already exists for given user and branch. user_group_id:"+dto.getUserGroupId() + " access_module_id:"+dto.getAccessModuleId());
//		}
		return null;
	}
	
	public Object list(SessionDTO session, Long companyId) {
		ErrorDTO error = listValidation(session);
		if(error!=null) {
			return error;
		}
		if(!CompanyType.MARKETPLACE.equalsIgnoreCase(session.getCompanyType())) {
			companyId = session.getCompanyId();
		}
		List<Object[]> datalistResult = repository.findAllUserGroupAccessMappingData(companyId);
		List<UserGroupAccessMappingDTO> dtoList = new ArrayList<>();
		if(datalistResult!=null && datalistResult.size()>0) {
			for(Object[] dataResult : datalistResult) {
				UserGroupAccessMappingDTO dto = this.fillUserGroupAccessMappingDTO(dataResult);
				dtoList.add(dto);
			}
		}
		return dtoList;
	}
	
	
	private UserGroupAccessMappingDTO fillUserGroupAccessMappingDTO(Object[] result) {
		UserGroupAccessMappingDTO dto = new UserGroupAccessMappingDTO();
		dto.setId(((BigInteger)result[0]).longValue());
		dto.setUserGroupId(((BigInteger)result[1]).longValue());
		dto.setAccessModuleId(((BigInteger)result[2]).longValue());
		dto.setUserGroupName((String)result[3]);
		dto.setAccessModuleName((String)result[4]);
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
		List<Object[]> datalistResult = repository.findUserGroupAccessMappingDataByIdAndCompanyId(id, session.getCompanyId());
		if(datalistResult!=null && datalistResult.size()>0) {
			Object[] dataResult = datalistResult.get(0);
			UserGroupAccessMappingDTO dto = this.fillUserGroupAccessMappingDTO(dataResult);
			return dto;
		}
		return null;
	}

	private ErrorDTO getValidation(SessionDTO session, Long id) {
		//check if user is superadmin  or usergroupid belongs to same company of session company id
		return null;
	}
	
	public List<UserGroupAccessMappingDTO> findUserGroupAccessModuleIdList(Long userGroupId) {
		List<UserGroupAccessMappingDTO> accessModuleList = new ArrayList<>();
		List<Object[]> datalistResult = repository.findIdListByUserGroupId(userGroupId);
		if(datalistResult!=null && datalistResult.size()>0) {
			for(Object[] dataResult : datalistResult) {
				UserGroupAccessMappingDTO ugam = new UserGroupAccessMappingDTO();
				ugam.setAccessModuleId(((BigInteger)dataResult[0]).longValue());
				ugam.setUserGroupId(((BigInteger)dataResult[1]).longValue());
				ugam.setCreateAccess(dataResult[2]!=null?(Boolean)dataResult[2]:null);
				ugam.setReadAccess(dataResult[3]!=null?(Boolean)dataResult[3]:null);
				ugam.setUpdateAccess(dataResult[4]!=null?(Boolean)dataResult[4]:null);
				ugam.setDeleteAccess(dataResult[5]!=null?(Boolean)dataResult[5]:null);
				ugam.setAccessModuleName((String)dataResult[6]);
				ugam.setAccessModuleType((String)dataResult[7]);
				accessModuleList.add(ugam);
			}
		}
		return accessModuleList;
	}
	
}

