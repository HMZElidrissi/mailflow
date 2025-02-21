package com.mailflow.campaignservice.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "campaigns")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Campaign {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String triggerTag;

  @Column(nullable = false)
  private Long templateId;

  @Builder.Default
  private boolean active = false;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Version
  private Long version;

  public void activate() {
    this.active = true;
  }

  public void deactivate() {
    this.active = false;
  }
}