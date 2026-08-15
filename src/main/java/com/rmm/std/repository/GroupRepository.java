package com.rmm.std.repository;

import com.rmm.std.repository.model.JGroup;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupRepository extends JpaRepository<JGroup, UUID> {

  boolean existsByRef(String ref);

  Page<JGroup> findByPromotionId(UUID promotionId, Pageable pageable);

  Page<JGroup> findByCareerId(UUID careerId, Pageable pageable);
}
