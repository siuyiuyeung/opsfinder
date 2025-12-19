package com.igsl.opsfinder.mapper;

import com.igsl.opsfinder.dto.request.RegisterRequest;
import com.igsl.opsfinder.dto.request.UserCreateRequest;
import com.igsl.opsfinder.dto.request.UserUpdateRequest;
import com.igsl.opsfinder.dto.response.UserResponse;
import com.igsl.opsfinder.entity.User;
import org.mapstruct.*;

/**
 * MapStruct mapper for User entity and DTOs.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    /**
     * Convert User entity to UserResponse DTO.
     * Maps role enum to string.
     *
     * @param user the user entity
     * @return user response DTO
     */
    @Mapping(target = "role", expression = "java(user.getRole().name())")
    UserResponse toResponse(User user);

    /**
     * Convert RegisterRequest DTO to User entity.
     * Sets active to false for new registrations (pending approval).
     * Sets default role to VIEWER.
     *
     * @param request the register request DTO
     * @return user entity
     */
    @Mapping(target = "active", constant = "false")
    @Mapping(target = "role", constant = "VIEWER")
    User toEntity(RegisterRequest request);

    /**
     * Convert UserCreateRequest DTO to User entity.
     * Used by admin to create users with specified role and status.
     *
     * @param request the user create request DTO
     * @return user entity
     */
    User toEntity(UserCreateRequest request);

    /**
     * Update existing User entity from UserUpdateRequest DTO.
     * Only updates non-null fields.
     *
     * @param request the user update request DTO with updated fields
     * @param user the existing user entity to update
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(UserUpdateRequest request, @MappingTarget User user);
}
