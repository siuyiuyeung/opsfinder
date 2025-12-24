package com.igsl.opsfinder.service;

import com.igsl.opsfinder.entity.ExcelFile;
import com.igsl.opsfinder.exception.UnauthorizedException;
import com.igsl.opsfinder.repository.ExcelFileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * Service for Excel file security and permission checks.
 * Used in @PreAuthorize annotations for fine-grained access control.
 */
@Service("excelSecurityService")
@RequiredArgsConstructor
@Slf4j
public class ExcelSecurityService {

    private final ExcelFileRepository excelFileRepository;

    /**
     * Check if the user is the owner of the Excel file.
     * Used in @PreAuthorize expressions for ownership-based access control.
     *
     * @param fileId the file ID
     * @param username the username to check
     * @return true if user is the owner
     */
    public boolean isOwner(Long fileId, String username) {
        if (fileId == null || username == null) {
            return false;
        }

        return excelFileRepository.findById(fileId)
                .map(file -> file.getUploadedBy().equals(username))
                .orElse(false);
    }

    /**
     * Check if the user has permission to delete the file.
     * ADMIN can delete any file, OPERATOR can delete own files only.
     *
     * @param fileId the file ID
     * @param username the current username
     * @param roles the user's roles
     * @throws UnauthorizedException if user doesn't have permission
     */
    public void checkDeletePermission(Long fileId, String username, Set<String> roles) {
        // ADMIN can delete any file
        if (roles.contains("ROLE_ADMIN")) {
            log.debug("User {} has ADMIN role - delete permission granted for file {}", username, fileId);
            return;
        }

        // OPERATOR can delete own files
        if (roles.contains("ROLE_OPERATOR")) {
            if (isOwner(fileId, username)) {
                log.debug("User {} is owner of file {} - delete permission granted", username, fileId);
                return;
            } else {
                log.warn("User {} attempted to delete file {} without ownership", username, fileId);
                throw new UnauthorizedException("You don't have permission to delete this file");
            }
        }

        // USER and other roles cannot delete
        log.warn("User {} with roles {} attempted to delete file {} - permission denied", username, roles, fileId);
        throw new UnauthorizedException("You don't have permission to delete files");
    }

    /**
     * Check if the user has permission to upload files.
     * ADMIN and OPERATOR can upload, USER cannot.
     *
     * @param roles the user's roles
     * @return true if user can upload
     */
    public boolean canUpload(Set<String> roles) {
        return roles.contains("ROLE_ADMIN") || roles.contains("ROLE_OPERATOR");
    }
}
