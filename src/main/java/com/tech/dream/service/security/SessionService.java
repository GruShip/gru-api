package com.tech.dream.service.security;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.fasterxml.uuid.Generators;
import com.google.gson.Gson;
import com.tech.dream.db.entity.Session;
import com.tech.dream.db.repository.SessionRepository;
import com.tech.dream.db.repository.UserBranchMappingRepository;
import com.tech.dream.db.repository.UserGroupAccessMappingRepository;
import com.tech.dream.model.ErrorDTO;
import com.tech.dream.model.LoginResponseDTO;
import com.tech.dream.model.SessionDTO;
import com.tech.dream.model.UserGroupAccessMappingDTO;
import com.tech.dream.util.DateUtility;

@Service
@Transactional
public class SessionService {
	
	@Autowired
	private SessionRepository sessionRepository;
	
	@Autowired
	private UserBranchMappingRepository userBranchMappingRepository;
	
	@Autowired
	private UserGroupAccessMappingRepository userGroupAccessMappingRepository;
	
	@Autowired
	private Gson gson;
	
	private int sessionDays = 30;
	
	public SessionDTO createSession(LoginResponseDTO loginResponse) throws Exception {
		List<Long> companyBranchIdList = null;
		String companyBranchIdListStr = userBranchMappingRepository.findUserAccessBranchList(loginResponse.getUserId());
		if(StringUtils.isEmpty(companyBranchIdListStr)) {
			companyBranchIdList = new ArrayList<>();
		}else {
			companyBranchIdList = Arrays.stream(companyBranchIdListStr.split(",")).map(Long::valueOf).collect(Collectors.toList());
		}
		List<Object[]> accessModuleResultSet = null;
		if(loginResponse.getUserGroupId()!=null) {
			accessModuleResultSet = userGroupAccessMappingRepository.findAccessModuleForUserGroup(loginResponse.getUserGroupId());
		}else {
			accessModuleResultSet = new ArrayList<>();
		}
		List<UserGroupAccessMappingDTO> accessModuleList = this.convertUserGroupAccessModuleResultSetToDTO(accessModuleResultSet);
		Map<Long, UserGroupAccessMappingDTO> accessModuleMap = new HashMap<Long, UserGroupAccessMappingDTO>();
		for(UserGroupAccessMappingDTO accessModule: accessModuleList) {
			accessModuleMap.put(accessModule.getAccessModuleId(), accessModule);
		}
		
		SessionDTO sessionDTO = new SessionDTO();
		sessionDTO.setUserId(loginResponse.getUserId());
		sessionDTO.setUserGroupId(loginResponse.getUserGroupId());
		sessionDTO.setCompanyId(loginResponse.getCompanyId());
		sessionDTO.setCompanyBranchIdList(companyBranchIdList);
		sessionDTO.setCompanyType(loginResponse.getCompanyType());
		
		Date currentDate = new Date();
		Session session = new Session();
		UUID uuid1 = Generators.timeBasedGenerator().generate();
		String token = uuid1.toString().toUpperCase();
		sessionDTO.setToken(token);
		session.setId(token);
		session.setUserId(loginResponse.getUserId());
		session.setCreatedDate(currentDate);
		session.setExpiryDate(DateUtility.addDate(currentDate, Calendar.DAY_OF_MONTH, sessionDays));
		session.setValue(gson.toJson(sessionDTO));
		sessionRepository.save(session);
		
		sessionDTO.setAccessModuleList(accessModuleMap);
		return sessionDTO;
	}

	private List<UserGroupAccessMappingDTO> convertUserGroupAccessModuleResultSetToDTO(
			List<Object[]> accessModuleResultSet) {
		List<UserGroupAccessMappingDTO> accessModuleList = new ArrayList<UserGroupAccessMappingDTO>();
		for(Object[] result:accessModuleResultSet) {
			UserGroupAccessMappingDTO dto = new  UserGroupAccessMappingDTO();
			dto.setAccessModuleId(((BigInteger)result[0]).longValue());
			dto.setCreateAccess((Boolean)result[1]);
			dto.setReadAccess((Boolean)result[2]);
			dto.setUpdateAccess((Boolean)result[3]);
			dto.setDeleteAccess((Boolean)result[4]);
			dto.setAccessModuleName((String)result[5]);
			dto.setAccessModuleCode((String)result[6]);
			accessModuleList.add(dto);
		}
		return accessModuleList;
	}

	public Object getSession(String token) {
		Date currentDate = new Date();
		Object[] sessionResultSet = sessionRepository.findByTokenAndExpiryDate(token, currentDate);
		if(sessionResultSet == null || sessionResultSet.length==0) {
			return new ErrorDTO(HttpStatus.UNAUTHORIZED, "Token is invalid or expired.");
		}
		String value = (String)sessionResultSet[0];
		SessionDTO sessionDTO = gson.fromJson(value, SessionDTO.class); 
		List<Object[]> accessModuleResultSet = null;
		if(sessionDTO.getUserGroupId()!=null) {
			accessModuleResultSet = userGroupAccessMappingRepository.findAccessModuleForUserGroup(sessionDTO.getUserGroupId());
		}else {
			accessModuleResultSet = new ArrayList<>();
		}
		List<UserGroupAccessMappingDTO> accessModuleList = this.convertUserGroupAccessModuleResultSetToDTO(accessModuleResultSet);
		Map<Long, UserGroupAccessMappingDTO> accessModuleMap = new HashMap<Long, UserGroupAccessMappingDTO>();
		for(UserGroupAccessMappingDTO accessModule: accessModuleList) {
			accessModuleMap.put(accessModule.getAccessModuleId(), accessModule);
		}
		sessionDTO.setAccessModuleList(accessModuleMap);
		return sessionDTO;
	}

	public Object logout(SessionDTO sessionDTO) throws Exception{
		sessionRepository.deleteSessionById(sessionDTO.getToken());
		return null;
	}

}
