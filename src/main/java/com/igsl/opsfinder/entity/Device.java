package com.igsl.opsfinder.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Device entity representing a VM or physical device in the inventory.
 * Supports PostgreSQL full-text search via search_vector column (managed by trigger).
 */
@Entity
@Table(name = "devices", indexes = {
        @Index(name = "idx_devices_zone", columnList = "zone"),
        @Index(name = "idx_devices_type", columnList = "type"),
        @Index(name = "idx_devices_ip", columnList = "ip")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Device extends BaseEntity {

    /**
     * Zone where the device is located (e.g., Production-East, Development).
     */
    @NotBlank(message = "Zone is required")
    @Size(max = 100, message = "Zone must not exceed 100 characters")
    @Column(nullable = false, length = 100)
    private String zone;

    /**
     * Username associated with the device.
     */
    @Size(max = 100, message = "Username must not exceed 100 characters")
    @Column(length = 100)
    private String username;

    /**
     * Device type (e.g., VM, Database, Server).
     */
    @NotBlank(message = "Type is required")
    @Size(max = 100, message = "Type must not exceed 100 characters")
    @Column(nullable = false, length = 100)
    private String type;

    /**
     * Additional remarks or notes about the device.
     */
    @Size(max = 500, message = "Remark must not exceed 500 characters")
    @Column(length = 500)
    private String remark;

    /**
     * Physical or logical location (e.g., Rack A1, Cloud).
     */
    @Size(max = 200, message = "Location must not exceed 200 characters")
    @Column(length = 200)
    private String location;

    /**
     * IP address of the device (IPv4 or IPv6).
     */
    @Pattern(regexp = "^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$|^(([0-9a-fA-F]{1,4}:){7,7}[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,7}:|([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})|:((:[0-9a-fA-F]{1,4}){1,7}|:)|fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]{1,}|::(ffff(:0{1,4}){0,1}:){0,1}((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])|([0-9a-fA-F]{1,4}:){1,4}:((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9]))$",
            message = "Invalid IP address format")
    @Size(max = 45, message = "IP must not exceed 45 characters")
    @Column(length = 45)
    private String ip;

    /**
     * Hostname of the device.
     */
    @Size(max = 255, message = "Hostname must not exceed 255 characters")
    @Column(length = 255)
    private String hostname;

    /**
     * Hardware model (e.g., Dell PowerEdge R740, Virtual Machine).
     */
    @Size(max = 200, message = "Hardware model must not exceed 200 characters")
    @Column(name = "hardware_model", length = 200)
    private String hardwareModel;

    /**
     * Datacenter where the device is hosted.
     */
    @Size(max = 100, message = "Datacenter must not exceed 100 characters")
    @Column(length = 100)
    private String datacenter;

    /**
     * Account type associated with the device (e.g., Service Account, Admin).
     */
    @Size(max = 100, message = "Account type must not exceed 100 characters")
    @Column(name = "account_type", length = 100)
    private String accountType;

    /**
     * Password index reference for credential management.
     */
    @Size(max = 100, message = "Password index must not exceed 100 characters")
    @Column(name = "password_index", length = 100)
    private String passwordIndex;

    // Note: search_vector column is managed by PostgreSQL trigger and not mapped here
}
