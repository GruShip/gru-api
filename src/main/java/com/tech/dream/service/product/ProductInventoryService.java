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

import com.tech.dream.db.entity.ProductInventory;
import com.tech.dream.db.entity.SellerProduct;
import com.tech.dream.db.repository.ProductInventoryRepository;
import com.tech.dream.db.repository.SellerProductRepository;
import com.tech.dream.model.ErrorDTO;
import com.tech.dream.model.PagingSortSearchDTO;
import com.tech.dream.model.ProductInventoryDTO;
import com.tech.dream.model.ResultDTO;
import com.tech.dream.model.SearchQueryDTO;
import com.tech.dream.model.SessionDTO;
import com.tech.dream.util.Constants;
import com.tech.dream.util.Constants.DataType;
import com.tech.dream.util.Constants.DisplayModuleNames;
import com.tech.dream.util.DateUtility;

@Service
@Transactional
public class ProductInventoryService {
	
	@Autowired
	private ProductInventoryRepository repository;
	
	@Autowired
	private SellerProductRepository sellerProductRepository;
	
	@Autowired
	private SellerProductService sellerProductService;
	
	@Autowired
	private LocalContainerEntityManagerFactoryBean emf;
	
	public Object create(ProductInventoryDTO dto, SessionDTO session) {
		ErrorDTO error = createValidation(dto, session);
		if (error != null) {
			return error;
		}
		ProductInventory entity = convertToEntity(dto);
		repository.save(entity);
		dto.setId(entity.getId());
		sellerProductService.updateQuantity(dto.getSellerProductId(), getQty(dto.getOperationType(), dto.getQuantity()));
		
		
		//dto = convertToDTO(entity);
		return dto;
	}

	public Object delete(ProductInventoryDTO dto, SessionDTO session) {
		ErrorDTO error = deleteValidation(dto, session);
		if (error != null) {
			return error;
		}
		repository.delete(dto.getId());
		List<Object[]> dataList = repository.findInventoryInfo(dto.getId());
		if(dataList!=null && dataList.size()>0) {
			Object[] result = dataList.get(0);
			Integer quantity = (Integer)result[0];
			String operationType = (String)result[1];
			Long sellerProductId = ((BigInteger)result[2]).longValue();
			sellerProductService.updateQuantity(sellerProductId, getQty(operationType, -quantity));
		}
		
		
		return true;
	}

	public ProductInventory convertToEntity(ProductInventoryDTO dto) {
		ProductInventory e = new ProductInventory();
		e.setId(dto.getId());
		e.setSellerProduct(new SellerProduct(dto.getSellerProductId()));
		e.setOperationType(dto.getOperationType().toUpperCase());
		e.setQuantity(dto.getQuantity());
		e.setTotalPurchasePrice(dto.getTotalPurchasePrice());
		e.setAwBillNumber(dto.getAwBillNumber());
		return e;
	}

	private ErrorDTO deleteValidation(ProductInventoryDTO dto, SessionDTO session) {
		if (dto.getId() == null) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "Id is mandatory and cannot be null.");
		}
//		if(!CompanyType.MARKETPLACE.equalsIgnoreCase(session.getCompanyType()) && dto.getCompanyId().longValue()!=session.getCompanyId().longValue()) {
//			return new ErrorDTO(HttpStatus.FORBIDDEN, DisplayModuleNames.USER+" does not have access to delete "+DisplayModuleNames.PRODUCT + " for given "+ DisplayModuleNames.COMPANY);
//		}
		if (repository.existsByProductInventoryId(dto.getId())<=0) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, DisplayModuleNames.SELLERPRODUCT+" does not exists for given id. ");
		}
		return null;
	}

	private ErrorDTO createValidation(ProductInventoryDTO dto, SessionDTO session) {
		if (dto.getSellerProductId() == null) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "SellerProductId is mandatory parameter and cannot be null.");
		}
		if (dto.getQuantity() == null || dto.getQuantity()==0) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "Quantity is mandatory parameter and must be greater than 0.");
		}
		if (StringUtils.isEmpty(dto.getOperationType())) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "PiecePerUnit is mandatory parameter and must be greater than 0.");
		}
//		if(!CompanyType.MARKETPLACE.equalsIgnoreCase(session.getCompanyType()) && dto.getCompanyId().longValue()!=session.getCompanyId().longValue()) {
//			return new ErrorDTO(HttpStatus.FORBIDDEN, DisplayModuleNames.USER+" does not have access to create "+DisplayModuleNames.PRODUCT + " for given "+ DisplayModuleNames.COMPANY);
//		}
		if (sellerProductRepository.existsBySellerProductId(dto.getSellerProductId())<=0) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, DisplayModuleNames.SELLERPRODUCT+" does not exists for the given seller productid.");
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
			String query = "SELECT pi.id, pi.quantity, pi.operation_type, pi.total_purchase_price, pi.aw_bill_number, sp.id as seller_product_id, sp.name as seller_product_name, pi.created_date "
					  + " from productinventory pi "
					  + " inner join sellerproduct sp on sp.id = pi.seller_product_id "
					  + " where pi.removed=FALSE";
			
			String totalCountQuery = "SELECT count(1) "
						+ " from productinventory pi "
						+ " inner join sellerproduct sp on sp.id = pi.seller_product_id "
						+ " where pi.removed=FALSE";
			
			
			if(sellerProductId != null) {
				query = query + " and pi.seller_product_id = "+sellerProductId.longValue();
				totalCountQuery = totalCountQuery + " and pi.seller_product_id = "+sellerProductId.longValue();
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
				query += " order by pi.id";
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
		
		List<ProductInventoryDTO> dtoList = new ArrayList<>();
		if(datalistResult!=null && datalistResult.size()>0) {
			for(Object[] dataResult : datalistResult) {
				ProductInventoryDTO dto = this.fillProductDTO(dataResult);
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
	
	//pi.id, pi.quantity, pi.operation_type, pi.total_purchase_price, pi.aw_bill_number, sp.id as seller_product_id, sp.name as seller_product_name, pi.created_date
	private ProductInventoryDTO fillProductDTO(Object[] result) throws ParseException {
		ProductInventoryDTO dto = new ProductInventoryDTO();
		dto.setId(((BigInteger)result[0]).longValue());
		dto.setQuantity((Integer)result[1]);
		dto.setOperationType((String)result[2]);
		dto.setTotalPurchasePrice((Double)result[3]);
		dto.setAwBillNumber((String)result[4]);
		dto.setSellerProductId(result[5]!=null?((BigInteger)result[5]).longValue():null);
		dto.setSellerProductName((String)result[6]);
		dto.setCreatedDate(DateUtility.dateToStringFormat((Date)result[7], Constants.MYSQL_DATE_FORMAT));
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
			case "quantity": 
			case "total_purchase_price":
			case "sellerProductId":
				return DataType.TYPE_INT;
			case "operationType": 
			case "name":
			case "awBillNumber":
			case "sellerProductName":
				return DataType.TYPE_STRING;
		}
		return "";
	}

	private String getDBField(String UIField) {
		switch(UIField) {
			case "id":
				return "pi.id";
			case "quantity":
				return "pi.quantity";
			case "operationType": 
				return "pi.operation_type";
			case "totalPurchasePrice":
				return "pi.total_purchase_price";
			case "awBillNumber":
				return "pi.aw_bill_number";
			case "sellerProductId": 
				return "sp.id";
			case "sellerProductName": 
				return "sp.name";
		}
		return "";
	}
	
	private int getQty(String operationType, int qty) {
		switch(operationType.toUpperCase()) {
			case "SALES":
			case "PURCHASE_RETURN":
			case "REMOVE":
				return -qty;
			default:
				return qty;
		}
	}
	
}
