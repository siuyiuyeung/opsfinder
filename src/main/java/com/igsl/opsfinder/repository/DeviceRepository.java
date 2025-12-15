package com.igsl.opsfinder.repository;

import com.igsl.opsfinder.entity.Device;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Device entity with PostgreSQL full-text search support.
 */
@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {

    /**
     * Full-text search across all device attributes using PostgreSQL ts_vector.
     * Results are ranked by relevance using ts_rank.
     *
     * @param searchTerm the search keyword(s)
     * @param pageable pagination parameters (no custom sort applied - results ordered by relevance)
     * @return page of devices matching the search term, ordered by relevance
     */
    @Query(value = """
            SELECT d.* FROM devices d
            WHERE d.search_vector @@ plainto_tsquery('english', :searchTerm)
            ORDER BY ts_rank(d.search_vector, plainto_tsquery('english', :searchTerm)) DESC
            """,
            countQuery = """
            SELECT COUNT(*) FROM devices d
            WHERE d.search_vector @@ plainto_tsquery('english', :searchTerm)
            """,
            nativeQuery = true)
    Page<Device> searchDevices(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Find devices by zone (exact match).
     *
     * @param zone the zone name
     * @param pageable pagination parameters
     * @return page of devices in the specified zone
     */
    Page<Device> findByZone(String zone, Pageable pageable);

    /**
     * Find devices by type (exact match).
     *
     * @param type the device type
     * @param pageable pagination parameters
     * @return page of devices of the specified type
     */
    Page<Device> findByType(String type, Pageable pageable);

    /**
     * Find devices by zone and type.
     *
     * @param zone the zone name
     * @param type the device type
     * @param pageable pagination parameters
     * @return page of devices matching both zone and type
     */
    Page<Device> findByZoneAndType(String zone, String type, Pageable pageable);

    /**
     * Find device by IP address (exact match).
     *
     * @param ip the IP address
     * @return list of devices with the specified IP (should be unique but using List for safety)
     */
    List<Device> findByIp(String ip);

    /**
     * Find device by hostname (exact match).
     *
     * @param hostname the hostname
     * @return list of devices with the specified hostname
     */
    List<Device> findByHostname(String hostname);

    /**
     * Get all distinct zones for filtering.
     *
     * @return list of unique zone names
     */
    @Query("SELECT DISTINCT d.zone FROM Device d ORDER BY d.zone")
    List<String> findDistinctZones();

    /**
     * Get all distinct types for filtering.
     *
     * @return list of unique device types
     */
    @Query("SELECT DISTINCT d.type FROM Device d ORDER BY d.type")
    List<String> findDistinctTypes();

    /**
     * Get all distinct datacenters for filtering.
     *
     * @return list of unique datacenter names
     */
    @Query("SELECT DISTINCT d.datacenter FROM Device d WHERE d.datacenter IS NOT NULL ORDER BY d.datacenter")
    List<String> findDistinctDatacenters();

    /**
     * Count devices by zone.
     *
     * @param zone the zone name
     * @return number of devices in the zone
     */
    long countByZone(String zone);

    /**
     * Count devices by type.
     *
     * @param type the device type
     * @return number of devices of the specified type
     */
    long countByType(String type);
}
