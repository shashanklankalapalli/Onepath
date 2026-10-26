package com.onepath.repository;

import com.onepath.entity.CollegeProcessUpdate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CollegeProcessUpdateRepository extends JpaRepository<CollegeProcessUpdate, Long> {
    List<CollegeProcessUpdate> findByProcessIdOrderByEffectiveDateDesc(Long processId);
}
