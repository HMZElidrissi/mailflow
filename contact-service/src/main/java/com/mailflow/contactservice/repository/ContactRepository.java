package com.mailflow.contactservice.repository;

import com.mailflow.contactservice.domain.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {

  Optional<Contact> findByEmail(String email);

  boolean existsByEmail(String email);

  @Query("SELECT c FROM Contact c WHERE :tag MEMBER OF c.tags")
  List<Contact> findByTag(@Param("tag") String tag);

  @Query("SELECT c FROM Contact c WHERE c.tags IN :tags")
  List<Contact> findByTags(@Param("tags") Set<String> tags);

  @Query(
      "SELECT DISTINCT c FROM Contact c LEFT JOIN c.tags t WHERE "
          + "LOWER(c.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR "
          + "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :query, '%')) OR "
          + "LOWER(c.email) LIKE LOWER(CONCAT('%', :query, '%')) OR "
          + "LOWER(t) LIKE LOWER(CONCAT('%', :query, '%'))")
  List<Contact> searchContacts(@Param("query") String query);
}
