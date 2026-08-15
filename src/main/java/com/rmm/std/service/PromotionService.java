package com.rmm.std.service;

import com.rmm.std.dto.PageResponse;
import com.rmm.std.dto.PromotionRequest;
import com.rmm.std.dto.PromotionResponse;
import com.rmm.std.dto.UserResponse;
import com.rmm.std.exception.NotFoundException;
import com.rmm.std.mapper.PromotionMapper;
import com.rmm.std.mapper.UserMapper;
import com.rmm.std.repository.PromotionRepository;
import com.rmm.std.repository.UserPromotionRepository;
import com.rmm.std.repository.model.JPromotion;
import com.rmm.std.repository.model.JUserPromotion;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class PromotionService {

  private final PromotionRepository promotionRepository;
  private final UserPromotionRepository userPromotionRepository;
  private final PromotionMapper promotionMapper;
  private final UserMapper userMapper;

  @Transactional
  public PromotionResponse create(PromotionRequest req) {
    JPromotion saved = promotionRepository.save(promotionMapper.toJ(promotionMapper.toDomain(req)));
    return promotionMapper.toRes(saved);
  }

  public PageResponse<PromotionResponse> list(Pageable pageable) {
    Page<JPromotion> page = promotionRepository.findAll(pageable);
    return PageResponse.from(page, page.map(promotionMapper::toRes).toList());
  }

  public PromotionResponse get(UUID id) {
    return promotionMapper.toRes(getEntity(id));
  }

  @Transactional
  public PromotionResponse update(UUID id, PromotionRequest req) {
    JPromotion existing = getEntity(id);
    existing.setLabel(req.getLabel());
    existing.setStartYear(req.getStartYear());
    return promotionMapper.toRes(promotionRepository.save(existing));
  }

  @Transactional
  public void delete(UUID id) {
    getEntity(id);
    promotionRepository.deleteById(id);
  }

  @Transactional(readOnly = true)
  public List<UserResponse> getGraduates(UUID promotionId) {
    getEntity(promotionId);
    return userPromotionRepository.findByPromotionIdAndGraduatedTrue(promotionId).stream()
        .map(JUserPromotion::getUser)
        .map(userMapper::toRes)
        .toList();
  }

  private JPromotion getEntity(UUID id) {
    return promotionRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("Promotion not found: " + id));
  }
}
