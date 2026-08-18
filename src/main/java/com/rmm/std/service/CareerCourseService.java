package com.rmm.std.service;

import com.rmm.std.dto.CareerCourseRequest;
import com.rmm.std.dto.CareerCourseResponse;
import com.rmm.std.dto.PageResponse;
import com.rmm.std.exception.BadRequestException;
import com.rmm.std.exception.ConflictException;
import com.rmm.std.exception.NotFoundException;
import com.rmm.std.mapper.CareerCourseMapper;
import com.rmm.std.repository.CareerCourseRepository;
import com.rmm.std.repository.CareerRepository;
import com.rmm.std.repository.CourseRepository;
import com.rmm.std.repository.model.JCareerCourse;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class CareerCourseService {

  private static final int MAX_CREDITS_PER_SEMESTER = 30;
  private static final int MAX_CREDITS_PER_YEAR = 60;

  private final CareerCourseRepository careerCourseRepository;
  private final CareerRepository careerRepository;
  private final CourseRepository courseRepository;
  private final CareerCourseMapper careerCourseMapper;

  @Transactional
  public CareerCourseResponse create(CareerCourseRequest req) {
    if (!careerRepository.existsById(req.getCareerId())) {
      throw new NotFoundException("Career not found: " + req.getCareerId());
    }
    if (!courseRepository.existsById(req.getCourseId())) {
      throw new NotFoundException("Course not found: " + req.getCourseId());
    }
    if (careerCourseRepository.existsByCareerIdAndCourseId(req.getCareerId(), req.getCourseId())) {
      throw new ConflictException(
          "Course " + req.getCourseId() + " is already part of career " + req.getCareerId());
    }
    validateCredits(req.getCareerId(), req.getCourseId(), req.getSemesterNumber(), null);
    JCareerCourse saved =
        careerCourseRepository.save(careerCourseMapper.toJ(careerCourseMapper.toDomain(req)));
    return careerCourseMapper.toRes(saved);
  }

  public PageResponse<CareerCourseResponse> list(UUID careerId, Pageable pageable) {
    Page<JCareerCourse> page =
        careerId == null
            ? careerCourseRepository.findAll(pageable)
            : careerCourseRepository.findByCareerId(careerId, pageable);
    return PageResponse.from(page, page.map(careerCourseMapper::toRes).toList());
  }

  public CareerCourseResponse get(UUID id) {
    return careerCourseMapper.toRes(getEntity(id));
  }

  @Transactional
  public CareerCourseResponse update(UUID id, CareerCourseRequest req) {
    JCareerCourse existing = getEntity(id);
    if (!careerRepository.existsById(req.getCareerId())) {
      throw new NotFoundException("Career not found: " + req.getCareerId());
    }
    if (!courseRepository.existsById(req.getCourseId())) {
      throw new NotFoundException("Course not found: " + req.getCourseId());
    }
    validateCredits(req.getCareerId(), req.getCourseId(), req.getSemesterNumber(), id);
    existing.setCareer(
        careerRepository
            .findById(req.getCareerId())
            .orElseThrow(() -> new NotFoundException("Career not found: " + req.getCareerId())));
    existing.setCourse(
        courseRepository
            .findById(req.getCourseId())
            .orElseThrow(() -> new NotFoundException("Course not found: " + req.getCourseId())));
    existing.setSemesterNumber(req.getSemesterNumber());
    return careerCourseMapper.toRes(careerCourseRepository.save(existing));
  }

  @Transactional
  public void delete(UUID id) {
    getEntity(id);
    careerCourseRepository.deleteById(id);
  }

  private void validateCredits(
      UUID careerId, UUID courseId, Integer semesterNumber, UUID excludeId) {
    int courseCredits =
        courseRepository
            .findById(courseId)
            .orElseThrow(() -> new NotFoundException("Course not found: " + courseId))
            .getCredits();
    int currentSemesterTotal =
        careerCourseRepository.sumCreditsByCareerIdAndSemesterNumber(careerId, semesterNumber);
    if (excludeId != null) {
      JCareerCourse existing = getEntity(excludeId);
      if (existing.getSemesterNumber().equals(semesterNumber)
          && existing.getCourse().getId().equals(courseId)) {
        currentSemesterTotal -= courseCredits;
      }
    }
    if (currentSemesterTotal + courseCredits > MAX_CREDITS_PER_SEMESTER) {
      throw new BadRequestException(
          "Adding this course would exceed the maximum of "
              + MAX_CREDITS_PER_SEMESTER
              + " credits per semester (current total: "
              + currentSemesterTotal
              + ", course credits: "
              + courseCredits
              + ")");
    }
    int yearTotal = careerCourseRepository.sumCreditsByCareerId(careerId);
    if (excludeId != null) {
      JCareerCourse existing = getEntity(excludeId);
      if (existing.getCourse().getId().equals(courseId)) {
        yearTotal -= courseCredits;
      }
    }
    if (yearTotal + courseCredits > MAX_CREDITS_PER_YEAR) {
      throw new BadRequestException(
          "Adding this course would exceed the maximum of "
              + MAX_CREDITS_PER_YEAR
              + " credits per year (current total: "
              + yearTotal
              + ", course credits: "
              + courseCredits
              + ")");
    }
  }

  private JCareerCourse getEntity(UUID id) {
    return careerCourseRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("Career-course link not found: " + id));
  }
}
