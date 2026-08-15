package com.rmm.std.service;

import com.rmm.std.dto.PageResponse;
import com.rmm.std.dto.UserPromotionRequest;
import com.rmm.std.dto.UserPromotionResponse;
import com.rmm.std.exception.ConflictException;
import com.rmm.std.exception.NotFoundException;
import com.rmm.std.mapper.UserPromotionMapper;
import com.rmm.std.repository.PromotionRepository;
import com.rmm.std.repository.UserPromotionRepository;
import com.rmm.std.repository.UserRepository;
import com.rmm.std.repository.model.JUserPromotion;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class UserPromotionService {

  private final UserPromotionRepository userPromotionRepository;
  private final UserRepository userRepository;
  private final PromotionRepository promotionRepository;
  private final UserPromotionMapper userPromotionMapper;

  @Transactional
  public UserPromotionResponse create(UserPromotionRequest req) {
    if (!userRepository.existsById(req.getUserId())) {
      throw new NotFoundException("User not found: " + req.getUserId());
    }
    if (!promotionRepository.existsById(req.getPromotionId())) {
      throw new NotFoundException("Promotion not found: " + req.getPromotionId());
    }
    if (userPromotionRepository.existsByUserIdAndPromotionId(
        req.getUserId(), req.getPromotionId())) {
      throw new ConflictException("User is already enrolled in this promotion");
    }
    if (req.isGraduated() && req.getGraduationDate() == null) {
      req.setGraduationDate(OffsetDateTime.now());
    }
    JUserPromotion saved =
        userPromotionRepository.save(userPromotionMapper.toJ(userPromotionMapper.toDomain(req)));
    return userPromotionMapper.toRes(saved);
  }

  public PageResponse<UserPromotionResponse> list(
      UUID userId, UUID promotionId, Boolean graduated, Pageable pageable) {
    Page<JUserPromotion> page =
        userPromotionRepository.search(userId, promotionId, graduated, pageable);
    return PageResponse.from(page, page.map(userPromotionMapper::toRes).toList());
  }

  public UserPromotionResponse get(UUID id) {
    return userPromotionMapper.toRes(getEntity(id));
  }

  @Transactional
  public UserPromotionResponse update(UUID id, UserPromotionRequest req) {
    JUserPromotion existing = getEntity(id);
    if (!userRepository.existsById(req.getUserId())) {
      throw new NotFoundException("User not found: " + req.getUserId());
    }
    if (!promotionRepository.existsById(req.getPromotionId())) {
      throw new NotFoundException("Promotion not found: " + req.getPromotionId());
    }
    existing.setUser(
        userRepository
            .findById(req.getUserId())
            .orElseThrow(() -> new NotFoundException("User not found: " + req.getUserId())));
    existing.setPromotion(
        promotionRepository
            .findById(req.getPromotionId())
            .orElseThrow(
                () -> new NotFoundException("Promotion not found: " + req.getPromotionId())));
    existing.setGraduated(req.isGraduated());
    if (req.isGraduated() && req.getGraduationDate() == null) {
      req.setGraduationDate(OffsetDateTime.now());
    }
    existing.setGraduationDate(req.getGraduationDate());
    return userPromotionMapper.toRes(userPromotionRepository.save(existing));
  }

  @Transactional
  public void delete(UUID id) {
    getEntity(id);
    userPromotionRepository.deleteById(id);
  }

  private JUserPromotion getEntity(UUID id) {
    return userPromotionRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("Enrollment not found: " + id));
  }
}
