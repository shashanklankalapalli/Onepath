package com.onepath.repository;

import com.onepath.entity.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {

    Optional<Service> findBySlug(String slug);

    boolean existsBySlug(String slug);

    List<Service> findByCategoryIgnoreCase(String category);

    @Query("SELECT s FROM Service s WHERE " +
           "LOWER(s.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(s.category) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(s.description) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Service> searchServices(@Param("query") String query);
}
