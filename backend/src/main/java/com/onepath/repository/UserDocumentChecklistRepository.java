package com.onepath.repository;

import com.onepath.entity.UserDocumentChecklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserDocumentChecklistRepository extends JpaRepository<UserDocumentChecklist, Long> {

    List<UserDocumentChecklist> findByUserId(Long userId);

    List<UserDocumentChecklist> findByUserIdAndTargetType(Long userId, String targetType);

    List<UserDocumentChecklist> findByUserIdAndTargetTypeAndTargetId(Long userId, String targetType, Long targetId);

    Optional<UserDocumentChecklist> findByUserIdAndTargetTypeAndTargetIdAndDocumentName(Long userId, String targetType, Long targetId, String documentName);

    List<UserDocumentChecklist> findByUserIdAndIsMissingTrue(Long userId);

    List<UserDocumentChecklist> findByUserIdAndTargetTypeAndIsMissingTrue(Long userId, String targetType);
}
