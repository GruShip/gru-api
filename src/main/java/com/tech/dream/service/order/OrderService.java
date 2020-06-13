package com.tech.dream.service.order;

import java.math.BigInteger;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.tech.dream.db.entity.Address;
import com.tech.dream.db.entity.City;
import com.tech.dream.db.entity.CompanyBranch;
import com.tech.dream.db.entity.Country;
import com.tech.dream.db.entity.Order;
import com.tech.dream.db.entity.OrderBatch;
import com.tech.dream.db.entity.OrderHistory;
import com.tech.dream.db.entity.SellerProduct;
import com.tech.dream.db.entity.State;
import com.tech.dream.db.repository.OrderBatchRepository;
import com.tech.dream.db.repository.OrderHistoryRepository;
import com.tech.dream.db.repository.OrderRepository;
import com.tech.dream.model.AddressDTO;
import com.tech.dream.model.AssetDTO;
import com.tech.dream.model.ErrorDTO;
import com.tech.dream.model.OrderDTO;
import com.tech.dream.model.OrderHistoryDTO;
import com.tech.dream.model.PagingSortSearchDTO;
import com.tech.dream.model.ProductAssetMappingDTO;
import com.tech.dream.model.ResultDTO;
import com.tech.dream.model.SearchQueryDTO;
import com.tech.dream.model.SellerProductAssetMappingDTO;
import com.tech.dream.model.SessionDTO;
import com.tech.dream.service.asset.AssetService;
import com.tech.dream.service.company.AddressService;
import com.tech.dream.util.CommonUtil;
import com.tech.dream.util.Constants;
import com.tech.dream.util.Constants.DataType;
import com.tech.dream.util.Constants.DisplayModuleNames;
import com.tech.dream.util.Constants.OrderEvents;
import com.tech.dream.util.Constants.OrderStates;
import com.tech.dream.util.Constants.OrderStatus;
import com.tech.dream.util.Constants.OrderType;

@Service
@Transactional
public class OrderService {

	@Autowired
	private OrderRepository repository;

	@Autowired
	private OrderBatchRepository orderBatchRepository;

	@Autowired
	private OrderHistoryRepository orderHistoryRepository;

	@Autowired
	private AddressService addressService;
	
	@Autowired
	private OrderStateMachine orderStateMachine;

	@Autowired
	private LocalContainerEntityManagerFactoryBean emf;
	
	@Autowired
	private AssetService assetService;

	private final StateMachineFactory<OrderStates, OrderEvents> factory = null; 
	

	public Object create(List<OrderDTO> dto, SessionDTO session) throws Exception {

		// CREATE VALIDATION
		for (OrderDTO orderDTO: dto){
			ErrorDTO error = createValidation(orderDTO, session);
			if (error != null) {
				return error;
			}
		}

		
		// CREATE ORDER BATCH
		OrderBatch orderBatch = new OrderBatch();
		orderBatchRepository.save(orderBatch);

		for (OrderDTO orderDTO: dto) {

			AddressDTO addressDTO = orderDTO.getBuyerAddress();
			if(addressDTO!=null) {
				Object result = addressService.process(addressDTO);
				if(result != null) {
					if(result instanceof ErrorDTO) {
						return result;
					}else {
						addressDTO = (AddressDTO) result;
					}
				}
				orderDTO.setBuyerAddress(addressDTO);
			}
			Order entity = convertToEntity(orderDTO);

			entity.setOrderBatch(orderBatch);
			entity.setStatus(Constants.OrderStatus.CREATED);
			repository.save(entity);
			orderDTO.setId(entity.getId());

			OrderHistory orderHistory = new OrderHistory();
			orderHistory.setStatus(Constants.OrderStatus.CREATED);
			orderHistory.setOrder(entity);
			orderHistoryRepository.save(orderHistory);

		}
		return dto;
	}
	

	private ErrorDTO createValidation(OrderDTO dto, SessionDTO session) {
		if (dto.getBuyerCompanyBranchId() == null){
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "BuyerCompanyBranchId is mandatory parameter and cannot be null.");
		}
		if (!CommonUtil.isMarketPlaceCompany(session) && session.getCompanyBranchIdList()!=null && !session.getCompanyBranchIdList().contains(dto.getBuyerCompanyBranchId()) ){
			return new ErrorDTO(HttpStatus.FORBIDDEN, DisplayModuleNames.USER+" does not have access to create "+DisplayModuleNames.ORDER + " for this " + DisplayModuleNames.COMPANYBRANCH);
		}
		if (dto.getSellerProductId() == null){
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "SellerProductId is mandatory parameter and cannot be null.");
		}
		if (dto.getBuyerAddress() == null) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "BuyerAddress is mandatory parameter and cannot be null.");
		}
		if (dto.getQuantity() == null){
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "Quantity is mandatory parameter and cannot be null.");
		}
		if (dto.getPrice() == null){
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "Price is mandatory parameter and cannot be null.");
		}
		if (dto.getTax() == null){
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "Tax is mandatory parameter and cannot be null.");
		}
		if (dto.getTotalPrice() == null){
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "TotalPrice is mandatory parameter and cannot be null.");
		}
		return null;
	}

	public Address convertToEntityAddress(AddressDTO dto) {
		Address e = new Address();
		e.setAddressLine1(dto.getAddressLine1());
		e.setAddressLine2(dto.getAddressLine2());
		e.setCountry(new Country(dto.getCountryId()));
		if(dto.getStateId()!=null) {
			e.setState(new State(dto.getStateId()));
		}
		if(dto.getCityId()!=null) {
			e.setCity(new City(dto.getCityId()));
		}
		e.setPincode(dto.getPincode());
		e.setAddressType(dto.getAddressType());
		e.setRemoved(dto.getRemoved());
		return e;
	}

	public Order convertToEntity(OrderDTO dto) {
		Order e = new Order();
		e.setId(dto.getId());
		e.setOrderNumber(dto.getOrderNumber());
		e.setSellerProduct(dto.getSellerProductId()!=null?new SellerProduct(dto.getSellerProductId()):null);
		e.setBuyerCompanyBranch(dto.getBuyerCompanyBranchId()!=null?new CompanyBranch(dto.getBuyerCompanyBranchId()):null);
		e.setStatus(dto.getStatus());
		e.setQuantity(dto.getQuantity());
		e.setPrice(dto.getPrice());
		e.setTax(dto.getTax());
		e.setTotalPrice(dto.getPrice() + dto.getTax());
		e.setOrderNumber(UUID.randomUUID().toString());
		if (dto.getBuyerAddress()!= null){
			e.setBuyerAddress(new Address(dto.getBuyerAddress().getId()));
		}
		return e;
	}
	
	@SuppressWarnings("unchecked")
	public ResultDTO list(SessionDTO session, String orderType, Long companyId, PagingSortSearchDTO filters) throws ParseException {
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
			String query = "select o.id as order_id, o.order_number, o.status, o.quantity, o.price, o.tax, o.total_price, bcb.id as bcb_id, bcb.name as bcb_name, bc.id as bc_id, bc.name as bc_name, "
					+ " ba.id as address_id, ba.address_line_1, ba.address_line_2, ba.city_id, ba.state_id, ba.country_id, ba.pincode, ba.address_type, "
					+ " sp.id as seller_product_id, sp.name as display_name, scb.id as cb_id, scb.name as cb_name, sc.id as c_id, sc.name as c_name, p.id as p_id, p.model_number, " + 
					" pc.id as pc_id, pc.name as pc_name, psc.id as psc_id, psc.name as psc_name, pb.id as pb_id, pb.name as pb_name, pb.company as pb_company, pt.id as pt_id, pt.name as pt_name, pm.id as pm_id, pm.name as pm_name, pco.id as pco_id, pco.name as pco_name, pme.id as pme_id, pme.name as pme_name, pst.id as pst_id, pst.name as pst_name, " + 
					" pss.id as product_screensize_id, pss.name as product_screensize_name, sp.tax_percentage, oh.desc as status_desc, p.desc as p_desc, sp.desc as sp_desc " + 
					" from `order` o " +
					" inner join orderhistory oh on oh.order_id = o.id and oh.status = o.status and oh.removed=0 " +
					" inner join companybranch bcb on bcb.id = o.buyer_company_branch_id " +
					" inner join company bc on bc.id = bcb.company_id " +
					" inner join address ba on ba.id = o.buyer_company_branch_id " +
					" inner join sellerproduct sp on sp.id=o.seller_product_id" + 
					" inner join product p on p.id = sp.product_id " + 
					" inner join productsubcategory psc on psc.id = p.product_sub_category_id " + 
					" inner join productcategory pc on pc.id = psc.product_category_id " + 
					" inner join productmodel pm on pm.id = p.product_model_id " + 
					" inner join producttype pt on pt.id = pm.product_type_id " + 
					" inner join productbrand pb on pb.id = pt.product_brand_id " + 
					" left join productcolor pco on pco.id = p.product_color_id " + 
					" left join productmemory pme on pme.id = p.product_memory_id " + 
					" left join productstorage pst on pst.id = p.product_storage_id " + 
					" left join productscreensize pss on pss.id = p.product_screensize_id " + 
					" inner join companybranch scb on scb.id = sp.company_branch_id " + 
					" inner join company sc on sc.id = scb.company_id " + 
					" where o.removed=false ";
			
			String totalCountQuery = "SELECT count(1) " +
					" from `order` o " +
					" inner join orderhistory oh on oh.order_id = o.id and oh.status = o.status and oh.removed=0 " +
					" inner join companybranch bcb on bcb.id = o.buyer_company_branch_id " +
					" inner join company bc on bc.id = bcb.company_id " +
					" inner join address ba on ba.id = o.buyer_company_branch_id " +
					" inner join sellerproduct sp on sp.id=o.seller_product_id" + 
					" inner join product p on p.id = sp.product_id " + 
					" inner join productsubcategory psc on psc.id = p.product_sub_category_id " + 
					" inner join productcategory pc on pc.id = psc.product_category_id " + 
					" inner join productmodel pm on pm.id = p.product_model_id " + 
					" inner join producttype pt on pt.id = pm.product_type_id " + 
					" inner join productbrand pb on pb.id = pt.product_brand_id " + 
					" left join productcolor pco on pco.id = p.product_color_id " + 
					" left join productmemory pme on pme.id = p.product_memory_id " + 
					" left join productstorage pst on pst.id = p.product_storage_id " + 
					" left join productscreensize pss on pss.id = p.product_screensize_id " + 
					" inner join companybranch scb on scb.id = sp.company_branch_id " + 
					" inner join company sc on sc.id = scb.company_id " + 
					" where o.removed=false ";
			
			if(StringUtils.isEmpty(orderType)) {
				orderType = OrderType.SALES;
			}
			switch(orderType) {
				case OrderType.PURCHASE:
					query = query + " and bc.id = "+companyId;
					totalCountQuery = totalCountQuery + " and bc.id = "+companyId;
					if(!CommonUtil.isMarketPlaceCompany(session)) {
						String cbIdListStr = session.getCompanyBranchIdList().toString();
						cbIdListStr = cbIdListStr.substring(1, cbIdListStr.length()-1);
						if(StringUtils.isEmpty(cbIdListStr)) {
							cbIdListStr = "0";
						}
						query = query + " and bcb.id in (" + cbIdListStr + ")";
						totalCountQuery = totalCountQuery + " and bcb.id in (" + cbIdListStr + ")";
					}
					break;
				default:
					query = query + " and sc.id = "+companyId;
					totalCountQuery = totalCountQuery + " and sc.id = "+companyId;
					if(!CommonUtil.isMarketPlaceCompany(session)) {
						String cbIdListStr = session.getCompanyBranchIdList().toString();
						cbIdListStr = cbIdListStr.substring(1, cbIdListStr.length()-1);
						if(StringUtils.isEmpty(cbIdListStr)) {
							cbIdListStr = "0";
						}
						query = query + " and scb.id in (" + cbIdListStr + ")";
						totalCountQuery = totalCountQuery + " and scb.id in (" + cbIdListStr + ")";
					}
					break;
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
				query += " order by o.id";
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

			if(datalistResult==null || datalistResult.size()!=filters.getPageSize().intValue()) {
				totalCount = datalistResult!=null?Long.valueOf(datalistResult.size()):0;
			} else {
				q = em.createNativeQuery(totalCountQuery);
				totalCount = Long.parseLong(String.valueOf(q.getSingleResult()));
			}
		}finally {
			em.close();
		}
		
		List<OrderDTO> dtoList = new ArrayList<>();
		if(datalistResult!=null && datalistResult.size()>0) {
			for(Object[] dataResult : datalistResult) {
				OrderDTO dto = this.fillOrderDTO(dataResult);
				dtoList.add(dto);
			}
		}
		
		if(dtoList.size()>0) {
			dtoList = fillProductAssets(dtoList);
			dtoList = fillSellerProductAssets(dtoList);
		}
		
		resultDTO.setData(dtoList);
		resultDTO.setErrorDTO(error);
		resultDTO.setTotalCount(totalCount);
		resultDTO.setPageNumber(filters.getPageNumber());
		resultDTO.setPageSize(filters.getPageSize());
		return resultDTO;
	}
	
	public List<OrderDTO> fillProductAssets(List<OrderDTO> dtoList) {
		Set<Long> productIdSet = new HashSet<Long>();
		for(OrderDTO dto:dtoList) {
			productIdSet.add(dto.getProductId());
		}
		List<ProductAssetMappingDTO> pamDTOs = assetService.getAssetsByProductId(new ArrayList<Long>(productIdSet));
		Map<Long, List<AssetDTO>> productIdAssetsMap = new HashMap<Long, List<AssetDTO>>();
		for(ProductAssetMappingDTO pam:pamDTOs) {
			List<AssetDTO> assets = productIdAssetsMap.get(pam.getProductId());
			if(assets == null) {
				assets = new ArrayList<>();
			}
			AssetDTO aDTO = new AssetDTO();
			aDTO.setActive(pam.getActive());
			aDTO.setAssetType(pam.getAssetType());
			aDTO.setAssetUrl(pam.getAssetUrl());
			aDTO.setFileName(pam.getFileName());
			aDTO.setExtension(pam.getExtension());
			assets.add(aDTO);
			productIdAssetsMap.put(pam.getProductId(), assets);
		}
		
		for (OrderDTO productDTO: dtoList){
			productDTO.getAssets().addAll(productIdAssetsMap.get(productDTO.getProductId())!=null?productIdAssetsMap.get(productDTO.getProductId()):new ArrayList<>());
		}
		return dtoList;
	}
	
	
	public List<OrderDTO> fillSellerProductAssets(List<OrderDTO> dtoList) {
		Set<Long> sellerProductIdSet = new HashSet<Long>();
		for(OrderDTO dto:dtoList) {
			sellerProductIdSet.add(dto.getSellerProductId());
		}
		List<SellerProductAssetMappingDTO> spamDTOs = assetService.getAssetsBySellerProductId(new ArrayList<Long>(sellerProductIdSet));
		Map<Long, List<AssetDTO>> sellerproductIdAssetsMap = new HashMap<Long, List<AssetDTO>>();
		for(SellerProductAssetMappingDTO spam:spamDTOs) {
			List<AssetDTO> assets = sellerproductIdAssetsMap.get(spam.getSellerProductId());
			if(assets == null) {
				assets = new ArrayList<>();
			}
			AssetDTO aDTO = new AssetDTO();
			aDTO.setActive(spam.getActive());
			aDTO.setAssetType(spam.getAssetType());
			aDTO.setAssetUrl(spam.getAssetUrl());
			aDTO.setFileName(spam.getFileName());
			aDTO.setExtension(spam.getExtension());
			assets.add(aDTO);
			sellerproductIdAssetsMap.put(spam.getSellerProductId(), assets);
		}
		
		for (OrderDTO dto: dtoList){
			dto.getAssets().addAll(sellerproductIdAssetsMap.get(dto.getSellerProductId())!=null?sellerproductIdAssetsMap.get(dto.getSellerProductId()):new ArrayList<>());
		}
		return dtoList;
	}

	public ResultDTO statusUpdate(SessionDTO session, OrderDTO orderDTO) throws Exception {
		ResultDTO resultDTO = new ResultDTO();
		Order order = repository.getOne(orderDTO.getId());
		boolean eventAccepted = false;
		String userType = "BUYER";
		boolean invalidOrderStatus = false;

		// orderType will tell if its buyer updating or seller updating
		switch(orderDTO.getOrderType()){
			case OrderType.PURCHASE: 
				userType = "BUYER";
				switch(orderDTO.getStatus()) {
					case OrderStatus.CANCELLED:
						eventAccepted = orderStateMachine.cancelOrder(order);
						break;
					default:
						invalidOrderStatus = true;
						break;
				}
				break;
			case OrderType.SALES:
				userType = "SELLER";
				switch(orderDTO.getStatus()) {
					case OrderStatus.ACCEPTED:
						eventAccepted = orderStateMachine.acceptOrder(order);
						break;
					case OrderStatus.REJECTED:
						eventAccepted = orderStateMachine.rejectOrder(order);
						break;
					case OrderStatus.INTRANSIT:
						eventAccepted = orderStateMachine.inTransitOrder(order);
						break;
					case OrderStatus.DELIVERED:
						eventAccepted = orderStateMachine.deliveredOrder(order);
						break;
					case OrderStatus.NOTDELIVERED:
						eventAccepted = orderStateMachine.notDeliveredOrder(order);
						break;
					default:
						invalidOrderStatus = true;
						break;
				}
				break;
			default: 
			resultDTO.setErrorDTO(new ErrorDTO(HttpStatus.BAD_REQUEST, "Please pass valid Ordertype PURCHASE or SALES") );
			return resultDTO;
		}
		if (eventAccepted) {
			// create order history for this
			OrderHistory orderHistory = new OrderHistory();
			orderHistory.setStatus(order.getStatus());
			orderHistory.setOrder(order);
			orderHistory.setDesc(orderDTO.getDesc());
			orderHistoryRepository.save(orderHistory);

			resultDTO.setData(orderDTO);
		}
		else{
			if (invalidOrderStatus) {
				resultDTO.setErrorDTO(new ErrorDTO( HttpStatus.BAD_REQUEST, "Order status " + orderDTO.getStatus() + " not valid for " + userType ));
			}	
			else if (!eventAccepted) {
				resultDTO.setErrorDTO(new ErrorDTO( HttpStatus.BAD_REQUEST, userType + " cannot change order status from " + order.getStatus() + " to " + orderDTO.getStatus() ));	
			}
		}
		return resultDTO;
	}

	public ResultDTO getHistory(SessionDTO session, Long orderId) throws ParseException {
		ResultDTO resultDTO = new ResultDTO();
		Long totalCount = 0L;
		ErrorDTO error = getHistoryValidation(session, orderId);
		if(error!=null) {
			resultDTO.setErrorDTO(error);
			return resultDTO;
		}
		EntityManager em = emf.getNativeEntityManagerFactory().createEntityManager();
		List<Object[]> datalistResult = null;
		try {
			String query = "select oh.id, oh.`desc`, oh.status, oh.order_id, oh.created_date from orderhistory oh" 
					  + " where oh.removed=FALSE ";
			
			if(orderId!=null) {
				query = query + " and oh.order_id = " + orderId;
			}
			query += " order by oh.id desc;";
			
			Query q = em.createNativeQuery(query);
			datalistResult = q.getResultList();
		}finally {
			em.close();
		}
		List<OrderHistoryDTO> dtoList = new ArrayList<>();
		if(datalistResult!=null && datalistResult.size()>0) {
			for(Object[] dataResult : datalistResult) {
				OrderHistoryDTO dto = this.fillOrderHistoryDTO(dataResult);
				dtoList.add(dto);
			}
		}
		resultDTO.setData(dtoList);
		resultDTO.setErrorDTO(error);
		resultDTO.setTotalCount(totalCount);
		return resultDTO;
	}

	private OrderHistoryDTO fillOrderHistoryDTO(Object[] result) throws ParseException {
		OrderHistoryDTO dto = new OrderHistoryDTO();
		dto.setId(((BigInteger)result[0]).longValue());
		dto.setDesc((String)result[1]);
		dto.setStatus((String)result[2]);
		dto.setOrderId(((BigInteger)result[3]).longValue());
		dto.setCreatedDate(String.valueOf(result[4]));
		return dto;
	}
	
	private OrderDTO fillOrderDTO(Object[] result) throws ParseException {
		OrderDTO dto = new OrderDTO();
		dto.setAssets(new ArrayList<>());
		dto.setId(((BigInteger)result[0]).longValue());
		dto.setOrderNumber((String)result[1]);
		dto.setStatus((String)result[2]);
		dto.setQuantity((Integer)result[3]);
		dto.setPrice((Double)result[4]);
		dto.setTax((Double)result[5]);
		dto.setTotalPrice((Double)result[6]);
		dto.setBuyerCompanyBranchId(((BigInteger)result[7]).longValue());
		dto.setBuyerCompanyBranchName((String)result[8]);
		dto.setBuyerCompanyId(((BigInteger)result[9]).longValue());
		dto.setBuyerCompanyName((String)result[10]);
		
		if(result[11]!=null) {
			AddressDTO a = new AddressDTO();
			a.setId(((BigInteger)result[11]).longValue());
			a.setAddressLine1((String)result[12]);
			a.setAddressLine2((String)result[13]);
			if(result[14]!=null) {
				a.setCityId(((BigInteger)result[14]).longValue());
			}
			if(result[15]!=null) {
				a.setStateId(((BigInteger)result[15]).longValue());
			}
			if(result[16]!=null) {
				a.setCountryId(((BigInteger)result[16]).longValue());
			}
			a.setPincode((String)result[17]);
			a.setAddressType((String)result[18]);
			dto.setBuyerAddress(a);	
		}
		
		dto.setSellerProductId(((BigInteger)result[19]).longValue());
		dto.setSellerProductName((String)result[20]);
		dto.setSellerCompanyBranchId(((BigInteger)result[21]).longValue());
		dto.setSellerCompanyBranchName((String)result[22]);
		dto.setSellerCompanyId(((BigInteger)result[23]).longValue());
		dto.setSellerCompanyName((String)result[24]);
		dto.setProductId(((BigInteger)result[25]).longValue());
		dto.setProductModelNumber((String)result[26]);
		dto.setProductCategoryId(((BigInteger)result[27]).longValue());
		dto.setProductCategoryName((String)result[28]);
		dto.setProductSubCategoryId(((BigInteger)result[29]).longValue());
		dto.setProductSubCategoryName((String)result[30]);
		dto.setProductBrandId(((BigInteger)result[31]).longValue());
		dto.setProductBrandName((String)result[32]);
		dto.setProductBrandCompanyName((String)result[33]);
		dto.setProductTypeId(((BigInteger)result[34]).longValue());
		dto.setProductTypeName((String)result[35]);
		dto.setProductModelId(((BigInteger)result[36]).longValue());
		dto.setProductModelName((String)result[37]);
		dto.setProductColorId(result[38]!=null?((BigInteger)result[38]).longValue():null);
		dto.setProductColorName((String)result[39]);
		dto.setProductMemoryId(result[40]!=null?((BigInteger)result[40]).longValue():null);
		dto.setProductMemoryName((String)result[41]);
		dto.setProductStorageId(result[42]!=null?((BigInteger)result[42]).longValue():null);
		dto.setProductStorageName((String)result[43]);
		dto.setProductScreenSizeId(result[44]!=null?((BigInteger)result[44]).longValue():null);
		dto.setProductScreenSizeName((String)result[45]);
		dto.setTaxPercentage(result[46]!=null?(Double)result[46]:null);
		dto.setStatusDesc((String)result[47]);
		dto.setProductDesc((String)result[48]);
		dto.setSellerProductDesc((String)result[49]);
		return dto;
	}
	
	private ErrorDTO listValidation(SessionDTO session) {
		return null;
	}

	private ErrorDTO getHistoryValidation(SessionDTO session, Long orderId) {
		return null;
	}
	
	private String getFieldDataType(String column) {
		switch (column) {
			case "id": 
			case "sellerProductId":
			case "quantity":
			case "price":
			case "tax":
			case "totalPrice":
			case "buyerCompanyBranchId":
			case "buyerCompanyId":
			case "taxPercentage":
			case "sellerCompanyBranchId":
			case "sellerCompanyId":
			case "productId":
			case "productCategoryId":
			case "productSubCategoryId":
			case "productBrandId":
			case "productTypeId":
			case "productModelId":
			case "productColorId":
			case "productMemoryId":
			case "productStorageId":
			case "productScreenSizeId": 
				return DataType.TYPE_INT;
			case "status":
			case "statusDesc":
			case "orderNumber":
			case "sellerProductName": 
			case "buyerCompanyBranchName":
			case "buyerCompanyName":
			case "sellerCompanyBranchName":
			case "sellerCompanyName":
			case "productModelNumber":
			case "productCategoryName":
			case "productSubCategoryName":
			case "productBrandName":
			case "productBrandCompanyName":
			case "productTypeName":
			case "productModelName":
			case "productColorName":
			case "productMemoryName":
			case "productStorageName":
			case "productScreenSizeName":
			case "productDesc":
			case "sellerProductDesc":
				return DataType.TYPE_STRING;
		}
		return "";
	}

	private String getDBField(String UIField) {
		switch(UIField) {
		case "id":
			return "o.id";
		case "quantity": 
			return "o.quantity";
		case "price": 
			return "o.price";
		case "tax": 
			return "o.tax";
		case "totalPrice": 
			return "o.total_price";
		case "sellerProductId":
			return "sp.id";
		case "buyerCompanyBranchId":
			return "bcb.id";
		case "buyerCompanyId":
			return "bc.id";
		case "taxPercentage":
			return "sp.tax_percentage";
		case "sellerCompanyBranchId":
			return "scb.id";
		case "sellerCompanyId":
			return "sc.id";
		case "productId":
			return "p.id";
		case "productCategoryId":
			return "pc.id";
		case "productSubCategoryId":
			return "psc.id";
		case "productBrandId":
			return "pb.id";
		case "productTypeId":
			return "pt.id";
		case "productModelId":
			return "pm.id";
		case "productColorId":
			return "pco.id";
		case "productMemoryId":
			return "pme.id";
		case "productStorageId":
			return "pst.id";
		case "sellerProductName":
			return "sp.name";
		case "sellerCompanyBranchName":
			return "scb.name";
		case "sellerCompanyName":
			return "sc.name";
		case "buyerCompanyBranchName":
			return "bcb.name";
		case "buyerCompanyName":
			return "bc.name";
		case "status":
			return "o.status";
		case "statusDesc":
			return "oh.desc";
		case "orderNumber":
			return "o.order_number";
		case "productModelNumber":
			return "p.model_number";
		case "productCategoryName":
			return "pc.name";
		case "productSubCategoryName":
			return "psc.name";
		case "productBrandName":
			return "pb.name";
		case "productBrandCompanyName":
			return "pb.company";
		case "productTypeName":
			return "pt.name";
		case "productModelName":
			return "pm.name";
		case "productColorName":
			return "pco.name";
		case "productMemoryName":
			return "pme.name";
		case "productStorageName":
			return "pst.name";
		case "productScreenSizeId": 
			return "pss.id";
		case "productScreenSizeName":
			return "pss.name";
		case "productDesc":
			return "p.`desc`";
		case "sellerProductDesc":
			return "sp.`desc`";
		}
		return "";
	}
}
