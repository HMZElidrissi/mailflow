package com.mailflow.templateservice.dto.response;

import java.util.List;
import lombok.*;
import org.springframework.data.domain.Page;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {
  private List<T> content;
  private int pageNo;
  private int pageSize;
  private long totalElements;
  private int totalPages;
  private boolean last;

  public static <T> PageResponse<T> of(List<T> content, Page<?> page) {
    return PageResponse.<T>builder()
        .content(content)
        .pageNo(page.getNumber())
        .pageSize(page.getSize())
        .totalElements(page.getTotalElements())
        .totalPages(page.getTotalPages())
        .last(page.isLast())
        .build();
  }
}
