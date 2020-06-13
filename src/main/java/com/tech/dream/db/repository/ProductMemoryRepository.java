package com.tech.dream.db.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tech.dream.db.entity.ProductMemory;

@Repository
public interface ProductMemoryRepository extends JpaRepository<ProductMemory, Long>{

	@Query(value = "select case when count(1)> 0 then true else false end from productmemory pme where lower(pme.name) = lower(:name) and pme.removed = FALSE", 
			  nativeQuery = true)
	long existsProductMemoryByName(@Param("name") String name);
	
	@Query(value = "select case when count(1)> 0 then true else false end from productmemory pme where lower(pme.name) = lower(:name) and pme.id <> :id and pme.removed = FALSE", 
			  nativeQuery = true)
	long existsProductMemoryByNameAndNotId(@Param("name") String name, @Param("id") Long id);

	@Modifying
	@Query(value = "UPDATE productmemory pme set name =:name, `desc`=:desc, active=:active, updated_date= now() where pme.id = :id",
          nativeQuery = true)
	void updateProductMemory(@Param("id")Long id, @Param("name")String name, @Param("active")Boolean active, @Param("desc")String desc);
	
	@Modifying
	@Query(value = "UPDATE productmemory set removed=1, updated_date= now() where id = :id", nativeQuery = true)
	void deleteProductMemory(@Param("id")Long id);
	
	@Query(value = "select case when count(1)> 0 then true else false end from productmemory pme where pme.id = :id and pme.removed = FALSE", 
			  nativeQuery = true)
	long existsProductMemoryById(@Param("id")Long id);
	
	@Query(value = "select pme.id, pme.name, pme.`desc`, pme.active "
			+ " from productmemory pme "
			+ " where pme.removed = FALSE order by pme.id desc", nativeQuery = true)
	List<Object[]> findAllProductMemoryData();
	
	@Query(value = "select pme.id, pme.name, pme.`desc`, pme.active "
			+ " from productmemory pme "
			+ " where pme.removed = FALSE and pme.id=:id order by pme.id desc", nativeQuery = true)
	List<Object[]> findProductMemoryDataById(@Param("id") Long id);
	
	
	
}
