package com.mailflow.templateservice.mapper;

import com.mailflow.templateservice.domain.EmailTemplate;
import com.mailflow.templateservice.dto.template.EmailTemplateRequest;
import com.mailflow.templateservice.dto.template.EmailTemplateResponse;
import org.mapstruct.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EmailTemplateMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    EmailTemplate toEntity(EmailTemplateRequest request);

    @Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "formatDateTime")
    @Mapping(target = "updatedAt", source = "updatedAt", qualifiedByName = "formatDateTime")
    EmailTemplateResponse toResponse(EmailTemplate template);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateTemplateFromDto(EmailTemplateRequest request, @MappingTarget EmailTemplate template);

    @Named("formatDateTime")
    default String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}