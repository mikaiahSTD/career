package com.rmm.std.repository;

import com.rmm.std.repository.model.JUserPromotion;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserPromotionRepository extends JpaRepository<JUserPromotion, UUID> {

  List<JUserPromotion> findByPromotionIdAndGraduatedTrue(UUID promotionId);

  boolean existsByUserIdAndPromotionId(UUID userId, UUID promotionId);

  @Query(
      """
      select up from JUserPromotion up
      where (cast(:userId as uuid) is null or up.user.id = :userId)
        and (cast(:promotionId as uuid) is null or up.promotion.id = :promotionId)
        and (cast(:graduated as boolean) is null or up.graduated = :graduated)
      """)
  Page<JUserPromotion> search(
      @Param("userId") UUID userId,
      @Param("promotionId") UUID promotionId,
      @Param("graduated") Boolean graduated,
      Pageable pageable);
}
