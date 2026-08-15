package com.rmm.std.repository;

import com.rmm.std.repository.model.JGradeHistory;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GradeHistoryRepository extends JpaRepository<JGradeHistory, UUID> {

  List<JGradeHistory> findByGradeIdOrderByModifiedAtDesc(UUID gradeId);
}
