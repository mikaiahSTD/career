package com.rmm.std.repository;

import com.rmm.std.repository.model.JCareerCourse;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CareerCourseRepository extends JpaRepository<JCareerCourse, UUID> {

  Page<JCareerCourse> findByCareerId(UUID careerId, Pageable pageable);

  boolean existsByCareerIdAndCourseId(UUID careerId, UUID courseId);
}
