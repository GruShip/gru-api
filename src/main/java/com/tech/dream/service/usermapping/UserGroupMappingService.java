package com.tech.dream.service.usermapping;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.tech.dream.db.entity.User;
import com.tech.dream.db.entity.UserGroup;
import com.tech.dream.db.entity.UserGroupMapping;
import com.tech.dream.db.repository.UserGroupMappingRepository;
import com.tech.dream.model.ErrorDTO;
import com.tech.dream.model.SessionDTO;
import com.tech.dream.model.UserGroupMappingDTO;
import com.tech.dream.util.Constants.CompanyType;

@Service
@Transactional
public class UserGroupMappingService {
	
	@Autowired
	private UserGroupMappingRepository repository;
	
	public void removeAllMappings(Long userId) {
		repository.removeAllUserGroupMapping(userId);
	}
	
	public Object create(UserGroupMappingDTO dto, Long userId, SessionDTO session) {
		if(dto == null) {
			removeAllMappings(userId);
			return null;
		}
		ErrorDTO error = createValidation(dto, session);
		if(error!=null) {
			return error;
		}
		if(repository.existsByUserIdAndUserGroupId(dto.getUserId(), dto.getUserGroupId())>0) {
			return dto;
		}
		removeAllMappings(userId);
		//repository.removeUserGroupMappingApartFromGivenUserGroupId(dto.getUserId(), dto.getUserGroupId());
		
		UserGroupMapping entity = convertToEntity(dto);
		repository.save(entity);
		dto = convertToDTO(entity);
		return dto;
	}
	
	public Object delete(UserGroupMappingDTO dto, SessionDTO session) {
		ErrorDTO error = deleteValidation(dto, session);
		if (error != null) {
			return error;
		}
		repository.delete(dto.getId());
		return true;
	}
	
	public UserGroupMapping convertToEntity(UserGroupMappingDTO dto) {
		UserGroupMapping e = new UserGroupMapping();
		e.setId(dto.getId());
		e.setUser(new User(dto.getUserId()));
		e.setUserGroup(new UserGroup(dto.getUserGroupId()));
		return e;
	}
	
	public UserGroupMappingDTO convertToDTO(UserGroupMapping e) {
		UserGroupMappingDTO dto = new UserGroupMappingDTO();
		dto.setId(e.getId());
		dto.setUserId(e.getUser().getId());
		dto.setUserGroupId(e.getUserGroup().getId());
		return dto;
	}

	private ErrorDTO deleteValidation(UserGroupMappingDTO dto, SessionDTO session) {
		if (StringUtils.isEmpty(dto.getId())) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "Id is mandatory and cannot be null.");
		}
		if (repository.existsByUserGroupMappingId(dto.getId(), session.getCompanyId())<=0) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST,
					"UserGroupMapping does not exists for given id. id: " + dto.getId());
		}
		return null;
	}
	
	private ErrorDTO createValidation(UserGroupMappingDTO dto, SessionDTO session) {
		if(dto.getUserId()==null) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "UserId is mandatory parameter and cannot be null.");
		}
		if(dto.getUserGroupId()==null) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "UserGroupId is mandatory parameter and cannot be null.");
		}
		if(repository.isSameClientForUserIdAndUserGroupIdAndSession(dto.getUserId(), dto.getUserGroupId(), session.getCompanyId())<=0) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "User & UserGroup does not belong to same company.");
		}
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
		List<Object[]> datalistResult = repository.findAllUserGroupMappingData(companyId);
		List<UserGroupMappingDTO> dtoList = new ArrayList<>();
		if(datalistResult!=null && datalistResult.size()>0) {
			for(Object[] dataResult : datalistResult) {
				UserGroupMappingDTO dto = this.fillUserGroupMappingDTO(dataResult);
				dtoList.add(dto);
			}
		}
		return dtoList;
	}
	
	
	private UserGroupMappingDTO fillUserGroupMappingDTO(Object[] result) {
		UserGroupMappingDTO dto = new UserGroupMappingDTO();
		dto.setId(((BigInteger)result[0]).longValue());
		dto.setUserId(((BigInteger)result[1]).longValue());
		dto.setUserGroupId(((BigInteger)result[2]).longValue());
		dto.setFirstName((String)result[3]);
		dto.setLastName((String)result[4]);
		dto.setUsername((String)result[5]);
		dto.setEmail((String)result[6]);
		dto.setUserGroupName((String)result[7]);
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
		List<Object[]> datalistResult = repository.findUserGroupMappingDataByIdAndCompanyId(id, session.getCompanyId());
		if(datalistResult!=null && datalistResult.size()>0) {
			Object[] dataResult = datalistResult.get(0);
			UserGroupMappingDTO dto = this.fillUserGroupMappingDTO(dataResult);
			return dto;
		}
		return null;
	}

	private ErrorDTO getValidation(SessionDTO session, Long id) {
		//check if user is superadmin  or usergroupid belongs to same company of session company id
		return null;
	}
	
}

