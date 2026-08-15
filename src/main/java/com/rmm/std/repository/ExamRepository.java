package com.rmm.std.repository;

import com.rmm.std.repository.model.JExam;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExamRepository extends JpaRepository<JExam, UUID> {

  Page<JExam> findByCourseId(UUID courseId, Pageable pageable);
}
