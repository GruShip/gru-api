package com.tech.dream.service.product;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
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
import com.tech.dream.db.entity.ProductCoupon;
import com.tech.dream.db.repository.ProductCouponRepository;
import com.tech.dream.db.repository.SellerProductRepository;
import com.tech.dream.model.ErrorDTO;
import com.tech.dream.model.PagingSortSearchDTO;
import com.tech.dream.model.ProductCouponDTO;
import com.tech.dream.model.ResultDTO;
import com.tech.dream.model.SearchQueryDTO;
import com.tech.dream.model.SessionDTO;
import com.tech.dream.util.CommonUtil;
import com.tech.dream.util.Constants;
import com.tech.dream.util.Constants.DataType;
import com.tech.dream.util.Constants.DisplayModuleNames;

@Service
@Transactional
public class ProductCouponService {
	
	@Autowired
	private ProductCouponRepository repository;
	
	@Autowired
	private SellerProductRepository sellerProductRepository;
	
	@Autowired
	private LocalContainerEntityManagerFactoryBean emf;
	
	public Object create(ProductCouponDTO dto, SessionDTO session) {
		ErrorDTO error = createValidation(dto, session);
		if (error != null) {
			return error;
		}
		ProductCoupon entity = convertToEntity(dto);
		repository.save(entity);
		dto.setId(entity.getId());
		dto.setActive(entity.getActive());
		
		//dto = convertToDTO(entity);
		return dto;
	}
	
	public Object update(ProductCouponDTO dto, SessionDTO session) {
		ErrorDTO error = updateValidation(dto, session);
		if (error != null) {
			return error;
		}
		repository.updateProductCoupon(dto.getId(), dto.getName(), dto.getActive()!=null?dto.getActive():true, dto.getDesc());
		return dto;
	}

	public Object delete(ProductCouponDTO dto, SessionDTO session) {
		ErrorDTO error = deleteValidation(dto, session);
		if (error != null) {
			return error;
		}
		repository.deleteProductCoupon(dto.getId());
		return true;
	}

	public ProductCoupon convertToEntity(ProductCouponDTO dto) {
		ProductCoupon e = new ProductCoupon();
		e.setId(dto.getId());
		e.setName(dto.getName());
		e.setDesc(dto.getDesc());
		e.setType(dto.getType().toUpperCase());
		e.setDiscountType(dto.getDiscountType().toUpperCase());
		e.setExpiryDate(dto.getExpiryDate());
		e.setCompany(new Company(dto.getCompanyId()));
		e.setActive(dto.getActive()!=null?dto.getActive():true);
		return e;
	}

	private ErrorDTO deleteValidation(ProductCouponDTO dto, SessionDTO session) {
		if (dto.getId()==null) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "Id is mandatory and cannot be null.");
		}
		if (repository.existsProductCouponById(dto.getId())<=0) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, DisplayModuleNames.PRODUCTCOUPON+" does not exists for given id.");
		}
		if (sellerProductRepository.existsSellerProductByProductCouponId(dto.getId())>0) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, DisplayModuleNames.SELLERPRODUCT+"'s are mapped to given "+DisplayModuleNames.PRODUCTCOUPON);
		}
		return null;
	}

	private ErrorDTO createValidation(ProductCouponDTO dto, SessionDTO session) {
		if(StringUtils.isEmpty(dto.getName())) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "Name is mandatory parameter and cannot be null.");
		}
		if(StringUtils.isEmpty(dto.getType())) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "Type is mandatory parameter and cannot be null.");
		}
		if(StringUtils.isEmpty(dto.getDiscountType())) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "DiscountType is mandatory parameter and cannot be null.");
		}
		if(dto.getCompanyId() == null) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "CompanyId is mandatory parameter and cannot be null.");
		}
		if(dto.getValue() == null && dto.getValue().doubleValue()<=0) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "Value is mandatory parameter and must be greater than 0.");
		}
		if(!CommonUtil.isMarketPlaceCompany(session) && session.getCompanyId().longValue()!=dto.getCompanyId().longValue()) {
			return new ErrorDTO(HttpStatus.FORBIDDEN, DisplayModuleNames.USER+" does not have access to create "+DisplayModuleNames.PRODUCTCOUPON + " for given "+ DisplayModuleNames.COMPANY);
		}
		if (repository.existsProductCouponByNameAndCompanyId(dto.getName(), dto.getCompanyId())>0) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, DisplayModuleNames.PRODUCTCOUPON+" already exists for give name and "+DisplayModuleNames.COMPANY);
		}
		return null;
	}

	private ErrorDTO updateValidation(ProductCouponDTO dto, SessionDTO session) {
		if (StringUtils.isEmpty(dto.getId())) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "Id is mandatory and cannot be null.");
		}
		if(StringUtils.isEmpty(dto.getName())) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "Name is mandatory parameter and cannot be null.");
		}
		if(StringUtils.isEmpty(dto.getType())) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "Type is mandatory parameter and cannot be null.");
		}
		if(StringUtils.isEmpty(dto.getDiscountType())) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "DiscountType is mandatory parameter and cannot be null.");
		}
		if(dto.getCompanyId() == null) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "CompanyId is mandatory parameter and cannot be null.");
		}
		if(dto.getValue() == null && dto.getValue().doubleValue()<=0) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "Value is mandatory parameter and must be greater than 0.");
		}
		if(!CommonUtil.isMarketPlaceCompany(session) && session.getCompanyId().longValue()!=dto.getCompanyId().longValue()) {
			return new ErrorDTO(HttpStatus.FORBIDDEN, DisplayModuleNames.USER+" does not have access to create "+DisplayModuleNames.PRODUCTCOUPON + " for given "+ DisplayModuleNames.COMPANY);
		}
		if (repository.existsProductCouponByNameAndCompanyIdAndNotId(dto.getName(), dto.getCompanyId(), dto.getId())>0) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, DisplayModuleNames.PRODUCTCOUPON+" already exists for give name and "+DisplayModuleNames.COMPANY);
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
		if (filters == null) filters = new PagingSortSearchDTO();

		EntityManager em = emf.getNativeEntityManagerFactory().createEntityManager();
		List<Object[]> datalistResult = null;

		try {
			String query = "select pcp.id, pcp.name, pcp.`desc`, pco.active, pcp.type, pcp.discount_type, pcp.value, pcp.expiry_date "
			+ " from productcoupon pcp "
			+ " where pcp.removed = FALSE";

			String totalCountQuery = "select count(1)"
			+ " from productcoupon pcp "
			+ " where pcp.removed = FALSE";
			
			if(companyId != null) {
				query = query + " and pcp.company_id="+companyId;
				totalCountQuery = totalCountQuery + " and pcp.company_id="+companyId;
			}
			
			// searching
			String searchQuery = "";
			if (filters.getSearch() != null){
				for (SearchQueryDTO sDto: filters.getSearch()){
					String dbField = getDBField(sDto.getSearchField());
					String fieldDatatype = getProductCouponFieldDataType(sDto.getSearchField());

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
				query += " order by pcp.id";
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
		
		List<ProductCouponDTO> dtoList = new ArrayList<>();
		if(datalistResult!=null && datalistResult.size()>0) {
			for(Object[] dataResult : datalistResult) {
				ProductCouponDTO dto = this.fillProductCouponDTO(dataResult);
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
	
	private ProductCouponDTO fillProductCouponDTO(Object[] result) {
		ProductCouponDTO dto = new ProductCouponDTO();
		dto.setId(((BigInteger)result[0]).longValue());
		dto.setName((String)result[1]);
		dto.setDesc((String)result[2]);
		dto.setActive(result[3]!=null?(Boolean)result[3]:null);
		dto.setType((String)result[4]);
		dto.setDiscountType((String)result[5]);
		dto.setValue((Double)result[6]);
		dto.setExpiryDate(result[7]!=null?(Date)result[7]:null);
		return dto;
	}

	private ErrorDTO listValidation(SessionDTO session) {
		return null;
	}

	private String getProductCouponFieldDataType(String column) {
		switch (column) {
			case "id":
			case "active":
			case "value":
			case "companyId":
				return DataType.TYPE_INT;
			case "name":
			case "desc":
			case "type":
			case "discountType":
			case "expiry_date":
				return DataType.TYPE_STRING;
		}
		return "";
	}

	private String getDBField(String UIField) {
		switch(UIField) {
			case "id":
				return "pcp.id";
			case "active":
				return "pcp.active";
			case "name":
				return "pcp." + UIField;
			case "desc":
				return "pcp.`desc`";
			case "type":
				return "pcp.type";
			case "discountType":
				return "pcp.discount_type";
			case "companyId":
				return "pcp.company_id";
			case "expiryDate":
				return "pcp.expiry_date";
		}
		return "";
	}
}
