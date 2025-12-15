package com.igsl.opsfinder.mapper;

import com.igsl.opsfinder.dto.request.ActionLevelRequest;
import com.igsl.opsfinder.dto.request.TechMessageRequest;
import com.igsl.opsfinder.dto.response.ActionLevelResponse;
import com.igsl.opsfinder.dto.response.TechMessageResponse;
import com.igsl.opsfinder.entity.ActionLevel;
import com.igsl.opsfinder.entity.TechMessage;
import org.mapstruct.*;

/**
 * MapStruct mapper for TechMessage, ActionLevel entities and DTOs.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TechMessageMapper {

    /**
     * Convert TechMessage entity to TechMessageResponse DTO.
     *
     * @param techMessage the tech message entity
     * @return tech message response DTO
     */
    TechMessageResponse toResponse(TechMessage techMessage);

    /**
     * Convert TechMessageRequest DTO to TechMessage entity.
     *
     * @param request the tech message request DTO
     * @return tech message entity
     */
    TechMessage toEntity(TechMessageRequest request);

    /**
     * Update existing TechMessage entity from TechMessageRequest DTO.
     *
     * @param request the tech message request DTO with updated fields
     * @param techMessage the existing tech message entity to update
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(TechMessageRequest request, @MappingTarget TechMessage techMessage);

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
