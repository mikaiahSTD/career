package com.rmm.std.repository;

import com.rmm.std.repository.model.JSemester;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SemesterRepository extends JpaRepository<JSemester, UUID> {

  Page<JSemester> findByPromotionId(UUID promotionId, Pageable pageable);
}
