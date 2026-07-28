package com.igsl.opsfinder.dto.excel;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing a single cell's data within a row.
 * Used to show complete row context for search results.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RowCellData {

    private String columnHeader;
    private Integer columnIndex;
    private String cellValue;
    // Lombok's getter is isMatchedCell(); Jackson would strip the "is" prefix and
    // serialize this as "matchedCell", which the frontend does not read.
    @JsonProperty("isMatchedCell")
    private boolean isMatchedCell; // Indicates if this is the cell that matched the search
}
