package com.tech.dream.service.product;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.tech.dream.db.entity.Asset;
import com.tech.dream.db.entity.CompanyBranch;
import com.tech.dream.db.entity.Product;
import com.tech.dream.db.entity.ProductCoupon;
import com.tech.dream.db.entity.SellerProduct;
import com.tech.dream.db.entity.SellerProductAssetMapping;
import com.tech.dream.db.entity.User;
import com.tech.dream.db.repository.SellerProductAssetMappingRepository;
import com.tech.dream.db.repository.SellerProductRepository;
import com.tech.dream.model.AssetDTO;
import com.tech.dream.model.ErrorDTO;
import com.tech.dream.model.FileUploadDTO;
import com.tech.dream.model.PagingSortSearchDTO;
import com.tech.dream.model.ProductAssetMappingDTO;
import com.tech.dream.model.ResultDTO;
import com.tech.dream.model.SearchQueryDTO;
import com.tech.dream.model.SellerProductAssetMappingDTO;
import com.tech.dream.model.SellerProductDTO;
import com.tech.dream.model.SessionDTO;
import com.tech.dream.service.asset.AssetService;
import com.tech.dream.util.CommonUtil;
import com.tech.dream.util.Constants;
import com.tech.dream.util.Constants.CompanyType;
import com.tech.dream.util.Constants.DataType;
import com.tech.dream.util.Constants.DisplayModuleNames;
import com.tech.dream.util.DigitalOceanUtil;

@Service
@Transactional
public class SellerProductService {
	
	@Autowired
	private SellerProductRepository repository;
	
	@Autowired
	private LocalContainerEntityManagerFactoryBean emf;

	@Autowired
	private AssetService assetService;

	@Autowired
	private SellerProductAssetMappingRepository sellerProductAssetMappingRepository;

	@Autowired
	private DigitalOceanUtil digitalOceanUtil;
	
	public Object create(SellerProductDTO dto, SessionDTO session) {
		ErrorDTO error = createValidation(dto, session);
		if (error != null) {
			return error;
		}
		SellerProduct entity = convertToEntity(dto);
		repository.save(entity);
		dto.setId(entity.getId());
		dto.setActive(entity.getActive());
		//dto = convertToDTO(entity);
		return dto;
	}

	public Object update(SellerProductDTO dto, SessionDTO session) {
		ErrorDTO error = updateValidation(dto, session);
		if (error != null) {
			return error;
		}
		repository.update(dto.getId(), dto.getDesc(), dto.getMrpSellPrice(), dto.getDealerSellPrice(), dto.getWholesaleSellPrice(), dto.getName(), dto.getTaxPercentage(), dto.getMrpProductCouponId(), dto.getDealerProductCouponId(), dto.getWholesaleProductCouponId());
		return dto;
	}

	public Object delete(SellerProductDTO dto, SessionDTO session) {
		ErrorDTO error = deleteValidation(dto, session);
		if (error != null) {
			return error;
		}
		repository.delete(dto.getId());
		return true;
	}

	public SellerProduct convertToEntity(SellerProductDTO dto) {
		SellerProduct e = new SellerProduct();
		e.setId(dto.getId());
		e.setName(dto.getName());
		e.setCompanyBranch(new CompanyBranch(dto.getCompanyBranchId()));
		e.setProduct(new Product(dto.getProductId()));
		e.setDesc(dto.getDesc());
		e.setMrpSellPrice(dto.getMrpSellPrice());
		e.setDealerSellPrice(dto.getDealerSellPrice());
		e.setWholesaleSellPrice(dto.getWholesaleSellPrice());
		e.setAvailableQty(0);
		e.setActive(false);
		e.setIsApproved(true);
		e.setTaxPercentage(dto.getTaxPercentage());
		if(dto.getMrpProductCouponId()!=null) {
			e.setMrpProductCoupon(new ProductCoupon(dto.getMrpProductCouponId()));
		}
		if(dto.getDealerProductCouponId()!=null) {
			e.setDealerProductCoupon(new ProductCoupon(dto.getDealerProductCouponId()));
		}
		if(dto.getWholesaleProductCouponId()!=null) {
			e.setWholesaleProductCoupon(new ProductCoupon(dto.getWholesaleProductCouponId()));
		}
		return e;
	}

	private ErrorDTO deleteValidation(SellerProductDTO dto, SessionDTO session) {
		if (dto.getId() == null) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "Id is mandatory and cannot be null.");
		}
//		if(!CompanyType.MARKETPLACE.equalsIgnoreCase(session.getCompanyType())) {
//			return new ErrorDTO(HttpStatus.FORBIDDEN, DisplayModuleNames.USER+" does not have access to delete "+DisplayModuleNames.SELLERPRODUCT);
//		}
		if (repository.existsBySellerProductId(dto.getId())<=0) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, DisplayModuleNames.SELLERPRODUCT+" does not exists for given id. ");
		}
		return null;
	}

	private ErrorDTO createValidation(SellerProductDTO dto, SessionDTO session) {
		if (StringUtils.isEmpty(dto.getName())) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "Name is mandatory parameter and cannot be null.");
		}
		if (dto.getProductId() == null) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "ProductId is mandatory parameter and cannot be null.");
		}
		if (dto.getCompanyBranchId() == null) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "CompanyBranchId is mandatory parameter and cannot be null.");
		}
		if(!CompanyType.MARKETPLACE.equalsIgnoreCase(session.getCompanyType()) && session.getCompanyBranchIdList()!=null && !session.getCompanyBranchIdList().contains(dto.getCompanyBranchId())) {
			return new ErrorDTO(HttpStatus.FORBIDDEN, DisplayModuleNames.USER+" does not have access to create "+DisplayModuleNames.SELLERPRODUCT + " for this " + DisplayModuleNames.COMPANYBRANCH);
		}
		if (repository.existsByProductIdAndCompanyBranchId(dto.getProductId(), dto.getCompanyBranchId())>0) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, DisplayModuleNames.SELLERPRODUCT+" already exists for given "+DisplayModuleNames.SELLERPRODUCT+", "+DisplayModuleNames.COMPANYBRANCH);
		}
		if(repository.existsSPByNameAndCompanyId(dto.getName(), dto.getCompanyBranchId())>0) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, DisplayModuleNames.SELLERPRODUCT+" Name already exists for the " + DisplayModuleNames.COMPANY);
		}
		return null;
	}

	private ErrorDTO updateValidation(SellerProductDTO dto, SessionDTO session) {
		if (StringUtils.isEmpty(dto.getName())) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "Name is mandatory parameter and cannot be null.");
		}
		if (dto.getId() == null) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "Id is mandatory parameter and cannot be null.");
		}
		if(!CompanyType.MARKETPLACE.equalsIgnoreCase(session.getCompanyType()) && session.getCompanyBranchIdList()!=null && !session.getCompanyBranchIdList().contains(dto.getCompanyBranchId())) {
			return new ErrorDTO(HttpStatus.FORBIDDEN, DisplayModuleNames.USER+" does not have access to create "+DisplayModuleNames.SELLERPRODUCT + " for this " + DisplayModuleNames.COMPANYBRANCH);
		}
		if (repository.existsBySellerProductId(dto.getId())<=0) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, DisplayModuleNames.SELLERPRODUCT+" does not exists for given id. ");
		}
		if(repository.existsSPByNameAndCompanyIdAndNotId(dto.getName(), dto.getCompanyBranchId(), dto.getId())>0) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, DisplayModuleNames.SELLERPRODUCT+" Name already exists for the " + DisplayModuleNames.COMPANY);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public ResultDTO list(SessionDTO session, Long companyId, PagingSortSearchDTO filters) {
		ResultDTO resultDTO = new ResultDTO();
		List<SellerProductDTO> dtoList = new ArrayList<>();
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
			String query = "SELECT sp.id, sp.active, sp.is_approved, sp.`desc`, sp.mrp_sell_price, sp.dealer_sell_price, sp.wholesale_sell_price, sp.available_qty, sp.product_id, p.model_number, sp.company_branch_id, cb.name as company_branch_name, sp.name as seller_product_name, c.id as company_id, c.name as company_name, sp.tax_percentage, "
					  + " mpcp.id as mrp_pcpid, mpcp.name as mrp_pcpname, mpcp.discount_type as mrp_pcpdt, mpcp.value as mrp_pcpvalue, dpcp.id as d_pcpid, dpcp.name as d_pcpname, dpcp.discount_type as d_pcpdt, dpcp.value as d_pcpvalue, wpcp.id as wrp_pcpid, wpcp.name as w_pcpname, wpcp.discount_type as w_pcpdt, wpcp.value as w_pcpvalue "
					  + " from sellerproduct sp "
					  + " inner join product p on p.id = sp.product_id "
					  + " inner join companybranch cb on cb.id = sp.company_branch_id "
					  + " inner join company c on c.id = cb.company_id "
					  + " left join productcoupon mpcp on mpcp.id = sp.mrp_product_coupon_id "
					  + " left join productcoupon dpcp on dpcp.id = sp.dealer_product_coupon_id "
					  + " left join productcoupon wpcp on wpcp.id = sp.wholesale_product_coupon_id "
					  + " where sp.removed=FALSE";
			
			String totalCountQuery = "SELECT count(1) "
					  + " from sellerproduct sp "
					  + " inner join product p on p.id = sp.product_id "
					  + " inner join companybranch cb on cb.id = sp.company_branch_id "
					  + " inner join company c on c.id = cb.company_id "
					  + " left join productcoupon mpcp on mpcp.id = sp.mrp_product_coupon_id "
					  + " left join productcoupon dpcp on dpcp.id = sp.dealer_product_coupon_id "
					  + " left join productcoupon wpcp on wpcp.id = sp.wholesale_product_coupon_id "
					  + " where sp.removed=FALSE";
			
			String searchQuery = "";
			if(!CommonUtil.isMarketPlaceCompany(session) && session.getCompanyBranchIdList()!=null) {
				String cbidStr = session.getCompanyBranchIdList().toString();
				searchQuery = searchQuery + " and sp.company_branch_id in ( " + cbidStr.substring(1, cbidStr.length()-1) + " )";
			}
			if(companyId!=null) {
				searchQuery = searchQuery + " and cb.company_id = " + companyId;
			}
			
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
				query += " order by sp.id";
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
			
			if(datalistResult!=null && datalistResult.size()>0) {
				for(Object[] dataResult : datalistResult) {
					SellerProductDTO dto = this.fillSellerProductDTO(dataResult);
					dtoList.add(dto);
				}
			}
			
			if(datalistResult==null || datalistResult.size()!=filters.getPageSize().intValue()) {
				totalCount = datalistResult!=null?Long.valueOf(datalistResult.size()):0;
			} else {
				q = em.createNativeQuery(totalCountQuery);
				totalCount = Long.parseLong(String.valueOf(q.getSingleResult()));
			}
		}finally {
			em.close();
		}

		if(dtoList.size()>0) {
			dtoList = fillSellerProductAssets(dtoList);
			dtoList = fillProductAssets(dtoList);
		}
		
		resultDTO.setData(dtoList);
		resultDTO.setErrorDTO(error);
		resultDTO.setTotalCount(totalCount);
		resultDTO.setPageNumber(filters.getPageNumber());
		resultDTO.setPageSize(filters.getPageSize());
		return resultDTO;
	}
	
	
	public List<SellerProductDTO> fillProductAssets(List<SellerProductDTO> dtoList) {
		Set<Long> productIdSet = new HashSet<Long>();
		for(SellerProductDTO dto:dtoList) {
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
		
		for (SellerProductDTO productDTO: dtoList){
			productDTO.setProductAssets(productIdAssetsMap.get(productDTO.getProductId())!=null?productIdAssetsMap.get(productDTO.getProductId()):new ArrayList<>());
		}
		return dtoList;
	}
	
	
	public List<SellerProductDTO> fillSellerProductAssets(List<SellerProductDTO> dtoList) {
		Set<Long> sellerProductIdSet = new HashSet<Long>();
		for(SellerProductDTO dto:dtoList) {
			sellerProductIdSet.add(dto.getId());
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
		
		for (SellerProductDTO dto: dtoList){
			dto.setSellerProductAssets(sellerproductIdAssetsMap.get(dto.getId())!=null?sellerproductIdAssetsMap.get(dto.getId()):new ArrayList<>());
		}
		return dtoList;
	}

	public ResultDTO uploadAsset(SessionDTO session, Long sellerProductId, List<FileUploadDTO> fileUploadDTOs) throws Exception{
		ResultDTO resultDTO = new ResultDTO();
		ErrorDTO error = uploadAssetValidation(sellerProductId, fileUploadDTOs);
		if(error!=null) {
			resultDTO.setErrorDTO(error);
			return resultDTO;
		}
	
		List<AssetDTO> sellerProductAssets = new ArrayList<AssetDTO>();
		for (FileUploadDTO fileUploadDTO: fileUploadDTOs) {
			MultipartFile file = fileUploadDTO.getFile();
			String fileType = fileUploadDTO.getFileType().toUpperCase();
			String uploadType = fileUploadDTO.getUploadType();

			String filename = "";
			String assetUrl = "";
			String extension = "";

			if (!"URL".equalsIgnoreCase(uploadType)) {
				filename = file.getOriginalFilename();
				extension = filename.substring(filename.lastIndexOf(".") + 1);
				assetUrl = digitalOceanUtil.saveFile(file,file.getOriginalFilename(), "sellerProduct", sellerProductId, fileType);
			} else{
				assetUrl = fileUploadDTO.getFileUrl();
			}

			// create asset record
			AssetDTO assetDTO = new AssetDTO();
			assetDTO.setAssetType(fileType);
			assetDTO.setAssetUrl(assetUrl);
			assetDTO.setFileName(filename);
			assetDTO.setExtension(extension);
			assetDTO.setUploadType(uploadType);
			assetDTO.setCreatedBy(session.getUserId());
			assetDTO = assetService.create(assetDTO, session);

			// create productAssetMapping record
			SellerProductAssetMapping sellerProductAssetMapping = new SellerProductAssetMapping();
			sellerProductAssetMapping.setActive(true);
			sellerProductAssetMapping.setAsset(new Asset(assetDTO.getId()));
			sellerProductAssetMapping.setSellerProduct(new SellerProduct(sellerProductId));
			sellerProductAssetMapping.setCreatedBy(new User(session.getUserId()));
			sellerProductAssetMapping.setUpdatedBy(new User(session.getUserId()));
			sellerProductAssetMappingRepository.save(sellerProductAssetMapping);

			sellerProductAssets.add(assetDTO);
		}
		
		resultDTO.setData(sellerProductAssets);
		resultDTO.setErrorDTO(error);
		return resultDTO;
	}

	public ResultDTO deleteAsset(SessionDTO session, List<SellerProductAssetMappingDTO> sellerProductAssetMappingDTOs) throws Exception{
		ResultDTO resultDTO = new ResultDTO();
		for (SellerProductAssetMappingDTO sellerProductAssetMappingDTO: sellerProductAssetMappingDTOs) {
			Long assetId = sellerProductAssetMappingDTO.getAssetId();
			Long sellerProductId = sellerProductAssetMappingDTO.getSellerProductId();
			ErrorDTO error = deleteAssetValidation(sellerProductId, assetId);
			if(error!=null) {
				resultDTO.setErrorDTO(error);
				return resultDTO;
			}
		}

		for (SellerProductAssetMappingDTO sellerProductAssetMappingDTO: sellerProductAssetMappingDTOs) {
			Long assetId = sellerProductAssetMappingDTO.getAssetId();
			Long sellerProductId = sellerProductAssetMappingDTO.getSellerProductId();
			sellerProductAssetMappingRepository.delete(sellerProductId, assetId);
			assetService.delete(assetId, session);
		}
		resultDTO.setData(true);
		return resultDTO;
	}
	
	private ErrorDTO uploadAssetValidation(Long sellerProductId, List<FileUploadDTO> fileUploadDTOs){
		if(sellerProductId == null){
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "sellerProductId is mandatory parameter and cannot be null.");
		}
		if (repository.existsBySellerProductId(sellerProductId) <= 0){
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "no seller product existing against this sellerProductId");
		}
		for (FileUploadDTO fileUploadDTO: fileUploadDTOs){
			String fileType = fileUploadDTO.getFileType();
			if (fileType != null ) fileType = fileType.toUpperCase();
			if (!"IMAGE".equals(fileType) && !"VIDEO".equals(fileType)){
				return new ErrorDTO( HttpStatus.BAD_REQUEST, "Invalid fileType, supported fileTypes are IMAGE/VIDEO");
			}
		}
		return null;
	}

	private ErrorDTO deleteAssetValidation(Long sellerProductId, Long assetId){
		if(sellerProductId == null){
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "productId is mandatory parameter and cannot be null.");
		}
		if(assetId == null){
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "assetId is mandatory parameter and cannot be null.");
		}
		if (sellerProductAssetMappingRepository.existsBySellerProductIdAndAssetId(sellerProductId, assetId) <= 0){
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "no asset existing against this product");
		}
		return null;
	}
	
	private SellerProductDTO fillSellerProductDTO(Object[] result) {
		SellerProductDTO dto = new SellerProductDTO();
		dto.setId(((BigInteger)result[0]).longValue());
		dto.setActive(result[1]!=null?(Boolean)result[1]:false);
		dto.setIsApproved(result[2]!=null?(Boolean)result[2]:false);
		dto.setDesc((String)result[3]);
		dto.setMrpSellPrice(result[4]!=null?((Double)result[4]).doubleValue():null);
		dto.setDealerSellPrice(result[5]!=null?((Double)result[5]).doubleValue():null);
		dto.setWholesaleSellPrice(result[6]!=null?((Double)result[6]).doubleValue():null);
		dto.setAvailableQty(result[7]!=null?((Integer)result[7]).intValue():null);
		dto.setProductId(((BigInteger)result[8]).longValue());
		dto.setProductModelNumber((String)result[9]);
		dto.setCompanyBranchId(((BigInteger)result[10]).longValue());
		dto.setCompanyBranchName((String)result[11]);
		dto.setName((String)result[12]);
		dto.setCompanyId(((BigInteger)result[13]).longValue());
		dto.setCompanyName((String)result[14]);
		dto.setTaxPercentage((Double)result[15]);
		dto.setMrpProductCouponId(result[16]!=null?((BigInteger)result[16]).longValue():null);
		dto.setMrpProductCouponName((String)result[17]);
		dto.setMrpProductCouponDiscountType((String)result[18]);
		dto.setMrpProductCouponValue((Double)result[19]);
		dto.setDealerProductCouponId(result[20]!=null?((BigInteger)result[20]).longValue():null);
		dto.setDealerProductCouponName((String)result[21]);
		dto.setDealerProductCouponDiscountType((String)result[22]);
		dto.setDealerProductCouponValue((Double)result[23]);
		dto.setWholesaleProductCouponId(result[24]!=null?((BigInteger)result[24]).longValue():null);
		dto.setWholesaleProductCouponName((String)result[25]);
		dto.setWholesaleProductCouponDiscountType((String)result[26]);
		dto.setWholesaleProductCouponValue((Double)result[27]);
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
		EntityManager em = emf.getNativeEntityManagerFactory().createEntityManager();
		List<Object[]> datalistResult = null;
		try {
			String query = "SELECT sp.id, sp.active, sp.is_approved, sp.`desc`, sp.mrp_sell_price, sp.dealer_sell_price, sp.wholesale_sell_price, sp.available_qty, sp.product_id, p.model_number, sp.company_branch_id, cb.name as company_branch_name, sp.name as seller_product_name, c.id as company_id, c.name as company_name, sp.tax_percentage "
					  + " from sellerproduct sp "
					  + " inner join product p on p.id = sp.product_id "
					  + " inner join companybranch cb on cb.id = sp.company_branch_id "
					  + " inner join company c on c.id = cb.company_id "
					  + " where sp.removed=FALSE ";
			
			if(!CommonUtil.isMarketPlaceCompany(session) && session.getCompanyBranchIdList()!=null) {
				String cbidStr = session.getCompanyBranchIdList().toString();
				query = query + " and sp.company_branch_id in ( " + cbidStr.substring(1, cbidStr.length()-1) + " )";
			}
			if(id!=null) {
				query = query + " and sp.id = " + id;
			}
			
			Query q = em.createNativeQuery(query);
			datalistResult = q.getResultList();
		}finally {
			em.close();
		}
		if(datalistResult!=null && datalistResult.size()>0) {
			Object[] dataResult = datalistResult.get(0);
			SellerProductDTO dto = this.fillSellerProductDTO(dataResult);
			return dto;
		}
		return null;
	}

	private ErrorDTO getValidation(SessionDTO session, Long id) {
		//check if user is superadmin  or usergroupid belongs to same company of session company id
		return null;
	}
	
	private String getFieldDataType(String column) {
		switch (column) {
			case "id":
			case "active": 
			case "isApproved":
			case "mrpSellPrice": 
			case "dealerSellPrice":
			case "wholesaleSellPrice":
			case "productModelId": 
			case "companyBranchId":
			case "companyId":
			case "availableQty":
			case "mrpProductCouponId":
			case "mrpProductCouponValue":
			case "dealerProductCouponId":
			case "dealerProductCouponValue":
			case "wholesaleProductCouponId":
			case "wholesaleProductCouponValue":
				return DataType.TYPE_INT;
			case "desc": 
			case "name":
			case "productModelNumber":
			case "companyBranchName":
			case "companyName":
			case "mrpProductCouponName":
			case "mrpProductCouponDiscountType":
			case "dealerProductCouponName":
			case "dealerProductCouponDiscountType":
			case "wholesaleProductCouponName":
			case "wholesaleProductCouponDiscountType":
				return DataType.TYPE_STRING;
		}
		return "";
	}

	private String getDBField(String UIField) {
		switch(UIField) {
			case "id":
				return "sp.id";
			case "name":
				return "sp.name";
			case "desc": 
				return "sp.`desc`";
			case "isApproved":
				return "sp.is_approved";
			case "mrpSellPrice": 
				return "sp.mrp_sell_price";
			case "dealerSellPrice":
				return "sp.dealer_sell_price";
			case "active": 
				return "sp.active";
			case "wholesaleSellPrice":
				return "sp.wholesale_sell_price";
			case "productModelId": 
				return "sp.product_id";
			case "productModelNumber":
				return "p.model_number";
			case "companyBranchId":
				return "sp.company_branch_id";
			case "companyBranchName":
				return "cb.name";
			case "availableQty":
				return "sp.available_qty";
			case "companyId":
				return "c.id";
			case "companyName":
				return "c.name";
			case "mrpProductCouponId":
				return "mpcp.id";
			case "mrpProductCouponValue":
				return "mpcp.value";
			case "dealerProductCouponId":
				return "dpcp.id";
			case "dealerProductCouponValue":
				return "dpcp.value";
			case "wholesaleProductCouponId":
				return "wpcp.id";
			case "wholesaleProductCouponValue":
				return "wpcp.value";
			case "mrpProductCouponName":
				return "mpcp.name";
			case "mrpProductCouponDiscountType":
				return "mpcp.discount_type";
			case "dealerProductCouponName":
				return "dpcp.name";
			case "dealerProductCouponDiscountType":
				return "dpcp.discount_type";
			case "wholesaleProductCouponName":
				return "wpcp.name";
			case "wholesaleProductCouponDiscountType":
				return "wpcp.discount_type";
			
		}
		return "";
	}

	public Object status(SellerProductDTO dto, SessionDTO session) {
		ErrorDTO error = updateStatusValidation(dto, session);
		if (error != null) {
			return error;
		}
		repository.updateStatus(dto.getId(), dto.getActive());
		return dto;
	}
	
	private ErrorDTO updateStatusValidation(SellerProductDTO dto, SessionDTO session) {
		if (dto.getId() == null) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "Id is mandatory parameter and cannot be null.");
		}
		if(!CompanyType.MARKETPLACE.equalsIgnoreCase(session.getCompanyType()) && session.getCompanyBranchIdList()!=null && session.getCompanyBranchIdList().contains(dto.getCompanyBranchId())) {
			return new ErrorDTO(HttpStatus.FORBIDDEN, DisplayModuleNames.USER+" does not have access to create "+DisplayModuleNames.SELLERPRODUCT + " for this " + DisplayModuleNames.COMPANYBRANCH);
		}
		if (repository.existsBySellerProductId(dto.getId())<=0) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, DisplayModuleNames.SELLERPRODUCT+" does not exists for given id. ");
		}
		return null;
	}
	
	public Object updateApproved(SellerProductDTO dto, SessionDTO session) {
		ErrorDTO error = updateApprovedValidation(dto, session);
		if (error != null) {
			return error;
		}
		repository.updateApproved(dto.getId(), dto.getIsApproved());
		return dto;
	}
	
	private ErrorDTO updateApprovedValidation(SellerProductDTO dto, SessionDTO session) {
		if (dto.getId() == null) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "Id is mandatory parameter and cannot be null.");
		}
		if(!CompanyType.MARKETPLACE.equalsIgnoreCase(session.getCompanyType()) && session.getCompanyBranchIdList()!=null && session.getCompanyBranchIdList().contains(dto.getCompanyBranchId())) {
			return new ErrorDTO(HttpStatus.FORBIDDEN, DisplayModuleNames.USER+" does not have access to create "+DisplayModuleNames.SELLERPRODUCT + " for this " + DisplayModuleNames.COMPANYBRANCH);
		}
		if (repository.existsBySellerProductId(dto.getId())<=0) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, DisplayModuleNames.SELLERPRODUCT+" does not exists for given id. ");
		}
		return null;
	}
	
	public boolean updateQuantity(Long id, int qty) {
		repository.updateQuantity(id, qty);
		return true;
	}

}
