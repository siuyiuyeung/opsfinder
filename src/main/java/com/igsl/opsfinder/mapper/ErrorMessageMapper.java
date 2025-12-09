package com.igsl.opsfinder.mapper;

import com.igsl.opsfinder.dto.request.ActionLevelRequest;
import com.igsl.opsfinder.dto.request.ErrorMessageRequest;
import com.igsl.opsfinder.dto.response.ActionLevelResponse;
import com.igsl.opsfinder.dto.response.ErrorMessageResponse;
import com.igsl.opsfinder.entity.ActionLevel;
import com.igsl.opsfinder.entity.ErrorMessage;
import org.mapstruct.*;

/**
 * MapStruct mapper for ErrorMessage, ActionLevel entities and DTOs.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ErrorMessageMapper {

    /**
     * Convert ErrorMessage entity to ErrorMessageResponse DTO.
     *
     * @param errorMessage the error message entity
     * @return error message response DTO
     */
    ErrorMessageResponse toResponse(ErrorMessage errorMessage);

    /**
     * Convert ErrorMessageRequest DTO to ErrorMessage entity.
     *
     * @param request the error message request DTO
     * @return error message entity
     */
    ErrorMessage toEntity(ErrorMessageRequest request);

    /**
     * Update existing ErrorMessage entity from ErrorMessageRequest DTO.
     *
     * @param request the error message request DTO with updated fields
     * @param errorMessage the existing error message entity to update
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(ErrorMessageRequest request, @MappingTarget ErrorMessage errorMessage);

    /**
     * Convert ActionLevel entity to ActionLevelResponse DTO.
     *
     * @param actionLevel the action level entity
     * @return action level response DTO
     */
    ActionLevelResponse toActionLevelResponse(ActionLevel actionLevel);

    /**
     * Convert ActionLevelRequest DTO to ActionLevel entity.
     *
     * @param request the action level request DTO
     * @return action level entity
     */
    ActionLevel toActionLevelEntity(ActionLevelRequest request);

    /**
     * Update existing ActionLevel entity from ActionLevelRequest DTO.
     *
     * @param request the action level request DTO with updated fields
     * @param actionLevel the existing action level entity to update
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateActionLevelFromRequest(ActionLevelRequest request, @MappingTarget ActionLevel actionLevel);
}
