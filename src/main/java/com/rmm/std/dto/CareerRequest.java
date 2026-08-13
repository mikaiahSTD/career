package com.rmm.std.dto;

import com.rmm.std.domain.Specialization;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CareerRequest {

  private String title;

  private Specialization specialization;
}
