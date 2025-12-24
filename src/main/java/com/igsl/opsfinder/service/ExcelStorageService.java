package com.igsl.opsfinder.service;

import com.igsl.opsfinder.exception.BadRequestException;
import com.igsl.opsfinder.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Service for managing Excel file storage on disk.
 * Stores files in a year/month directory structure with UUID-based filenames.
 */
@Service
@Slf4j
public class ExcelStorageService {

    private final Path baseStorageDirectory;

    public ExcelStorageService(
            @Value("${excel.storage.base-directory:./data/excel-files}") String baseDirectory) {
        this.baseStorageDirectory = Paths.get(baseDirectory).toAbsolutePath().normalize();
        initializeStorageDirectory();
    }

    /**
     * Initialize the base storage directory.
     */
    private void initializeStorageDirectory() {
        try {
            Files.createDirectories(baseStorageDirectory);
            log.info("Excel storage directory initialized at: {}", baseStorageDirectory);
        } catch (IOException e) {
            log.error("Failed to create storage directory: {}", baseStorageDirectory, e);
            throw new RuntimeException("Could not create storage directory", e);
        }
    }

    /**
     * Store an Excel file on disk.
     * Creates a UUID-based filename in a YYYY/MM directory structure.
     *
     * @param file the file to store
     * @param originalFilename the original filename for logging
     * @return the full file path where the file was stored
     * @throws BadRequestException if file storage fails
     */
    public String storeFile(MultipartFile file, String originalFilename) {
        log.info("Storing Excel file: {}", originalFilename);

        try {
            // Generate UUID-based filename
            String storedFilename = generateStoredFilename();

            // Create year/month directory structure
            LocalDate now = LocalDate.now();
            Path yearMonthDirectory = baseStorageDirectory
                    .resolve(String.valueOf(now.getYear()))
                    .resolve(String.format("%02d", now.getMonthValue()));

            Files.createDirectories(yearMonthDirectory);

            // Full file path
            Path targetPath = yearMonthDirectory.resolve(storedFilename);

            // Copy file to target location
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            String filePath = targetPath.toString();
            log.info("Successfully stored file: {} -> {}", originalFilename, filePath);

            return filePath;

        } catch (IOException e) {
            log.error("Failed to store file: {}", originalFilename, e);
            throw new BadRequestException("Failed to store file: " + e.getMessage());
        }
    }

    /**
     * Delete a file from disk.
     * Handles missing files gracefully (logs warning but doesn't throw exception).
     *
     * @param filePath the full path to the file
     */
    public void deleteFile(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            log.warn("Attempted to delete file with null or empty path");
            return;
        }

        try {
            Path path = Paths.get(filePath);

            if (Files.exists(path)) {
                Files.delete(path);
                log.info("Successfully deleted file: {}", filePath);

                // Try to clean up empty parent directories (year/month)
                cleanupEmptyDirectories(path.getParent());
            } else {
                log.warn("File not found for deletion: {}", filePath);
            }

        } catch (IOException e) {
            log.error("Failed to delete file: {}", filePath, e);
            // Don't throw exception - log and continue
        }
    }

    /**
     * Retrieve a file from disk.
     *
     * @param filePath the full path to the file
     * @return File object
     * @throws ResourceNotFoundException if file doesn't exist
     */
    public File retrieveFile(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            throw new ResourceNotFoundException("File path is null or empty");
        }

        Path path = Paths.get(filePath);

        if (!Files.exists(path)) {
            log.error("File not found: {}", filePath);
            throw new ResourceNotFoundException("File not found: " + filePath);
        }

        if (!Files.isReadable(path)) {
            log.error("File is not readable: {}", filePath);
            throw new ResourceNotFoundException("File is not readable: " + filePath);
        }

        return path.toFile();
    }

    /**
     * Check if a file exists at the given path.
     *
     * @param filePath the full path to the file
     * @return true if file exists and is readable
     */
    public boolean fileExists(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return false;
        }

        Path path = Paths.get(filePath);
        return Files.exists(path) && Files.isReadable(path);
    }

    /**
     * Get the size of a file in bytes.
     *
     * @param filePath the full path to the file
     * @return file size in bytes
     * @throws ResourceNotFoundException if file doesn't exist
     */
    public long getFileSize(String filePath) {
        try {
            Path path = Paths.get(filePath);
            return Files.size(path);
        } catch (IOException e) {
            throw new ResourceNotFoundException("Failed to get file size: " + filePath);
        }
    }

    /**
     * Generate a unique filename with UUID and .xlsx extension.
     *
     * @return generated filename (e.g., "a1b2c3d4-e5f6-7890-abcd-ef1234567890.xlsx")
     */
    private String generateStoredFilename() {
        return UUID.randomUUID().toString() + ".xlsx";
    }

    /**
     * Clean up empty parent directories after file deletion.
     * Removes year/month directories if they become empty.
     *
     * @param directory the directory to check and clean
     */
    private void cleanupEmptyDirectories(Path directory) {
        if (directory == null || !Files.isDirectory(directory)) {
            return;
        }

        try {
            // Check if directory is empty
            if (Files.list(directory).findAny().isEmpty()) {
                Files.delete(directory);
                log.debug("Deleted empty directory: {}", directory);

                // Recursively clean up parent (year directory)
                Path parent = directory.getParent();
                if (parent != null && !parent.equals(baseStorageDirectory)) {
                    cleanupEmptyDirectories(parent);
                }
            }
        } catch (IOException e) {
            log.warn("Failed to clean up directory: {}", directory, e);
            // Don't throw - this is best-effort cleanup
        }
    }

    /**
     * Get the base storage directory path.
     *
     * @return base storage directory
     */
    public Path getBaseStorageDirectory() {
        return baseStorageDirectory;
    }
}
