package com.mailflow.contactservice.mapper;

import com.mailflow.contactservice.domain.Contact;
import com.mailflow.contactservice.dto.contact.ContactRequest;
import com.mailflow.contactservice.dto.contact.ContactResponse;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class ContactMapper {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Contact toEntity(ContactRequest dto) {
        return Contact.builder()
                .email(dto.email())
                .firstName(dto.firstName())
                .lastName(dto.lastName())
                .tags(dto.tags())
                .build();
    }

    public ContactResponse toResponse(Contact entity) {
        return ContactResponse.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .tags(entity.getTags())
                .createdAt(entity.getCreatedAt().format(DATE_FORMATTER))
                .updatedAt(entity.getUpdatedAt().format(DATE_FORMATTER))
                .build();
    }

    public void updateContactFromDto(ContactRequest dto, Contact contact) {
        contact.setEmail(dto.email());
        contact.setFirstName(dto.firstName());
        contact.setLastName(dto.lastName());
        if (dto.tags() != null) {
            contact.setTags(dto.tags());
        }
    }
}