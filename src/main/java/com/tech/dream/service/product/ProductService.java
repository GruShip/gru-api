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
import com.tech.dream.db.entity.Product;
import com.tech.dream.db.entity.ProductAssetMapping;
import com.tech.dream.db.entity.ProductColor;
import com.tech.dream.db.entity.ProductMemory;
import com.tech.dream.db.entity.ProductModel;
import com.tech.dream.db.entity.ProductScreenSize;
import com.tech.dream.db.entity.ProductStorage;
import com.tech.dream.db.entity.ProductSubCategory;
import com.tech.dream.db.entity.ProductTaxRate;
import com.tech.dream.db.entity.User;
import com.tech.dream.db.repository.ProductAssetMappingRepository;
import com.tech.dream.db.repository.ProductRepository;
import com.tech.dream.db.repository.SellerProductRepository;
import com.tech.dream.model.AssetDTO;
import com.tech.dream.model.ErrorDTO;
import com.tech.dream.model.FileUploadDTO;
import com.tech.dream.model.PagingSortSearchDTO;
import com.tech.dream.model.ProductAssetMappingDTO;
import com.tech.dream.model.ProductDTO;
import com.tech.dream.model.ResultDTO;
import com.tech.dream.model.SearchQueryDTO;
import com.tech.dream.model.SessionDTO;
import com.tech.dream.service.asset.AssetService;
import com.tech.dream.util.Constants;
import com.tech.dream.util.Constants.CompanyType;
import com.tech.dream.util.Constants.DataType;
import com.tech.dream.util.Constants.DisplayModuleNames;
import com.tech.dream.util.DigitalOceanUtil;

@Service
@Transactional
public class ProductService {
	
	@Autowired
	private ProductRepository repository;
	
	@Autowired
	private SellerProductRepository sellerProductRepository;
	
	@Autowired
	private LocalContainerEntityManagerFactoryBean emf;

	@Autowired
	private DigitalOceanUtil digitalOceanUtil;

	@Autowired
	private AssetService assetService;

	@Autowired
	private ProductAssetMappingRepository productAssetMappingRepository;
	
	public Object create(ProductDTO dto, SessionDTO session) {
		ErrorDTO error = createValidation(dto, session);
		if (error != null) {
			return error;
		}
		Product entity = convertToEntity(dto);
		repository.save(entity);
		dto.setId(entity.getId());
		dto.setActive(entity.getActive());
		//dto = convertToDTO(entity);
		return dto;
	}

	public Object update(ProductDTO dto, SessionDTO session) {
		ErrorDTO error = updateValidation(dto, session);
		if (error != null) {
			return error;
		}
		repository.update(dto.getId(), dto.getProductSubCategoryId(), dto.getProductModelId(), dto.getProductColorId(), dto.getProductMemoryId(), dto.getProductStorageId(), dto.getModelNumber(), dto.getProductScreenSizeId(), dto.getProductTaxRateId(), dto.getActive()!=null?dto.getActive():true,dto.getDesc());

		//UserGroup entity = convertToEntity(dto);
		//repository.save(entity);
		//dto = convertToDTO(entity);
		return dto;
	}

	public Object delete(ProductDTO dto, SessionDTO session) {
		ErrorDTO error = deleteValidation(dto, session);
		if (error != null) {
			return error;
		}
		repository.delete(dto.getId());
		return true;
	}

	public ResultDTO uploadAsset(SessionDTO session, Long productId, List<FileUploadDTO> fileUploadDTOs) throws Exception{
		ResultDTO resultDTO = new ResultDTO();
		ErrorDTO error = uploadAssetValidation(productId, fileUploadDTOs);
		if(error!=null) {
			resultDTO.setErrorDTO(error);
			return resultDTO;
		}
	
		List<AssetDTO> productAssets = new ArrayList<AssetDTO>();
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
				assetUrl = digitalOceanUtil.saveFile(file,file.getOriginalFilename(), "product", productId, fileType);
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
			ProductAssetMapping productAssetMapping = new ProductAssetMapping();
			productAssetMapping.setActive(true);
			productAssetMapping.setAsset(new Asset(assetDTO.getId()));
			productAssetMapping.setProduct(new Product(productId));
			productAssetMapping.setCreatedBy(new User(session.getUserId()));
			productAssetMapping.setUpdatedBy(new User(session.getUserId()));
			productAssetMappingRepository.save(productAssetMapping);

			productAssets.add(assetDTO);
		}
		
		resultDTO.setData(productAssets);
		resultDTO.setErrorDTO(error);
		return resultDTO;
	}

	public ResultDTO deleteAsset(SessionDTO session, List<ProductAssetMappingDTO> productAssetMappingDTOs) throws Exception{
		ResultDTO resultDTO = new ResultDTO();
		for (ProductAssetMappingDTO productAssetMappingDTO: productAssetMappingDTOs) {
			Long assetId = productAssetMappingDTO.getAssetId();
			Long productId = productAssetMappingDTO.getProductId();
			ErrorDTO error = deleteAssetValidation(productId, assetId);
			if(error!=null) {
				resultDTO.setErrorDTO(error);
				return resultDTO;
			}
		}

		for (ProductAssetMappingDTO productAssetMappingDTO: productAssetMappingDTOs) {
			Long assetId = productAssetMappingDTO.getAssetId();
			Long productId = productAssetMappingDTO.getProductId();
			productAssetMappingRepository.delete(productId, assetId);
			assetService.delete(assetId, session);
		}
		resultDTO.setData(true);
		return resultDTO;
	}

	public Product convertToEntity(ProductDTO dto) {
		Product e = new Product();
		e.setId(dto.getId());
		e.setProductSubCategory(new ProductSubCategory(dto.getProductSubCategoryId()));
		e.setProductModel(new ProductModel(dto.getProductModelId()));
		e.setModelNumber(dto.getModelNumber());
		e.setProductColor(dto.getProductColorId()!=null?new ProductColor(dto.getProductColorId()):null);
		e.setProductMemory(dto.getProductMemoryId()!=null?new ProductMemory(dto.getProductMemoryId()):null);
		e.setProductStorage(dto.getProductStorageId()!=null?new ProductStorage(dto.getProductStorageId()):null);
		e.setProductScreenSize(dto.getProductScreenSizeId()!=null?new ProductScreenSize(dto.getProductScreenSizeId()):null);
		e.setProductTaxRate(dto.getProductTaxRateId()!=null?new ProductTaxRate(dto.getProductTaxRateId()):null);
		e.setActive(dto.getActive()!=null?dto.getActive():true);
		e.setDesc(dto.getDesc());
		return e;
	}

	private ErrorDTO deleteValidation(ProductDTO dto, SessionDTO session) {
		if (dto.getId() == null) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "Id is mandatory and cannot be null.");
		}
		if(!CompanyType.MARKETPLACE.equalsIgnoreCase(session.getCompanyType())) {
			return new ErrorDTO(HttpStatus.FORBIDDEN, DisplayModuleNames.USER+" does not have access to delete "+DisplayModuleNames.SYSTEMPRODUCT);
		}
		if (repository.existsByProductId(dto.getId())<=0) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, DisplayModuleNames.SYSTEMPRODUCT+" does not exists for given id. ");
		}
		if (sellerProductRepository.existsSellerProductByProductId(dto.getId())>0) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, DisplayModuleNames.PRODUCT+"'s are mapped to given "+DisplayModuleNames.SELLERPRODUCT);
		}
		return null;
	}

	private ErrorDTO createValidation(ProductDTO dto, SessionDTO session) {
		if (StringUtils.isEmpty(dto.getModelNumber())) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "ModelNumber is mandatory parameter and cannot be null.");
		}
		if (dto.getProductCategoryId() == null) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "ProductCategoryId is mandatory parameter and cannot be null.");
		}
		if (dto.getProductSubCategoryId() == null) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "ProductSubCategoryId is mandatory parameter and cannot be null.");
		}
		if (dto.getProductBrandId() == null) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "ProductBrandId is mandatory parameter and cannot be null.");
		}
		if (dto.getProductTypeId() == null) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "ProductTypeId is mandatory parameter and cannot be null.");
		}
		if (dto.getProductModelId() == null) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "ProductModelId is mandatory parameter and cannot be null.");
		}
		if(!CompanyType.MARKETPLACE.equalsIgnoreCase(session.getCompanyType())) {
			return new ErrorDTO(HttpStatus.FORBIDDEN, DisplayModuleNames.USER+" does not have access to create "+DisplayModuleNames.SYSTEMPRODUCT);
		}
		if (repository.existsMappingByCategoryIdAndSubCategoryId(dto.getProductCategoryId(), dto.getProductSubCategoryId())<=0) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, DisplayModuleNames.PRODUCTSUBCATEGORY+" & "+DisplayModuleNames.PRODUCTCATEGORY+" are not linked.");
		}
		if (repository.existsMappingByBrandIdAndTypeIdAndModelId(dto.getProductBrandId(), dto.getProductTypeId(), dto.getProductModelId())<=0) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, DisplayModuleNames.PRODUCTBRAND+", "+DisplayModuleNames.PRODUCTTYPE+" & "+DisplayModuleNames.PRODUCTMODEL+" are not linked.");
		}
		if (repository.existsByCategoryIdAndSubCategoryIdAndBrandIdAndTypeIdAndModelIdAndModelNumber(dto.getProductCategoryId(), dto.getProductSubCategoryId(), dto.getProductBrandId(), dto.getProductTypeId(), dto.getProductModelId(), dto.getModelNumber())>0) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, DisplayModuleNames.PRODUCT+" already exists for given "+DisplayModuleNames.PRODUCTCATEGORY+", "+DisplayModuleNames.PRODUCTSUBCATEGORY+", "+DisplayModuleNames.PRODUCTBRAND+ ", "+ DisplayModuleNames.PRODUCTTYPE+ ", "+DisplayModuleNames.PRODUCTMODEL+", ModelNumber");
		}
		return null;
	}

	private ErrorDTO updateValidation(ProductDTO dto, SessionDTO session) {
		if (dto.getId() == null) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "Id is mandatory parameter and cannot be null.");
		}
		if (StringUtils.isEmpty(dto.getModelNumber())) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "ModelNumber is mandatory parameter and cannot be null.");
		}
		if (dto.getProductCategoryId() == null) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "ProductCategoryId is mandatory parameter and cannot be null.");
		}
		if (dto.getProductSubCategoryId() == null) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "ProductSubCategoryId is mandatory parameter and cannot be null.");
		}
		if (dto.getProductBrandId() == null) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "ProductBrandId is mandatory parameter and cannot be null.");
		}
		if (dto.getProductTypeId() == null) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "ProductTypeId is mandatory parameter and cannot be null.");
		}
		if (dto.getProductModelId() == null) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "ProductModelId is mandatory parameter and cannot be null.");
		}
		if(!CompanyType.MARKETPLACE.equalsIgnoreCase(session.getCompanyType())) {
			return new ErrorDTO(HttpStatus.FORBIDDEN, DisplayModuleNames.USER+" does not have access to create "+DisplayModuleNames.SYSTEMPRODUCT);
		}
		
		if (repository.existsMappingByCategoryIdAndSubCategoryId(dto.getProductCategoryId(), dto.getProductSubCategoryId())<=0) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, DisplayModuleNames.PRODUCTSUBCATEGORY+" & "+DisplayModuleNames.PRODUCTCATEGORY+" are not linked.");
		}
		if (repository.existsMappingByBrandIdAndTypeIdAndModelId(dto.getProductBrandId(), dto.getProductTypeId(), dto.getProductModelId())<=0) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, DisplayModuleNames.PRODUCTBRAND+", "+DisplayModuleNames.PRODUCTTYPE+" & "+DisplayModuleNames.PRODUCTMODEL+" are not linked.");
		}
		if (repository.existsByCategoryIdAndSubCategoryIdAndBrandIdAndTypeIdAndModelIdAndModelNumberAndNotId(dto.getProductCategoryId(), dto.getProductSubCategoryId(), dto.getProductBrandId(), dto.getProductTypeId(), dto.getProductModelId(), dto.getModelNumber(),dto.getId())>0) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, DisplayModuleNames.PRODUCT+" already exists for given "+DisplayModuleNames.PRODUCTCATEGORY+", "+DisplayModuleNames.PRODUCTSUBCATEGORY+", "+DisplayModuleNames.PRODUCTBRAND+ ", "+ DisplayModuleNames.PRODUCTTYPE+ ", "+DisplayModuleNames.PRODUCTMODEL+", ModelNumber");
		}
		return null;
	}

	private ErrorDTO uploadAssetValidation(Long productId, List<FileUploadDTO> fileUploadDTOs){
		if(productId == null){
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "productId is mandatory parameter and cannot be null.");
		}
		if (repository.existsByProductId(productId) <= 0){
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "no asset existing against this productId");
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

	private ErrorDTO deleteAssetValidation(Long productId, Long assetId){
		if(productId == null){
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "productId is mandatory parameter and cannot be null.");
		}
		if(assetId == null){
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "assetId is mandatory parameter and cannot be null.");
		}
		if (productAssetMappingRepository.existsByProductIdAndAssetId(productId, assetId) <= 0){
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "no asset existing against this product");
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public ResultDTO list(SessionDTO session, PagingSortSearchDTO filters) {
		ErrorDTO error = listValidation(session);
		ResultDTO resultDTO = new ResultDTO();
		Long totalCount = 0L; 
		if(error!=null) {
			resultDTO.setErrorDTO(error);
			return resultDTO;
		}
		if (filters == null) filters = new PagingSortSearchDTO();

		EntityManager em = emf.getNativeEntityManagerFactory().createEntityManager();
		List<Object[]> datalistResult = null;
		try {
			String query = "SELECT p.id, p.active, pc.id as product_category_id, pc.name as product_category_name, psc.id as product_sub_category_id, psc.name as product_sub_category_name, pb.id as product_brand_id, pb.name as product_brand_name, pt.id as product_type_id, pt.name as product_type_name, pm.id as product_model_id, pm.name as product_model_name, pco.id as product_color_id, pco.name as product_color_name, pbm.id as product_memory_id, pbm.name as product_memory_name, ps.id as product_storage_id, ps.name as product_storage_name, p.model_number, pss.id as product_screensize_id, pss.name as product_screensize_name, ptr.id as product_taxrate_id, ptr.tax_percentage, p.`desc`"
					  + " from product p "
					  + " inner join productsubcategory psc on p.product_sub_category_id = psc.id "
					  + " inner join productcategory pc on pc.id = psc.product_category_id "
					  + " inner join productmodel pm on pm.id = p.product_model_id "
					  + " inner join producttype pt on pt.id = pm.product_type_id "
					  + " inner join productbrand pb on pb.id = pt.product_brand_id "
					  + " left join productcolor pco on pco.id = p.product_color_id "
					  + " left join productmemory pbm on pbm.id = p.product_memory_id "
					  + " left join productstorage ps on ps.id = p.product_storage_id "
					  + " left join productscreensize pss on pss.id = p.product_screensize_id "
					  + " left join producttaxrate ptr on ptr.id = p.product_taxrate_id "
					  + " where p.removed=FALSE";

			
			String totalCountQuery = "SELECT count(1)"
					  + " from product p "
					  + " inner join productsubcategory psc on p.product_sub_category_id = psc.id "
					  + " inner join productcategory pc on pc.id = psc.product_category_id "
					  + " inner join productmodel pm on pm.id = p.product_model_id "
					  + " inner join producttype pt on pt.id = pm.product_type_id "
					  + " inner join productbrand pb on pb.id = pt.product_brand_id "
					  + " left join productcolor pco on pco.id = p.product_color_id "
					  + " left join productmemory pbm on pbm.id = p.product_memory_id "
					  + " left join productstorage ps on ps.id = p.product_storage_id "
					  + " left join productscreensize pss on pss.id = p.product_screensize_id "
					  + " left join producttaxrate ptr on ptr.id = p.product_taxrate_id "
					  + " where p.removed=FALSE";
			
			// searching
			String searchQuery = "";
			if (filters.getSearch() != null){
				for (SearchQueryDTO sDto: filters.getSearch()){
					String dbField = getDBField(sDto.getSearchField());
					String fieldDatatype = getProductFieldDataType(sDto.getSearchField());

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
				query += " order by p.id";
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
		
		List<ProductDTO> dtoList = new ArrayList<>();
		if(datalistResult!=null && datalistResult.size()>0) {
			for(Object[] dataResult : datalistResult) {
				ProductDTO dto = this.fillSystemProductDTO(dataResult);
				dtoList.add(dto);
			}
		}
		if(dtoList.size()>0) {
			dtoList = fillProductAssets(dtoList);
		}
	
		resultDTO.setData(dtoList);
		resultDTO.setErrorDTO(error);
		resultDTO.setTotalCount(totalCount);
		resultDTO.setPageNumber(filters.getPageNumber());
		resultDTO.setPageSize(filters.getPageSize());
		return resultDTO;
	}
	
	public List<ProductDTO> fillProductAssets(List<ProductDTO> dtoList) {
		Set<Long> productIdSet = new HashSet<Long>();
		for(ProductDTO dto:dtoList) {
			productIdSet.add(dto.getId());
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
		
		for (ProductDTO productDTO: dtoList){
			productDTO.setProductAssets(productIdAssetsMap.get(productDTO.getId())!=null?productIdAssetsMap.get(productDTO.getId()):new ArrayList<>());
		}
		return dtoList;
	}

	
	private ProductDTO fillSystemProductDTO(Object[] result) {
		ProductDTO dto = new ProductDTO();
		dto.setId(((BigInteger)result[0]).longValue());
		dto.setActive(result[1]!=null?(Boolean)result[1]:null);
		dto.setProductCategoryId(((BigInteger)result[2]).longValue());
		dto.setProductCategoryName((String)result[3]);
		dto.setProductSubCategoryId(((BigInteger)result[4]).longValue());
		dto.setProductSubCategoryName((String)result[5]);
		dto.setProductBrandId(((BigInteger)result[6]).longValue());
		dto.setProductBrandName((String)result[7]);
		dto.setProductTypeId(((BigInteger)result[8]).longValue());
		dto.setProductTypeName((String)result[9]);
		dto.setProductModelId(((BigInteger)result[10]).longValue());
		dto.setProductModelName((String)result[11]);
		dto.setProductColorId(result[12]!=null?((BigInteger)result[12]).longValue():null);
		dto.setProductColorName((String)result[13]);
		dto.setProductMemoryId(result[14]!=null?((BigInteger)result[14]).longValue():null);
		dto.setProductMemoryName((String)result[15]);
		dto.setProductStorageId(result[16]!=null?((BigInteger)result[16]).longValue():null);
		dto.setProductStorageName((String)result[17]);
		dto.setModelNumber((String)result[18]);
		dto.setProductScreenSizeId(result[19]!=null?((BigInteger)result[19]).longValue():null);
		dto.setProductScreenSizeName((String)result[20]);
		dto.setProductTaxRateId(result[21]!=null?((BigInteger)result[21]).longValue():null);
		dto.setProductTaxRatePercentage(result[22]!=null?(Double)result[22]:null);
		dto.setDesc(result[23]!=null?(String)result[23]:null);
		
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
			String query = "SELECT p.id, p.active, pc.id as product_category_id, pc.name as product_category_name, psc.id as product_sub_category_id, psc.name as product_sub_category_name, pb.id as product_brand_id, pb.name as product_brand_name, pt.id as product_type_id, pt.name as product_type_name, pm.id as product_model_id, pm.name as product_model_name, pco.id as product_color_id, pco.name as product_color_name, pbm.id as product_memory_id, pbm.name as product_memory_name, ps.id as product_storage_id, ps.name as product_storage_name, p.model_number, pss.id as product_screensize_id, pss.name as product_screensize_name, ptr.id as product_taxrate_id, ptr.tax_percentage, p.`desc`"
					  + " from product p "
					  + " inner join productsubcategory psc on p.product_sub_category_id = psc.id "
					  + " inner join productcategory pc on pc.id = psc.product_category_id "
					  + " inner join productmodel pm on pm.id = p.product_model_id "
					  + " inner join producttype pt on pt.id = pm.product_type_id "
					  + " inner join productbrand pb on pb.id = pt.product_brand_id "
					  + " left join productcolor pco on pco.id = p.product_color_id "
					  + " left join productmemory pbm on pbm.id = p.product_memory_id "
					  + " left join productstorage ps on ps.id = p.product_storage_id "
					  + " left join productscreensize pss on pss.id = p.product_screensize_id "
					  + " left join producttaxrate ptr on ptr.id = p.product_taxrate_id "
					  + " where p.removed=FALSE and p.id = ?";
			Query q = em.createNativeQuery(query);
			q.setParameter(1, id);
			datalistResult = q.getResultList();
		}finally {
			em.close();
		}
		if(datalistResult!=null && datalistResult.size()>0) {
			Object[] dataResult = datalistResult.get(0);
			ProductDTO dto = this.fillSystemProductDTO(dataResult);

			List<ProductDTO> dtoList = new ArrayList<ProductDTO>();
			dtoList.add(dto);
			dtoList = fillProductAssets(dtoList);
			dto = dtoList.get(0);

			return dto;
		}
		return null;
	}

	private ErrorDTO getValidation(SessionDTO session, Long id) {
		//check if user is superadmin  or usergroupid belongs to same company of session company id
		return null;
	}

	private String getProductFieldDataType(String column) {
		switch (column) {
			case "id":
			case "active": 
			case "productCategoryId": 
			case "productSubCategoryId": 
			case "productBrandId":
			case "productTypeId": 
			case "productModelId": 
			case "productColorId":
			case "productMemoryId": 
			case "productStorageId": 
			case "productScreenSizeId": 
			case "productTaxRateId": 
			case "productTaxRatePercentage": 
				return DataType.TYPE_INT;
			
			case "desc":
			case "modelNumber":
			case "productCategoryName": 
			case "productSubCategoryName": 
			case "productBrandName": 
			case "productTypeName": 
			case "productModelName":
			case "productColorName":
			case "productMemoryName":
			case "productStorageName":
			case "productScreenSizeName":
				return DataType.TYPE_STRING;
			
		}
		return "";
	}

	private String getDBField(String UIField) {
		switch(UIField) {
			case "id":
			case "active": 
				return "p." + UIField;
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
				return "pbm.id";
			case "productStorageId": 
				return "ps.id";
			case "productScreenSizeId": 
				return "pss.id";
			case "productTaxRateId": 
				return "ptr.id";
			
			case "desc":
				return "p.`desc`";
			case "modelNumber":
				return "p.model_number";
			case "productCategoryName": 
				return "pc.name"; 
			case "productSubCategoryName": 
				return "psc.name";
			case "productBrandName": 
				return "pb.name"; 
			case "productTypeName": 
				return "pt.name";
			case "productModelName":
				return "pm.name";
			case "productColorName":
				return "pco.name";
			case "productMemoryName":
				return "pbm.name";
			case "productStorageName":
				return "ps.name";
			case "productScreenSizeName":
				return "pss.name";
			case "productTaxRatePercentage":
				return "ptr.tax_percentage";
		}
		return "";
	}
	
}
