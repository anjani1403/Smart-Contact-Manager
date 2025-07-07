package com.smart.dao;

import org.springframework.data.domain.Pageable;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smart.entities.Contact;
import com.smart.entities.User;

public interface ContactRepository extends JpaRepository<Contact, Integer>{
	//pagination....
	
	@Query("from Contact as c where c.user.id =:userId")
	//currentPage - page
	//contactPerPage - 5
	public Page<Contact> findContactsByUser(@Param("userId")int UserId, Pageable pageable);
	
	//search functionality method
	public List<Contact> findByNameContainingAndUser(String name, User user);
	
	public Page<Contact> findByUserOrderByNameAsc(User user, Pageable pageable);
}
