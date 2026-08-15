package com.rmm.std.repository;

import com.rmm.std.repository.model.JCourseGroup;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseGroupRepository extends JpaRepository<JCourseGroup, UUID> {

  boolean existsByCourseIdAndTeacherIdAndGroupId(UUID courseId, UUID teacherId, UUID groupId);

  @Query(
      """
      select cg from JCourseGroup cg
      where (cast(:courseId as uuid) is null or cg.course.id = :courseId)
        and (cast(:teacherId as uuid) is null or cg.teacher.id = :teacherId)
        and (cast(:groupId as uuid) is null or cg.group.id = :groupId)
      """)
  Page<JCourseGroup> search(
      @Param("courseId") UUID courseId,
      @Param("teacherId") UUID teacherId,
      @Param("groupId") UUID groupId,
      Pageable pageable);
}
