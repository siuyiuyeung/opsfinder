package com.igsl.opsfinder.mapper;

import com.igsl.opsfinder.dto.excel.ExcelFileDetailResponse;
import com.igsl.opsfinder.dto.excel.ExcelFileResponse;
import com.igsl.opsfinder.dto.excel.ExcelSearchResultResponse;
import com.igsl.opsfinder.entity.ExcelCell;
import com.igsl.opsfinder.entity.ExcelFile;
import com.igsl.opsfinder.entity.ExcelSheet;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

/**
 * MapStruct mapper for Excel file entities to DTOs.
 */
@Mapper(componentModel = "spring")
public interface ExcelFileMapper {

    /**
     * Map ExcelFile entity to response DTO.
     *
     * @param excelFile the entity
     * @return response DTO
     */
    @Mapping(target = "status", source = "status", qualifiedByName = "statusToString")
    ExcelFileResponse toResponse(ExcelFile excelFile);

    /**
     * Map ExcelFile entity to detailed response DTO with sheets.
     *
     * @param excelFile the entity with sheets loaded
     * @return detailed response DTO
     */
    @Mapping(target = "status", source = "status", qualifiedByName = "statusToString")
    @Mapping(target = "sheets", source = "sheets")
    ExcelFileDetailResponse toDetailResponse(ExcelFile excelFile);

    /**
     * Map ExcelSheet entity to SheetInfo DTO.
     *
     * @param excelSheet the sheet entity
     * @return sheet info DTO
     */
    @Mapping(target = "sheetId", source = "id")
    @Mapping(target = "headers", source = ".", qualifiedByName = "sheetToHeaders")
    ExcelFileDetailResponse.SheetInfo toSheetInfo(ExcelSheet excelSheet);

    /**
     * Map ExcelCell entity to search result response DTO.
     *
     * @param excelCell the cell entity
     * @return search result response DTO
     */
    @Mapping(target = "cellId", source = "id")
    @Mapping(target = "fileName", source = "excelSheet.excelFile.originalFilename")
    @Mapping(target = "sheetName", source = "excelSheet.sheetName")
    @Mapping(target = "fileId", source = "excelSheet.excelFile.id")
    @Mapping(target = "sheetId", source = "excelSheet.id")
    ExcelSearchResultResponse toSearchResultResponse(ExcelCell excelCell);

    /**
     * Convert Status enum to string.
     *
     * @param status the status enum
     * @return status as string
     */
    @Named("statusToString")
    default String statusToString(ExcelFile.Status status) {
        return status != null ? status.name() : null;
    }

    /**
     * Extract headers list from ExcelSheet.
     *
     * @param excelSheet the sheet entity
     * @return list of headers
     */
    @Named("sheetToHeaders")
    default List<String> sheetToHeaders(ExcelSheet excelSheet) {
        return excelSheet != null ? excelSheet.getHeadersList() : List.of();
    }
}
