package com.tech.dream.service.company;

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
import com.tech.dream.db.repository.CompanyBranchRepository;
import com.tech.dream.db.repository.CompanyRepository;
import com.tech.dream.db.repository.UserGroupRepository;
import com.tech.dream.model.AccessModuleDTO;
import com.tech.dream.model.AddressDTO;
import com.tech.dream.model.CompanyBranchDTO;
import com.tech.dream.model.CompanyDTO;
import com.tech.dream.model.ErrorDTO;
import com.tech.dream.model.PagingSortSearchDTO;
import com.tech.dream.model.ResultDTO;
import com.tech.dream.model.SearchQueryDTO;
import com.tech.dream.model.SessionDTO;
import com.tech.dream.model.UserDTO;
import com.tech.dream.model.UserGroupAccessMappingDTO;
import com.tech.dream.model.UserGroupDTO;
import com.tech.dream.service.accessmodule.AccessModuleService;
import com.tech.dream.service.branch.CompanyBranchService;
import com.tech.dream.service.user.UserService;
import com.tech.dream.service.usergroup.UserGroupService;
import com.tech.dream.util.Constants;
import com.tech.dream.util.Constants.CompanyLevel;
import com.tech.dream.util.Constants.CompanyType;
import com.tech.dream.util.Constants.DataType;
import com.tech.dream.util.Constants.DisplayModuleNames;

@Service
@Transactional
public class CompanyService {
	
	@Autowired
	private CompanyRepository repository;
	
	@Autowired
	private CompanyBranchRepository companyBranchRepository;
	
	@Autowired
	private UserGroupRepository userGroupRepository;
	
	@Autowired
	private AddressService addressService;

	@Autowired
	private LocalContainerEntityManagerFactoryBean emf;
	
	@Autowired
	private CompanyBranchService companyBranchService; 
	
	@Autowired
	private UserGroupService userGroupService;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private AccessModuleService accessModuleService;
	
	@Autowired
	private ProductListingCompanyMappingService productListingCompanyMappingService;
	
	public ResultDTO list(SessionDTO session, PagingSortSearchDTO filters) {
		ResultDTO resultDTO = new ResultDTO();
		Long totalCount = 0L; 
		ErrorDTO error = listValidation(session);
		if(error!=null) {
			resultDTO.setErrorDTO(error);
			return resultDTO;
		}

		EntityManager em = emf.getNativeEntityManagerFactory().createEntityManager();
		List<Object[]> datalistResult = null;

		if (filters==null) filters = new PagingSortSearchDTO();

		try {
			String query = "SELECT c.id, c.name, c.code, c.`desc`, c.email, c.domain, c.taxcode, c.phone_number_1, c.phone_number_2, a.id as address_id, a.address_line_1, a.address_line_2, a.city_id, a.state_id, a.country_id, a.pincode, a.address_type,c.type,c.company_type "
			+ " from company c left join address a on a.id = c.primary_address_id "
			+ " where c.removed = FALSE";

			String totalCountQuery = "SELECT count(1)"
			+ " from company c left join address a on a.id = c.primary_address_id "
			+ " where c.removed = FALSE";

			// searching
			String searchQuery = "";
			if (filters.getSearch() != null){
				for (SearchQueryDTO sDto: filters.getSearch()){
					String dbField = getDBField(sDto.getSearchField());
					String fieldDatatype = getCompanyFieldDataType(sDto.getSearchField());

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
				query += " order by c.id";
			}
	
			if (filters.getSortOrder() != null) {
				query += " " + filters.getSortOrder();
			} else {
				query += " desc";
			}

			// pagination
			if (filters.getPageNumber() == null) { 
				filters.setPageNumber(Constants.DEFAULT_PAGE_NUMBER);
			}
			if (filters.getPageSize() == null) {
				filters.setPageNumber(Constants.DEFAULT_PAGE_NUMBER);
				filters.setPageSize(Constants.DEFAULT_PAGE_SIZE);
			}
			query += " limit " + (filters.getPageNumber() * filters.getPageSize()) + "," + filters.getPageSize();
			query += ";";

			Query q = em.createNativeQuery(query);
			datalistResult = q.getResultList();

			q = em.createNativeQuery(totalCountQuery);
			totalCount = Long.parseLong(String.valueOf(q.getSingleResult()));
		}finally {
			em.close();
		}
		
		List<CompanyDTO> dtoList = new ArrayList<>();
		if(datalistResult!=null && datalistResult.size()>0) {
			for(Object[] dataResult : datalistResult) {
				CompanyDTO dto = this.fillCompanyDTO(dataResult);
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
	
	private ErrorDTO listValidation(SessionDTO session) {
		if(!CompanyType.MARKETPLACE.equalsIgnoreCase(session.getCompanyType())) {
			return new ErrorDTO(HttpStatus.FORBIDDEN, "Only MarketPlace "+DisplayModuleNames.USER+" are allowed to access the list view of the "+DisplayModuleNames.COMPANY);
		}
		return null;
	}

	public Object get(SessionDTO session, Long id) {
		if(!CompanyType.MARKETPLACE.equalsIgnoreCase(session.getCompanyType())) {
			id = session.getCompanyId();
		}
		
		List<Object[]> datalistResult = repository.findCompanyDataById(id);
		if(datalistResult!=null && datalistResult.size()>0) {
			Object[] dataResult = datalistResult.get(0);
			CompanyDTO dto = this.fillCompanyDTO(dataResult);
			dto.setProductListingCompanyIdList(this.getProductListingCompanyIdList(dto.getId()));
			return dto;
		}
		return null;
	}
	
	public Object create(CompanyDTO dto, SessionDTO session)  throws Exception{
		ErrorDTO error = createValidation(dto, session);
		if(error!=null) {
			return error;
		}
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
		Company company = convertToEntity(dto);
		repository.save(company);
		dto.setId(company.getId());
		dto.setActive(company.getActive());
		
		productListingCompanyMappingService.createList(dto.getProductListingCompanyIdList(), company.getId(), session);
		
		Object cbresult = this.createDefaultCompanyBranch(dto, session);
		if(cbresult instanceof ErrorDTO) {
			return cbresult;
		}
		CompanyBranchDTO cbdto =  (CompanyBranchDTO) cbresult;
		Object ugresult = this.createDefaultUserGroup(dto, session);
		if(ugresult instanceof ErrorDTO) {
			return ugresult;
		}
		UserGroupDTO ugdto =  (UserGroupDTO) ugresult;
		this.createDefaultUser(dto, session, ugdto.getId(), cbdto.getId());
		//dto = convertToDTO(company);
		return dto;
	}
	
	private Object createDefaultCompanyBranch(CompanyDTO dto, SessionDTO session) throws Exception {
		CompanyBranchDTO bdto = new CompanyBranchDTO();
		bdto.setCompanyId(dto.getId());
		bdto.setName("Demo Head Branch");
		bdto.setIsAdmin(true);
		return companyBranchService.create(bdto, session);
	}
	
	private Object createDefaultUser(CompanyDTO dto, SessionDTO session, Long userGroupId, Long companyBranchId) throws Exception {
		String username = dto.getName().toLowerCase().replace(" ", "") + "_admin";
		UserDTO bdto = new UserDTO();
		bdto.setCompanyId(dto.getId());
		bdto.setUsername(username);
		bdto.setFirstName("Admin");
		bdto.setLastName("Admin");
		bdto.setPassword("admin");
		bdto.setConfirmPassword("admin");
		bdto.setEmail("test@test.com");
		bdto.setIsAdmin(true);
		bdto.setUserGroupId(userGroupId);
		List<Long> companyBranchIdList = new ArrayList<>();
		companyBranchIdList.add(companyBranchId);
		bdto.setCompanyBranchIdList(companyBranchIdList);
		return userService.createGeneralUser(bdto, session);
	}
	
	private Object createDefaultUserGroup(CompanyDTO dto, SessionDTO session) throws Exception {
		Object resultamList = accessModuleService.list(session, dto.getId());
		if(resultamList instanceof ErrorDTO) {
			return resultamList;
		}
		
		UserGroupDTO bdto = new UserGroupDTO();
		bdto.setCompanyId(dto.getId());
		bdto.setName("Demo Head User Group");
		bdto.setIsAdmin(true);
		
		List<AccessModuleDTO> amList = (List<AccessModuleDTO>) resultamList;
		List<UserGroupAccessMappingDTO> ugamDTOList = new ArrayList<>();
		for(AccessModuleDTO am: amList) {
			UserGroupAccessMappingDTO ugamDTO = new UserGroupAccessMappingDTO();
			ugamDTO.setAccessModuleId(am.getId());
			String type = am.getType();
			for(int i = 0;i<type.length();i++) {
				char c = type.charAt(i);
				switch(c) {
					case 'C':
						ugamDTO.setCreateAccess(true);
						break;
					case 'R':
						ugamDTO.setReadAccess(true);
						break;
					case 'U':
						ugamDTO.setUpdateAccess(true);
						break;
					case 'D':
						ugamDTO.setDeleteAccess(true);
						break;
				}
			}
			ugamDTOList.add(ugamDTO);
		}
		bdto.setAccessModuleList(ugamDTOList);
		return userGroupService.create(bdto, session);
	}

	public Object update(CompanyDTO dto, SessionDTO session)  throws Exception{
		ErrorDTO error = updateValidation(dto, session);
		if(error!=null) {
			return error;
		}
		AddressDTO primaryAddressDTO = dto.getPrimaryAddress();
		if (primaryAddressDTO != null) {
			Object result = addressService.process(primaryAddressDTO);
			if (result != null) {
				if (result instanceof ErrorDTO) {
					return result;
				} else {
					primaryAddressDTO = (AddressDTO) result;
				}
			}
			dto.setPrimaryAddress(primaryAddressDTO);
		}
		Long primaryAddressId = dto.getPrimaryAddress()!=null?dto.getPrimaryAddress().getId():null;
		repository.update(dto.getId(), dto.getCode(), dto.getName(), dto.getEmail(), dto.getDesc(), dto.getDomain(), dto.getTaxcode(), dto.getPhoneNumber1(), dto.getPhoneNumber2(), dto.getActive()!=null?dto.getActive():true, primaryAddressId, dto.getCompanyType()!=null?dto.getCompanyType():CompanyLevel.RETAILER);
		
		productListingCompanyMappingService.createList(dto.getProductListingCompanyIdList(), dto.getId(), session);
		//Company company = convertToEntity(companyDTO);
		//repository.save(company);
		//companyDTO = convertToDTO(company);
		return dto;
	}

	public Object delete(CompanyDTO dto, SessionDTO session) {
		ErrorDTO error = deleteValidation(dto, session);
		if (error != null) {
			return error;
		}
		repository.delete(dto.getId());
		return true;
	}
	
	private ErrorDTO deleteValidation(CompanyDTO dto, SessionDTO session) {
		if(!CompanyType.MARKETPLACE.equalsIgnoreCase(session.getCompanyType())) {
			return new ErrorDTO(HttpStatus.FORBIDDEN, "Only Marketplace "+DisplayModuleNames.USER+" are allowed to delete the "+DisplayModuleNames.COMPANY);
		}
		if (StringUtils.isEmpty(dto.getId())) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "Id is mandatory and cannot be null.");
		}
		if (!repository.existsById(dto.getId())) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, DisplayModuleNames.COMPANY+" does not exists for given id. id: " + dto.getId());
		}
		if (companyBranchRepository.existsBranchForCompany(dto.getId())>0) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, DisplayModuleNames.COMPANY+" has "+DisplayModuleNames.COMPANYBRANCH+" linked to it. Please remove them before deleting "+DisplayModuleNames.COMPANY);
		}
		if (userGroupRepository.existsUserGroupForCompany(dto.getId())>0) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, DisplayModuleNames.COMPANY+" has "+ DisplayModuleNames.USERGROUP+" linked to it. Please remove them before deleting "+DisplayModuleNames.COMPANY);
		}
		return null;
	}
	
	public Company convertToEntity(CompanyDTO dto) {
		Company e = new Company();
		e.setId(dto.getId());;
		e.setCode(dto.getCode());
		e.setName(dto.getName());
		e.setEmail(dto.getEmail());
		e.setDesc(dto.getDesc());
		e.setDomain(dto.getDomain());
		e.setTaxcode(dto.getTaxcode());
		e.setPhoneNumber1(dto.getPhoneNumber1());
		e.setPhoneNumber2(dto.getPhoneNumber2());
		e.setActive(dto.getActive());
		e.setType(CompanyType.SELLER);
		if(dto.getPrimaryAddress()!=null) {
			e.setPrimaryAddress(new Address(dto.getPrimaryAddress().getId()));
		}
		e.setCompanyType(dto.getCompanyType()!=null?dto.getCompanyType():CompanyLevel.RETAILER);
		return e;
	}
	
	public CompanyDTO convertToDTO(Company e) {
		CompanyDTO dto = new CompanyDTO();
		dto.setId(e.getId());
		dto.setCode(e.getCode());
		dto.setName(e.getName());
		dto.setEmail(e.getEmail());
		dto.setDesc(e.getDesc());
		dto.setDomain(e.getDomain());
		dto.setTaxcode(e.getTaxcode());
		dto.setPhoneNumber1(e.getPhoneNumber1());
		dto.setPhoneNumber2(e.getPhoneNumber2());
		dto.setActive(e.getActive());
		dto.setCompanyType(e.getCompanyType());
		return dto;
	}

	private ErrorDTO createValidation(CompanyDTO dto, SessionDTO session) {
		if(!CompanyType.MARKETPLACE.equalsIgnoreCase(session.getCompanyType())) {
			return new ErrorDTO(HttpStatus.FORBIDDEN, "Only Marketplace "+DisplayModuleNames.USER+" are allowed to create the "+DisplayModuleNames.COMPANY);
		}
		if(StringUtils.isEmpty(dto.getName())) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "Name is mandatory parameter and cannot be null.");
		}
		if(repository.existsByName(dto.getName())) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, DisplayModuleNames.COMPANY+" already exists for give name.");
		}
		return null;
	}
	
	private ErrorDTO updateValidation(CompanyDTO dto, SessionDTO session) {
		if(StringUtils.isEmpty(dto.getId())) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "Id is mandatory and cannot be null.");
		}
		if(!CompanyType.MARKETPLACE.equalsIgnoreCase(session.getCompanyType()) && session.getCompanyId() != dto.getId()) {
			return new ErrorDTO(HttpStatus.FORBIDDEN, DisplayModuleNames.USER+" does not have access to update the "+DisplayModuleNames.COMPANY);
		}
		if(!repository.existsById(dto.getId())) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, DisplayModuleNames.COMPANY+" does not exists for given id.");
		}
		if(StringUtils.isEmpty(dto.getName())) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "Name is mandatory parameter and cannot be null.");
		}
		
		if(repository.existsByNameAndNotId(dto.getName(), dto.getId())) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, DisplayModuleNames.COMPANY+" already exists for give name.");
		}
		return null;
	}
	
	private CompanyDTO fillCompanyDTO(Object[] result) {
		CompanyDTO dto = new CompanyDTO();
		dto.setId(((BigInteger)result[0]).longValue());
		dto.setName((String)result[1]);
		dto.setCode((String)result[2]);
		dto.setDesc((String)result[3]);
		dto.setEmail((String)result[4]);
		dto.setDomain((String)result[5]);
		dto.setTaxcode((String)result[6]);
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
		dto.setType((String)result[17]);
		dto.setCompanyType((String)result[18]);
		return dto;
	}

	private String getCompanyFieldDataType(String column) {
		switch (column) {
			case "id":
			case "active":
				return DataType.TYPE_INT;
			case "name":
			case "code":
			case "desc": 
			case "domain":
			case "email": 
			case "taxcode": 
			case "type":
			case "phoneNumber1": 
			case "phoneNumber2":
			case "companyType":
				return DataType.TYPE_STRING;				
		}
		return "";
	}

	private String getDBField(String UIField) {
		switch(UIField) {
			case "id":
			case "name":
			case "code": 
			case "domain":
			case "email": 
			case "taxcode": 
			case "active": 
			case "type":
				return "c." + UIField;
			case "desc":
				return "c.`desc`";
			case "phoneNumber1": 
				return "c.phone_number_1";
			case "phoneNumber2":
				return "c.phone_number_2";
			case "companyType":
				return "c.company_type";
		}
		return "";
	}

	public List<Long> getProductListingCompanyIdList(Long sourceCompanyId){
		return productListingCompanyMappingService.getProductListingCompanyIdList(sourceCompanyId);
	}
	
}
