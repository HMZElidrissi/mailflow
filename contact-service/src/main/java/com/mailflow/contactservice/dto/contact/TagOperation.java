package com.mailflow.contactservice.dto.contact;

import lombok.Builder;

import java.util.Set;

@Builder
public record TagOperation(Set<String> tags) {}
