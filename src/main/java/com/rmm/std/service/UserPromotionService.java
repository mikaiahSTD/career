package com.rmm.std.service;

import com.rmm.std.constant.PromotionStatus;
import com.rmm.std.dto.PageResponse;
import com.rmm.std.dto.UserPromotionRepeatRequest;
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
    applyDefaults(req);
    JUserPromotion saved =
        userPromotionRepository.save(userPromotionMapper.toJ(userPromotionMapper.toDomain(req)));
    return userPromotionMapper.toRes(saved);
  }

  public PageResponse<UserPromotionResponse> list(
      UUID userId, UUID promotionId, PromotionStatus status, Pageable pageable) {
    Page<JUserPromotion> page =
        status == null
            ? userPromotionRepository.searchWithoutStatus(userId, promotionId, pageable)
            : userPromotionRepository.search(userId, promotionId, status, pageable);
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
    if (req.getStatus() != null) {
      existing.setStatus(req.getStatus());
    }
    if (req.getStartDate() != null) {
      existing.setStartDate(req.getStartDate());
    }
    if (req.getEndDate() != null) {
      existing.setEndDate(req.getEndDate());
    }
    if (req.getStatus() == PromotionStatus.GRADUATED && req.getGraduationDate() == null) {
      req.setGraduationDate(OffsetDateTime.now());
    }
    if (req.getGraduationDate() != null) {
      existing.setGraduationDate(req.getGraduationDate());
    }
    return userPromotionMapper.toRes(userPromotionRepository.save(existing));
  }

  @Transactional
  public void delete(UUID id) {
    getEntity(id);
    userPromotionRepository.deleteById(id);
  }

  /**
   * A student who repeats a year gets a new {@code user_promotion} row for the target promotion.
   * The previous row is transitioned to {@code REPEATING} with {@code endDate = now()} in the same
   * transaction, so it is never left as {@code IN_PROGRESS} after the student has moved on.
   */
  @Transactional
  public UserPromotionResponse repeatYear(UserPromotionRepeatRequest req) {
    if (!userRepository.existsById(req.getUserId())) {
      throw new NotFoundException("User not found: " + req.getUserId());
    }
    if (!promotionRepository.existsById(req.getFromPromotionId())) {
      throw new NotFoundException("Promotion not found: " + req.getFromPromotionId());
    }
    if (!promotionRepository.existsById(req.getToPromotionId())) {
      throw new NotFoundException("Promotion not found: " + req.getToPromotionId());
    }
    JUserPromotion previous =
        userPromotionRepository
            .findByUserIdAndPromotionId(req.getUserId(), req.getFromPromotionId())
            .orElseThrow(
                () ->
                    new NotFoundException(
                        "No enrollment found for user "
                            + req.getUserId()
                            + " in promotion "
                            + req.getFromPromotionId()));
    if (userPromotionRepository.existsByUserIdAndPromotionId(
        req.getUserId(), req.getToPromotionId())) {
      throw new ConflictException("User is already enrolled in this promotion");
    }
    previous.setStatus(PromotionStatus.REPEATING);
    previous.setEndDate(OffsetDateTime.now());
    userPromotionRepository.save(previous);

    JUserPromotion next =
        JUserPromotion.builder()
            .user(previous.getUser())
            .promotion(
                promotionRepository
                    .findById(req.getToPromotionId())
                    .orElseThrow(
                        () ->
                            new NotFoundException(
                                "Promotion not found: " + req.getToPromotionId())))
            .status(PromotionStatus.IN_PROGRESS)
            .startDate(OffsetDateTime.now())
            .build();
    return userPromotionMapper.toRes(userPromotionRepository.save(next));
  }

  private void applyDefaults(UserPromotionRequest req) {
    if (req.getStatus() == null) {
      req.setStatus(PromotionStatus.IN_PROGRESS);
    }
    if (req.getStartDate() == null) {
      req.setStartDate(OffsetDateTime.now());
    }
    if (req.getStatus() == PromotionStatus.GRADUATED && req.getGraduationDate() == null) {
      req.setGraduationDate(OffsetDateTime.now());
    }
  }

  private JUserPromotion getEntity(UUID id) {
    return userPromotionRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("Enrollment not found: " + id));
  }
}
