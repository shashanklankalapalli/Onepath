package com.onepath.repository;

import com.onepath.entity.College;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CollegeRepository extends JpaRepository<College, Long> {

    @Query("SELECT c FROM College c WHERE c.active = true AND " +
           "(LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(c.shortName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(c.location) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<College> searchColleges(@Param("query") String query);

    List<College> findByActiveTrueOrderByNameAsc();
}
