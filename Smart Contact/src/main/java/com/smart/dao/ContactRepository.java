package com.smart.dao;

import com.smart.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ContactRepository extends JpaRepository<Contact,Integer> {

    @Query("from Contact c where c.user.id = :userId")
    public List<Contact> findContactByUser(@Param("userId") int userId);
}
