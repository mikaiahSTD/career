package com.rmm.std.repository;

import com.rmm.std.repository.model.JCareerCourse;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CareerCourseRepository extends JpaRepository<JCareerCourse, UUID> {

  Page<JCareerCourse> findByCareerId(UUID careerId, Pageable pageable);

  boolean existsByCareerIdAndCourseId(UUID careerId, UUID courseId);

  @Query(
      """
      select coalesce(sum(cc.course.credits), 0) from JCareerCourse cc
      where cc.career.id = :careerId and cc.semesterNumber = :semesterNumber
      """)
  int sumCreditsByCareerIdAndSemesterNumber(
      @Param("careerId") UUID careerId, @Param("semesterNumber") Integer semesterNumber);

  @Query(
      "select coalesce(sum(cc.course.credits), 0) from JCareerCourse cc where cc.career.id ="
          + " :careerId")
  int sumCreditsByCareerId(@Param("careerId") UUID careerId);

  Optional<JCareerCourse> findByCareerIdAndCourseId(UUID careerId, UUID courseId);

  Optional<JCareerCourse> findByCourseId(UUID courseId);
}
