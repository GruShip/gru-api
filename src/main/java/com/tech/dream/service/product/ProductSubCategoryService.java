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

import com.tech.dream.db.entity.ProductCategory;
import com.tech.dream.db.entity.ProductSubCategory;
import com.tech.dream.db.repository.ProductCategoryRepository;
import com.tech.dream.db.repository.ProductRepository;
import com.tech.dream.model.ErrorDTO;
import com.tech.dream.model.PagingSortSearchDTO;
import com.tech.dream.model.ProductSubCategoryDTO;
import com.tech.dream.model.ResultDTO;
import com.tech.dream.model.SearchQueryDTO;
import com.tech.dream.model.SessionDTO;
import com.tech.dream.util.Constants;
import com.tech.dream.util.Constants.CompanyType;
import com.tech.dream.util.Constants.DataType;
import com.tech.dream.util.Constants.DisplayModuleNames;

@Service
@Transactional
public class ProductSubCategoryService {
	
	@Autowired
	private ProductCategoryRepository repository;
	
	@Autowired
	private ProductRepository productRepository;
	
	@Autowired
	private LocalContainerEntityManagerFactoryBean emf;
	
	public Object create(ProductSubCategoryDTO dto, SessionDTO session) {
		ErrorDTO error = createValidation(dto, session);
		if (error != null) {
			return error;
		}
		EntityManager em = emf.getNativeEntityManagerFactory().createEntityManager();
		try{
			em.getTransaction().begin();
			ProductSubCategory entity = convertToEntity(dto);
			em.persist(entity);
			dto.setId(entity.getId());
			dto.setActive(entity.getActive());
			//dto = convertToDTO(entity);
			em.getTransaction().commit();
		}finally {
			em.close();
		}
		return dto;
	}
	
	public Object update(ProductSubCategoryDTO dto, SessionDTO session) {
		ErrorDTO error = updateValidation(dto, session);
		if (error != null) {
			return error;
		}
		repository.updateProductSubCategory(dto.getId(), dto.getName(), dto.getActive()!=null?dto.getActive():true, dto.getDesc(), dto.getProductCategoryId());
		return dto;
	}

	public Object delete(ProductSubCategoryDTO dto, SessionDTO session) {
		ErrorDTO error = deleteValidation(dto, session);
		if (error != null) {
			return error;
		}
		repository.deleteProductSubCategory(dto.getId());
		return true;
	}

	public ProductSubCategory convertToEntity(ProductSubCategoryDTO dto) {
		ProductSubCategory e = new ProductSubCategory();
		e.setId(dto.getId());
		e.setName(dto.getName());
		e.setDesc(dto.getDesc());
		e.setActive(dto.getActive()!=null?dto.getActive():true);
		e.setProductCategory(new ProductCategory(dto.getProductCategoryId()));
		return e;
	}

	private ErrorDTO deleteValidation(ProductSubCategoryDTO dto, SessionDTO session) {
		if (StringUtils.isEmpty(dto.getId())) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "Id is mandatory and cannot be null.");
		}
		if(!CompanyType.MARKETPLACE.equalsIgnoreCase(session.getCompanyType())) {
			return new ErrorDTO(HttpStatus.FORBIDDEN, DisplayModuleNames.USER+" does not have access to create "+DisplayModuleNames.PRODUCTSUBCATEGORY);
		}
		if (repository.existsProductSubCategoryById(dto.getId())<=0) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, DisplayModuleNames.PRODUCTSUBCATEGORY+" does not exists for given id.");
		}
		if (productRepository.existsProductByProductSubCategoryId(dto.getId())>0) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, DisplayModuleNames.PRODUCT+"'s are mapped to given "+DisplayModuleNames.PRODUCTSUBCATEGORY);
		}
		return null;
	}

	private ErrorDTO createValidation(ProductSubCategoryDTO dto, SessionDTO session) {
		if (StringUtils.isEmpty(dto.getName())) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "Name is mandatory parameter and cannot be null.");
		}
		if (dto.getProductCategoryId()==null) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "CategoryId is mandatory parameter and cannot be null.");
		}
		
		if(!CompanyType.MARKETPLACE.equalsIgnoreCase(session.getCompanyType())) {
			return new ErrorDTO(HttpStatus.FORBIDDEN, DisplayModuleNames.USER+" does not have access to create "+DisplayModuleNames.PRODUCTSUBCATEGORY);
		}
		if (repository.existsProductSubCategoryByNameAndCategoryId(dto.getName(), dto.getProductCategoryId())>0) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, DisplayModuleNames.PRODUCTSUBCATEGORY+" already exists for give name.");
		}
		return null;
	}

	private ErrorDTO updateValidation(ProductSubCategoryDTO dto, SessionDTO session) {
		if (StringUtils.isEmpty(dto.getId())) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "Id is mandatory and cannot be null.");
		}
		if(!CompanyType.MARKETPLACE.equalsIgnoreCase(session.getCompanyType())) {
			return new ErrorDTO(HttpStatus.FORBIDDEN, DisplayModuleNames.USER+" does not have access to create "+DisplayModuleNames.PRODUCTSUBCATEGORY);
		}
		if (dto.getProductCategoryId()==null) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "CategoryId is mandatory parameter and cannot be null.");
		}
		if (StringUtils.isEmpty(dto.getName())) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "Name is mandatory parameter and cannot be null.");
		}
		if (repository.existsProductSubCategoryByNameAndCategoryIdAndNotId(dto.getName(), dto.getProductCategoryId(), dto.getId())>0) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, DisplayModuleNames.PRODUCTSUBCATEGORY+" already exists for give name and "+DisplayModuleNames.PRODUCTCATEGORY);
		}
		return null;
	}

	public ResultDTO list(SessionDTO session, Long productCategoryId, PagingSortSearchDTO filters) {
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
			String query = "select psc.id, psc.name, psc.`desc`, psc.active, psc.product_category_id, pc.name as product_category_name "
			+ " from productsubcategory psc "
			+ " inner join productcategory pc on pc.id = psc.product_category_id and pc.removed=FALSE "
			+ " where psc.removed = FALSE ";

			String totalCountQuery = "select count(1)"
			+ " from productsubcategory psc "
			+ " inner join productcategory pc on pc.id = psc.product_category_id and pc.removed=FALSE "
			+ " where psc.removed = FALSE ";
			if(productCategoryId!=null && productCategoryId.longValue()>0) {
				query = query + " and pc.id = " + productCategoryId;
				totalCountQuery = totalCountQuery + " and pc.id = " + productCategoryId; 
			}
			// searching
			String searchQuery = "";
			if (filters.getSearch() != null){
				for (SearchQueryDTO sDto: filters.getSearch()){
					String dbField = getDBField(sDto.getSearchField());
					String fieldDatatype = getProductSubCategoryFieldDataType(sDto.getSearchField());

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
				query += " order by psc.id";
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
		
		List<ProductSubCategoryDTO> dtoList = new ArrayList<>();
		if(datalistResult!=null && datalistResult.size()>0) {
			for(Object[] dataResult : datalistResult) {
				ProductSubCategoryDTO dto = this.fillProductSubCategoryDTO(dataResult);
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
		
		List<Object[]> datalistResult = repository.findProductSubCategoryDataById(id);
		ProductSubCategoryDTO dto = null;
		if(datalistResult!=null && datalistResult.size()>0) {
			Object[] dataResult = datalistResult.get(0);
			dto = this.fillProductSubCategoryDTO(dataResult);
		}
		return dto;
	}
	
	private ErrorDTO getValidation(SessionDTO session, Long id) {
		//check if user is superadmin  or usergroupid belongs to same company of session company id
		return null;
	}
	
	
	private ProductSubCategoryDTO fillProductSubCategoryDTO(Object[] result) {
		ProductSubCategoryDTO dto = new ProductSubCategoryDTO();
		dto.setId(((BigInteger)result[0]).longValue());
		dto.setName((String)result[1]);
		dto.setDesc((String)result[2]);
		dto.setActive(result[3]!=null?(Boolean)result[3]:null);
		dto.setProductCategoryId(((BigInteger)result[4]).longValue());
		dto.setProductCategoryName((String)result[5]);
		return dto;
	}

	private ErrorDTO listValidation(SessionDTO session) {
		return null;
	}

	private String getProductSubCategoryFieldDataType(String column) {
		switch (column) {
			case "id":
			case "active":
			case "productCategoryId":
				return DataType.TYPE_INT;
			case "name":
			case "desc":
			case "productCategoryName":
				return DataType.TYPE_STRING;
		}
		return "";
	}

	private String getDBField(String UIField) {
		switch(UIField) {
			case "id":
			case "name":
			case "active":
				return "psc." + UIField;
			case "desc": 
				return "psc.`desc`";
			case "productCategoryId":
				return "psc.product_category_id";
			case "productCategoryName": 
				return "psc.product_category_name";
		}
		return "";
	}
}
