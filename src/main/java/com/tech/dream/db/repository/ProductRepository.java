package com.tech.dream.db.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tech.dream.db.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>{

	@Query(value = "select case when count(1)> 0 then true else false end from product p "
			+ " inner join productsubcategory psc on p.product_sub_category_id = psc.id "
			+ " inner join productcategory pc on pc.id = psc.product_category_id "
			+ " inner join productmodel pm on pm.id = p.product_model_id "
			+ " inner join producttype pt on pt.id = pm.product_type_id "
			+ " inner join productbrand pb on pb.id = pt.product_brand_id "
			+ " where pc.id=:productCategoryId and psc.id=:productSubCategoryId and "
			+ " pc.id=:productBrandId and pt.id = :productTypeId and "
			+ " pm.id = :productModelId and p.model_number=:modelNumber and p.removed = FALSE",  nativeQuery = true)
	int existsByCategoryIdAndSubCategoryIdAndBrandIdAndTypeIdAndModelIdAndModelNumber(@Param("productCategoryId")Long productCategoryId,
			@Param("productSubCategoryId")Long productSubCategoryId, @Param("productBrandId")Long productBrandId, 
			@Param("productTypeId")Long productTypeId, @Param("productModelId")Long productModelId, @Param("modelNumber")String modelNumber);
	
	@Query(value = "select case when count(1)> 0 then true else false end from product p "
			+ " inner join productsubcategory psc on p.product_sub_category_id = psc.id "
			+ " inner join productcategory pc on pc.id = psc.product_category_id "
			+ " inner join productmodel pm on pm.id = p.product_model_id "
			+ " inner join producttype pt on pt.id = pm.product_type_id "
			+ " inner join productbrand pb on pb.id = pt.product_brand_id "
			+ " where pc.id=:productCategoryId and psc.id=:productSubCategoryId and "
			+ " pc.id=:productBrandId and pt.id = :productTypeId and "
			+ " pm.id = :productModelId and p.model_number=:modelNumber and p.removed = FALSE and p.id <> :id",  nativeQuery = true)
	int existsByCategoryIdAndSubCategoryIdAndBrandIdAndTypeIdAndModelIdAndModelNumberAndNotId(@Param("productCategoryId")Long productCategoryId,
			@Param("productSubCategoryId")Long productSubCategoryId, @Param("productBrandId")Long productBrandId, 
			@Param("productTypeId")Long productTypeId, @Param("productModelId")Long productModelId, @Param("modelNumber")String modelNumber, @Param("id")Long id);
	
	@Modifying
	@Query(value = "UPDATE product p set product_sub_category_id=:productSubCategoryId, product_model_id = :productModelId, product_color_id = :productColorId, product_memory_id = :productMemoryId, product_storage_id = :productStorageId, model_number=:modelNumber, product_screensize_id=:productScreenSizeId, product_taxrate_id=:productTaxRateId, active=:active, `desc`=:desc, updated_date= now() where p.id = :id", nativeQuery = true)
	void update(@Param("id") Long id, @Param("productSubCategoryId") Long productSubCategoryId, @Param("productModelId")Long productModelId, @Param("productColorId")Long productColorId, @Param("productMemoryId")Long productMemoryId, @Param("productStorageId")Long productStorageId, @Param("modelNumber")String modelNumber, @Param("productScreenSizeId")Long productScreenSizeId, @Param("productTaxRateId")Long productTaxRateId, @Param("active") Boolean active, @Param("desc")String desc);
	
	@Modifying
	@Query(value = "UPDATE product set removed=1, updated_date= now()  where id = :id", nativeQuery = true)
	void delete(@Param("id")Long id);
	
	@Query(value = "select case when count(1)> 0 then true else false end from product p where p.id = :id and p.removed = FALSE", nativeQuery = true)
	long existsByProductId(@Param("id")Long id);
	
	//-----------
	
	@Query(value = "select case when count(1)> 0 then true else false end from productsubcategory psc "
			+ " inner join productcategory pc on pc.id = psc.product_category_id "
			+ " where pc.removed=FALSE and psc.removed = FALSE and pc.id =:productCategoryId and psc.id = :productSubCategoryId",  nativeQuery = true)
	int existsMappingByCategoryIdAndSubCategoryId(@Param("productCategoryId")Long productCategoryId, @Param("productSubCategoryId")Long productSubCategoryId);

	@Query(value = "select case when count(1)> 0 then true else false end from productbrand pb "
			+ " inner join producttype pt on pb.id = pt.product_brand_id "
			+ " inner join productmodel pm on pt.id = pm.product_type_id "
			+ " where pb.removed=FALSE and pt.removed = FALSE and pm.removed = FALSE and pb.id =:productBrandId and pt.id = :productTypeId and pm.id=:productModelId",  nativeQuery = true)
	int existsMappingByBrandIdAndTypeIdAndModelId(@Param("productBrandId")Long productBrandId, @Param("productTypeId")Long productTypeId, @Param("productModelId")Long productModelId);

	@Query(value = "select case when count(1)> 0 then true else false end from product p "
			+ " inner join productsubcategory psc on psc.id = p.product_sub_category_id "
			+ " where psc.product_category_id = :productCategoryId and p.removed = FALSE", nativeQuery = true)
	long existsProductByProductCategoryId(@Param("productCategoryId")Long productCategoryId);
	
	@Query(value = "select case when count(1)> 0 then true else false end from product p "
			+ " where p.product_sub_category_id = :productSubCategoryId and p.removed = FALSE", nativeQuery = true)
	long existsProductByProductSubCategoryId(@Param("productSubCategoryId")Long productSubCategoryId);
	
	@Query(value = "select case when count(1)> 0 then true else false end from product p "
			+ " inner join productmodel pm on pm.id = p.product_model_id "
			+ " inner join producttype pt on pt.id = p.product_type_id "
			+ " where pt.product_brand_id = :productBrandId and p.removed = FALSE", nativeQuery = true)
	long existsProductByProductBrandId(@Param("productBrandId")Long productBrandId);
	
	@Query(value = "select case when count(1)> 0 then true else false end from product p "
			+ " inner join productmodel pm on pm.id = p.product_model_id "
			+ " where pm.product_type_id = :productTypeId and p.removed = FALSE", nativeQuery = true)
	long existsProductByProductTypeId(@Param("productTypeId")Long productTypeId);
	
	@Query(value = "select case when count(1)> 0 then true else false end from product p "
			+ " where p.product_model_id = :productModelId and p.removed = FALSE", nativeQuery = true)
	long existsProductByProductModelId(@Param("productModelId")Long productModelId);
	
	@Query(value = "select case when count(1)> 0 then true else false end from product p "
			+ " where p.product_color_id = :productColorId and p.removed = FALSE", nativeQuery = true)
	long existsProductByProductColorId(@Param("productColorId")Long productColorId);
	
	@Query(value = "select case when count(1)> 0 then true else false end from product p "
			+ " where p.product_memory_id = :productMemoryId and p.removed = FALSE", nativeQuery = true)
	long existsProductByProductMemoryId(@Param("productMemoryId")Long productMemoryId);
	
	@Query(value = "select case when count(1)> 0 then true else false end from product p "
			+ " where p.product_storage_id = :productStorageId and p.removed = FALSE", nativeQuery = true)
	long existsProductByProductStorageId(@Param("productStorageId")Long productStorageId);

	@Query(value = "select case when count(1)> 0 then true else false end from product p "
			+ " where p.product_screensize_id = :productScreenSizeId and p.removed = FALSE", nativeQuery = true)
	long existsProductByProductScreenSizeId(@Param("productScreenSizeId")Long productScreenSizeId);

	@Query(value = "select case when count(1)> 0 then true else false end from product p "
			+ " where p.product_taxrate_id = :productTaxRateId and p.removed = FALSE", nativeQuery = true)
	long existsProductByProductTaxRateId(@Param("productTaxRateId")Long productTaxRateId);
	
}
