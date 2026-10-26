package com.onepath.repository;

import com.onepath.entity.UserDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserDocumentRepository extends JpaRepository<UserDocument, Long> {

    List<UserDocument> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<UserDocument> findByUserIdAndCategoryOrderByCreatedAtDesc(Long userId, String category);

    Optional<UserDocument> findByIdAndUserId(Long id, Long userId);
}
