package com.tech.dream.service.security;

import java.math.BigInteger;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.tech.dream.db.repository.UserRepository;
import com.tech.dream.model.ErrorDTO;
import com.tech.dream.model.LoginRequestDTO;
import com.tech.dream.model.LoginResponseDTO;
import com.tech.dream.model.SessionDTO;
import com.tech.dream.util.CommonUtil;
import com.tech.dream.util.Constants.UserType;

@Service
@Transactional
public class SecurityService {

	@Autowired
	private UserRepository repository;
	
	@Autowired
	private SessionService sessionService;
	
	public Object signin(LoginRequestDTO dto) throws Exception {
		String type = dto.getType();
		
		if(UserType.FIELD_AGENT.equalsIgnoreCase(type)) {
			return signinFieldAgent(dto);
		}else {
			return signinGeneralUser(dto);
		}
	}

	private Object signinFieldAgent(LoginRequestDTO dto) {
		
		return null;
	}

	private Object signinGeneralUser(LoginRequestDTO dto) throws Exception {
		ErrorDTO error = signinGeneralUserValidation(dto);
		if(error!=null) {
			return error;
		}
		String hash = CommonUtil.getHash(dto.getPassword());
		Object loginResponseResult = repository.findGeneralUserByUsernameAndPassword(dto.getUsername(), hash, UserType.GENERAL);
		if(loginResponseResult == null) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "Invalid username and password.");
		}
		LoginResponseDTO loginResponse = this.convertLoginResultToLoginResponseDTO(loginResponseResult);
		
		SessionDTO sessionDTO = sessionService.createSession(loginResponse);
		loginResponse.setToken(sessionDTO.getToken());
		loginResponse.setAccessModuleList(new ArrayList<>(sessionDTO.getAccessModuleList().values()));
		return loginResponse;
	}

	private LoginResponseDTO convertLoginResultToLoginResponseDTO(Object loginResponseResultObj) {
		Object[] loginResponseResult = (Object[]) loginResponseResultObj;
		LoginResponseDTO dto = new LoginResponseDTO();
		dto.setType(loginResponseResult[0]!=null?(String)loginResponseResult[0]:null);
		dto.setUserId(((BigInteger)loginResponseResult[1]).longValue());
		dto.setFirstName(loginResponseResult[2]!=null?(String)loginResponseResult[2]:null);
		dto.setLastName(loginResponseResult[3]!=null?(String)loginResponseResult[3]:null);
		dto.setEmail(loginResponseResult[4]!=null?(String)loginResponseResult[4]:null);
		dto.setPhoneNumber1(loginResponseResult[5]!=null?(String)loginResponseResult[5]:null);
		dto.setPhoneNumber2(loginResponseResult[6]!=null?(String)loginResponseResult[6]:null);
		dto.setCompanyId(loginResponseResult[7]!=null ?((BigInteger)loginResponseResult[7]).longValue():null);
		dto.setUserGroupId(loginResponseResult[8]!=null?((BigInteger)loginResponseResult[8]).longValue():null);
		dto.setCompanyType(loginResponseResult[9]!=null?(String)loginResponseResult[9]:null);
		return dto;
	}

	private ErrorDTO signinGeneralUserValidation(LoginRequestDTO dto) {
		if(StringUtils.isEmpty(dto.getUsername())) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "Username is mandatory parameter and cannot be empty.");
		}
		
		if(StringUtils.isEmpty(dto.getPassword())) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "Password is mandatory parameter and cannot be empty.");
		}
		
		return null;
	}

}
