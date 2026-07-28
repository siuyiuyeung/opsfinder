package com.igsl.opsfinder.dto.excel;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Guards the JSON property name of {@link RowCellData#isMatchedCell}.
 *
 * <p>Lombok generates {@code isMatchedCell()} for the primitive boolean field
 * {@code isMatchedCell}, and Jackson strips the {@code is} prefix when deriving
 * a property name. Without an explicit {@code @JsonProperty}, the field
 * serializes as {@code matchedCell}, which the frontend does not read.
 */
class RowCellDataSerializationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void serializesMatchedFlagAsIsMatchedCell() {
        RowCellData cell = RowCellData.builder()
                .columnHeader("SERVICE")
                .columnIndex(4)
                .cellValue("Email Agent")
                .isMatchedCell(true)
                .build();

        String json = objectMapper.writeValueAsString(cell);

        assertTrue(json.contains("\"isMatchedCell\":true"),
                "Expected property 'isMatchedCell' in JSON but was: " + json);
    }
}
