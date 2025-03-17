package com.mailflow.templateservice.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "templates")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Email Template name is required")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Subject is required")
    @Column(nullable = false)
    private String subject;

    @NotBlank(message = "Content is required")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "template_variables", joinColumns = @JoinColumn(name = "template_id"))
    @MapKeyColumn(name = "variable_name")
    @Column(name = "default_value")
    @Builder.Default
    private Map<String, String> variables = new HashMap<>();

    @Column(name = "description")
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    private Long version;

  /**
   * Validates a template by checking if all variables in the content are defined
   * in the variables map using a simple regex to find variable placeholders
   * like {{variable_name}}
   *
   * @return true if all variables are properly defined, false otherwise
   */
  public boolean validateVariables() {
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\{\\{([^}]+)}}");
        java.util.regex.Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {
            String variableName = matcher.group(1).trim();
            if (!variables.containsKey(variableName)) {
                return false;
            }
        }

        return true;
    }

    public String renderContent(Map<String, String> variableValues) {
        String renderedContent = content;

        for (Map.Entry<String, String> entry : variables.entrySet()) {
            String variableName = entry.getKey();
            String value = variableValues.getOrDefault(variableName, entry.getValue());
            renderedContent = renderedContent.replace("{{" + variableName + "}}", value);
        }

        return renderedContent;
    }

    public String renderSubject(Map<String, String> variableValues) {
        String renderedSubject = subject;

        for (Map.Entry<String, String> entry : variables.entrySet()) {
            String variableName = entry.getKey();
            String value = variableValues.getOrDefault(variableName, entry.getValue());
            renderedSubject = renderedSubject.replace("{{" + variableName + "}}", value);
        }

        return renderedSubject;
    }
}
