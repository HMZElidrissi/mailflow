package com.mailflow.templateservice.repository;

import com.mailflow.templateservice.domain.EmailTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailTemplateRepository extends JpaRepository<EmailTemplate, Long> {
    Optional<EmailTemplate> findByName(String name);

    boolean existsByName(String name);

    @Query(
            "SELECT e FROM EmailTemplate e WHERE "
                    + "LOWER(e.name) LIKE LOWER(CONCAT('%', :query, '%')) OR "
                    + "LOWER(e.subject) LIKE LOWER(CONCAT('%', :query, '%')) OR "
                    + "LOWER(e.content) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<EmailTemplate> searchTemplates(@Param("query") String query, Pageable pageable);
}