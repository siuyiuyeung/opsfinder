package com.igsl.opsfinder.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for device responses in API endpoints.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceResponse {

    private Long id;
    private String zone;
    private String username;
    private String type;
    private String remark;
    private String location;
    private String ip;
    private String hostname;
    private String hardwareModel;
    private String datacenter;
    private String accountType;
    private String passwordIndex;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
