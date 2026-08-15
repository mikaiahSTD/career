package com.rmm.std.service;

import com.rmm.std.dto.GroupRequest;
import com.rmm.std.dto.GroupResponse;
import com.rmm.std.dto.PageResponse;
import com.rmm.std.exception.ConflictException;
import com.rmm.std.exception.NotFoundException;
import com.rmm.std.mapper.GroupMapper;
import com.rmm.std.repository.CareerRepository;
import com.rmm.std.repository.GroupRepository;
import com.rmm.std.repository.PromotionRepository;
import com.rmm.std.repository.model.JGroup;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class GroupService {

  private final GroupRepository groupRepository;
  private final PromotionRepository promotionRepository;
  private final CareerRepository careerRepository;
  private final GroupMapper groupMapper;

  @Transactional
  public GroupResponse create(GroupRequest req) {
    if (groupRepository.existsByRef(req.getRef())) {
      throw new ConflictException("Group with ref: " + req.getRef() + " already exists");
    }
    if (!promotionRepository.existsById(req.getPromotionId())) {
      throw new NotFoundException("Promotion not found: " + req.getPromotionId());
    }
    if (!careerRepository.existsById(req.getCareerId())) {
      throw new NotFoundException("Career not found: " + req.getCareerId());
    }
    JGroup saved = groupRepository.save(groupMapper.toJ(groupMapper.toDomain(req)));
    return groupMapper.toRes(saved);
  }

  public PageResponse<GroupResponse> list(UUID promotionId, UUID careerId, Pageable pageable) {
    Page<JGroup> page;
    if (promotionId != null) {
      page = groupRepository.findByPromotionId(promotionId, pageable);
    } else if (careerId != null) {
      page = groupRepository.findByCareerId(careerId, pageable);
    } else {
      page = groupRepository.findAll(pageable);
    }
    return PageResponse.from(page, page.map(groupMapper::toRes).toList());
  }

  public GroupResponse get(UUID id) {
    return groupMapper.toRes(getEntity(id));
  }

  @Transactional
  public GroupResponse update(UUID id, GroupRequest req) {
    JGroup existing = getEntity(id);
    if (groupRepository.existsByRef(req.getRef()) && !existing.getRef().equals(req.getRef())) {
      throw new ConflictException("Group with ref: " + req.getRef() + " already exists");
    }
    if (!promotionRepository.existsById(req.getPromotionId())) {
      throw new NotFoundException("Promotion not found: " + req.getPromotionId());
    }
    if (!careerRepository.existsById(req.getCareerId())) {
      throw new NotFoundException("Career not found: " + req.getCareerId());
    }
    existing.setRef(req.getRef());
    existing.setPromotion(
        promotionRepository
            .findById(req.getPromotionId())
            .orElseThrow(
                () -> new NotFoundException("Promotion not found: " + req.getPromotionId())));
    existing.setCareer(
        careerRepository
            .findById(req.getCareerId())
            .orElseThrow(() -> new NotFoundException("Career not found: " + req.getCareerId())));
    return groupMapper.toRes(groupRepository.save(existing));
  }

  @Transactional
  public void delete(UUID id) {
    getEntity(id);
    groupRepository.deleteById(id);
  }

  private JGroup getEntity(UUID id) {
    return groupRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("Group not found: " + id));
  }
}
