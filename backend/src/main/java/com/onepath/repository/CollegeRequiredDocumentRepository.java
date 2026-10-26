package com.onepath.repository;

import com.onepath.entity.CollegeRequiredDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CollegeRequiredDocumentRepository extends JpaRepository<CollegeRequiredDocument, Long> {
    List<CollegeRequiredDocument> findByProcessId(Long processId);
}
