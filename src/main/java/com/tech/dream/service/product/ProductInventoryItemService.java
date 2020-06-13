package com.tech.dream.service.product;

import java.math.BigInteger;
import java.text.ParseException;
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

import com.tech.dream.db.entity.ProductInventoryItem;
import com.tech.dream.db.entity.SellerProduct;
import com.tech.dream.db.repository.ProductInventoryItemRepository;
import com.tech.dream.db.repository.SellerProductRepository;
import com.tech.dream.model.ErrorDTO;
import com.tech.dream.model.PagingSortSearchDTO;
import com.tech.dream.model.ProductInventoryItemDTO;
import com.tech.dream.model.ResultDTO;
import com.tech.dream.model.SearchQueryDTO;
import com.tech.dream.model.SessionDTO;
import com.tech.dream.util.Constants;
import com.tech.dream.util.Constants.DataType;
import com.tech.dream.util.Constants.DisplayModuleNames;
import com.tech.dream.util.DateUtility;

@Service
@Transactional
public class ProductInventoryItemService {
	
	@Autowired
	private ProductInventoryItemRepository repository;
	
	@Autowired
	private SellerProductRepository sellerProductRepository;
	
	@Autowired
	private SellerProductService sellerProductService;
	
	@Autowired
	private LocalContainerEntityManagerFactoryBean emf;
	
	public Object create(ProductInventoryItemDTO dto, SessionDTO session) {
		ErrorDTO error = createValidation(dto, session);
		if (error != null) {
			return error;
		}
		ProductInventoryItem entity = convertToEntity(dto);
		repository.save(entity);
		dto.setId(entity.getId());
		//dto = convertToDTO(entity);
		return dto;
	}
	
	public Object update(ProductInventoryItemDTO dto, SessionDTO session) {
		ErrorDTO error = updateValidation(dto, session);
		if (error != null) {
			return error;
		}
		repository.update(dto.getId(), dto.getItemId(), dto.getImei1(), dto.getImei2(), dto.getSellerProductId());
		return dto;
	}

	public Object delete(ProductInventoryItemDTO dto, SessionDTO session) {
		ErrorDTO error = deleteValidation(dto, session);
		if (error != null) {
			return error;
		}
		repository.delete(dto.getId());
		return true;
	}

	public ProductInventoryItem convertToEntity(ProductInventoryItemDTO dto) {
		ProductInventoryItem e = new ProductInventoryItem();
		e.setId(dto.getId());
		e.setSellerProduct(new SellerProduct(dto.getSellerProductId()));
		e.setItemId(dto.getItemId());
		e.setImei1(dto.getImei1());
		e.setImei2(dto.getImei2());
		return e;
	}

	private ErrorDTO deleteValidation(ProductInventoryItemDTO dto, SessionDTO session) {
		if (dto.getId() == null) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "Id is mandatory and cannot be null.");
		}
//		if(!CompanyType.MARKETPLACE.equalsIgnoreCase(session.getCompanyType()) && dto.getCompanyId().longValue()!=session.getCompanyId().longValue()) {
//			return new ErrorDTO(HttpStatus.FORBIDDEN, DisplayModuleNames.USER+" does not have access to delete "+DisplayModuleNames.PRODUCT + " for given "+ DisplayModuleNames.COMPANY);
//		}
		if (repository.existsByProductInventoryItemId(dto.getId())<=0) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, DisplayModuleNames.PRODUCTINVENTORYITEM+" does not exists for given id. ");
		}
		return null;
	}

	private ErrorDTO createValidation(ProductInventoryItemDTO dto, SessionDTO session) {
		if (dto.getSellerProductId() == null) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "SellerProductId is mandatory parameter and cannot be null.");
		}
		if (StringUtils.isEmpty(dto.getItemId())) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "ItemId is mandatory parameter and cannot be null.");
		}
//		if(!CompanyType.MARKETPLACE.equalsIgnoreCase(session.getCompanyType()) && dto.getCompanyId().longValue()!=session.getCompanyId().longValue()) {
//			return new ErrorDTO(HttpStatus.FORBIDDEN, DisplayModuleNames.USER+" does not have access to create "+DisplayModuleNames.PRODUCT + " for given "+ DisplayModuleNames.COMPANY);
//		}
		if (sellerProductRepository.existsBySellerProductId(dto.getSellerProductId())<=0) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, DisplayModuleNames.SELLERPRODUCT+" does not exists for the given seller productid.");
		}
		if (repository.existsByItemIdAndSellerProductId(dto.getItemId(),dto.getSellerProductId())>0) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, DisplayModuleNames.PRODUCTINVENTORYITEM+" does not exists for the given itemId and sellerProductId.");
		}
		return null;
	}
	
	private ErrorDTO updateValidation(ProductInventoryItemDTO dto, SessionDTO session) {
		if (dto.getId() == null) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "Id is mandatory parameter and cannot be null.");
		}
		if (dto.getSellerProductId() == null) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "SellerProductId is mandatory parameter and cannot be null.");
		}
		if (StringUtils.isEmpty(dto.getItemId())) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "ItemId is mandatory parameter and cannot be null.");
		}
//		if(!CompanyType.MARKETPLACE.equalsIgnoreCase(session.getCompanyType()) && dto.getCompanyId().longValue()!=session.getCompanyId().longValue()) {
//			return new ErrorDTO(HttpStatus.FORBIDDEN, DisplayModuleNames.USER+" does not have access to create "+DisplayModuleNames.PRODUCT + " for given "+ DisplayModuleNames.COMPANY);
//		}
		if (sellerProductRepository.existsBySellerProductId(dto.getSellerProductId())<=0) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, DisplayModuleNames.SELLERPRODUCT+" does not exists for the given seller productid.");
		}
		if (repository.existsByItemIdAndSellerProductIdAndNotId(dto.getItemId(),dto.getSellerProductId(), dto.getId())>0) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, DisplayModuleNames.PRODUCTINVENTORYITEM+" does not exists for the given itemId and sellerProductId.");
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public ResultDTO list(SessionDTO session, Long sellerProductId, PagingSortSearchDTO filters) throws ParseException {
		
		ResultDTO resultDTO = new ResultDTO();
		Long totalCount = 0L;
		ErrorDTO error = listValidation(session);
		if(error!=null) {
			resultDTO.setErrorDTO(error);
			return resultDTO;
		}
		EntityManager em = emf.getNativeEntityManagerFactory().createEntityManager();
		List<Object[]> datalistResult = null;
		try {
			String query = "SELECT pii.id, pii.item_id, pii.imei_1, pii.imei_2, sp.id as seller_product_id, sp.name as seller_product_name, pii.created_date "
					  + " from productinventoryitem pii "
					  + " inner join sellerproduct sp on sp.id = pii.seller_product_id "
					  + " where pii.removed=FALSE";
			
			String totalCountQuery = "SELECT count(1) "
						+ " from productinventoryitem pii "
						+ " inner join sellerproduct sp on sp.id = pii.seller_product_id "
						+ " where pii.removed=FALSE";
			
			
			if(sellerProductId != null) {
				query = query + " and pii.seller_product_id = "+sellerProductId.longValue();
				totalCountQuery = totalCountQuery + " and pii.seller_product_id = "+sellerProductId.longValue();
			}
			
			String searchQuery = "";
			// searching
			if (filters.getSearch() != null){
				for (SearchQueryDTO sDto: filters.getSearch()){
					String dbField = getDBField(sDto.getSearchField());
					String fieldDatatype = getFieldDataType(sDto.getSearchField());

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
				query += " order by pii.id";
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
		
		List<ProductInventoryItemDTO> dtoList = new ArrayList<>();
		if(datalistResult!=null && datalistResult.size()>0) {
			for(Object[] dataResult : datalistResult) {
				ProductInventoryItemDTO dto = this.fillProductInventoryItemDTO(dataResult);
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
	
	//String query = "SELECT pii.id, pii.item_id, pii.imei_1, pii.imei, sp.id as seller_product_id, sp.name as seller_product_name, pii.created_date "
	private ProductInventoryItemDTO fillProductInventoryItemDTO(Object[] result) throws ParseException {
		ProductInventoryItemDTO dto = new ProductInventoryItemDTO();
		dto.setId(((BigInteger)result[0]).longValue());
		dto.setItemId((String)result[1]);
		dto.setImei1((String)result[2]);
		dto.setImei2((String)result[3]);
		dto.setSellerProductId(result[4]!=null?((BigInteger)result[4]).longValue():null);
		dto.setSellerProductName((String)result[5]);
		dto.setCreatedDate(DateUtility.dateToStringFormat((Date)result[6], Constants.MYSQL_DATE_FORMAT));
//		if(!StringUtils.isEmpty(createDateStr)) {
//			dto.setCreatedDate(DateUtility.stringToDateFormat(createDateStr, Constants.MYSQL_DATE_FORMAT));
//		}
		return dto;
	}

	private ErrorDTO listValidation(SessionDTO session) {
		return null;
	}

	private String getFieldDataType(String column) {
		switch (column) {
			case "id":
			case "sellerProductId":
				return DataType.TYPE_INT;
			case "imei1": 
			case "imei2":
			case "itemId":
			case "sellerProductName":
				return DataType.TYPE_STRING;
		}
		return "";
	}

	private String getDBField(String UIField) {
		switch(UIField) {
			case "id":
				return "pii.id";
			case "imei1":
				return "pii.imei_1";
			case "imei2": 
				return "pii.imei_2";
			case "itemId":
				return "pii.item_id";
			case "sellerProductId": 
				return "sp.id";
			case "sellerProductName": 
				return "sp.name";
		}
		return "";
	}
	
}
