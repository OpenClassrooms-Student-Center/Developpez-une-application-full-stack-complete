package com.orion.mdd.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Reads and writes the users. MySQL compares usernames and e-mails without regard to case.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

}
