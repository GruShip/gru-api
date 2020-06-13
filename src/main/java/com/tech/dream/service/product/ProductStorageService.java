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

import com.tech.dream.db.entity.ProductStorage;
import com.tech.dream.db.repository.ProductRepository;
import com.tech.dream.db.repository.ProductStorageRepository;
import com.tech.dream.model.ErrorDTO;
import com.tech.dream.model.PagingSortSearchDTO;
import com.tech.dream.model.ProductStorageDTO;
import com.tech.dream.model.ResultDTO;
import com.tech.dream.model.SearchQueryDTO;
import com.tech.dream.model.SessionDTO;
import com.tech.dream.util.Constants;
import com.tech.dream.util.Constants.CompanyType;
import com.tech.dream.util.Constants.DataType;
import com.tech.dream.util.Constants.DisplayModuleNames;

@Service
@Transactional
public class ProductStorageService {
	
	@Autowired
	private ProductStorageRepository repository;
	
	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private LocalContainerEntityManagerFactoryBean emf;
	
	public Object create(ProductStorageDTO dto, SessionDTO session) {
		ErrorDTO error = createValidation(dto, session);
		if (error != null) {
			return error;
		}
		ProductStorage entity = convertToEntity(dto);
		repository.save(entity);
		dto.setId(entity.getId());
		dto.setActive(entity.getActive());
		
		//dto = convertToDTO(entity);
		return dto;
	}
	
	public Object update(ProductStorageDTO dto, SessionDTO session) {
		ErrorDTO error = updateValidation(dto, session);
		if (error != null) {
			return error;
		}
		repository.updateProductStorage(dto.getId(), dto.getName(), dto.getActive()!=null?dto.getActive():true, dto.getDesc());
		return dto;
	}

	public Object delete(ProductStorageDTO dto, SessionDTO session) {
		ErrorDTO error = deleteValidation(dto, session);
		if (error != null) {
			return error;
		}
		repository.deleteProductStorage(dto.getId());
		return true;
	}

	public ProductStorage convertToEntity(ProductStorageDTO dto) {
		ProductStorage e = new ProductStorage();
		e.setId(dto.getId());
		e.setName(dto.getName());
		e.setDesc(dto.getDesc());
		e.setActive(dto.getActive()!=null?dto.getActive():true);
		return e;
	}

	private ErrorDTO deleteValidation(ProductStorageDTO dto, SessionDTO session) {
		if (StringUtils.isEmpty(dto.getId())) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "Id is mandatory and cannot be null.");
		}
		if(!CompanyType.MARKETPLACE.equalsIgnoreCase(session.getCompanyType())) {
			return new ErrorDTO(HttpStatus.FORBIDDEN, DisplayModuleNames.USER+" does not have access to create "+DisplayModuleNames.PRODUCTSTORAGE);
		}
		if (repository.existsProductStorageById(dto.getId())<=0) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, DisplayModuleNames.PRODUCTSTORAGE+" does not exists for given id.");
		}
		if (productRepository.existsProductByProductStorageId(dto.getId())>0) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, DisplayModuleNames.PRODUCT+"'s are mapped to given "+DisplayModuleNames.PRODUCTSTORAGE);
		}
		return null;
	}

	private ErrorDTO createValidation(ProductStorageDTO dto, SessionDTO session) {
		if (StringUtils.isEmpty(dto.getName())) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "Name is mandatory parameter and cannot be null.");
		}
		
		if(!CompanyType.MARKETPLACE.equalsIgnoreCase(session.getCompanyType())) {
			return new ErrorDTO(HttpStatus.FORBIDDEN, DisplayModuleNames.USER+" does not have access to create "+DisplayModuleNames.PRODUCTSTORAGE);
		}
		if (repository.existsProductStorageByName(dto.getName())>0) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, DisplayModuleNames.PRODUCTSTORAGE+" already exists for give name.");
		}
		return null;
	}

	private ErrorDTO updateValidation(ProductStorageDTO dto, SessionDTO session) {
		if (StringUtils.isEmpty(dto.getId())) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "Id is mandatory and cannot be null.");
		}
		if(!CompanyType.MARKETPLACE.equalsIgnoreCase(session.getCompanyType())) {
			return new ErrorDTO(HttpStatus.FORBIDDEN, DisplayModuleNames.USER+" does not have access to create "+DisplayModuleNames.PRODUCTSTORAGE);
		}
		if (StringUtils.isEmpty(dto.getName())) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "Name is mandatory parameter and cannot be null.");
		}
		if (repository.existsProductStorageByNameAndNotId(dto.getName(), dto.getId())>0) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, DisplayModuleNames.PRODUCTSTORAGE+" already exists for give name");
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
			String query = "select pst.id, pst.name, pst.`desc`, pst.active "
			+ " from productstorage pst "
			+ " where pst.removed = FALSE"; 

			String totalCountQuery = "select count(1) "
			+ " from productstorage pst "
			+ " where pst.removed = FALSE"; 
			
			// searching
			String searchQuery = "";
			if (filters.getSearch() != null){
				for (SearchQueryDTO sDto: filters.getSearch()){
					String dbField = getDBField(sDto.getSearchField());
					String fieldDatatype = getProductStorageDataType(sDto.getSearchField());

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
				query += " order by pst.id";
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
		
		List<ProductStorageDTO> dtoList = new ArrayList<>();
		if(datalistResult!=null && datalistResult.size()>0) {
			for(Object[] dataResult : datalistResult) {
				ProductStorageDTO dto = this.fillProductStorageDTO(dataResult);
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
		
		List<Object[]> datalistResult = repository.findProductStorageDataById(id);
		ProductStorageDTO dto = null;
		if(datalistResult!=null && datalistResult.size()>0) {
			Object[] dataResult = datalistResult.get(0);
			dto = this.fillProductStorageDTO(dataResult);
		}
		return dto;
	}
	
	private ErrorDTO getValidation(SessionDTO session, Long id) {
		//check if user is superadmin  or usergroupid belongs to same company of session company id
		return null;
	}
	
	
	private ProductStorageDTO fillProductStorageDTO(Object[] result) {
		ProductStorageDTO dto = new ProductStorageDTO();
		dto.setId(((BigInteger)result[0]).longValue());
		dto.setName((String)result[1]);
		dto.setDesc((String)result[2]);
		dto.setActive(result[3]!=null?(Boolean)result[3]:null);
		
		return dto;
	}

	private ErrorDTO listValidation(SessionDTO session) {
		return null;
	}

	private String getProductStorageDataType(String column) {
		switch (column) {
			case "id":
			case "active":
				return DataType.TYPE_INT;
			case "name":
			case "desc":
				return DataType.TYPE_STRING;
		}
		return "";
	}

	private String getDBField(String UIField) {
		switch(UIField) {
			case "id":
			case "active":
			case "name":
				return "pst." + UIField;
			case "desc": 
				return "pst.`desc`";
		}
		return "";
	}
	
}
