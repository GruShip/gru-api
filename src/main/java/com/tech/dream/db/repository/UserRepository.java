package com.tech.dream.db.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tech.dream.db.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long>{

	
	@Query(value = "select case when count(1)> 0 then true else false end from user u inner join company c on c.id = u.company_id where u.username = (?1) and u.removed = FALSE", 
			  nativeQuery = true)
	long existsByUserName(String userName);

	@Query(value = "select case when count(1)> 0 then true else false end from user u where u.id = (:id) and u.is_admin and u.removed = FALSE", 
			  nativeQuery = true)
	long isAdminUser(@Param("id")Long id);
	
	Optional<User> findById(Long id);

	@Query(value = "select u.type, u.id as userId, u.first_name as firstName, u.last_name as lastName, u.email, u.phone_number_1 as phoneNumber1, u.phone_number_2, u.company_id as companyId, ug.id as userGroupId, c.type as company_type "
			  		+ " from user u "
			  		+ " inner join company c on c.id=u.company_id and c.removed=FALSE "
			  		+ " left join usergroupmapping ugm on ugm.user_id=u.id and ugm.removed=FALSE "
			  		+ " left join usergroup ug on ug.id = ugm.user_group_id and ug.active = TRUE and ug.removed=FALSE "
			  		+ " where u.username = ?1 and u.password = ?2 and u.type = ?3 and u.removed = FALSE and u.active=TRUE ", 
			  nativeQuery = true)
	Object findGeneralUserByUsernameAndPassword(String userName, String password, String type);
	
	@Modifying
	@Query(value = "UPDATE user u set first_name =:firstName, last_name =:lastName, phone_number_1 =:phoneNumber1, phone_number_2=:phoneNumber2, email =:email, active=:active, primary_address_id = :primaryAddressId ,updated_date= now()  where u.id = :id", nativeQuery = true)
	void update(@Param("id")Long id, @Param("firstName")String firstName, @Param("lastName")String lastName, @Param("phoneNumber1")String phoneNumber1, @Param("phoneNumber2")String phoneNumber2, @Param("email")String email, @Param("active")Boolean active, @Param("primaryAddressId") Long primaryAddressId);
	
	@Modifying
	@Query(value = "UPDATE user set removed=1, updated_date= now()  where id = :id", nativeQuery = true)
	void delete(@Param("id")Long id);

	@Query(value = "select u.id, u.first_name, u.last_name, u.username, u.email, u.type, u.is_system_admin, u.phone_number_1, u.phone_number_2, a.id as address_id, a.address_line_1, a.address_line_2, a.city_id, a.state_id, a.country_id, a.pincode, a.address_type, ugm.user_group_id, u.company_id, u.is_admin "
			+ " from user u left join address a on a.id = u.primary_address_id "
			+ " left join usergroupmapping ugm on ugm.user_id = u.id and ugm.removed=FALSE "
			+ " where u.removed = FALSE and u.company_id = (:companyId) order by u.id desc", nativeQuery = true)
	List<Object[]> findAllUserData(@Param("companyId") Long companyId);

	@Query(value = "select u.id, u.first_name, u.last_name, u.username, u.email, u.type, u.is_system_admin, u.phone_number_1, u.phone_number_2, a.id as address_id, a.address_line_1, a.address_line_2, a.city_id, a.state_id, a.country_id, a.pincode, a.address_type, ugm.user_group_id, u.company_id, u.is_admin "
			+ " from user u left join address a on a.id = u.primary_address_id "
			+ " left join usergroupmapping ugm on ugm.user_id = u.id and ugm.removed=FALSE"
			+ " where u.removed = FALSE and u.id = (:id) ", nativeQuery = true)
	List<Object[]> findUserDataById(@Param("id") Long id);
	
	@Query(value = "select case when count(1)> 0 then true else false end from user u where u.id = (:id) and u.company_id = (:companyId) and u.removed = FALSE", nativeQuery = true)
	long existsByIdAndCompanyId(@Param("id") Long id, @Param("companyId") Long companyId);



}
