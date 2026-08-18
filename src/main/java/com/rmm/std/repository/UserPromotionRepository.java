package com.rmm.std.repository;

import com.rmm.std.constant.PromotionStatus;
import com.rmm.std.repository.model.JUserPromotion;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserPromotionRepository extends JpaRepository<JUserPromotion, UUID> {

  List<JUserPromotion> findByPromotionIdAndStatus(UUID promotionId, PromotionStatus status);

  List<JUserPromotion> findByPromotionId(UUID promotionId);

  boolean existsByUserIdAndPromotionId(UUID userId, UUID promotionId);

  Optional<JUserPromotion> findByUserIdAndPromotionId(UUID userId, UUID promotionId);

  @Query(
      """
      select up from JUserPromotion up
      where (cast(:userId as uuid) is null or up.user.id = :userId)
        and (cast(:promotionId as uuid) is null or up.promotion.id = :promotionId)
        and up.status = :status
      """)
  Page<JUserPromotion> search(
      @Param("userId") UUID userId,
      @Param("promotionId") UUID promotionId,
      @Param("status") PromotionStatus status,
      Pageable pageable);

  @Query(
      """
      select up from JUserPromotion up
      where (cast(:userId as uuid) is null or up.user.id = :userId)
        and (cast(:promotionId as uuid) is null or up.promotion.id = :promotionId)
      """)
  Page<JUserPromotion> searchWithoutStatus(
      @Param("userId") UUID userId, @Param("promotionId") UUID promotionId, Pageable pageable);
}
