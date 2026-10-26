package com.onepath.repository;

import com.onepath.entity.CollegeProcessStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CollegeProcessStepRepository extends JpaRepository<CollegeProcessStep, Long> {
    List<CollegeProcessStep> findByProcessIdOrderByStepNumberAsc(Long processId);
}
