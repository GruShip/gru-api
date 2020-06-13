package com.tech.dream.db.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tech.dream.db.entity.AccessModule;

@Repository
public interface AccessModuleRepository extends JpaRepository<AccessModule, Long>{
	
	@Query("select case when count(am)> 0 then true else false end from AccessModule am where lower(am.name) = lower(:name) and am.removed = FALSE")
	boolean existsByName(@Param("name") String name);
	
	@Query("select case when count(am)> 0 then true else false end from AccessModule am where am.id = (:id) and am.removed = FALSE")
	boolean existsByAccessModuleId(@Param("id") Long id);
	
	@Query("select case when count(am)> 0 then true else false end from AccessModule am where lower(am.name) = lower(:name) and am.id <> (:id) and am.removed = FALSE")
	long existsByNameAndNotId(@Param("name") String name, @Param("id") Long id);

	@Modifying
	@Query(value = "UPDATE accessmodule am set code =:code, name =:name,`desc`=:desc, updated_date= now() where am.id = :id", nativeQuery = true)
	void update(@Param("id")Long id, @Param("code")String code, @Param("name")String name, @Param("desc")String desc);
	
	@Modifying
	@Query(value = "UPDATE accessmodule set removed=1, updated_date= now() where id = :id", nativeQuery = true)
	void delete(@Param("id")Long id);

	@Query(value = "select am.id, am.name, am.code, am.`desc`, am.type, am.is_admin_module, am.is_client_module "
			+ " from accessmodule am "
			+ " where am.removed = FALSE and (am.is_admin_module=:isAdminModule or am.is_client_module=:isClientModule)", nativeQuery = true)
	List<Object[]> findAllAccessModuleData(@Param("isAdminModule") Boolean isAdminModule, @Param("isClientModule") Boolean isClientModule);
	
	@Query(value = "select am.id, am.name, am.code, am.`desc`, am.type, am.is_admin_module, am.is_client_module "
			+ " from accessmodule am "
			+ " where am.removed = FALSE and am.id = (:id) order by am.id desc", nativeQuery = true)
	List<Object[]> findAccessModuleDataById(@Param("id") Long id);
}
