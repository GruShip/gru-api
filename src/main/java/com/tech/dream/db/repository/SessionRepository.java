package com.tech.dream.db.repository;

import java.util.Date;

import javax.websocket.server.PathParam;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.tech.dream.db.entity.Session;

@Repository
public interface SessionRepository extends JpaRepository<Session, String>{

	@Query(
			  value = "select s.value "
			  		+ "from session s "
			  		+ "where s.id = ?1 and s.expiry_date > ?2 and s.removed = FALSE ", 
			  nativeQuery = true)
	Object[] findByTokenAndExpiryDate(String token, Date currentDate);

	
	@Modifying
	@Query(value = "UPDATE session s set removed=1 where s.id = :id", nativeQuery = true)
	void deleteSessionById(@PathParam("id") String id);

}
