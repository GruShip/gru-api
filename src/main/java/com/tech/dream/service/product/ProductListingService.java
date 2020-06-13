package com.tech.dream.service.product;

import java.math.BigInteger;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tech.dream.model.AssetDTO;
import com.tech.dream.model.ErrorDTO;
import com.tech.dream.model.PagingSortSearchDTO;
import com.tech.dream.model.ProductAssetMappingDTO;
import com.tech.dream.model.ProductListingDTO;
import com.tech.dream.model.ResultDTO;
import com.tech.dream.model.SearchQueryDTO;
import com.tech.dream.model.SellerProductAssetMappingDTO;
import com.tech.dream.model.SessionDTO;
import com.tech.dream.service.asset.AssetService;
import com.tech.dream.service.company.ProductListingCompanyMappingService;
import com.tech.dream.util.CommonUtil;
import com.tech.dream.util.Constants;
import com.tech.dream.util.Constants.DataType;

@Service
@Transactional
public class ProductListingService {

	@Autowired
	private LocalContainerEntityManagerFactoryBean emf;
	
	@Autowired
	private AssetService assetService;

	@Autowired
	private ProductListingCompanyMappingService productListingCompanyMappingService;

	@SuppressWarnings("unchecked")
	public ResultDTO list(SessionDTO session, Long companyId, PagingSortSearchDTO filters) throws ParseException {
		ResultDTO resultDTO = new ResultDTO();
		List<ProductListingDTO> dtoList = new ArrayList<>();
		Long totalCount = 0L;
		ErrorDTO error = listValidation(session);
		if(error!=null) {
			resultDTO.setErrorDTO(error);
			return resultDTO;
		}
		EntityManager em = emf.getNativeEntityManagerFactory().createEntityManager();
		List<Object[]> datalistResult = null;
		try {
			List<Long> destinationCompanyIdList = productListingCompanyMappingService.getProductListingCompanyIdList(companyId); 
			
			if(destinationCompanyIdList!=null && destinationCompanyIdList.size()>0) {
				String query = "select sp.id, sp.name as display_name, sp.mrp_sell_price, sp.dealer_sell_price, sp.wholesale_sell_price, cb.id as cb_id, cb.name as cb_name, c.id as c_id, c.name as c_name, p.id as p_id, p.model_number, " + 
						" pc.id as pc_id, pc.name as pc_name, psc.id as psc_id, psc.name as psc_name, pb.id as pb_id, pb.name as pb_name, pb.company as pb_company, pt.id as pt_id, pt.name as pt_name, pm.id as pm_id, pm.name as pm_name, pco.id as pco_id, pco.name as pco_name, pme.id as pme_id, pme.name as pme_name, pst.id as pst_id, pst.name as pst_name, " +
						" pss.id as product_screensize_id, pss.name as product_screensize_name, ptr.id as product_taxrate_id, ptr.tax_percentage, " +
						" mpcp.id as mrp_pcpid, mpcp.name as mrp_pcpname, mpcp.discount_type as mrp_pcpdt, mpcp.value as mrp_pcpvalue, dpcp.id as d_pcpid, dpcp.name as d_pcpname, dpcp.discount_type as d_pcpdt, dpcp.value as d_pcpvalue, wpcp.id as wrp_pcpid, wpcp.name as w_pcpname, wpcp.discount_type as w_pcpdt, wpcp.value as w_pcpvalue, p.desc as p_desc, sp.desc as sp_desc " + 
						" from sellerproduct sp " + 
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
						" left join producttaxrate ptr on ptr.id = p.product_taxrate_id " +
						" inner join companybranch cb on cb.id = sp.company_branch_id " + 
						" left join productcoupon mpcp on mpcp.id = sp.mrp_product_coupon_id " + 
						" left join productcoupon dpcp on dpcp.id = sp.dealer_product_coupon_id " + 
						" left join productcoupon wpcp on wpcp.id = sp.wholesale_product_coupon_id " +
						" inner join company c on c.id = cb.company_id " + 
						" where sp.removed=false and sp.active=true ";
				
				String totalCountQuery = "SELECT count(1) " +
						" from sellerproduct sp " + 
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
						" left join producttaxrate ptr on ptr.id = p.product_taxrate_id " +
						" left join productcoupon mpcp on mpcp.id = sp.mrp_product_coupon_id " + 
						" left join productcoupon dpcp on dpcp.id = sp.dealer_product_coupon_id " + 
						" left join productcoupon wpcp on wpcp.id = sp.wholesale_product_coupon_id " +
						" inner join companybranch cb on cb.id = sp.company_branch_id " + 
						" inner join company c on c.id = cb.company_id " + 
						" where sp.removed=false and sp.active=true ";
				
				if(!destinationCompanyIdList.contains(0L)) {
					query = query + " and c.id in (" + destinationCompanyIdList.toString().substring(1, destinationCompanyIdList.toString().length()-1)+ ")";
					totalCountQuery = totalCountQuery + " and c.id in (" + destinationCompanyIdList.toString().substring(1, destinationCompanyIdList.toString().length()-1)+ ")";
				} 
				
				if(companyId!=null) {
					query = query + " and c.id <> " + companyId;
					totalCountQuery = totalCountQuery + " and c.id <> " + companyId;
				}
				
				String searchQuery = "";
				// searching
				if (filters.getSearch() != null){
					for (SearchQueryDTO sDto: filters.getSearch()){
						String searchType = CommonUtil.getSearchType(sDto.getSearchType());
						String dbField = getDBField(sDto.getSearchField());
						String fieldDatatype = getFieldDataType(sDto.getSearchField());
	
						searchQuery += " and " + CommonUtil.getSearchQueryField(searchType, fieldDatatype, dbField, sDto.getSearchText());
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
	
				if(datalistResult==null || datalistResult.size()!=filters.getPageSize().intValue()) {
					totalCount = datalistResult!=null?Long.valueOf(datalistResult.size()):0;
				} else {
					q = em.createNativeQuery(totalCountQuery);
					totalCount = Long.parseLong(String.valueOf(q.getSingleResult()));
				}
			}
		}finally {
			em.close();
		}
		
		if(datalistResult!=null && datalistResult.size()>0) {
			for(Object[] dataResult : datalistResult) {
				ProductListingDTO dto = this.fillProductListingDTO(dataResult);
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
	
	public List<ProductListingDTO> fillProductAssets(List<ProductListingDTO> dtoList) {
		Set<Long> productIdSet = new HashSet<Long>();
		for(ProductListingDTO dto:dtoList) {
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
		
		for (ProductListingDTO productDTO: dtoList){
			productDTO.getAssets().addAll(productIdAssetsMap.get(productDTO.getProductId())!=null?productIdAssetsMap.get(productDTO.getProductId()):new ArrayList<>());
		}
		return dtoList;
	}
	
	
	public List<ProductListingDTO> fillSellerProductAssets(List<ProductListingDTO> dtoList) {
		Set<Long> sellerProductIdSet = new HashSet<Long>();
		for(ProductListingDTO dto:dtoList) {
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
		
		for (ProductListingDTO dto: dtoList){
			dto.getAssets().addAll(sellerproductIdAssetsMap.get(dto.getSellerProductId())!=null?sellerproductIdAssetsMap.get(dto.getSellerProductId()):new ArrayList<>());
		}
		return dtoList;
	}

	private ProductListingDTO fillProductListingDTO(Object[] result) throws ParseException {
		ProductListingDTO dto = new ProductListingDTO();
		dto.setAssets(new ArrayList<>());
		dto.setSellerProductId(((BigInteger) result[0]).longValue());
		dto.setSellerProductName((String) result[1]);
		dto.setMrpSellPrice((Double) result[2]);
		dto.setDealerSellPrice((Double) result[3]);
		dto.setWholesaleSellPrice((Double) result[4]);
		dto.setCompanyBranchId(((BigInteger) result[5]).longValue());
		dto.setCompanyBranchName((String) result[6]);
		dto.setCompanyId(((BigInteger) result[7]).longValue());
		dto.setCompanyName((String) result[8]);
		dto.setProductId(((BigInteger) result[9]).longValue());
		dto.setProductModelNumber((String) result[10]);
		dto.setProductCategoryId(((BigInteger) result[11]).longValue());
		dto.setProductCategoryName((String) result[12]);
		dto.setProductSubCategoryId(((BigInteger) result[13]).longValue());
		dto.setProductSubCategoryName((String) result[14]);
		dto.setProductBrandId(((BigInteger) result[15]).longValue());
		dto.setProductBrandName((String) result[16]);
		dto.setProductBrandCompanyName((String) result[17]);
		dto.setProductTypeId(((BigInteger) result[18]).longValue());
		dto.setProductTypeName((String) result[19]);
		dto.setProductModelId(((BigInteger) result[20]).longValue());
		dto.setProductModelName((String) result[21]);
		dto.setProductColorId(result[22] != null ? ((BigInteger) result[22]).longValue() : null);
		dto.setProductColorName((String) result[23]);
		dto.setProductMemoryId(result[24] != null ? ((BigInteger) result[24]).longValue() : null);
		dto.setProductMemoryName((String) result[25]);
		dto.setProductStorageId(result[26] != null ? ((BigInteger) result[26]).longValue() : null);
		dto.setProductStorageName((String) result[27]);
		dto.setProductScreenSizeId(result[28] != null ? ((BigInteger) result[28]).longValue() : null);
		dto.setProductScreenSizeName((String) result[29]);
		dto.setProductTaxRateId(result[30] != null ? ((BigInteger) result[30]).longValue() : null);
		dto.setProductTaxRatePercentage(result[31] != null ? (Double) result[31] : null);
		
		dto.setMrpProductCouponId(result[32]!=null?((BigInteger)result[32]).longValue():null);
		dto.setMrpProductCouponName((String)result[33]);
		dto.setMrpProductCouponDiscountType((String)result[34]);
		dto.setMrpProductCouponValue((Double)result[35]);
		dto.setDealerProductCouponId(result[36]!=null?((BigInteger)result[36]).longValue():null);
		dto.setDealerProductCouponName((String)result[37]);
		dto.setDealerProductCouponDiscountType((String)result[38]);
		dto.setDealerProductCouponValue((Double)result[39]);
		dto.setWholesaleProductCouponId(result[40]!=null?((BigInteger)result[40]).longValue():null);
		dto.setWholesaleProductCouponName((String)result[41]);
		dto.setWholesaleProductCouponDiscountType((String)result[42]);
		dto.setWholesaleProductCouponValue((Double)result[43]);
		dto.setProductDesc((String)result[44]);
		dto.setSellerProductDesc((String)result[45]);
		return dto;
	}

	private ErrorDTO listValidation(SessionDTO session) {
		return null;
	}

	private String getFieldDataType(String column) {
		switch (column) {
		case "sellerProductId":
		case "mrpSellPrice":
		case "dealerSellPrice":
		case "wholesaleSellPrice":
		case "companyBranchId":
		case "companyId":
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
		case "productTaxRateId":
		case "productTaxRatePercentage":
		case "mrpProductCouponId":
		case "mrpProductCouponValue":
		case "dealerProductCouponId":
		case "dealerProductCouponValue":
		case "wholesaleProductCouponId":
		case "wholesaleProductCouponValue":
			return DataType.TYPE_INT;
		case "sellerProductName":
		case "companyBranchName":
		case "companyName":
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
		case "mrpProductCouponName":
		case "mrpProductCouponDiscountType":
		case "dealerProductCouponName":
		case "dealerProductCouponDiscountType":
		case "wholesaleProductCouponName":
		case "wholesaleProductCouponDiscountType":
		case "productDesc":
		case "sellerProductDesc":
			return DataType.TYPE_STRING;
		}
		return "";
	}

	private String getDBField(String UIField) {
		switch (UIField) {
		case "sellerProductId":
			return "sp.id";
		case "mrpSellPrice":
			return "sp.mrp_sell_price";
		case "dealerSellPrice":
			return "sp.dealer_sell_price";
		case "wholesaleSellPrice":
			return "sp.wholesale_sell_price";
		case "companyBranchId":
			return "cb.id";
		case "companyId":
			return "c.id";
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
		case "companyBranchName":
			return "cb.name";
		case "companyName":
			return "c.name";
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
		case "productTaxRateId":
			return "ptr.id";
		case "productScreenSizeName":
			return "pss.name";
		case "productTaxRatePercentage":
			return "ptr.tax_percentage";
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
		case "productDesc":
			return "p.`desc`";
		case "sellerProductDesc":
			return "sp.`desc`";
		}
		return "";
	}

}
