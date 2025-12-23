package com.igsl.opsfinder.dto.csv;

import com.opencsv.bean.CsvBindByName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for CSV export operations.
 * Contains only the 11 core device fields (no audit fields).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceExportDto {

    @CsvBindByName(column = "zone")
    private String zone;

    @CsvBindByName(column = "username")
    private String username;

    @CsvBindByName(column = "type")
    private String type;

    @CsvBindByName(column = "remark")
    private String remark;

    @CsvBindByName(column = "location")
    private String location;

    @CsvBindByName(column = "ip")
    private String ip;

    @CsvBindByName(column = "hostname")
    private String hostname;

    @CsvBindByName(column = "hardwareModel")
    private String hardwareModel;

    @CsvBindByName(column = "datacenter")
    private String datacenter;

    @CsvBindByName(column = "accountType")
    private String accountType;

    @CsvBindByName(column = "passwordIndex")
    private String passwordIndex;
}
