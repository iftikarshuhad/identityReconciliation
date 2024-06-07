package com.idendity.recon.repository;

import com.idendity.recon.model.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ContactRepository extends JpaRepository<Contact, String> {
    @Query(value = "select * from contact where email = :email order by created_at desc limit 1;", nativeQuery = true)
    Contact getEmailContact(@Param("email") String email);

    @Query(value = "select * from contact where phone_number = :phoneNumber order by created_at desc limit 1;", nativeQuery = true)
    Contact getPhoneContact(@Param("phoneNumber") String phoneNumber);

    @Query(value = "select * from contact where linked_id = :linkedId ;", nativeQuery = true)
    List<Contact> getAllSecondaryContact(@Param("linkedId") Integer linkedId);

    @Query(value = "select * from contact where id = :linkedId ;", nativeQuery = true)
    Contact getPrimaryContactByLinkedId(@Param("linkedId") Integer linkedId);

}
