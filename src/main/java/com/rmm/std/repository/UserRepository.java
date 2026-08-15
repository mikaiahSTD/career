package com.rmm.std.repository;

import com.rmm.std.constant.Role;
import com.rmm.std.repository.model.JUser;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<JUser, UUID> {

  Optional<JUser> findByEmail(String email);

  List<JUser> findByRole(Role role);

  Page<JUser> findByRole(Role role, Pageable pageable);

  boolean existsByEmail(String email);
}
