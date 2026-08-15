package com.rmm.std.repository;

import com.rmm.std.repository.model.JUserGroup;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserGroupRepository extends JpaRepository<JUserGroup, UUID> {

  Optional<JUserGroup> findFirstByUserIdAndEndDateIsNull(UUID userId);

  @Query(
      """
      select ug from JUserGroup ug
      where (cast(:userId as uuid) is null or ug.user.id = :userId)
        and (cast(:groupId as uuid) is null or ug.group.id = :groupId)
        and (cast(:current as boolean) is null or (:current = true and ug.endDate is null))
      """)
  Page<JUserGroup> search(
      @Param("userId") UUID userId,
      @Param("groupId") UUID groupId,
      @Param("current") Boolean current,
      Pageable pageable);
}
