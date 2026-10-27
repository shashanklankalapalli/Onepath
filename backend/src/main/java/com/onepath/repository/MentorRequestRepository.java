package com.onepath.repository;

import com.onepath.entity.MentorRequest;
import com.onepath.entity.MentorRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MentorRequestRepository extends JpaRepository<MentorRequest, Long> {

    Optional<MentorRequest> findByRequestId(String requestId);

    boolean existsByRequestId(String requestId);

    List<MentorRequest> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT m FROM MentorRequest m WHERE " +
           "(:status IS NULL OR m.status = :status) AND " +
           "(:process IS NULL OR :process = '' OR LOWER(m.departmentOrProcess) LIKE LOWER(CONCAT('%', :process, '%'))) AND " +
           "(:search IS NULL OR :search = '' OR " +
           " LOWER(m.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(m.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(m.requestId) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(m.departmentOrProcess) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " (m.phoneNumber IS NOT NULL AND LOWER(m.phoneNumber) LIKE LOWER(CONCAT('%', :search, '%')))) " +
           "ORDER BY m.createdAt DESC")
    List<MentorRequest> searchRequests(@Param("search") String search,
                                       @Param("status") MentorRequestStatus status,
                                       @Param("process") String process);
}
