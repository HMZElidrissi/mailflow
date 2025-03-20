package com.mailflow.emailservice.domain;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("emails")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Email {

    @Id
    private Long id;

    @Column("campaign_id")
    private Long campaignId;

    @Column("contact_id")
    private Long contactId;

    @Column("template_id")
    private Long templateId;

    @Column("recipient_email")
    private String recipientEmail;

    @Column("subject")
    private String subject;

    @Column("content")
    private String content;

    @Column("status")
    @Builder.Default
    private EmailStatus status = EmailStatus.PENDING;

    @Column("sent_at")
    private LocalDateTime sentAt;

    @Column("opened_at")
    private LocalDateTime openedAt;

    @Column("clicked_at")
    private LocalDateTime clickedAt;

    @Column("tracking_id")
    private String trackingId;

    @Column("error_message")
    private String errorMessage;

    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column("updated_at")
    private LocalDateTime updatedAt;

    @Version
    private Long version;
}