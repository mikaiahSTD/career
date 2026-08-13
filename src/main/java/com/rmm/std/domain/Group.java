package com.rmm.std.domain;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Group {

    private UUID id;
    private String ref;
    private UUID promotionId;
    private UUID careerId;
}
