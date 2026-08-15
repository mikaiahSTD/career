package com.rmm.std.mapper;

import com.rmm.std.domain.UserGroup;
import com.rmm.std.dto.UserGroupRequest;
import com.rmm.std.dto.UserGroupResponse;
import com.rmm.std.exception.NotFoundException;
import com.rmm.std.repository.GroupRepository;
import com.rmm.std.repository.UserRepository;
import com.rmm.std.repository.model.JGroup;
import com.rmm.std.repository.model.JUser;
import com.rmm.std.repository.model.JUserGroup;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class UserGroupMapper {

  private final UserRepository userRepository;
  private final GroupRepository groupRepository;

  public JUserGroup toJ(UserGroup userGroup) {
    if (userGroup == null) {
      return null;
    }
    JUser user =
        userRepository
            .findById(userGroup.getUserId())
            .orElseThrow(() -> new NotFoundException("User not found: " + userGroup.getUserId()));
    JGroup group =
        groupRepository
            .findById(userGroup.getGroupId())
            .orElseThrow(() -> new NotFoundException("Group not found: " + userGroup.getGroupId()));
    return JUserGroup.builder()
        .id(userGroup.getId())
        .user(user)
        .group(group)
        .startDate(userGroup.getStartDate())
        .endDate(userGroup.getEndDate())
        .build();
  }

  public UserGroup toDomain(JUserGroup jUserGroup) {
    if (jUserGroup == null) {
      return null;
    }
    return UserGroup.builder()
        .id(jUserGroup.getId())
        .userId(jUserGroup.getUser().getId())
        .groupId(jUserGroup.getGroup().getId())
        .startDate(jUserGroup.getStartDate())
        .endDate(jUserGroup.getEndDate())
        .build();
  }

  public UserGroup toDomain(UserGroupRequest req) {
    if (req == null) {
      return null;
    }
    return UserGroup.builder()
        .userId(req.getUserId())
        .groupId(req.getGroupId())
        .startDate(req.getStartDate())
        .endDate(req.getEndDate())
        .build();
  }

  public UserGroupResponse toRes(JUserGroup jUserGroup) {
    if (jUserGroup == null) {
      return null;
    }
    return UserGroupResponse.builder()
        .id(jUserGroup.getId())
        .userId(jUserGroup.getUser().getId())
        .groupId(jUserGroup.getGroup().getId())
        .startDate(jUserGroup.getStartDate())
        .endDate(jUserGroup.getEndDate())
        .build();
  }
}
