package com.rmm.std.repository;

import com.rmm.std.repository.model.JCareer;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CareerRepository extends JpaRepository<JCareer, UUID> {}
