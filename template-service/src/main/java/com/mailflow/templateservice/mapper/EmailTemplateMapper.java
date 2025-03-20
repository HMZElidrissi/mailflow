package com.mailflow.templateservice.mapper;

import com.mailflow.templateservice.domain.EmailTemplate;
import com.mailflow.templateservice.dto.template.EmailTemplateRequest;
import com.mailflow.templateservice.dto.template.EmailTemplateResponse;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EmailTemplateMapper {

    EmailTemplate toEntity(EmailTemplateRequest request);

    EmailTemplateResponse toResponse(EmailTemplate template);

    void updateTemplateFromDto(EmailTemplateRequest request, @MappingTarget EmailTemplate template);
}