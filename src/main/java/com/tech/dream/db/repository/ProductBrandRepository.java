package com.tech.dream.db.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tech.dream.db.entity.ProductBrand;

@Repository
public interface ProductBrandRepository extends JpaRepository<ProductBrand, Long>{
	
	@Query(value = "select case when count(1)> 0 then true else false end from productbrand pb where lower(pb.name) = lower(:name) and pb.removed = FALSE", 
			  nativeQuery = true)
	long existsProductBrandByName(@Param("name") String name);
	
	@Query(value = "select case when count(1)> 0 then true else false end from productbrand pb where lower(pb.name) = lower(:name) and pb.id <> :id and pb.removed = FALSE", 
			  nativeQuery = true)
	long existsProductBrandByNameAndNotId(@Param("name") String name, @Param("id") Long id);

	@Modifying
	@Query(value = "UPDATE productbrand pb set name =:name, company=:company, `desc`=:desc, active=:active, updated_date= now() where pb.id = :id",
            nativeQuery = true)
	void updateProductBrand(@Param("id")Long id, @Param("name")String name, @Param("company")String company, @Param("active")Boolean active, @Param("desc")String desc);
	
	@Modifying
	@Query(value = "UPDATE productbrand set removed=1, updated_date= now() where id = :id", nativeQuery = true)
	void deleteProductBrand(@Param("id")Long id);
	
	@Query(value = "select case when count(1)> 0 then true else false end from productbrand pb where pb.id = :id and pb.removed = FALSE", 
			  nativeQuery = true)
	long existsProductBrandById(@Param("id")Long id);
	
	@Query(value = "select pb.id, pb.name, pb.company, pb.`desc`, pb.active "
			+ " from productbrand pb "
			+ " where pb.removed = FALSE order by pb.id desc", nativeQuery = true)
	List<Object[]> findAllProductBrandData();
	
	@Query(value = "select pb.id, pb.name, pb.company, pb.`desc`, pb.active "
			+ " from productbrand pb "
			+ " where pb.removed = FALSE and pb.id=:id order by pb.id desc", nativeQuery = true)
	List<Object[]> findProductBrandDataById(@Param("id") Long id);
	
}
