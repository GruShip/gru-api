package com.tech.dream.service.user;

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

import com.tech.dream.db.entity.Address;
import com.tech.dream.db.entity.Company;
import com.tech.dream.db.entity.User;
import com.tech.dream.db.repository.UserRepository;
import com.tech.dream.model.AddressDTO;
import com.tech.dream.model.ErrorDTO;
import com.tech.dream.model.PagingSortSearchDTO;
import com.tech.dream.model.ResultDTO;
import com.tech.dream.model.SearchQueryDTO;
import com.tech.dream.model.SessionDTO;
import com.tech.dream.model.UserBranchMappingDTO;
import com.tech.dream.model.UserDTO;
import com.tech.dream.model.UserGroupMappingDTO;
import com.tech.dream.service.company.AddressService;
import com.tech.dream.service.usermapping.UserBranchMappingService;
import com.tech.dream.service.usermapping.UserGroupMappingService;
import com.tech.dream.util.CommonUtil;
import com.tech.dream.util.Constants;

@Service
@Transactional
public class UserService implements Constants{
	
	@Autowired
	private UserRepository repository;
	
	@Autowired
	private UserGroupMappingService userGroupMappingService;
	
	@Autowired
	private UserBranchMappingService userBranchMappingService;
	
	@Autowired
	private UserCompanyAccessMappingService userCompanyAccessMappingService;
	
	@Autowired
	private AddressService addressService;

	@Autowired
	private LocalContainerEntityManagerFactoryBean emf;

	public Object updateGeneralUser(UserDTO dto, SessionDTO session) throws Exception{
		ErrorDTO error = updateGeneralUserValidation(dto, session);
		if(error!=null) {
			return error;
		}
		dto.setType(UserType.GENERAL);
		AddressDTO primaryAddressDTO = dto.getPrimaryAddress();
		if(primaryAddressDTO!=null) {
			Object result = addressService.process(primaryAddressDTO);
			if(result != null) {
				if(result instanceof ErrorDTO) {
					return result;
				}else {
					primaryAddressDTO = (AddressDTO) result;
				}
			}
			dto.setPrimaryAddress(primaryAddressDTO);
		}
		Long primaryAddressId = dto.getPrimaryAddress()!=null?dto.getPrimaryAddress().getId():null;
		repository.update(dto.getId(), dto.getFirstName(), dto.getLastName(), dto.getPhoneNumber1(), dto.getPhoneNumber2(), dto.getEmail(), dto.getActive()!=null?dto.getActive():true, primaryAddressId);
		
		if(repository.isAdminUser(dto.getId())<=0) {
			UserGroupMappingDTO ugm = null;
			if(dto.getUserGroupId()!=null) {
				ugm = new UserGroupMappingDTO();
				ugm.setUserGroupId(dto.getUserGroupId());
				ugm.setUserId(dto.getId());
			}
			userGroupMappingService.create(ugm, dto.getId(), session);
		}
		
		List<UserBranchMappingDTO> ubmList = new ArrayList<>();
		if(dto.getCompanyBranchIdList()!=null && dto.getCompanyBranchIdList().size()>0) {	
			for(Long id: dto.getCompanyBranchIdList()) {
				UserBranchMappingDTO ubm = new UserBranchMappingDTO();
				ubm.setUserId(dto.getId());
				ubm.setCompanyBranchId(id);
				ubmList.add(ubm);
			}
			
		}
		userBranchMappingService.createList(ubmList, dto.getId(), session);
		
		//------ Assign Company Access to User -------
		if(isMarketPlaceCompany(dto.getCompanyId())) {
			userCompanyAccessMappingService.create(dto.getId(), dto.getAccessCompanyIdList());
		}
		
		return dto;
	}

	public Object delete(UserDTO dto, SessionDTO session) {
		ErrorDTO error = deleteValidation(dto, session);
		if (error != null) {
			return error;
		}
		repository.delete(dto.getId());
		return true;
	}
	
	public Object createGeneralUser(UserDTO dto, SessionDTO session) throws Exception {
		ErrorDTO error = createGeneralUserValidation(dto, session);
		if(error!=null) {
			return error;
		}
		dto.setType(UserType.GENERAL);
		AddressDTO primaryAddressDTO = dto.getPrimaryAddress();
		if(primaryAddressDTO!=null) {
			Object result = addressService.process(primaryAddressDTO);
			if(result != null) {
				if(result instanceof ErrorDTO) {
					return result;
				}else {
					primaryAddressDTO = (AddressDTO) result;
				}
			}
			dto.setPrimaryAddress(primaryAddressDTO);
		}
		//repository.update(dto.getId(), dto.getFirstName(), dto.getLastName(), dto.getPhoneNumber(), dto.getEmail());
		User entity = convertToEntity(dto);
		repository.save(entity);
		dto.setId(entity.getId());
		
		//------ Assign Usergroup to User  -------
		UserGroupMappingDTO ugm = null;
		if(dto.getUserGroupId()!=null) {
			ugm = new UserGroupMappingDTO();
			ugm.setUserGroupId(dto.getUserGroupId());
			ugm.setUserId(entity.getId());
		}
		userGroupMappingService.create(ugm, dto.getId(), session);
		
		//------ Assign User to Branch -------
		List<UserBranchMappingDTO> ubmList = new ArrayList<>();
		if(dto.getCompanyBranchIdList()!=null && dto.getCompanyBranchIdList().size()>0) {	
			for(Long id: dto.getCompanyBranchIdList()) {
				UserBranchMappingDTO ubm = new UserBranchMappingDTO();
				ubm.setUserId(entity.getId());
				ubm.setCompanyBranchId(id);
				ubmList.add(ubm);
			}
			
		}
		userBranchMappingService.createList(ubmList, dto.getId(), session);
		
		//------ Assign Company Access to User -------
		if(isMarketPlaceCompany(dto.getCompanyId())) {
			userCompanyAccessMappingService.create(dto.getId(), dto.getAccessCompanyIdList());
		}
		//dto = convertToDTO(user);
		return dto;
	}
	
	private Object createFieldAgent(UserDTO dto, SessionDTO session) {
		return null;
	}
	
	public User convertToEntity(UserDTO dto) throws Exception{
		User e = new User();
		e.setId(dto.getId());
		e.setFirstName(dto.getFirstName());
		e.setLastName(dto.getLastName());
		e.setPhoneNumber1(dto.getPhoneNumber1());
		e.setPhoneNumber2(dto.getPhoneNumber2());
		e.setUsername(dto.getUsername());
		e.setEmail(dto.getEmail());
		e.setPassword(CommonUtil.getHash(dto.getPassword()));
		e.setCompany(new Company(dto.getCompanyId()));
		e.setType(dto.getType());
		e.setActive(dto.getActive()!=null?dto.getActive():true);
		e.setIsAdmin(dto.getIsAdmin()!=null?dto.getIsAdmin():false);
		if(dto.getPrimaryAddress()!=null) {
			e.setPrimaryAddress(new Address(dto.getPrimaryAddress().getId()));
		}
		return e;
	}
	
	public UserDTO convertToDTO(User e) {
		UserDTO dto = new UserDTO();
		dto.setId(e.getId());;
		dto.setFirstName(e.getFirstName());
		dto.setLastName(e.getLastName());
		dto.setPhoneNumber1(e.getPhoneNumber1());
		dto.setPhoneNumber2(e.getPhoneNumber2());
		dto.setUsername(e.getUsername());
		dto.setEmail(e.getEmail());
		dto.setCompanyId(e.getCompany().getId());
		dto.setActive(e.getActive());
		return dto;
	}
	
	private ErrorDTO deleteValidation(UserDTO dto, SessionDTO session) {
		//Find out is user is general user or field agent
		return deleteGeneralUserValidation(dto, session);
	}
	
	private ErrorDTO deleteGeneralUserValidation(UserDTO dto, SessionDTO session) {
		if (StringUtils.isEmpty(dto.getId())) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "Id is mandatory and cannot be null.");
		}
		if (!CompanyType.MARKETPLACE.equalsIgnoreCase(session.getCompanyType()) && repository.existsByIdAndCompanyId(dto.getId(), session.getCompanyId())<=0) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, DisplayModuleNames.USER+" does not exists for given id and "+DisplayModuleNames.COMPANY);
		}
		
		return null;
	}
	
	private ErrorDTO createGeneralUserValidation(UserDTO dto, SessionDTO session) {
		if(!CompanyType.MARKETPLACE.equalsIgnoreCase(session.getCompanyType()) && session.getCompanyId() != dto.getCompanyId()){
			return new ErrorDTO(HttpStatus.FORBIDDEN, DisplayModuleNames.USER+" does not have access to the given "+DisplayModuleNames.COMPANY);
		}
		if(StringUtils.isEmpty(dto.getFirstName())) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "FirstName is mandatory parameter and cannot be empty.");
		}
		
		if(StringUtils.isEmpty(dto.getLastName())) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "LastName is mandatory parameter and cannot be empty.");
		}
		
		if(StringUtils.isEmpty(dto.getUsername())) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "UserName is mandatory parameter and cannot be empty.");
		}
		
		if(StringUtils.isEmpty(dto.getPassword())) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "Password is mandatory parameter and cannot be empty.");
		}
		
		if(StringUtils.isEmpty(dto.getConfirmPassword())) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "ConfirmPassword is mandatory parameter and cannot be empty.");
		}
		
		if(!dto.getPassword().equals(dto.getConfirmPassword())) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "Password and ConfirmPassword are not same.");
		}
		
		if(dto.getCompanyId()==null) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "CompanyId is mandatory parameter and cannot be empty.");
		}
		if(!CompanyType.MARKETPLACE.equalsIgnoreCase(session.getCompanyType()) && session.getCompanyId().longValue() != dto.getCompanyId().longValue()) {
			return new ErrorDTO(HttpStatus.FORBIDDEN, "Session "+DisplayModuleNames.USER+" does not have access to create "+DisplayModuleNames.USER+" for provided "+DisplayModuleNames.COMPANY);
		}
		if(repository.existsByUserName(dto.getUsername()) > 0) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, DisplayModuleNames.USER+" already exists for given Username & "+DisplayModuleNames.COMPANY);
		}
		return null;
	}
	
	
	private ErrorDTO updateGeneralUserValidation(UserDTO dto, SessionDTO session) {
		if(!CompanyType.MARKETPLACE.equalsIgnoreCase(session.getCompanyType()) && session.getCompanyId().intValue() != dto.getCompanyId().intValue()){
			return new ErrorDTO(HttpStatus.FORBIDDEN, DisplayModuleNames.USER+" does not have access to the given "+DisplayModuleNames.COMPANY);
		}
		if(dto.getId()==null) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "Id is mandatory and cannot be null.");
		}
		if(!repository.existsById(dto.getId())) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, DisplayModuleNames.USER+" does not exists for given id.");
		}
		if(StringUtils.isEmpty(dto.getFirstName())) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "FirstName is mandatory parameter and cannot be empty.");
		}
		
		if(StringUtils.isEmpty(dto.getLastName())) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "LastName is mandatory parameter and cannot be empty.");
		}
		
		return null;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println(CommonUtil.getHash("systemadmin"));
	}

	public ResultDTO list(SessionDTO session, Long companyId, PagingSortSearchDTO filters) {
		ResultDTO resultDTO = new ResultDTO();
		Long totalCount = 0L; 
		if(!CompanyType.MARKETPLACE.equalsIgnoreCase(session.getCompanyType())) {
			companyId = session.getCompanyId();
		}
		ErrorDTO error = listValidation(session, companyId);
		if(error!=null) {
			resultDTO.setErrorDTO(error);
			return resultDTO;
		}

		if (filters == null) filters = new PagingSortSearchDTO();

		EntityManager em = emf.getNativeEntityManagerFactory().createEntityManager();
		List<Object[]> datalistResult = null;

		try {
			String query = "select u.id, u.first_name, u.last_name, u.username, u.email, u.type, u.is_system_admin, u.phone_number_1, u.phone_number_2, a.id as address_id, a.address_line_1, a.address_line_2, a.city_id, a.state_id, a.country_id, a.pincode, a.address_type, ugm.user_group_id, u.company_id,u.is_admin "
			+ " from user u left join address a on a.id = u.primary_address_id "
			+ " left join usergroupmapping ugm on ugm.user_id = u.id and ugm.removed=FALSE "
			+ " where u.removed = FALSE and u.company_id = " + companyId; 

			String totalCountQuery = "select count(1) from user u left join address a on a.id = u.primary_address_id "
			+ " left join usergroupmapping ugm on ugm.user_id = u.id and ugm.removed=FALSE "
			+ " where u.removed = FALSE and u.company_id = " + companyId;
			
			// searching
			String searchQuery = "";
			if (filters.getSearch() != null){
				for (SearchQueryDTO sDto: filters.getSearch()){
					String dbField = getDBField(sDto.getSearchField());
					String fieldDatatype = getUserFieldDataType(sDto.getSearchField());

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
				query += " order by u.id";
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


		List<UserDTO> dtoList = new ArrayList<>();
		if(datalistResult!=null && datalistResult.size()>0) {
			for(Object[] dataResult : datalistResult) {
				UserDTO dto = this.fillUserDTO(dataResult);
				dto.setCompanyBranchIdList(this.getUserBranchList(dto.getId()));
				if(isMarketPlaceCompany(dto.getCompanyId())) {
					dto.setAccessCompanyIdList(this.getAccessCompanyIdList(dto.getId()));
				}
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

	private UserDTO fillUserDTO(Object[] result) {
		UserDTO dto = new UserDTO();
		dto.setId(((BigInteger)result[0]).longValue());
		dto.setFirstName((String)result[1]);
		dto.setLastName((String)result[2]);
		dto.setUsername((String)result[3]);
		dto.setEmail((String)result[4]);
		dto.setType((String)result[5]);
		dto.setIsSystemAdmin((Boolean)result[6]);
		dto.setPhoneNumber1((String)result[7]);
		dto.setPhoneNumber2((String)result[8]);
		if(result[9]!=null) {
			AddressDTO a = new AddressDTO();
			a.setId(((BigInteger)result[9]).longValue());
			a.setAddressLine1((String)result[10]);
			a.setAddressLine2((String)result[11]);
			if(result[12]!=null) {
				a.setCityId(((BigInteger)result[12]).longValue());
			}
			if(result[13]!=null) {
				a.setStateId(((BigInteger)result[13]).longValue());
			}
			if(result[14]!=null) {
				a.setCountryId(((BigInteger)result[14]).longValue());
			}
			a.setPincode((String)result[15]);
			a.setAddressType((String)result[16]);
			dto.setPrimaryAddress(a);
		}
		dto.setUserGroupId(result[17]!=null?((BigInteger)result[17]).longValue():null);
		dto.setCompanyId(result[18]!=null?((BigInteger)result[18]).longValue():null);
		dto.setIsAdmin((Boolean)result[19]);
		return dto;
	}

	private ErrorDTO listValidation(SessionDTO session, Long companyId) {
		return null;
	}

	public Object get(SessionDTO session, Long id) {
//		if(!CompanyType.MARKETPLACE.equalsIgnoreCase(session.getCompanyType())) {
//			id = session.getUserId();
//		}
		List<Object[]> datalistResult = repository.findUserDataById(id);
		if(datalistResult!=null && datalistResult.size()>0) {
			Object[] dataResult = datalistResult.get(0);
			UserDTO dto = this.fillUserDTO(dataResult);
			dto.setCompanyBranchIdList(this.getUserBranchList(dto.getId()));
			if(isMarketPlaceCompany(dto.getCompanyId())) {
				dto.setAccessCompanyIdList(this.getAccessCompanyIdList(dto.getId()));
			}
			return dto;
		}
		return null;
	}
	
	private List<Long> getAccessCompanyIdList(Long userId) {
		List<Long> companyIdList = userCompanyAccessMappingService.findUserAccessCompanyIdList(userId);
		return companyIdList;
	}

	private List<Long> getUserBranchList(Long userId) {
		List<Long> userBranchIdList = userBranchMappingService.findUserBranchIdList(userId);
		return userBranchIdList;
	}
	
	public boolean isMarketPlaceCompany(Long companyId) {
		return 1 == companyId.longValue();
	}

	private String getUserFieldDataType(String column) {
		switch (column) {
			case "id":
			case "active":
			case "isSystemAdmin":
				return DataType.TYPE_INT;
			case "username": 
			case "email": 
			case "type":
			case "firstName":
			case "lastName":
			case "phoneNumber1": 
			case "phoneNumber2":
				return DataType.TYPE_STRING;
		}
		return "";
	}

	private String getDBField(String UIField) {
		switch(UIField) {
			case "id":
			case "username": 
			case "email":
			case "type":
			case "active":
				return "u." + UIField;
			case "firstName":
				return "u.first_name";
			case "lastName":
				return "u.last_name";
			case "phoneNumber1": 
				return "u.phone_number_1";
			case "phoneNumber2":
				return "u.phone_number_2";
			case "isSystemAdmin":
				return "u.is_system_admin";
			case "userGroupId":
				return "ugm.user_group_id";
		}
		return "";
	}
	
}
