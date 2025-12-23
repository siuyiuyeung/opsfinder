package com.igsl.opsfinder.dto.csv;

import com.opencsv.bean.CsvBindByName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for CSV import operations using OpenCSV annotations.
 * Maps CSV columns to device fields with validation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceImportDto {

    @CsvBindByName(column = "zone", required = true)
    @NotBlank(message = "Zone is required")
    @Size(max = 100, message = "Zone must not exceed 100 characters")
    private String zone;

    @CsvBindByName(column = "username")
    @Size(max = 100, message = "Username must not exceed 100 characters")
    private String username;

    @CsvBindByName(column = "type", required = true)
    @NotBlank(message = "Type is required")
    @Size(max = 100, message = "Type must not exceed 100 characters")
    private String type;

    @CsvBindByName(column = "remark")
    @Size(max = 500, message = "Remark must not exceed 500 characters")
    private String remark;

    @CsvBindByName(column = "location")
    @Size(max = 200, message = "Location must not exceed 200 characters")
    private String location;

    @CsvBindByName(column = "ip")
    @Pattern(regexp = "^$|^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$|^(([0-9a-fA-F]{1,4}:){7,7}[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,7}:|([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})|:((:[0-9a-fA-F]{1,4}){1,7}|:)|fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]{1,}|::(ffff(:0{1,4}){0,1}:){0,1}((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])|([0-9a-fA-F]{1,4}:){1,4}:((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9]))$",
            message = "Invalid IP address format")
    @Size(max = 45, message = "IP must not exceed 45 characters")
    private String ip;

    @CsvBindByName(column = "hostname")
    @Size(max = 255, message = "Hostname must not exceed 255 characters")
    private String hostname;

    @CsvBindByName(column = "hardwareModel")
    @Size(max = 200, message = "Hardware model must not exceed 200 characters")
    private String hardwareModel;

    @CsvBindByName(column = "datacenter")
    @Size(max = 100, message = "Datacenter must not exceed 100 characters")
    private String datacenter;

    @CsvBindByName(column = "accountType")
    @Size(max = 100, message = "Account type must not exceed 100 characters")
    private String accountType;

    @CsvBindByName(column = "passwordIndex")
    @Size(max = 100, message = "Password index must not exceed 100 characters")
    private String passwordIndex;
}
