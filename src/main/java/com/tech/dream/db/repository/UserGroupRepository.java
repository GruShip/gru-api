package com.tech.dream.db.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tech.dream.db.entity.UserGroup;

@Repository
public interface UserGroupRepository extends JpaRepository<UserGroup, Long>{
	
	@Query(value = "select case when count(1)> 0 then true else false end from usergroup ug where ug.id = ?1 and ug.company_id = ?2 and ug.removed = FALSE",  nativeQuery = true)
	long existsByIdAndCompanyId(Long id, Long companyId);

	@Query(value = "select case when count(1)> 0 then true else false end from usergroup ug where ug.company_id = ?2 and lower(ug.name) = lower(?1) and ug.removed = FALSE",  nativeQuery = true)
	long existsByNameAndCompanyId(String name, Long companyId);
	
	@Query(value = "select case when count(1)> 0 then true else false end from usergroup ug where ug.company_id = ?2 and lower(ug.name) = lower(?1) and ug.id <> ?3 and ug.removed = FALSE",  nativeQuery = true)
	long existsByNameAndCompanyIdAndNotId(String name, Long companyId, Long id);
	
	@Query(value = "select case when count(1)> 0 then true else false end from usergroup ug where ug.id = ?1 and ug.removed = FALSE", nativeQuery = true)
	long existsByUserGroupId(Long id);
	
	@Query(value = "select case when count(1)> 0 then true else false end from usergroup ug where ug.id = ?1 and ug.is_admin=true and ug.removed = FALSE", nativeQuery = true)
	long isAdminUserGroup(Long id);
	
	@Modifying
	@Query(value = "UPDATE usergroup ug set code =:code, name =:name,active=:active,`desc`=:desc, updated_date= now()  where ug.id = :id", nativeQuery = true)
	void update(@Param("id")Long id, @Param("code")String code, @Param("name")String name, @Param("active")Boolean active, @Param("desc")String desc);
	
	@Modifying
	@Query(value = "UPDATE usergroup set removed=1, updated_date= now()  where id = :id", nativeQuery = true)
	void delete(@Param("id")Long id);

	@Query(value = "select ug.id, ug.name, ug.code, ug.`desc`, ug.active, ug.company_id "
			+ " from usergroup ug "
			+ " where ug.removed = FALSE and ug.company_id = (:companyId) order by ug.id desc", nativeQuery = true)
	List<Object[]> findAllUserGroupData(@Param("companyId") Long companyId);

	@Query(value = "select ug.id, ug.name, ug.code, ug.`desc`, ug.active, ug.company_id, ug.is_admin "
			+ " from usergroup ug "
			+ " where ug.removed = FALSE and ug.id = (:id) and (1 = :companyId or ug.company_id = :companyId)", nativeQuery = true)
	List<Object[]> findUserGroupDataByIdAndCompanyId(@Param("id") Long id, @Param("companyId") Long companyId);

	@Query(value = "select case when count(1)> 0 then true else false end from usergroup ug where ug.company_id = :companyId and ug.removed = FALSE", nativeQuery = true)
	int existsUserGroupForCompany(@Param("companyId") Long companyId);

}
