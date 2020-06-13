package com.tech.dream.service.usermapping;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.tech.dream.db.entity.CompanyBranch;
import com.tech.dream.db.entity.User;
import com.tech.dream.db.entity.UserBranchMapping;
import com.tech.dream.db.repository.UserBranchMappingRepository;
import com.tech.dream.model.ErrorDTO;
import com.tech.dream.model.SessionDTO;
import com.tech.dream.model.UserBranchMappingDTO;
import com.tech.dream.util.Constants.CompanyType;

@Service
@Transactional
public class UserBranchMappingService {
	
	@Autowired
	private UserBranchMappingRepository repository;
	
	public Object create(UserBranchMappingDTO dto, SessionDTO session) {
		ErrorDTO error = createValidation(dto, session);
		if(error!=null) {
			return error;
		}
		if(repository.existsByUserIdAndBranchId(dto.getUserId(), dto.getCompanyBranchId())>0) {
			return dto;
		}
		List<Long> companyBranchIdList = new ArrayList<>();
		companyBranchIdList.add(dto.getCompanyBranchId());
		repository.removeUserBranchMappingApartFromGivenBranchIdList(dto.getUserId(), companyBranchIdList);
		UserBranchMapping entity = convertToEntity(dto);
		repository.save(entity);
		dto = convertToDTO(entity);
		return dto;
	}
	
	public void removeAllMappings(Long userId) {
		repository.removeAllUserBranchMapping(userId);
	}
	
	public Object createList(List<UserBranchMappingDTO> dtoList, Long userId, SessionDTO session) {
		if(dtoList==null || dtoList.size()==0) {
			removeAllMappings(userId);
			return true;
		}
		List<ErrorDTO> errors = new ArrayList<ErrorDTO>();
		int index = 0;
		List<Long> companyBranchIdList = new ArrayList<>();
		for(UserBranchMappingDTO dto:dtoList) {
			ErrorDTO error = createValidation(dto, session);
			if(error!=null) {
				error.setIndex(index);
				errors.add(error);
			}
			companyBranchIdList.add(dto.getCompanyBranchId());
			index++;
		}
		if(errors.size()>0) {
			return errors;
		}
		
		List<UserBranchMapping> entityList = new ArrayList<>();
		repository.removeUserBranchMappingApartFromGivenBranchIdList(userId, companyBranchIdList);
		for(UserBranchMappingDTO dto:dtoList) {
			if(repository.existsByUserIdAndBranchId(dto.getUserId(), dto.getCompanyBranchId())>0) {
				continue;
			}
			
			UserBranchMapping entity = convertToEntity(dto);
			entityList.add(entity);
		}
		if(entityList.size()>0) {
			repository.saveAll(entityList);
		}
		//dto = convertToDTO(entity);
		return true;
	}
	
	public UserBranchMapping convertToEntity(UserBranchMappingDTO dto) {
		UserBranchMapping e = new UserBranchMapping();
		e.setId(dto.getId());
		e.setUser(new User(dto.getUserId()));
		e.setBranch(new CompanyBranch(dto.getCompanyBranchId()));
		return e;
	}
	
	public UserBranchMappingDTO convertToDTO(UserBranchMapping e) {
		UserBranchMappingDTO dto = new UserBranchMappingDTO();
		dto.setId(e.getId());
		dto.setUserId(e.getUser().getId());
		dto.setCompanyBranchId(e.getBranch().getId());
		return dto;
	}

	public Object delete(UserBranchMappingDTO dto, SessionDTO session) {
		ErrorDTO error = deleteValidation(dto, session);
		if (error != null) {
			return error;
		}
		repository.delete(dto.getId());
		return true;
	}
	
	private ErrorDTO deleteValidation(UserBranchMappingDTO dto, SessionDTO session) {
		if (StringUtils.isEmpty(dto.getId())) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "Id is mandatory and cannot be null.");
		}
		if(repository.existsByUserBranchMappingId(dto.getId(), session.getCompanyId())<=0) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST,
					"UserBranchMapping does not exists for given id. id: " + dto.getId());
		}
		return null;
	}
	
	private ErrorDTO createValidation(UserBranchMappingDTO dto, SessionDTO session) {
		if(dto.getUserId()==null) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "UserId is mandatory parameter and cannot be null.");
		}
		if(dto.getCompanyBranchId()==null) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "Branch is mandatory parameter and cannot be null.");
		}
		if(repository.isSameClientForUserIdAndBranchIdAndSession(dto.getUserId(), dto.getCompanyBranchId(), session.getCompanyId())<=0) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "User & Branch does not belong to same company.");
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
		List<Object[]> datalistResult = repository.findAllUserBranchMappingData(companyId);
		List<UserBranchMappingDTO> dtoList = new ArrayList<>();
		if(datalistResult!=null && datalistResult.size()>0) {
			for(Object[] dataResult : datalistResult) {
				UserBranchMappingDTO dto = this.fillUserBranchMappingDTO(dataResult);
				dtoList.add(dto);
			}
		}
		return dtoList;
	}
	
	
	private UserBranchMappingDTO fillUserBranchMappingDTO(Object[] result) {
		UserBranchMappingDTO dto = new UserBranchMappingDTO();
		dto.setId(((BigInteger)result[0]).longValue());
		dto.setUserId(((BigInteger)result[1]).longValue());
		dto.setCompanyBranchId(((BigInteger)result[2]).longValue());
		dto.setFirstName((String)result[3]);
		dto.setLastName((String)result[4]);
		dto.setUsername((String)result[5]);
		dto.setEmail((String)result[6]);
		dto.setCompanyBranchName((String)result[7]);
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
		List<Object[]> datalistResult = repository.findUserBranchMappingDataByIdAndCompanyId(id, session.getCompanyId());
		if(datalistResult!=null && datalistResult.size()>0) {
			Object[] dataResult = datalistResult.get(0);
			UserBranchMappingDTO dto = this.fillUserBranchMappingDTO(dataResult);
			return dto;
		}
		return null;
	}

	private ErrorDTO getValidation(SessionDTO session, Long id) {
		//check if user is superadmin  or usergroupid belongs to same company of session company id. Currently check is in fetch query only.
		return null;
	}

	public List<Long> findUserBranchIdList(Long userId) {
		List<Long> branchIdList = new ArrayList<>();
		List<Object[]> datalistResult = repository.findIdListByUserId(userId);
		if(datalistResult!=null && datalistResult.size()>0) {
			for(Object[] dataResult : datalistResult) {
				branchIdList.add(((BigInteger)dataResult[0]).longValue());
			}
		}
		return branchIdList;
	}
	
}

