package com.mailflow.contactservice.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "contacts")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Contact {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank(message = "Email is required")
  @Email(message = "Invalid email format")
  @Column(nullable = false, unique = true)
  private String email;

  @Column(name = "first_name")
  private String firstName;

  @Column(name = "last_name")
  private String lastName;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "contact_tags", joinColumns = @JoinColumn(name = "contact_id"))
  @Column(name = "tag")
  @Builder.Default
  private Set<String> tags = new HashSet<>();

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Version private Long version;

  public void addTag(String tag) {
    if (tags == null) {
      tags = new HashSet<>();
    }
    tags.add(tag.toLowerCase());
  }

  public void removeTags(Set<String> tagsToRemove) {
    if (tags != null && tagsToRemove != null) {
      tags.removeAll(
          tagsToRemove.stream()
              .map(String::toLowerCase)
              .collect(Collectors.toSet()));
    }
  }
}
