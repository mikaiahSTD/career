package com.rmm.std.service;

import com.rmm.std.dto.CourseRequest;
import com.rmm.std.dto.CourseResponse;
import com.rmm.std.dto.PageResponse;
import com.rmm.std.exception.ConflictException;
import com.rmm.std.exception.NotFoundException;
import com.rmm.std.mapper.CourseMapper;
import com.rmm.std.repository.CourseRepository;
import com.rmm.std.repository.model.JCourse;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class CourseService {

  private final CourseRepository courseRepository;
  private final CourseMapper courseMapper;

  @Transactional
  public CourseResponse create(CourseRequest req) {
    if (courseRepository.existsByRef(req.getRef())) {
      throw new ConflictException("Course with ref: " + req.getRef() + " already exists");
    }
    JCourse saved = courseRepository.save(courseMapper.toJ(courseMapper.toDomain(req)));
    return courseMapper.toRes(saved);
  }

  public PageResponse<CourseResponse> list(Pageable pageable) {
    Page<JCourse> page = courseRepository.findAll(pageable);
    return PageResponse.from(page, page.map(courseMapper::toRes).toList());
  }

  public CourseResponse get(UUID id) {
    return courseMapper.toRes(getEntity(id));
  }

  @Transactional
  public CourseResponse update(UUID id, CourseRequest req) {
    JCourse existing = getEntity(id);
    if (courseRepository.existsByRef(req.getRef()) && !existing.getRef().equals(req.getRef())) {
      throw new ConflictException("Course with ref: " + req.getRef() + " already exists");
    }
    existing.setRef(req.getRef());
    existing.setTitle(req.getTitle());
    existing.setCredits(req.getCredits());
    return courseMapper.toRes(courseRepository.save(existing));
  }

  @Transactional
  public void delete(UUID id) {
    getEntity(id);
    courseRepository.deleteById(id);
  }

  private JCourse getEntity(UUID id) {
    return courseRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("Course not found: " + id));
  }
}
