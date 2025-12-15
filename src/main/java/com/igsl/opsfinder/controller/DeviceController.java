package com.igsl.opsfinder.controller;

import com.igsl.opsfinder.dto.request.DeviceRequest;
import com.igsl.opsfinder.dto.response.DeviceResponse;
import com.igsl.opsfinder.service.DeviceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for device management operations.
 * Endpoints are protected with role-based access control.
 */
@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
@Slf4j
public class DeviceController {

    private final DeviceService deviceService;

    /**
     * Search devices by keyword using full-text search.
     * Results are ordered by relevance (ts_rank) from PostgreSQL full-text search.
     * Accessible by all authenticated users.
     *
     * @param q search term
     * @param page page number (0-indexed)
     * @param size page size
     * @return page of device responses ordered by relevance
     */
    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<DeviceResponse>> searchDevices(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("Search devices request - term: {}, page: {}, size: {}", q, page, size);

        Pageable pageable = PageRequest.of(page, size);

        Page<DeviceResponse> devices = deviceService.searchDevices(q, pageable);
        return ResponseEntity.ok(devices);
    }

    /**
     * Get all devices with pagination and filtering.
     * Accessible by all authenticated users.
     *
     * @param zone optional zone filter
     * @param type optional type filter
     * @param page page number (0-indexed)
     * @param size page size
     * @param sort sort field and direction
     * @return page of device responses
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<DeviceResponse>> getAllDevices(
            @RequestParam(required = false) String zone,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id,desc") String sort) {

        log.info("Get all devices request - zone: {}, type: {}, page: {}, size: {}", zone, type, page, size);

        String[] sortParams = sort.split(",");
        Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc")
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));

        Page<DeviceResponse> devices;
        if (zone != null && type != null) {
            devices = deviceService.getDevicesByZoneAndType(zone, type, pageable);
        } else if (zone != null) {
            devices = deviceService.getDevicesByZone(zone, pageable);
        } else if (type != null) {
            devices = deviceService.getDevicesByType(type, pageable);
        } else {
            devices = deviceService.getAllDevices(pageable);
        }

        return ResponseEntity.ok(devices);
    }

    /**
     * Get device by ID.
     * Accessible by all authenticated users.
     *
     * @param id device ID
     * @return device response
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DeviceResponse> getDeviceById(@PathVariable Long id) {
        log.info("Get device by ID request - id: {}", id);
        DeviceResponse device = deviceService.getDeviceById(id);
        return ResponseEntity.ok(device);
    }

    /**
     * Get distinct zones for filtering.
     * Accessible by all authenticated users.
     *
     * @return list of unique zone names
     */
    @GetMapping("/filters/zones")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<String>> getDistinctZones() {
        log.info("Get distinct zones request");
        List<String> zones = deviceService.getDistinctZones();
        return ResponseEntity.ok(zones);
    }

    /**
     * Get distinct types for filtering.
     * Accessible by all authenticated users.
     *
     * @return list of unique device types
     */
    @GetMapping("/filters/types")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<String>> getDistinctTypes() {
        log.info("Get distinct types request");
        List<String> types = deviceService.getDistinctTypes();
        return ResponseEntity.ok(types);
    }

    /**
     * Get distinct datacenters for filtering.
     * Accessible by all authenticated users.
     *
     * @return list of unique datacenter names
     */
    @GetMapping("/filters/datacenters")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<String>> getDistinctDatacenters() {
        log.info("Get distinct datacenters request");
        List<String> datacenters = deviceService.getDistinctDatacenters();
        return ResponseEntity.ok(datacenters);
    }

    /**
     * Create a new device.
     * Accessible by ADMIN and OPERATOR roles only.
     *
     * @param request device creation request
     * @return created device response
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<DeviceResponse> createDevice(@Valid @RequestBody DeviceRequest request) {
        log.info("Create device request - zone: {}, type: {}", request.getZone(), request.getType());
        DeviceResponse device = deviceService.createDevice(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(device);
    }

    /**
     * Update an existing device.
     * Accessible by ADMIN and OPERATOR roles only.
     *
     * @param id device ID
     * @param request device update request
     * @return updated device response
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<DeviceResponse> updateDevice(
            @PathVariable Long id,
            @Valid @RequestBody DeviceRequest request) {

        log.info("Update device request - id: {}", id);
        DeviceResponse device = deviceService.updateDevice(id, request);
        return ResponseEntity.ok(device);
    }

    /**
     * Delete a device by ID.
     * Accessible by ADMIN role only.
     *
     * @param id device ID
     * @return no content response
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteDevice(@PathVariable Long id) {
        log.info("Delete device request - id: {}", id);
        deviceService.deleteDevice(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get device count by zone.
     * Accessible by all authenticated users.
     *
     * @param zone zone name
     * @return device count
     */
    @GetMapping("/stats/zone/{zone}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Long> countDevicesByZone(@PathVariable String zone) {
        log.info("Count devices by zone request - zone: {}", zone);
        long count = deviceService.countDevicesByZone(zone);
        return ResponseEntity.ok(count);
    }

    /**
     * Get device count by type.
     * Accessible by all authenticated users.
     *
     * @param type device type
     * @return device count
     */
    @GetMapping("/stats/type/{type}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Long> countDevicesByType(@PathVariable String type) {
        log.info("Count devices by type request - type: {}", type);
        long count = deviceService.countDevicesByType(type);
        return ResponseEntity.ok(count);
    }
}
