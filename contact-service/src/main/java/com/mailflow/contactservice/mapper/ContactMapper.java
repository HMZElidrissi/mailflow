package com.mailflow.contactservice.mapper;

import com.mailflow.contactservice.dto.ContactDTO;
import com.mailflow.contactservice.domain.Contact;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class ContactMapper {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Contact toEntity(ContactDTO.Request dto) {
        return Contact.builder()
                .email(dto.getEmail())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .tags(dto.getTags())
                .build();
    }

    public ContactDTO.Response toResponse(Contact entity) {
        return ContactDTO.Response.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .tags(entity.getTags())
                .createdAt(entity.getCreatedAt().format(DATE_FORMATTER))
                .updatedAt(entity.getUpdatedAt().format(DATE_FORMATTER))
                .build();
    }

    public void updateContactFromDto(ContactDTO.Request dto, Contact contact) {
        contact.setEmail(dto.getEmail());
        contact.setFirstName(dto.getFirstName());
        contact.setLastName(dto.getLastName());
        if (dto.getTags() != null) {
            contact.setTags(dto.getTags());
        }
    }
}