package com.orion.mdd.user;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Reads and writes the users.
 */
public interface UserRepository extends JpaRepository<User, Long> {

}
