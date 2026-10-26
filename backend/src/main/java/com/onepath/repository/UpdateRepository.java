package com.onepath.repository;

import com.onepath.entity.Update;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UpdateRepository extends JpaRepository<Update, Long> {
    List<Update> findAllByOrderByCreatedAtDesc();
    List<Update> findByCategoryIgnoreCaseOrderByCreatedAtDesc(String category);
    List<Update> findByUrgentTrueOrderByCreatedAtDesc();
    boolean existsByTitle(String title);
}
