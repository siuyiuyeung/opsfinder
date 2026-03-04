package com.igsl.opsfinder.mapper;

import com.igsl.opsfinder.dto.response.ApiKeyCreatedResponse;
import com.igsl.opsfinder.dto.response.ApiKeyResponse;
import com.igsl.opsfinder.dto.response.ApiKeyUsageLogResponse;
import com.igsl.opsfinder.entity.ApiKey;
import com.igsl.opsfinder.entity.ApiKeyUsageLog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct mapper for ApiKey and ApiKeyUsageLog entities and DTOs.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ApiKeyMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    ApiKeyResponse toResponse(ApiKey apiKey);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "plainTextKey", ignore = true)
    ApiKeyCreatedResponse toCreatedResponse(ApiKey apiKey);

    ApiKeyUsageLogResponse toUsageLogResponse(ApiKeyUsageLog log);
}
