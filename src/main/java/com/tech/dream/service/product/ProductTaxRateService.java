package com.tech.dream.service.product;

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

import com.tech.dream.db.entity.ProductTaxRate;
import com.tech.dream.db.repository.ProductRepository;
import com.tech.dream.db.repository.ProductTaxRateRepository;
import com.tech.dream.model.ErrorDTO;
import com.tech.dream.model.PagingSortSearchDTO;
import com.tech.dream.model.ProductTaxRateDTO;
import com.tech.dream.model.ResultDTO;
import com.tech.dream.model.SearchQueryDTO;
import com.tech.dream.model.SessionDTO;
import com.tech.dream.util.Constants;
import com.tech.dream.util.Constants.CompanyType;
import com.tech.dream.util.Constants.DataType;
import com.tech.dream.util.Constants.DisplayModuleNames;

@Service
@Transactional
public class ProductTaxRateService {
	
	@Autowired
	private ProductTaxRateRepository repository;
	
	@Autowired
	private ProductRepository productRepository;
	
	@Autowired
	private LocalContainerEntityManagerFactoryBean emf;
	
	public Object create(ProductTaxRateDTO dto, SessionDTO session) {
		ErrorDTO error = createValidation(dto, session);
		if (error != null) {
			return error;
		}
		ProductTaxRate entity = convertToEntity(dto);
		repository.save(entity);
		dto.setId(entity.getId());
		dto.setActive(entity.getActive());
		
		//dto = convertToDTO(entity);
		return dto;
	}
	
	public Object update(ProductTaxRateDTO dto, SessionDTO session) {
		ErrorDTO error = updateValidation(dto, session);
		if (error != null) {
			return error;
		}
		repository.updateProductTaxRate(dto.getId(), dto.getTaxPercentage(), dto.getActive()!=null?dto.getActive():true, dto.getDesc());
		return dto;
	}

	public Object delete(ProductTaxRateDTO dto, SessionDTO session) {
		ErrorDTO error = deleteValidation(dto, session);
		if (error != null) {
			return error;
		}
		repository.deleteProductTaxRate(dto.getId());
		return true;
	}

	public ProductTaxRate convertToEntity(ProductTaxRateDTO dto) {
		ProductTaxRate e = new ProductTaxRate();
		e.setId(dto.getId());
		e.setTaxPercentage(dto.getTaxPercentage());
		e.setDesc(dto.getDesc());
		e.setActive(dto.getActive()!=null?dto.getActive():true);
		return e;
	}

	private ErrorDTO deleteValidation(ProductTaxRateDTO dto, SessionDTO session) {
		if (StringUtils.isEmpty(dto.getId())) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "Id is mandatory and cannot be null.");
		}
		if(!CompanyType.MARKETPLACE.equalsIgnoreCase(session.getCompanyType())) {
			return new ErrorDTO(HttpStatus.FORBIDDEN, DisplayModuleNames.USER+" does not have access to create "+DisplayModuleNames.PRODUCTTAXRATE);
		}
		if (repository.existsProductTaxRateById(dto.getId())<=0) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, DisplayModuleNames.PRODUCTTAXRATE+" does not exists for given id.");
		}
		if (productRepository.existsProductByProductTaxRateId(dto.getId())>0) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, DisplayModuleNames.PRODUCT+"'s are mapped to given "+DisplayModuleNames.PRODUCTTAXRATE);
		}
		return null;
	}

	private ErrorDTO createValidation(ProductTaxRateDTO dto, SessionDTO session) {
		if (StringUtils.isEmpty(dto.getTaxPercentage())) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "Tax Percentage is mandatory parameter and cannot be null.");
		}
		
		if(!CompanyType.MARKETPLACE.equalsIgnoreCase(session.getCompanyType())) {
			return new ErrorDTO(HttpStatus.FORBIDDEN, DisplayModuleNames.USER+" does not have access to create "+DisplayModuleNames.PRODUCTTAXRATE);
		}
		if (repository.existsProductTaxRateByTaxPercentage(dto.getTaxPercentage())>0) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, DisplayModuleNames.PRODUCTTAXRATE+" already exists for give name.");
		}
		return null;
	}

	private ErrorDTO updateValidation(ProductTaxRateDTO dto, SessionDTO session) {
		if (StringUtils.isEmpty(dto.getId())) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "Id is mandatory and cannot be null.");
		}
		if(!CompanyType.MARKETPLACE.equalsIgnoreCase(session.getCompanyType())) {
			return new ErrorDTO(HttpStatus.FORBIDDEN, DisplayModuleNames.USER+" does not have access to create "+DisplayModuleNames.PRODUCTTAXRATE);
		}
		if (StringUtils.isEmpty(dto.getTaxPercentage())) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "Tax Percentage is mandatory parameter and cannot be null.");
		}
		if (repository.existsProductTaxRateByTaxPercentageAndNotId(dto.getTaxPercentage(), dto.getId())>0) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, DisplayModuleNames.PRODUCTTAXRATE+" already exists for give tax percentage");
		}
		return null;
	}

	public ResultDTO list(SessionDTO session, PagingSortSearchDTO filters) {
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
			String query = "select ptr.id, ptr.tax_percentage, ptr.`desc`, ptr.active "
			+ " from producttaxrate ptr "
			+ " where ptr.removed = FALSE ";

			String totalCountQuery = "select count(1)"
			+ " from producttaxrate ptr "
			+ " where ptr.removed = FALSE ";
			
			// searching
			String searchQuery = "";
			if (filters.getSearch() != null){
				for (SearchQueryDTO sDto: filters.getSearch()){
					String dbField = getDBField(sDto.getSearchField());
					String fieldDatatype = getProductTaxRateFieldDataType(sDto.getSearchField());

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
				query += " order by ptr.id ";
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
		
		List<ProductTaxRateDTO> dtoList = new ArrayList<>();
		if(datalistResult!=null && datalistResult.size()>0) {
			for(Object[] dataResult : datalistResult) {
				ProductTaxRateDTO dto = this.fillProductTaxRateDTO(dataResult);
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
	
	public Object get(SessionDTO session, Long id) {
		ErrorDTO error = getValidation(session, id);
		if(error!=null) {
			return error;
		}
		
		List<Object[]> datalistResult = repository.findProductTaxRateDataById(id);
		ProductTaxRateDTO dto = null;
		if(datalistResult!=null && datalistResult.size()>0) {
			Object[] dataResult = datalistResult.get(0);
			dto = this.fillProductTaxRateDTO(dataResult);
		}
		return dto;
	}
	
	private ErrorDTO getValidation(SessionDTO session, Long id) {
		//check if user is superadmin  or usergroupid belongs to same company of session company id
		return null;
	}
	
	
	private ProductTaxRateDTO fillProductTaxRateDTO(Object[] result) {
		ProductTaxRateDTO dto = new ProductTaxRateDTO();
		dto.setId(((BigInteger)result[0]).longValue());
		dto.setTaxPercentage((Double)result[1]);
		dto.setDesc((String)result[2]);
		dto.setActive(result[3]!=null?(Boolean)result[3]:null);
		
		return dto;
	}

	private ErrorDTO listValidation(SessionDTO session) {
		return null;
	}

	private String getProductTaxRateFieldDataType(String column) {
		switch (column) {
			case "id":
			case "active":
				return DataType.TYPE_INT;
			case "taxPercentage":
			case "desc":
				return DataType.TYPE_STRING;
		}
		return "";
	}

	private String getDBField(String UIField) {
		switch(UIField) {
			case "id":
			case "active":
			case "taxPercentage":
				return "ptr." + UIField;
			case "desc":
				return "ptr.`desc`";
		}
		return "";
	}
}
