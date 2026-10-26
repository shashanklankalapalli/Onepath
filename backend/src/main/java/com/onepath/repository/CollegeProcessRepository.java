package com.onepath.repository;

import com.onepath.entity.CollegeProcess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CollegeProcessRepository extends JpaRepository<CollegeProcess, Long> {

    List<CollegeProcess> findByCollegeIdOrderByIdAsc(Long collegeId);

    List<CollegeProcess> findByCollegeIdAndCategoryIgnoreCase(Long collegeId, String category);

    Optional<CollegeProcess> findByIdAndCollegeId(Long id, Long collegeId);
}
