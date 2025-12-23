package com.igsl.opsfinder.service;

import com.igsl.opsfinder.dto.csv.DeviceExportDto;
import com.igsl.opsfinder.dto.request.DeviceRequest;
import com.igsl.opsfinder.dto.response.DeviceResponse;
import com.igsl.opsfinder.entity.Device;
import com.igsl.opsfinder.exception.ResourceNotFoundException;
import com.igsl.opsfinder.mapper.DeviceMapper;
import com.igsl.opsfinder.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for device management with CRUD operations and full-text search.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final DeviceMapper deviceMapper;

    /**
     * Search devices by keyword using PostgreSQL full-text search.
     *
     * @param searchTerm the search keyword(s)
     * @param pageable pagination and sorting parameters
     * @return page of device responses matching the search term
     */
    public Page<DeviceResponse> searchDevices(String searchTerm, Pageable pageable) {
        log.debug("Searching devices with term: {}", searchTerm);
        Page<Device> devices = deviceRepository.searchDevices(searchTerm, pageable);
        return devices.map(deviceMapper::toResponse);
    }

    /**
     * Get all devices with pagination.
     *
     * @param pageable pagination and sorting parameters
     * @return page of device responses
     */
    public Page<DeviceResponse> getAllDevices(Pageable pageable) {
        log.debug("Fetching all devices with pagination: {}", pageable);
        Page<Device> devices = deviceRepository.findAll(pageable);
        return devices.map(deviceMapper::toResponse);
    }

    /**
     * Get device by ID with caching.
     *
     * @param id the device ID
     * @return device response
     * @throws ResourceNotFoundException if device not found
     */
    @Cacheable(value = "devices", key = "#id")
    public DeviceResponse getDeviceById(Long id) {
        log.debug("Fetching device by ID: {}", id);
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with ID: " + id));
        return deviceMapper.toResponse(device);
    }

    /**
     * Get devices by zone.
     *
     * @param zone the zone name
     * @param pageable pagination parameters
     * @return page of device responses
     */
    public Page<DeviceResponse> getDevicesByZone(String zone, Pageable pageable) {
        log.debug("Fetching devices by zone: {}", zone);
        Page<Device> devices = deviceRepository.findByZone(zone, pageable);
        return devices.map(deviceMapper::toResponse);
    }

    /**
     * Get devices by type.
     *
     * @param type the device type
     * @param pageable pagination parameters
     * @return page of device responses
     */
    public Page<DeviceResponse> getDevicesByType(String type, Pageable pageable) {
        log.debug("Fetching devices by type: {}", type);
        Page<Device> devices = deviceRepository.findByType(type, pageable);
        return devices.map(deviceMapper::toResponse);
    }

    /**
     * Get devices by zone and type.
     *
     * @param zone the zone name
     * @param type the device type
     * @param pageable pagination parameters
     * @return page of device responses
     */
    public Page<DeviceResponse> getDevicesByZoneAndType(String zone, String type, Pageable pageable) {
        log.debug("Fetching devices by zone: {} and type: {}", zone, type);
        Page<Device> devices = deviceRepository.findByZoneAndType(zone, type, pageable);
        return devices.map(deviceMapper::toResponse);
    }

    /**
     * Get all distinct zones for filtering.
     *
     * @return list of unique zone names
     */
    public List<String> getDistinctZones() {
        log.debug("Fetching distinct zones");
        return deviceRepository.findDistinctZones();
    }

    /**
     * Get all distinct types for filtering.
     *
     * @return list of unique device types
     */
    public List<String> getDistinctTypes() {
        log.debug("Fetching distinct types");
        return deviceRepository.findDistinctTypes();
    }

    /**
     * Get all distinct datacenters for filtering.
     *
     * @return list of unique datacenter names
     */
    public List<String> getDistinctDatacenters() {
        log.debug("Fetching distinct datacenters");
        return deviceRepository.findDistinctDatacenters();
    }

    /**
     * Get all devices for export (no pagination, core fields only).
     * Accessible by all authenticated users.
     *
     * @return list of device export DTOs
     */
    public List<DeviceExportDto> getAllDevicesForExport() {
        log.info("Fetching all devices for CSV export");
        List<Device> devices = deviceRepository.findAll();
        return devices.stream()
                .map(this::convertToExportDto)
                .collect(Collectors.toList());
    }

    /**
     * Get filtered devices for export.
     *
     * @param zone optional zone filter
     * @param type optional type filter
     * @return list of filtered device export DTOs
     */
    public List<DeviceExportDto> getDevicesForExport(String zone, String type) {
        log.info("Fetching filtered devices for CSV export - zone: {}, type: {}", zone, type);

        List<Device> devices;
        if (zone != null && type != null) {
            devices = deviceRepository.findByZoneAndType(zone, type);
        } else if (zone != null) {
            devices = deviceRepository.findByZone(zone);
        } else if (type != null) {
            devices = deviceRepository.findByType(type);
        } else {
            devices = deviceRepository.findAll();
        }

        return devices.stream()
                .map(this::convertToExportDto)
                .collect(Collectors.toList());
    }

    /**
     * Convert Device entity to DeviceExportDto.
     *
     * @param device the device entity
     * @return device export DTO
     */
    private DeviceExportDto convertToExportDto(Device device) {
        return DeviceExportDto.builder()
                .zone(device.getZone())
                .username(device.getUsername())
                .type(device.getType())
                .remark(device.getRemark())
                .location(device.getLocation())
                .ip(device.getIp())
                .hostname(device.getHostname())
                .hardwareModel(device.getHardwareModel())
                .datacenter(device.getDatacenter())
                .accountType(device.getAccountType())
                .passwordIndex(device.getPasswordIndex())
                .build();
    }

    /**
     * Create a new device.
     *
     * @param request device creation request
     * @return created device response
     */
    @Transactional
    public DeviceResponse createDevice(DeviceRequest request) {
        log.info("Creating new device with zone: {} and type: {}", request.getZone(), request.getType());

        Device device = deviceMapper.toEntity(request);
        Device savedDevice = deviceRepository.save(device);

        log.info("Device created successfully with ID: {}", savedDevice.getId());
        return deviceMapper.toResponse(savedDevice);
    }

    /**
     * Update an existing device.
     *
     * @param id the device ID
     * @param request device update request
     * @return updated device response
     * @throws ResourceNotFoundException if device not found
     */
    @Transactional
    @CacheEvict(value = "devices", key = "#id")
    public DeviceResponse updateDevice(Long id, DeviceRequest request) {
        log.info("Updating device with ID: {}", id);

        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with ID: " + id));

        deviceMapper.updateEntityFromRequest(request, device);
        Device updatedDevice = deviceRepository.save(device);

        log.info("Device updated successfully with ID: {}", id);
        return deviceMapper.toResponse(updatedDevice);
    }

    /**
     * Delete a device by ID.
     *
     * @param id the device ID
     * @throws ResourceNotFoundException if device not found
     */
    @Transactional
    @CacheEvict(value = "devices", key = "#id")
    public void deleteDevice(Long id) {
        log.info("Deleting device with ID: {}", id);

        if (!deviceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Device not found with ID: " + id);
        }

        deviceRepository.deleteById(id);
        log.info("Device deleted successfully with ID: {}", id);
    }

    /**
     * Get device count by zone.
     *
     * @param zone the zone name
     * @return number of devices in the zone
     */
    public long countDevicesByZone(String zone) {
        return deviceRepository.countByZone(zone);
    }

    /**
     * Get device count by type.
     *
     * @param type the device type
     * @return number of devices of the specified type
     */
    public long countDevicesByType(String type) {
        return deviceRepository.countByType(type);
    }
}
