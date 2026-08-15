package com.rmm.std.service;

import com.rmm.std.dto.PageResponse;
import com.rmm.std.dto.SemesterRequest;
import com.rmm.std.dto.SemesterResponse;
import com.rmm.std.exception.NotFoundException;
import com.rmm.std.mapper.SemesterMapper;
import com.rmm.std.repository.PromotionRepository;
import com.rmm.std.repository.SemesterRepository;
import com.rmm.std.repository.model.JSemester;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class SemesterService {

  private final SemesterRepository semesterRepository;
  private final PromotionRepository promotionRepository;
  private final SemesterMapper semesterMapper;

  @Transactional
  public SemesterResponse create(SemesterRequest req) {
    if (!promotionRepository.existsById(req.getPromotionId())) {
      throw new NotFoundException("Promotion not found: " + req.getPromotionId());
    }
    JSemester saved = semesterRepository.save(semesterMapper.toJ(semesterMapper.toDomain(req)));
    return semesterMapper.toRes(saved);
  }

  public PageResponse<SemesterResponse> list(UUID promotionId, Pageable pageable) {
    Page<JSemester> page =
        promotionId == null
            ? semesterRepository.findAll(pageable)
            : semesterRepository.findByPromotionId(promotionId, pageable);
    return PageResponse.from(page, page.map(semesterMapper::toRes).toList());
  }

  public SemesterResponse get(UUID id) {
    return semesterMapper.toRes(getEntity(id));
  }

  @Transactional
  public SemesterResponse update(UUID id, SemesterRequest req) {
    JSemester existing = getEntity(id);
    if (!promotionRepository.existsById(req.getPromotionId())) {
      throw new NotFoundException("Promotion not found: " + req.getPromotionId());
    }
    existing.setPromotion(
        promotionRepository
            .findById(req.getPromotionId())
            .orElseThrow(
                () -> new NotFoundException("Promotion not found: " + req.getPromotionId())));
    existing.setNumber(req.getNumber());
    existing.setStartDate(req.getStartDate());
    existing.setEndDate(req.getEndDate());
    return semesterMapper.toRes(semesterRepository.save(existing));
  }

  @Transactional
  public void delete(UUID id) {
    getEntity(id);
    semesterRepository.deleteById(id);
  }

  private JSemester getEntity(UUID id) {
    return semesterRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("Semester not found: " + id));
  }
}
