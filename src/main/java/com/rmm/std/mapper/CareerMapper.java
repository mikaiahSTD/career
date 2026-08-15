package com.rmm.std.mapper;

import com.rmm.std.domain.Career;
import com.rmm.std.dto.CareerRequest;
import com.rmm.std.dto.CareerResponse;
import com.rmm.std.repository.model.JCareer;
import org.springframework.stereotype.Component;

@Component
public class CareerMapper {

  public JCareer toJ(Career career) {
    if (career == null) {
      return null;
    }
    return JCareer.builder()
        .id(career.getId())
        .title(career.getTitle())
        .specialization(career.getSpecialization())
        .build();
  }

  public Career toDomain(JCareer jCareer) {
    if (jCareer == null) {
      return null;
    }
    return Career.builder()
        .id(jCareer.getId())
        .title(jCareer.getTitle())
        .specialization(jCareer.getSpecialization())
        .build();
  }

  public Career toDomain(CareerRequest req) {
    if (req == null) {
      return null;
    }
    return Career.builder().title(req.getTitle()).specialization(req.getSpecialization()).build();
  }

  public CareerResponse toRes(JCareer jCareer) {
    if (jCareer == null) {
      return null;
    }
    return CareerResponse.builder()
        .id(jCareer.getId())
        .title(jCareer.getTitle())
        .specialization(jCareer.getSpecialization())
        .build();
  }
}
