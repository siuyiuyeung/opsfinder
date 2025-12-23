package com.igsl.opsfinder.service;

import com.igsl.opsfinder.dto.csv.DeviceExportDto;
import com.igsl.opsfinder.dto.csv.DeviceImportDto;
import com.igsl.opsfinder.dto.csv.DeviceImportResult;
import com.igsl.opsfinder.dto.request.DeviceRequest;
import com.igsl.opsfinder.exception.BadRequestException;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for CSV parsing and generation operations.
 * Handles file upload validation, CSV parsing with error reporting,
 * and CSV generation from device data.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CsvService {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final String CSV_CONTENT_TYPE = "text/csv";

    private final Validator validator;
    private final DeviceService deviceService;

    /**
     * Parse CSV file and import devices.
     * Validates each row and provides detailed error reporting.
     *
     * @param file uploaded CSV file
     * @return import result with row-by-row success/failure details
     * @throws BadRequestException if file is invalid or empty
     */
    public DeviceImportResult importDevicesFromCsv(MultipartFile file) {
        validateFile(file);

        log.info("Starting CSV import - filename: {}, size: {} bytes",
                file.getOriginalFilename(), file.getSize());

        DeviceImportResult result = DeviceImportResult.builder()
                .totalRows(0)
                .successCount(0)
                .failureCount(0)
                .build();

        try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            CsvToBean<DeviceImportDto> csvToBean = new CsvToBeanBuilder<DeviceImportDto>(reader)
                    .withType(DeviceImportDto.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .withIgnoreEmptyLine(true)
                    .build();

            List<DeviceImportDto> devices = csvToBean.parse();
            result.setTotalRows(devices.size());

            log.info("Parsed {} rows from CSV", devices.size());

            // Process each row with validation and error handling
            for (int i = 0; i < devices.size(); i++) {
                int rowNumber = i + 2; // 1-indexed, accounting for header row
                DeviceImportDto deviceDto = devices.get(i);

                try {
                    // Validate DTO
                    Set<ConstraintViolation<DeviceImportDto>> violations =
                            validator.validate(deviceDto);

                    if (!violations.isEmpty()) {
                        String errors = violations.stream()
                                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                                .collect(Collectors.joining(", "));
                        result.addFailure(rowNumber, "Validation failed: " + errors, deviceDto);
                        continue;
                    }

                    // Convert to DeviceRequest and create device
                    DeviceRequest request = convertToDeviceRequest(deviceDto);
                    deviceService.createDevice(request);
                    result.addSuccess(rowNumber, deviceDto);

                } catch (Exception e) {
                    log.error("Failed to import row {}: {}", rowNumber, e.getMessage());
                    result.addFailure(rowNumber, e.getMessage(), deviceDto);
                }
            }

            log.info("CSV import completed - success: {}, failures: {}",
                    result.getSuccessCount(), result.getFailureCount());

        } catch (Exception e) {
            log.error("CSV parsing failed", e);
            throw new BadRequestException("Failed to parse CSV file: " + e.getMessage());
        }

        return result;
    }

    /**
     * Export devices to CSV format.
     *
     * @param devices list of export DTOs
     * @param writer output writer
     * @throws IOException if writing fails
     * @throws CsvDataTypeMismatchException if CSV data type mismatch occurs
     * @throws CsvRequiredFieldEmptyException if required CSV field is empty
     */
    public void exportDevicesToCsv(List<DeviceExportDto> devices, Writer writer)
            throws IOException, CsvDataTypeMismatchException, CsvRequiredFieldEmptyException {

        log.info("Exporting {} devices to CSV", devices.size());

        StatefulBeanToCsv<DeviceExportDto> beanToCsv = new StatefulBeanToCsvBuilder<DeviceExportDto>(writer)
                .withApplyQuotesToAll(false)
                .build();

        beanToCsv.write(devices);
        writer.flush();

        log.info("CSV export completed successfully");
    }

    /**
     * Validate uploaded file.
     *
     * @param file the file to validate
     * @throws BadRequestException if validation fails
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is empty or not provided");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException(
                    String.format("File size exceeds maximum allowed size of %d MB",
                            MAX_FILE_SIZE / 1024 / 1024));
        }

        String contentType = file.getContentType();
        String filename = file.getOriginalFilename();

        if (contentType == null ||
            (!contentType.equals(CSV_CONTENT_TYPE) &&
             !contentType.equals("application/vnd.ms-excel"))) {

            if (filename == null || !filename.toLowerCase().endsWith(".csv")) {
                throw new BadRequestException("File must be a CSV file");
            }
        }
    }

    /**
     * Convert DeviceImportDto to DeviceRequest.
     *
     * @param dto the import DTO
     * @return device request DTO
     */
    private DeviceRequest convertToDeviceRequest(DeviceImportDto dto) {
        return DeviceRequest.builder()
                .zone(dto.getZone())
                .username(dto.getUsername())
                .type(dto.getType())
                .remark(dto.getRemark())
                .location(dto.getLocation())
                .ip(dto.getIp())
                .hostname(dto.getHostname())
                .hardwareModel(dto.getHardwareModel())
                .datacenter(dto.getDatacenter())
                .accountType(dto.getAccountType())
                .passwordIndex(dto.getPasswordIndex())
                .build();
    }
}
