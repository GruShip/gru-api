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

import com.tech.dream.db.entity.ProductBrand;
import com.tech.dream.db.entity.ProductType;
import com.tech.dream.db.repository.ProductRepository;
import com.tech.dream.db.repository.ProductTypeRepository;
import com.tech.dream.model.ErrorDTO;
import com.tech.dream.model.PagingSortSearchDTO;
import com.tech.dream.model.ProductTypeDTO;
import com.tech.dream.model.ResultDTO;
import com.tech.dream.model.SearchQueryDTO;
import com.tech.dream.model.SessionDTO;
import com.tech.dream.util.Constants;
import com.tech.dream.util.Constants.CompanyType;
import com.tech.dream.util.Constants.DataType;
import com.tech.dream.util.Constants.DisplayModuleNames;

@Service
@Transactional
public class ProductTypeService {
	
	@Autowired
	private ProductTypeRepository repository;
	
	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private LocalContainerEntityManagerFactoryBean emf;
	
	public Object create(ProductTypeDTO dto, SessionDTO session) {
		ErrorDTO error = createValidation(dto, session);
		if (error != null) {
			return error;
		}
		try{
			ProductType entity = convertToEntity(dto);
			repository.save(entity);
			dto.setId(entity.getId());
			dto.setActive(entity.getActive());
			//dto = convertToDTO(entity);
		}finally {
		}
		return dto;
	}
	
	public Object update(ProductTypeDTO dto, SessionDTO session) {
		ErrorDTO error = updateValidation(dto, session);
		if (error != null) {
			return error;
		}
		repository.updateProductType(dto.getId(), dto.getName(), dto.getActive()!=null?dto.getActive():true, dto.getDesc(), dto.getProductBrandId());
		return dto;
	}

	public Object delete(ProductTypeDTO dto, SessionDTO session) {
		ErrorDTO error = deleteValidation(dto, session);
		if (error != null) {
			return error;
		}
		repository.deleteProductType(dto.getId());
		return true;
	}

	public ProductType convertToEntity(ProductTypeDTO dto) {
		ProductType e = new ProductType();
		e.setId(dto.getId());
		e.setName(dto.getName());
		e.setDesc(dto.getDesc());
		e.setActive(dto.getActive()!=null?dto.getActive():true);
		e.setProductBrand(new ProductBrand(dto.getProductBrandId()));
		return e;
	}

	private ErrorDTO deleteValidation(ProductTypeDTO dto, SessionDTO session) {
		if (StringUtils.isEmpty(dto.getId())) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "Id is mandatory and cannot be null.");
		}
		if(!CompanyType.MARKETPLACE.equalsIgnoreCase(session.getCompanyType())) {
			return new ErrorDTO(HttpStatus.FORBIDDEN, DisplayModuleNames.USER+" does not have access to delete "+DisplayModuleNames.PRODUCTTYPE);
		}
		if (repository.existsProductTypeById(dto.getId())<=0) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, DisplayModuleNames.PRODUCTTYPE+" does not exists for given id.");
		}
		if (productRepository.existsProductByProductTypeId(dto.getId())>0) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, DisplayModuleNames.PRODUCT+"'s are mapped to given "+DisplayModuleNames.PRODUCTTYPE);
		}
		return null;
	}

	private ErrorDTO createValidation(ProductTypeDTO dto, SessionDTO session) {
		if (StringUtils.isEmpty(dto.getName())) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "Name is mandatory parameter and cannot be null.");
		}
		if (dto.getProductBrandId()==null) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "BrandId is mandatory parameter and cannot be null.");
		}
		
		if(!CompanyType.MARKETPLACE.equalsIgnoreCase(session.getCompanyType())) {
			return new ErrorDTO(HttpStatus.FORBIDDEN, DisplayModuleNames.USER+" does not have access to create "+DisplayModuleNames.PRODUCTTYPE);
		}
		if (repository.existsProductTypeByNameAndBrandId(dto.getName(), dto.getProductBrandId())>0) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, DisplayModuleNames.PRODUCTTYPE+" already exists for give name and "+DisplayModuleNames.PRODUCTBRAND);
		}
		return null;
	}

	private ErrorDTO updateValidation(ProductTypeDTO dto, SessionDTO session) {
		if (StringUtils.isEmpty(dto.getId())) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "Id is mandatory and cannot be null.");
		}
		if (dto.getProductBrandId()==null) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "BrandId is mandatory parameter and cannot be null.");
		}
		if(!CompanyType.MARKETPLACE.equalsIgnoreCase(session.getCompanyType())) {
			return new ErrorDTO(HttpStatus.FORBIDDEN, DisplayModuleNames.USER+" does not have access to create "+DisplayModuleNames.PRODUCTTYPE);
		}
		if (StringUtils.isEmpty(dto.getName())) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "Name is mandatory parameter and cannot be null.");
		}
		if (repository.existsProductTypeByNameAndBrandIdAndNotId(dto.getName(), dto.getProductBrandId(), dto.getId())>0) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, DisplayModuleNames.PRODUCTTYPE+" already exists for give name and "+DisplayModuleNames.PRODUCTBRAND);
		}
		return null;
	}

	public ResultDTO list(SessionDTO session, Long productBrandId, PagingSortSearchDTO filters) {
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
			String query = "select pt.id, pt.name, pt.`desc`, pt.active, pt.product_brand_id, pb.name as product_brand_name "
			+ " from producttype pt "
			+ " inner join productbrand pb on pb.id = pt.product_brand_id and pb.removed=FALSE "
			+ " where pt.removed = FALSE ";

			String totalCountQuery = "select count(1)"
			+ " from producttype pt "
			+ " inner join productbrand pb on pb.id = pt.product_brand_id and pb.removed=FALSE "
			+ " where pt.removed = FALSE ";
			
			if(productBrandId!=null && productBrandId.longValue()>0) {
				query = query + " and pt.product_brand_id = " + productBrandId;
				totalCountQuery = totalCountQuery + " and pt.product_brand_id = " + productBrandId;  
			}
			
			// searching
			String searchQuery = "";
			if (filters.getSearch() != null){
				for (SearchQueryDTO sDto: filters.getSearch()){
					String dbField = getDBField(sDto.getSearchField());
					String fieldDatatype = getProductTypeFieldDataType(sDto.getSearchField());

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
				query += " order by pt.id";
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
		
		List<ProductTypeDTO> dtoList = new ArrayList<>();
		if(datalistResult!=null && datalistResult.size()>0) {
			for(Object[] dataResult : datalistResult) {
				ProductTypeDTO dto = this.fillProductTypeDTO(dataResult);
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
		
		List<Object[]> datalistResult = repository.findProductTypeDataById(id);
		ProductTypeDTO dto = null;
		if(datalistResult!=null && datalistResult.size()>0) {
			Object[] dataResult = datalistResult.get(0);
			dto = this.fillProductTypeDTO(dataResult);
		}
		return dto;
	}
	
	private ErrorDTO getValidation(SessionDTO session, Long id) {
		//check if user is superadmin  or usergroupid belongs to same company of session company id
		return null;
	}
	
	
	private ProductTypeDTO fillProductTypeDTO(Object[] result) {
		ProductTypeDTO dto = new ProductTypeDTO();
		dto.setId(((BigInteger)result[0]).longValue());
		dto.setName((String)result[1]);
		dto.setDesc((String)result[2]);
		dto.setActive(result[3]!=null?(Boolean)result[3]:null);
		dto.setProductBrandId(((BigInteger)result[4]).longValue());
		dto.setProductBrandName((String)result[5]);
		return dto;
	}

	private ErrorDTO listValidation(SessionDTO session) {
		return null;
	}

	private String getProductTypeFieldDataType(String column) {
		switch (column) {
			case "id":
			case "active":
			case "productBrandId":
				return DataType.TYPE_INT;
			case "name":
			case "desc":
			case "productBrandName":
				return DataType.TYPE_STRING;
		}
		return "";
	}

	private String getDBField(String UIField) {
		switch(UIField) {
			case "id":
			case "active":
			case "name":
				return "pt." + UIField;
			case "desc":
				return "pt.`desc`";
			case "productBrandId": 
				return "pb.id"; 
			case "productBrandName":
				return "pb.name";
		}
		return "";
	}
}
