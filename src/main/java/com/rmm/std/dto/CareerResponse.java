package com.rmm.std.dto;

import java.util.UUID;

import com.rmm.std.domain.Specialization;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CareerResponse {

    private UUID id;
    private String title;
    private Specialization specialization;
}
