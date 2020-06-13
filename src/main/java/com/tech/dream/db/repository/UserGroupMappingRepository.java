package com.tech.dream.db.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tech.dream.db.entity.UserGroupMapping;

@Repository
public interface UserGroupMappingRepository extends JpaRepository<UserGroupMapping, Long>{
	
	@Query(
			  value = "select case when count(1)> 0 then true else false end from user u inner join usergroup ug on u.company_id = ug.company_id where u.id = ?1 and ug.id = ?2 and (1 = ?3 or u.company_id = ?3) and u.removed=FALSE and ug.removed = FALSE", 
			  nativeQuery = true)
	long isSameClientForUserIdAndUserGroupIdAndSession(Long userId, Long userGroupId, Long companyId);
	
	
	@Query(
			  value = "select case when count(1)> 0 then true else false end from usergroupmapping ugm where ugm.user_id = ?1 and ugm.user_group_id = ?2 and ugm.removed = FALSE", 
			  nativeQuery = true)
	long existsByUserIdAndUserGroupId(Long userId, Long userGroupId);
	
	@Query(
			  value = "select case when count(1)> 0 then true else false end from usergroupmapping ugm where ubm.user_id = ?1 and ugm.user_group_id = ?2 and ugm.id <> ?3 and ugm.removed = FALSE", 
			  nativeQuery = true)
	long existsByUserIdAndUserGroupIdAndNotId(Long userId, Long userGroupId, Long id);

	
	@Query(value = "select case when count(1)> 0 then true else false end from usergroupmapping ugm inner join user u on u.id = ugm.user_id where ugm.id = :id and (1 = :companyId or u.company_id = :companyId) and ugm.removed = FALSE",
			nativeQuery = true)
	long existsByUserGroupMappingId(@Param("id")Long id, @Param("companyId") Long companyId);
	
	
	@Query(value = "select case when count(1)> 0 then true else false end from usergroupmapping ugm where ugm.user_group_id = :userGroupId and ugm.removed = FALSE", 
			  nativeQuery = true)
	long existsUsersForUserGroup(@Param("userGroupId") Long id);
	
	@Modifying
	@Query(value = "UPDATE usergroupmapping set removed=TRUE, updated_date= now() where id = :id", nativeQuery = true)
	void delete(@Param("id")Long id);

	
	@Query(value = "select ugm.id, ugm.user_id, ugm.user_group_id, u.first_name, u.last_name, u.username, u.email, ug.name as user_group_name "
			+ " from usergroupmapping ugm inner join user u on u.id = ugm.user_id "
			+ " inner join usergroup ug on ug.id = ugm.user_group_id "
			+ " where ugm.removed = FALSE and u.company_id = (:companyId) order by ugm.id desc", nativeQuery = true)
	List<Object[]> findAllUserGroupMappingData(@Param("companyId")Long companyId);


	@Query(value = "select ugm.id, ugm.user_id, ugm.user_group_id, u.first_name, u.last_name, u.username, u.email, ug.name as user_group_name "
			+ " from usergroupmapping ugm inner join user u on u.id = ugm.user_id "
			+ " inner join usergroup ug on ug.id = ugm.user_group_id "
			+ " where ugm.removed = FALSE and ugm.id = :id and (1 = :companyId or u.company_id = (:companyId))", nativeQuery = true)
	List<Object[]> findUserGroupMappingDataByIdAndCompanyId(@Param("id") Long id, @Param("companyId") Long companyId);


	@Modifying
	@Query(value = "UPDATE usergroupmapping set removed=TRUE, updated_date= now() where user_id = :userId and user_group_id <> :userGroupId and removed=FALSE", nativeQuery = true)
	void removeUserGroupMappingApartFromGivenUserGroupId(@Param("userId")Long userId, @Param("userGroupId") Long userGroupId);

	@Modifying
	@Query(value = "UPDATE usergroupmapping set removed=TRUE, updated_date= now() where user_id = :userId and removed=FALSE", nativeQuery = true)
	void removeAllUserGroupMapping(@Param("userId")Long userId);
}
