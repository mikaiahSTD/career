package com.rmm.std.repository;

import com.rmm.std.repository.model.JGrade;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GradeRepository extends JpaRepository<JGrade, UUID> {

  boolean existsByExamIdAndStudentId(UUID examId, UUID studentId);

  List<JGrade> findByStudentId(UUID studentId);

  @Query(
      """
      select g from JGrade g
      where (cast(:examId as uuid) is null or g.exam.id = :examId)
        and (cast(:studentId as uuid) is null or g.student.id = :studentId)
      """)
  Page<JGrade> search(
      @Param("examId") UUID examId, @Param("studentId") UUID studentId, Pageable pageable);

  @Query(
      """
      select g from JGrade g
      where (cast(:examId as uuid) is null or g.exam.id = :examId)
        and (cast(:studentId as uuid) is null or g.student.id = :studentId)
        and g.exam.course.id in (
          select ct.course.id from JCourseTeacher ct where ct.teacher.id = :teacherId
        )
      """)
  Page<JGrade> searchForTeacher(
      @Param("teacherId") UUID teacherId,
      @Param("examId") UUID examId,
      @Param("studentId") UUID studentId,
      Pageable pageable);

  @Query(
      """
      select count(g) > 0 from JGrade g
      where g.id = :gradeId
        and g.exam.course.id in (
          select ct.course.id from JCourseTeacher ct where ct.teacher.id = :teacherId
        )
      """)
  boolean existsByIdAndTeacherTeachesCourse(
      @Param("gradeId") UUID gradeId, @Param("teacherId") UUID teacherId);

  @Query(
      """
      select count(e) > 0 from JExam e
      where e.id = :examId
        and e.course.id in (
          select ct.course.id from JCourseTeacher ct where ct.teacher.id = :teacherId
        )
      """)
  boolean existsExamByTeacherTeachesCourse(
      @Param("examId") UUID examId, @Param("teacherId") UUID teacherId);

  @Query(
      """
      select distinct g from JGrade g
      join g.exam e
      join JCareerCourse cc on cc.course = e.course
      where g.student.id = :studentId
        and cc.semesterNumber = :semesterNumber
        and cc.career.id in (
          select gr.career.id from JGroup gr
          join JUserGroup ug on ug.group = gr
          where ug.user.id = :studentId and ug.endDate is null
        )
      """)
  List<JGrade> findGradesForStudentInSemester(
      @Param("studentId") UUID studentId, @Param("semesterNumber") Integer semesterNumber);
}
