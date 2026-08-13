package com.rmm.std.domain;

import com.rmm.std.constant.Specialization;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Career {

  private UUID id;
  private String title;
  private Specialization specialization;
}
