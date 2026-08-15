package com.rmm.std.service;

import com.rmm.std.dto.CareerRequest;
import com.rmm.std.dto.CareerResponse;
import com.rmm.std.dto.PageResponse;
import com.rmm.std.exception.NotFoundException;
import com.rmm.std.mapper.CareerMapper;
import com.rmm.std.repository.CareerRepository;
import com.rmm.std.repository.model.JCareer;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class CareerService {

  private final CareerRepository careerRepository;
  private final CareerMapper careerMapper;

  @Transactional
  public CareerResponse create(CareerRequest req) {
    JCareer saved = careerRepository.save(careerMapper.toJ(careerMapper.toDomain(req)));
    return careerMapper.toRes(saved);
  }

  public PageResponse<CareerResponse> list(Pageable pageable) {
    Page<JCareer> page = careerRepository.findAll(pageable);
    return PageResponse.from(page, page.map(careerMapper::toRes).toList());
  }

  public CareerResponse get(UUID id) {
    return careerMapper.toRes(getEntity(id));
  }

  @Transactional
  public CareerResponse update(UUID id, CareerRequest req) {
    JCareer existing = getEntity(id);
    existing.setTitle(req.getTitle());
    existing.setSpecialization(req.getSpecialization());
    return careerMapper.toRes(careerRepository.save(existing));
  }

  @Transactional
  public void delete(UUID id) {
    getEntity(id);
    careerRepository.deleteById(id);
  }

  private JCareer getEntity(UUID id) {
    return careerRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("Career not found: " + id));
  }
}
