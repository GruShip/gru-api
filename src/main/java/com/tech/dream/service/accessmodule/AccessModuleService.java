package com.tech.dream.service.accessmodule;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.tech.dream.db.entity.AccessModule;
import com.tech.dream.db.repository.AccessModuleRepository;
import com.tech.dream.model.AccessModuleDTO;
import com.tech.dream.model.ErrorDTO;
import com.tech.dream.model.SessionDTO;
import com.tech.dream.util.Constants.CompanyType;

@Service
@Transactional
public class AccessModuleService {

	@Autowired
	private AccessModuleRepository repository;

	public Object create(AccessModuleDTO dto, SessionDTO session) {
		ErrorDTO error = createValidation(dto, session);
		if (error != null) {
			return error;
		}
		AccessModule entity = convertToEntity(dto);
		repository.save(entity);
		dto = convertToDTO(entity);
		return dto;
	}

	public Object update(AccessModuleDTO dto, SessionDTO session) {
		ErrorDTO error = updateValidation(dto, session);
		if (error != null) {
			return error;
		}
		repository.update(dto.getId(), dto.getCode(), dto.getName(), dto.getDesc());
		//AccessModule entity = convertToEntity(dto);
		//repository.save(entity);
		//dto = convertToDTO(entity);
		return dto;
	}

	public Object delete(AccessModuleDTO dto, SessionDTO session) {
		ErrorDTO error = deleteValidation(dto, session);
		if (error != null) {
			return error;
		}
		repository.delete(dto.getId());
		return true;
	}

	public AccessModule convertToEntity(AccessModuleDTO dto) {
		AccessModule e = new AccessModule();
		e.setId(dto.getId());
		e.setCode(dto.getCode());
		e.setName(dto.getName());
		e.setDesc(dto.getDesc());
		return e;
	}

	public AccessModuleDTO convertToDTO(AccessModule e) {
		AccessModuleDTO dto = new AccessModuleDTO();
		dto.setId(e.getId());
		dto.setCode(e.getCode());
		dto.setName(e.getName());
		dto.setDesc(e.getDesc());
		return dto;
	}
	
	private ErrorDTO deleteValidation(AccessModuleDTO dto, SessionDTO session) {
		if(!CompanyType.MARKETPLACE.equalsIgnoreCase(session.getCompanyType())) {
			return new ErrorDTO(HttpStatus.FORBIDDEN, "Only Marketplace user's are allowed to access the accessmodule.");
		}
		if (StringUtils.isEmpty(dto.getId())) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "Id is mandatory and cannot be null.");
		}
		if (!repository.existsByAccessModuleId(dto.getId())) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST,
					"AccessModule does not exists for given id. id: " + dto.getId());
		}
		
		return null;
	}

	private ErrorDTO createValidation(AccessModuleDTO dto, SessionDTO session) {
		if(!CompanyType.MARKETPLACE.equalsIgnoreCase(session.getCompanyType())) {
			return new ErrorDTO(HttpStatus.FORBIDDEN, "Only Marketplace user's are allowed to access the accessmodule.");
		}
		if (StringUtils.isEmpty(dto.getName())) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "Name is mandatory parameter and cannot be null.");
		}
		if (repository.existsByName(dto.getName())) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST,
					"AccessModule already exists for give name.");
		}
		return null;
	}

	private ErrorDTO updateValidation(AccessModuleDTO dto, SessionDTO session) {
		if(!CompanyType.MARKETPLACE.equalsIgnoreCase(session.getCompanyType())) {
			return new ErrorDTO(HttpStatus.FORBIDDEN, "Only Marketplace user's are allowed to access the accessmodule.");
		}
		if (StringUtils.isEmpty(dto.getId())) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "Id is mandatory and cannot be null.");
		}
		if (!repository.existsByAccessModuleId(dto.getId())) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST,
					"AccesModule does not exists for given id. id: " + dto.getId());
		}
		if (StringUtils.isEmpty(dto.getName())) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "Name is mandatory parameter and cannot be null.");
		}
		
		if (repository.existsByNameAndNotId(dto.getName(), dto.getId())>0) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST,
					"AccessModule already exists for give branch name and company.");
		}
		return null;
	}

	public Object list(SessionDTO session, Long companyId) {
		if(!CompanyType.MARKETPLACE.equals(session.getCompanyType())) {
			companyId = session.getCompanyId();
		}
		boolean isAdminModuleNeeded = false;
		boolean isClientModuleNeeded = true;
		if(1 == companyId.intValue()) {
			isAdminModuleNeeded = true;
			isClientModuleNeeded = false;
		}
		
		List<Object[]> datalistResult = repository.findAllAccessModuleData(isAdminModuleNeeded, isClientModuleNeeded);
		List<AccessModuleDTO> dtoList = new ArrayList<>();
		if(datalistResult!=null && datalistResult.size()>0) {
			for(Object[] dataResult : datalistResult) {
				AccessModuleDTO dto = this.fillAccessModuleDTO(dataResult);
				dtoList.add(dto);
			}
		}
		return dtoList;
	}
	
	private AccessModuleDTO fillAccessModuleDTO(Object[] result) {
		AccessModuleDTO dto = new AccessModuleDTO();
		dto.setId(((BigInteger)result[0]).longValue());
		dto.setName((String)result[1]);
		dto.setCode((String)result[2]);
		dto.setDesc((String)result[3]);
		dto.setType((String)result[4]);
		dto.setIsAdminModule(result[5]!=null?(Boolean)result[5]:null);
		dto.setIsClientModule(result[5]!=null?(Boolean)result[6]:null);
		return dto;
	}

	public Object get(SessionDTO session, Long id) {
		List<Object[]> datalistResult = repository.findAccessModuleDataById(id);
		if(datalistResult!=null && datalistResult.size()>0) {
			Object[] dataResult = datalistResult.get(0);
			AccessModuleDTO dto = this.fillAccessModuleDTO(dataResult);
			return dto;
		}
		return null;
	}
	
}

