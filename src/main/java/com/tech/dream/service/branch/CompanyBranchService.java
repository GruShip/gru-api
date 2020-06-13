package com.tech.dream.service.branch;

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
import com.tech.dream.db.entity.CompanyBranch;
import com.tech.dream.db.repository.CompanyBranchRepository;
import com.tech.dream.db.repository.CompanyRepository;
import com.tech.dream.db.repository.OrderRepository;
import com.tech.dream.db.repository.SellerProductRepository;
import com.tech.dream.db.repository.UserBranchMappingRepository;
import com.tech.dream.model.AddressDTO;
import com.tech.dream.model.CompanyBranchDTO;
import com.tech.dream.model.ErrorDTO;
import com.tech.dream.model.PagingSortSearchDTO;
import com.tech.dream.model.ResultDTO;
import com.tech.dream.model.SearchQueryDTO;
import com.tech.dream.model.SessionDTO;
import com.tech.dream.service.company.AddressService;
import com.tech.dream.util.CommonUtil;
import com.tech.dream.util.Constants;
import com.tech.dream.util.Constants.CompanyType;
import com.tech.dream.util.Constants.DataType;
import com.tech.dream.util.Constants.DisplayModuleNames;

@Service
@Transactional
public class CompanyBranchService {
	
	@Autowired
	private CompanyBranchRepository repository;
	
	@Autowired
	private UserBranchMappingRepository userBranchMappingRepository;
	
	@Autowired
	private CompanyRepository companyRepository;
	
	@Autowired
	private AddressService addressService;
	
	@Autowired
	private SellerProductRepository sellerProductRepository;
	
	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private LocalContainerEntityManagerFactoryBean emf;
	
	public Object create(CompanyBranchDTO dto, SessionDTO session) throws Exception{
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
		CompanyBranch entity = convertToEntity(dto);
		repository.save(entity);
		dto.setId(entity.getId());
		dto.setActive(entity.getActive());
		//dto = convertToDTO(entity);
		return dto;
	}
	
	public Object update(CompanyBranchDTO dto, SessionDTO session) throws Exception{
		ErrorDTO error = updateValidation(dto, session);
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
		Long primaryAddressId = dto.getPrimaryAddress()!=null?dto.getPrimaryAddress().getId():null;
		repository.update(dto.getId(), dto.getCode(), dto.getName(), dto.getParentBranchId(), dto.getActive()!=null?dto.getActive():true, primaryAddressId, dto.getDesc());
		return dto;
	}
	
	public Object delete(CompanyBranchDTO dto, SessionDTO session) {
		ErrorDTO error = deleteValidation(dto, session);
		if (error != null) {
			return error;
		}
		repository.delete(dto.getId());
		return true;
	}
	
	public CompanyBranch convertToEntity(CompanyBranchDTO dto) {
		CompanyBranch e = new CompanyBranch();
		e.setId(dto.getId());;
		e.setCode(dto.getCode());
		e.setName(dto.getName());
		if(dto.getParentBranchId() != null) {
			e.setParentCompanyBranch(new CompanyBranch(dto.getParentBranchId()));
		}
		e.setCompany(new Company(dto.getCompanyId()));
		e.setEmail(dto.getEmail());
		e.setDesc(dto.getDesc());
		e.setDomain(dto.getDomain());
		e.setTaxcode(dto.getTaxcode());
		e.setPhoneNumber1(dto.getPhoneNumber1());
		e.setPhoneNumber2(dto.getPhoneNumber2());
		e.setActive(dto.getActive()!=null?dto.getActive():true);
		e.setIsAdmin(dto.getIsAdmin()!=null?dto.getIsAdmin():false);
		if(dto.getPrimaryAddress()!=null) {
			e.setPrimaryAddress(new Address(dto.getPrimaryAddress().getId()));
		}
		return e;
	}
	
	public CompanyBranchDTO convertToDTO(CompanyBranch e) {
		CompanyBranchDTO dto = new CompanyBranchDTO();
		dto.setId(e.getId());
		dto.setCode(e.getCode());
		dto.setName(e.getName());
		dto.setCompanyId(e.getCompany().getId());
		if(e.getParentCompanyBranch()!=null) {
			dto.setParentBranchId(e.getParentCompanyBranch().getId());
		}
		dto.setEmail(e.getEmail());
		dto.setDesc(e.getDesc());
		dto.setDomain(e.getDomain());
		dto.setTaxcode(e.getTaxcode());
		dto.setPhoneNumber1(e.getPhoneNumber1());
		dto.setPhoneNumber2(e.getPhoneNumber2());
		dto.setActive(e.getActive());
		return dto;
	}
	
	private ErrorDTO deleteValidation(CompanyBranchDTO dto, SessionDTO session) {
		if (dto.getId()==null) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "Id is mandatory and cannot be null.");
		}
		if (repository.existsByCompanyBranchIdAndCompanyId(dto.getId(), session.getCompanyId())<=0) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, DisplayModuleNames.COMPANYBRANCH+" does not exists for given id and "+DisplayModuleNames.COMPANY);
		}
		if (repository.isAdminCompanyBranch(dto.getId())>0) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "Admin " + DisplayModuleNames.COMPANYBRANCH+" cannot be deleted.");
		}
		if (userBranchMappingRepository.existsUsersForCompanyBranch(dto.getId())>0) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, DisplayModuleNames.COMPANYBRANCH+" already has "+ DisplayModuleNames.USER+" mapped to it. Please remove them and try again.");
		}
		if (sellerProductRepository.existsSellerProductByCompanyBranchId(dto.getId())>0) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, DisplayModuleNames.SELLERPRODUCT+"'s are mapped to given "+DisplayModuleNames.COMPANYBRANCH);
		}
		if (orderRepository.existsOrderByCompanyBranchId(dto.getId())>0) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, DisplayModuleNames.ORDER+"'s are mapped to given "+DisplayModuleNames.COMPANYBRANCH);
		}
		return null;
	}
	
	private ErrorDTO createValidation(CompanyBranchDTO dto, SessionDTO session) {
		if(!CompanyType.MARKETPLACE.equalsIgnoreCase(session.getCompanyType()) && session.getCompanyId() != dto.getCompanyId()){
			return new ErrorDTO(HttpStatus.FORBIDDEN, DisplayModuleNames.USER+" does not have access to the given "+ DisplayModuleNames.COMPANY);
		}
		if(StringUtils.isEmpty(dto.getName())) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "Name is mandatory parameter and cannot be null.");
		}
		if(dto.getCompanyId()==null) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "CompanyId is mandatory parameter and cannot be null.");
		}
		if(!companyRepository.existsById(dto.getCompanyId())) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, DisplayModuleNames.COMPANY+" with the given id does not exists.");
		}
		if(!StringUtils.isEmpty(dto.getParentBranchId()) && repository.existsByCompanyBranchIdAndCompanyId(dto.getParentBranchId(), dto.getCompanyId())<=0) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "Parent "+DisplayModuleNames.COMPANYBRANCH+" does not exists for given id and "+DisplayModuleNames.COMPANY);
		}
		if(repository.existsByNameAndCompanyId(dto.getName(), dto.getCompanyId())>0) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, DisplayModuleNames.COMPANYBRANCH+" already exists for give branch name and "+DisplayModuleNames.COMPANY);
		}
		return null;
	}
	
	private ErrorDTO updateValidation(CompanyBranchDTO dto, SessionDTO session) {
		if(!CompanyType.MARKETPLACE.equalsIgnoreCase(session.getCompanyType()) && session.getCompanyId() != dto.getCompanyId()){
			return new ErrorDTO(HttpStatus.FORBIDDEN, DisplayModuleNames.USER+" does not have access to the given "+DisplayModuleNames.COMPANY);
		}
		if(dto.getId()==null) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "Id is mandatory and cannot be null.");
		}
		if (repository.existsByCompanyBranchIdAndCompanyId(dto.getId(), session.getCompanyId())<=0) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, DisplayModuleNames.COMPANYBRANCH+" does not exists for given id and "+DisplayModuleNames.COMPANY);
		}
		if(StringUtils.isEmpty(dto.getName())) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "Name is mandatory parameter and cannot be null.");
		}
		if(dto.getCompanyId()==null) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "CompanyId is mandatory parameter and cannot be null.");
		}
		if(repository.existsByNameAndCompanyIdAndNotId(dto.getName(), dto.getCompanyId(), dto.getId())>0) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, DisplayModuleNames.COMPANYBRANCH+" already exists for give branch name and "+DisplayModuleNames.COMPANY);
		}
		if(!StringUtils.isEmpty(dto.getParentBranchId()) && repository.isSameCompanyForCompanyBranchIdAndParentCompanyBranchId(dto.getId(), dto.getParentBranchId())<=0) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, DisplayModuleNames.COMPANYBRANCH+" & Parent "+DisplayModuleNames.COMPANYBRANCH+" does not belong to same "+DisplayModuleNames.COMPANY);
		}
		return null;
	}

	public ResultDTO list(SessionDTO session, Long companyId, PagingSortSearchDTO filters) {
		ResultDTO resultDTO = new ResultDTO();
		Long totalCount = 0L; 
		ErrorDTO error = listValidation(session);
		if(!CompanyType.MARKETPLACE.equalsIgnoreCase(session.getCompanyType())){
			companyId = session.getCompanyId();
		}
		if(error!=null) {
			resultDTO.setErrorDTO(error);
			return resultDTO;
		}

		if (filters == null) filters = new PagingSortSearchDTO();
		if(companyId == null) {
			companyId = 0L; 
		}
		List<Object[]> datalistResult = null;
		if(CommonUtil.isMarketPlaceCompany(session) || (session.getCompanyBranchIdList()!=null && session.getCompanyBranchIdList().size()>0)) {
		
			EntityManager em = emf.getNativeEntityManagerFactory().createEntityManager();
			
			
			try {
				String query = "select cb.id, cb.name, cb.code, cb.`desc`, cb.email, cb.domain, cb.taxcode, cb.phone_number_1, cb.phone_number_2, a.id as address_id, a.address_line_1, a.address_line_2, a.city_id, a.state_id, a.country_id, a.pincode, a.address_type, pcb.id as pcb_id, pcb.name as pcb_name,cb.is_admin "
				+ " from companybranch cb left join address a on a.id = cb.primary_address_id left join companybranch pcb on pcb.id = cb.parent_company_branch_id"
				+ " where cb.removed = FALSE and cb.company_id = " + companyId; 
	
				String totalCountQuery = "select count(1)"
				+ " from companybranch cb left join address a on a.id = cb.primary_address_id left join companybranch pcb on pcb.id = cb.parent_company_branch_id"
				+ " where cb.removed = FALSE and cb.company_id = " + companyId; 
				
				if(!CommonUtil.isMarketPlaceCompany(session)) {
					query = query + " and  cb.id in (" + session.getCompanyBranchIdList().toString().substring(1, session.getCompanyBranchIdList().toString().length()-1)+ ")";
					totalCountQuery = totalCountQuery + " and  cb.id in (" + session.getCompanyBranchIdList().toString().substring(1, session.getCompanyBranchIdList().toString().length()-1)+ ")";
				}
				
				// searching
				String searchQuery = "";
				if (filters.getSearch() != null){
					for (SearchQueryDTO sDto: filters.getSearch()){
						String dbField = getDBField(sDto.getSearchField());
						String fieldDatatype = getCompanyBranchFieldDataType(sDto.getSearchField());
	
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
					query += " order by cb.id";
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
			}finally{
				em.close();
			}
		}
		List<CompanyBranchDTO> dtoList = new ArrayList<>();
		if(datalistResult!=null && datalistResult.size()>0) {
			for(Object[] dataResult : datalistResult) {
				CompanyBranchDTO dto = this.fillCompanyBranchDTO(dataResult);
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

	private CompanyBranchDTO fillCompanyBranchDTO(Object[] result) {
		CompanyBranchDTO dto = new CompanyBranchDTO();
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
		dto.setParentBranchId(result[17]!=null?((BigInteger)result[17]).longValue():null);
		dto.setParentBranchName((String)result[18]);
		dto.setIsAdmin((Boolean)result[19]);
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
		List<Object[]> datalistResult = repository.findCompanyDataById(id);
		if(datalistResult!=null && datalistResult.size()>0) {
			Object[] dataResult = datalistResult.get(0);
			CompanyBranchDTO dto = this.fillCompanyBranchDTO(dataResult);
			return dto;
		}
		return null;
	}

	private ErrorDTO getValidation(SessionDTO session, Long id) {
		if(!CompanyType.MARKETPLACE.equalsIgnoreCase(session.getCompanyType())) {
			if(repository.existsByIdAndCompanyId(session.getCompanyId(), id)<=0) {
				return new ErrorDTO(HttpStatus.FORBIDDEN, DisplayModuleNames.USER+" does not have access to the given "+DisplayModuleNames.COMPANYBRANCH);
			}
		}
		return null;
	}

	private String getCompanyBranchFieldDataType(String column) {
		switch (column) {
			case "id":
			case "active": 
			case "companyId":
			case "parentBranchId":
				return DataType.TYPE_INT;
			case "name": 
			case "code":
			case "desc":
			case "domain":
			case "email":
			case "phoneNumber1":
			case "phoneNumber2":
			case "taxcode":
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
				return "cb." + UIField;
			case "desc": 
				return "cb.`desc`";
			case "phoneNumber1":
				return "cb.phone_number_1";
			case "phoneNumber2":
				return "cb.phone_number_2";
			case "companyId": 
				return "cb.company_id";
			case "parentBranchId":
				return "cb.parent_company_branch_id";
		}
		return "";
	}
	
}
