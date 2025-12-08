package com.igsl.opsfinder.mapper;

import com.igsl.opsfinder.dto.request.DeviceRequest;
import com.igsl.opsfinder.dto.response.DeviceResponse;
import com.igsl.opsfinder.entity.Device;
import org.mapstruct.*;

/**
 * MapStruct mapper for Device entity and DTOs.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DeviceMapper {

    /**
     * Convert Device entity to DeviceResponse DTO.
     *
     * @param device the device entity
     * @return device response DTO
     */
    DeviceResponse toResponse(Device device);

    /**
     * Convert DeviceRequest DTO to Device entity.
     *
     * @param request the device request DTO
     * @return device entity
     */
    Device toEntity(DeviceRequest request);

    /**
     * Update existing Device entity from DeviceRequest DTO.
     *
     * @param request the device request DTO with updated fields
     * @param device the existing device entity to update
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(DeviceRequest request, @MappingTarget Device device);
}
