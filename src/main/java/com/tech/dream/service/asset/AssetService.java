package com.tech.dream.service.asset;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tech.dream.db.entity.Asset;
import com.tech.dream.db.entity.User;
import com.tech.dream.db.repository.AssetRepository;
import com.tech.dream.model.AssetDTO;
import com.tech.dream.model.ProductAssetMappingDTO;
import com.tech.dream.model.SellerProductAssetMappingDTO;
import com.tech.dream.model.SessionDTO;

@Service
@Transactional
public class AssetService {
	
	@Autowired
	private AssetRepository repository;

	@Autowired
	private LocalContainerEntityManagerFactoryBean emf;

    public AssetDTO create(AssetDTO dto, SessionDTO session) throws Exception {
		Asset entity = convertToEntity(session, dto);
		repository.save(entity);
		dto.setId(entity.getId());
		dto.setActive(entity.getActive());
		return dto;
	}

	public void delete(Long assetId, SessionDTO session) throws Exception {
		repository.delete(assetId);
	}

	public List<ProductAssetMappingDTO> getAssetsByProductId(List<Long> productIdList){	
		EntityManager em = emf.getNativeEntityManagerFactory().createEntityManager();
		List<Object[]> datalistResult = null;
		try {
			String query = "select ass.id, ass.asset_type, ass.asset_url,ass.active, ass.upload_type,ass.file_name, ass.extension, pasm.product_id from asset ass inner join productassetmapping pasm on ass.id = pasm.asset_id "
			+ " where ass.removed = FALSE and pasm.removed = FALSE ";
		
			if (productIdList.size() > 0)  {
				query += " and pasm.product_id in ( " + productIdList.toString().substring(1, productIdList.toString().length()-1) + ");";
			}
			Query q = em.createNativeQuery(query);
			datalistResult = q.getResultList();
		}finally {
			em.close();
		}
		List<ProductAssetMappingDTO> dtoList = new ArrayList<>();
		if(datalistResult!=null && datalistResult.size()>0) {
			for(Object[] dataResult : datalistResult) {
				ProductAssetMappingDTO dto = fillProductAssetMappingDTO(dataResult);
				dtoList.add(dto);
			}
		}
		
		return dtoList;
	}

	private ProductAssetMappingDTO fillProductAssetMappingDTO(Object[] result){
		ProductAssetMappingDTO dto = new ProductAssetMappingDTO();
		dto.setAssetId(((BigInteger)result[0]).longValue());
		dto.setAssetType((String)result[1]);
		dto.setAssetUrl((String)result[2]);
		dto.setActive(result[3]!=null?(Boolean)result[3]:null);
		dto.setUploadType((String)result[4]);
		dto.setFileName((String)result[5]);
		dto.setExtension((String)result[6]);
		dto.setProductId(((BigInteger)result[7]).longValue());
		return dto;
	}
	
	private SellerProductAssetMappingDTO fillSellerProductAssetMappingDTO(Object[] result){
		SellerProductAssetMappingDTO dto = new SellerProductAssetMappingDTO();
		dto.setAssetId(((BigInteger)result[0]).longValue());
		dto.setAssetType((String)result[1]);
		dto.setAssetUrl((String)result[2]);
		dto.setActive(result[3]!=null?(Boolean)result[3]:null);
		dto.setUploadType((String)result[4]);
		dto.setFileName((String)result[5]);
		dto.setExtension((String)result[6]);
		dto.setSellerProductId(((BigInteger)result[7]).longValue());
		return dto;
	}

	/* private AssetDTO fillSystemAssetDTO(Object[] result) {
		AssetDTO dto = new AssetDTO();
		dto.setId(((BigInteger)result[0]).longValue());
		dto.setAssetType((String)result[1]);
		dto.setAssetUrl((String)result[2]);
		dto.setActive(result[3]!=null?(Boolean)result[3]:null);
		dto.setUploadType((String)result[4]);
		dto.setFileName((String)result[5]);
		dto.setExtension((String)result[6]);
		return dto;
	} */
	
	public Asset convertToEntity(SessionDTO session, AssetDTO dto) {
		Asset e = new Asset();
		e.setId(dto.getId());
		e.setAssetType(dto.getAssetType());
		e.setAssetUrl(dto.getAssetUrl());
		e.setFileName(dto.getFileName());
		e.setExtension(dto.getExtension());
		e.setUploadType(dto.getUploadType());
		e.setActive(dto.getActive()!=null?dto.getActive():true);
		if (dto.getCreatedBy() != null){
			e.setCreatedBy(new User(dto.getCreatedBy()));
		}
		if (session.getUserId() != null){
			e.setUpdatedBy(new User(session.getUserId()));
		}
		return e;
	}

	public List<SellerProductAssetMappingDTO> getAssetsBySellerProductId(ArrayList<Long> sellerProductIdList) {
		EntityManager em = emf.getNativeEntityManagerFactory().createEntityManager();
		List<Object[]> datalistResult = null;
		try {
			String query = "select ass.id, ass.asset_type, ass.asset_url,ass.active, ass.upload_type,ass.file_name, ass.extension, spam.seller_product_id from asset ass inner join sellerproductassetmapping spam on ass.id = spam.asset_id "
			+ " where ass.removed = FALSE and spam.removed = FALSE ";
		
			if (sellerProductIdList.size() > 0)  {
				query += " and spam.seller_product_id in ( " + sellerProductIdList.toString().substring(1, sellerProductIdList.toString().length()-1) + ");";
			}
			Query q = em.createNativeQuery(query);
			datalistResult = q.getResultList();
		}finally {
			em.close();
		}
		List<SellerProductAssetMappingDTO> dtoList = new ArrayList<>();
		if(datalistResult!=null && datalistResult.size()>0) {
			for(Object[] dataResult : datalistResult) {
				SellerProductAssetMappingDTO dto = fillSellerProductAssetMappingDTO(dataResult);
				dtoList.add(dto);
			}
		}
		
		return dtoList;
	}
    
}