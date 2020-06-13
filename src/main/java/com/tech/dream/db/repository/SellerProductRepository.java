package com.tech.dream.db.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tech.dream.db.entity.SellerProduct;

@Repository
public interface SellerProductRepository extends JpaRepository<SellerProduct, Long>{

	@Query(value = "select case when count(1)> 0 then true else false end from sellerproduct sp "
			+ " inner join product p on p.id = sp.product_id "
			+ " inner join companybranch cb on cb.id = sp.company_branch_id "
			+ " where sp.product_id=:productId and sp.id=:companyBranchId and "
			+ " sp.removed = FALSE",  nativeQuery = true)
	int existsByProductIdAndCompanyBranchId(@Param("productId")Long productId, @Param("companyBranchId")Long companyBranchId);
	
	
	@Query(value = "select case when count(1)> 0 then true else false end from sellerproduct sp "
			+ " inner join product p on p.id = sp.product_id "
			+ " inner join companybranch cb on cb.id = sp.company_branch_id "
			+ " where sp.product_id=:productId and sp.id=:companyBranchId and "
			+ " and sp.id <> :id and sp.removed = FALSE",  nativeQuery = true)
	int existsByProductIdAndCompanyBranchIdAndNotId(@Param("productId")Long productId, @Param("companyBranchId")Long companyBranchId, @Param("id")Long id);
	
	@Modifying
	@Query(value = "UPDATE sellerproduct sp set `desc`=:desc, mrp_sell_price = :mrpSellPrice, dealer_sell_price = :dealerSellPrice, wholesale_sell_price = :wholesaleSellPrice, name=:name, tax_percentage=:taxPercentage, mrp_product_coupon_id=:mrpProductCouponId, dealer_product_coupon_id=:dealerProductCouponId, wholesale_product_coupon_id=:wholesaleProductCouponId, updated_date= now() where sp.id = :id", nativeQuery = true)
	void update(@Param("id") Long id, @Param("desc") String desc, @Param("mrpSellPrice") Double mrpSellPrice, @Param("dealerSellPrice") Double dealerSellPrice, @Param("wholesaleSellPrice") Double wholesaleSellPrice, @Param("name") String name, @Param("taxPercentage") Double taxPercentage,@Param("mrpProductCouponId") Long mrpProductCouponId,@Param("dealerProductCouponId") Long dealerProductCouponId,@Param("wholesaleProductCouponId") Long wholesaleProductCouponId);
	
	@Modifying
	@Query(value = "UPDATE sellerproduct set removed=1, updated_date= now()  where id = :id", nativeQuery = true)
	void delete(@Param("id")Long id);
	
	@Query(value = "select case when count(1)> 0 then true else false end from sellerproduct sp where sp.id = :id and sp.removed = FALSE", nativeQuery = true)
	long existsBySellerProductId(@Param("id")Long id);

	@Query(value = "select case when count(1)> 0 then true else false end from sellerproduct sp inner join companybranch cb on cb.id = sp.company_branch_id where lower(sp.name) = lower(:name) and cb.company_id=(select company_id from companybranch where id=:companyBranchId) and sp.removed = FALSE ", nativeQuery = true)
	long existsSPByNameAndCompanyId(@Param("name")String name, @Param("companyBranchId") Long companyBranchId);
	
	@Query(value = "select case when count(1)> 0 then true else false end from sellerproduct sp inner join companybranch cb on cb.id = sp.company_branch_id where lower(sp.name) = lower(:name) and sp.id <> :id and cb.company_id=(select company_id from companybranch where id=:companyBranchId) and sp.removed = FALSE ", nativeQuery = true)
	long existsSPByNameAndCompanyIdAndNotId(@Param("name")String name, @Param("companyBranchId") Long companyBranchId, @Param("id") Long id);

	@Modifying
	@Query(value = "UPDATE sellerproduct sp set active=:active, updated_date= now() where sp.id = :id", nativeQuery = true)
	void updateStatus(@Param("id")Long id, @Param("active")Boolean active);

	@Modifying
	@Query(value = "UPDATE sellerproduct sp set is_approved=:isApproved, updated_date= now() where sp.id = :id", nativeQuery = true)
	void updateApproved(@Param("id")Long id, @Param("isApproved") Boolean isApproved);

	@Modifying
	@Query(value = "UPDATE sellerproduct sp set available_qty= available_qty + (:qty), updated_date= now() where sp.id = :id", nativeQuery = true)
	void updateQuantity(@Param("id")Long id, @Param("qty")int qty);


	@Query(value = "select case when count(1)> 0 then true else false end from sellerproduct sp "
			+ " where sp.product_id = :productId and sp.removed = FALSE", nativeQuery = true)
	long existsSellerProductByProductId(@Param("productId")Long productId);
	
	@Query(value = "select case when count(1)> 0 then true else false end from sellerproduct sp "
			+ " where sp.company_branch_id = :companyBranchId and sp.removed = FALSE", nativeQuery = true)
	long existsSellerProductByCompanyBranchId(@Param("companyBranchId")Long companyBranchId);
	
	@Query(value = "select case when count(1)> 0 then true else false end from sellerproduct sp "
			+ " where (sp.mrp_product_coupon_id = :productCouponId or sp.dealer_product_coupon_id = :productCouponId or sp.wholesale_product_coupon_id = :productCouponId) and sp.removed = FALSE", nativeQuery = true)
	long existsSellerProductByProductCouponId(@Param("productCouponId")Long productCouponId);
	
	//-----------
	
}
