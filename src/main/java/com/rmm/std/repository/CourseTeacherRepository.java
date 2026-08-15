package com.rmm.std.repository;

import com.rmm.std.repository.model.JCourseTeacher;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseTeacherRepository extends JpaRepository<JCourseTeacher, UUID> {

  boolean existsByCourseIdAndTeacherId(UUID courseId, UUID teacherId);

  @Query(
      """
      select ct from JCourseTeacher ct
      where (cast(:courseId as uuid) is null or ct.course.id = :courseId)
        and (cast(:teacherId as uuid) is null or ct.teacher.id = :teacherId)
      """)
  Page<JCourseTeacher> search(
      @Param("courseId") UUID courseId, @Param("teacherId") UUID teacherId, Pageable pageable);
}
