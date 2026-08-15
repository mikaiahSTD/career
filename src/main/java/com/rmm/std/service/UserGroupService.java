package com.rmm.std.service;

import com.rmm.std.domain.User;
import com.rmm.std.dto.PageResponse;
import com.rmm.std.dto.UserGroupRequest;
import com.rmm.std.dto.UserGroupResponse;
import com.rmm.std.exception.ForbiddenException;
import com.rmm.std.exception.NotFoundException;
import com.rmm.std.mapper.UserGroupMapper;
import com.rmm.std.repository.GroupRepository;
import com.rmm.std.repository.UserGroupRepository;
import com.rmm.std.repository.UserRepository;
import com.rmm.std.repository.model.JUserGroup;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class UserGroupService {

  private final UserGroupRepository userGroupRepository;
  private final UserRepository userRepository;
  private final GroupRepository groupRepository;
  private final UserGroupMapper userGroupMapper;

  @Transactional
  public UserGroupResponse create(UserGroupRequest req) {
    if (!userRepository.existsById(req.getUserId())) {
      throw new NotFoundException("User not found: " + req.getUserId());
    }
    if (!groupRepository.existsById(req.getGroupId())) {
      throw new NotFoundException("Group not found: " + req.getGroupId());
    }
    userGroupRepository
        .findFirstByUserIdAndEndDateIsNull(req.getUserId())
        .ifPresent(
            open -> {
              open.setEndDate(OffsetDateTime.now());
              userGroupRepository.save(open);
            });
    if (req.getStartDate() == null) {
      req.setStartDate(OffsetDateTime.now());
    }
    JUserGroup saved = userGroupRepository.save(userGroupMapper.toJ(userGroupMapper.toDomain(req)));
    return userGroupMapper.toRes(saved);
  }

  public PageResponse<UserGroupResponse> list(
      UUID userId, UUID groupId, Boolean current, Pageable pageable) {
    Page<JUserGroup> page = userGroupRepository.search(userId, groupId, current, pageable);
    return PageResponse.from(page, page.map(userGroupMapper::toRes).toList());
  }

  public UserGroupResponse get(UUID id) {
    return userGroupMapper.toRes(getEntity(id));
  }

  @Transactional
  public UserGroupResponse update(UUID id, UserGroupRequest req) {
    JUserGroup existing = getEntity(id);
    if (!userRepository.existsById(req.getUserId())) {
      throw new NotFoundException("User not found: " + req.getUserId());
    }
    if (!groupRepository.existsById(req.getGroupId())) {
      throw new NotFoundException("Group not found: " + req.getGroupId());
    }
    existing.setUser(
        userRepository
            .findById(req.getUserId())
            .orElseThrow(() -> new NotFoundException("User not found: " + req.getUserId())));
    existing.setGroup(
        groupRepository
            .findById(req.getGroupId())
            .orElseThrow(() -> new NotFoundException("Group not found: " + req.getGroupId())));
    existing.setStartDate(req.getStartDate());
    existing.setEndDate(req.getEndDate());
    return userGroupMapper.toRes(userGroupRepository.save(existing));
  }

  @Transactional
  public void delete(UUID id) {
    getEntity(id);
    userGroupRepository.deleteById(id);
  }

  public void assertCanRead(User requestingUser, UUID id) {
    JUserGroup membership = getEntity(id);
    if (requestingUser.getRole() == com.rmm.std.constant.Role.ADMIN
        || requestingUser.getRole() == com.rmm.std.constant.Role.TEACHER
        || membership.getUser().getId().equals(requestingUser.getId())) {
      return;
    }
    throw new ForbiddenException("You are not allowed to access this group membership");
  }

  private JUserGroup getEntity(UUID id) {
    return userGroupRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("Group membership not found: " + id));
  }
}
