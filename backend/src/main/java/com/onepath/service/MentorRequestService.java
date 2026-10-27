package com.onepath.service;

import com.onepath.dto.MentorRequestCreateDto;
import com.onepath.dto.MentorRequestResponseDto;
import com.onepath.dto.MentorStatusUpdateDto;
import com.onepath.entity.MentorRequest;
import com.onepath.entity.MentorRequestStatus;
import com.onepath.exception.BadRequestException;
import com.onepath.exception.ResourceNotFoundException;
import com.onepath.repository.MentorRequestRepository;
import com.onepath.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MentorRequestService {

    private static final Logger log = LoggerFactory.getLogger(MentorRequestService.class);
    private static final String ID_ALPHABET = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final MentorRequestRepository mentorRequestRepository;

    public MentorRequestService(MentorRequestRepository mentorRequestRepository) {
        this.mentorRequestRepository = mentorRequestRepository;
    }

    @Transactional
    public MentorRequestResponseDto createRequest(MentorRequestCreateDto dto, UserPrincipal currentUser) {
        if (dto == null) {
            throw new BadRequestException("Request payload cannot be empty");
        }

        String contactMethod = dto.getPreferredContactMethod() != null ? dto.getPreferredContactMethod().trim() : "";
        String lowerMethod = contactMethod.toLowerCase();

        boolean requiresPhone = lowerMethod.contains("phone") || lowerMethod.contains("call") || lowerMethod.contains("whatsapp");
        if (requiresPhone && (dto.getPhoneNumber() == null || dto.getPhoneNumber().trim().isEmpty())) {
            throw new BadRequestException("Phone number is required when preferred contact method is " + contactMethod);
        }

        String requestId = generateUniqueRequestId();
        Long userId = (currentUser != null) ? currentUser.getId() : null;

        MentorRequest entity = new MentorRequest(
                requestId,
                userId,
                dto.getFullName().trim(),
                dto.getEmail().trim(),
                dto.getDepartmentOrProcess().trim(),
                dto.getProblemDescription().trim(),
                dto.getDesiredOutcome().trim(),
                contactMethod,
                dto.getPhoneNumber() != null ? dto.getPhoneNumber().trim() : null,
                dto.getAdditionalInfo() != null ? dto.getAdditionalInfo().trim() : null
        );

        MentorRequest saved = mentorRequestRepository.save(entity);
        log.info("New mentor assistance request created with ID: {} for email: {}", saved.getRequestId(), saved.getEmail());

        return new MentorRequestResponseDto(saved);
    }

    @Transactional(readOnly = true)
    public List<MentorRequestResponseDto> getAllRequests(String search, MentorRequestStatus status, String process) {
        String cleanSearch = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
        String cleanProcess = (process != null && !process.trim().isEmpty() && !process.equalsIgnoreCase("ALL")) ? process.trim() : null;

        List<MentorRequest> list = mentorRequestRepository.searchRequests(cleanSearch, status, cleanProcess);
        return list.stream().map(MentorRequestResponseDto::new).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MentorRequestResponseDto getRequestById(Long id, UserPrincipal currentUser) {
        MentorRequest request = mentorRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mentor request not found with ID: " + id));

        if (currentUser != null) {
            boolean isAdmin = currentUser.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN") || a.getAuthority().equals("ROLE_COLLEGE_ADMIN"));
            if (!isAdmin && (request.getUserId() == null || !request.getUserId().equals(currentUser.getId()))) {
                throw new AccessDeniedException("You are not authorized to view this mentor request");
            }
        }

        return new MentorRequestResponseDto(request);
    }

    @Transactional
    public MentorRequestResponseDto updateStatus(Long id, MentorStatusUpdateDto dto) {
        if (dto == null || dto.getStatus() == null) {
            throw new BadRequestException("Valid status is required");
        }

        MentorRequest request = mentorRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mentor request not found with ID: " + id));

        request.setStatus(dto.getStatus());
        if (dto.getAdminNotes() != null) {
            request.setAdminNotes(dto.getAdminNotes().trim());
        }

        MentorRequest updated = mentorRequestRepository.save(request);
        log.info("Mentor request {} status updated to {}", updated.getRequestId(), updated.getStatus());

        return new MentorRequestResponseDto(updated);
    }

    @Transactional(readOnly = true)
    public List<MentorRequestResponseDto> getMyRequests(Long userId) {
        if (userId == null) {
            throw new BadRequestException("User ID is required to fetch personal requests");
        }
        return mentorRequestRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(MentorRequestResponseDto::new)
                .collect(Collectors.toList());
    }

    private String generateUniqueRequestId() {
        for (int attempts = 0; attempts < 10; attempts++) {
            StringBuilder sb = new StringBuilder("MNT-");
            for (int i = 0; i < 6; i++) {
                sb.append(ID_ALPHABET.charAt(RANDOM.nextInt(ID_ALPHABET.length())));
            }
            String candidate = sb.toString();
            if (!mentorRequestRepository.existsByRequestId(candidate)) {
                return candidate;
            }
        }
        return "MNT-" + System.currentTimeMillis() % 1000000;
    }
}
