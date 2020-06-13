package com.tech.dream.db.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tech.dream.db.entity.UserGroupAccessMapping;

@Repository
public interface UserGroupAccessMappingRepository extends JpaRepository<UserGroupAccessMapping, Long>{
	
	@Query(value = "select case when count(1)> 0 then true else false end from usergroupaccessmapping ugam inner join usergroup ug on ug.id = ugam.user_group_id where ugam.user_group_id = :userGroupId and ugam.access_module_id = :accessModuleId and (1 = :companyId or ug.company_id = :companyId) and ugam.removed = FALSE", 
			  nativeQuery = true)
	long existsByUserGroupIdAndAccessModuleIdAndSession(@Param("userGroupId")Long userGroupId,@Param("accessModuleId") Long accessModuleId,@Param("companyId") Long companyId);
	
	@Query(value = "select ugam.id from usergroupaccessmapping ugam inner join usergroup ug on ug.id = ugam.user_group_id where ugam.user_group_id = :userGroupId and ugam.access_module_id = :accessModuleId and (1 = :companyId or ug.company_id = :companyId) and ugam.removed = FALSE", 
			  nativeQuery = true)
	List<Object[]>getIdByUserGroupIdAndAccessModuleIdAndSession(@Param("userGroupId")Long userGroupId,@Param("accessModuleId") Long accessModuleId,@Param("companyId") Long companyId);
	
	@Query(value = "select case when count(1)> 0 then true else false end from usergroupaccessmapping ugam where ugam.user_group_id = ?1 and ugam.access_module_id = ?2 and ugam.id <> ?3 and ugam.removed = FALSE", 
			  nativeQuery = true)
	long existsByUserGroupIdAndAccessModuleIdAndNotId(Long userGroupId, Long accessModuleId, Long id);

	
	@Query(value = "select case when count(1)> 0 then true else false end from usergroupaccessmapping ugam inner join usergroup ug on ug.id = ugam.user_group_id where ugam.id = :id and (1 = :companyId or ug.company_id = :companyId) and ugam.removed = FALSE",
			nativeQuery = true)
	long existsByUserGroupAccessMappingIdAndSession(@Param("id")Long id, @Param("companyId") Long companyId);

	@Query(value = "select ugam.access_module_id, ugam.create_access, ugam.read_access, ugam.update_access, ugam.delete_access, am.name as access_module_name, am.code as access_module_code "
			+ " from usergroupaccessmapping ugam inner join accessmodule am on am.id = ugam.access_module_id where ugam.user_group_id = ?1 and ugam.removed = FALSE order by ugam.id desc",
			nativeQuery = true)
	List<Object[]> findAccessModuleForUserGroup(Long userGroupId);
	
	@Modifying
	@Query(value = "UPDATE usergroupaccessmapping set removed=TRUE, updated_date= now() where id = :id", nativeQuery = true)
	void delete(@Param("id")Long id);

	@Query(value = "select ugam.id, ugam.user_group_id, ugam.access_module_id, am.name as access_module_name, ug.name as user_group_name "
			+ " from usergroupaccessmapping ugam inner join accessmodule am on am.id = ugam.access_module_id "
			+ " inner join usergroup ug on ug.id = ugam.user_group_id "
			+ " where ugam.removed = FALSE and (1 = :companyId or ug.company_id = :companyId) order by ugam.id desc", nativeQuery = true)
	List<Object[]> findAllUserGroupAccessMappingData(@Param("companyId")Long companyId);

	@Query(value = "select ugam.id, ugam.user_group_id, ugam.access_module_id, am.name as access_module_name, ug.name as user_group_name "
			+ " from usergroupaccessmapping ugam inner join accessmodule am on am.id = ugam.access_module_id "
			+ " inner join usergroup ug on ug.id = ugam.user_group_id "
			+ " where ugam.removed = FALSE and ugam.id = :id and (1 = :companyId or ug.company_id = :companyId)", nativeQuery = true)
	List<Object[]> findUserGroupAccessMappingDataByIdAndCompanyId(@Param("id") Long id,@Param("companyId") Long companyId);

	@Modifying
	@Query(value = "UPDATE usergroupaccessmapping set removed=TRUE, updated_date= now() where user_group_id = :userGroupId and access_module_id not in (:accessModuleIdList) and removed=FALSE", nativeQuery = true)
	void removeUserGroupAccessMappingApartFromGivenAccessModuleIdList(@Param("userGroupId") Long userGroupId, @Param("accessModuleIdList") List<Long> accessModuleIdList);

	@Query(value = "select ugam.access_module_id, ugam.user_group_id,ugam.create_access, ugam.read_access, ugam.update_access, ugam.delete_access, am.name, am.type  "
			+ " from usergroupaccessmapping ugam "
			+ " inner join accessmodule am on am.id = ugam.access_module_id "
			+ " where ugam.removed = FALSE and ugam.user_group_id = :userGroupId order by ugam.id desc", nativeQuery = true)
	List<Object[]> findIdListByUserGroupId(@Param("userGroupId")Long userGroupId);

	@Modifying
	@Query(value = "UPDATE usergroupaccessmapping set removed=TRUE, updated_date= now() where user_group_id = :userGroupId and removed=FALSE", nativeQuery = true)
	void removeAllUserGroupAccessMapping(@Param("userGroupId")Long userGroupId);
}
