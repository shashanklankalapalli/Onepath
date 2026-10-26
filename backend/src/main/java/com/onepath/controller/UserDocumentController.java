package com.onepath.controller;

import com.onepath.dto.UserDocumentDto;
import com.onepath.entity.UserDocument;
import com.onepath.security.UserPrincipal;
import com.onepath.service.UserDocumentService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping({"/api/documents", "/documents"})
public class UserDocumentController {

    private final UserDocumentService userDocumentService;

    public UserDocumentController(UserDocumentService userDocumentService) {
        this.userDocumentService = userDocumentService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserDocumentDto> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("documentName") String documentName,
            @RequestParam(value = "category", required = false) String category,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        UserDocumentDto created = userDocumentService.uploadDocument(
                userPrincipal.getId(),
                documentName,
                category,
                file
        );
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<UserDocumentDto>> getUserDocuments(
            @RequestParam(value = "category", required = false) String category,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        List<UserDocumentDto> list = userDocumentService.getUserDocuments(userPrincipal.getId(), category);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}/file")
    public ResponseEntity<Resource> getDocumentFile(
            @PathVariable Long id,
            @RequestParam(value = "download", defaultValue = "false") boolean download,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        UserDocument doc = userDocumentService.getDocumentForUser(userPrincipal.getId(), id);
        Resource resource = userDocumentService.loadFileAsResource(userPrincipal.getId(), id);

        String dispositionType = download ? "attachment" : "inline";
        String encodedFilename = URLEncoder.encode(doc.getOriginalFileName(), StandardCharsets.UTF_8).replace("+", "%20");

        MediaType mediaType;
        try {
            mediaType = MediaType.parseMediaType(doc.getContentType());
        } catch (Exception e) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, dispositionType + "; filename=\"" + doc.getOriginalFileName() + "\"; filename*=UTF-8''" + encodedFilename)
                .body(resource);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        userDocumentService.deleteDocument(userPrincipal.getId(), id);
        return ResponseEntity.noContent().build();
    }
}
