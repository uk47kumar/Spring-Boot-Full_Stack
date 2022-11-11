package com.smart.dao;

import com.smart.entity.Contact;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;



public interface ContactRepository extends JpaRepository<Contact,Integer> {

    // Pageable contains two object first is (current-page) and second is how many no. of contacts show to the user (contact per-page)
    @Query("from Contact c where c.user.id = :userId")
    public Page<Contact> findContactByUser(@Param("userId") int userId, Pageable pageable);
}
