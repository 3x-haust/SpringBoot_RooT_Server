package io.github._3xhaust.root_server.domain.user.repository;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import io.github._3xhaust.root_server.domain.user.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    boolean existsByName(String name);
    Optional<User> findByEmail(String email);
    Optional<User> findByName(String name);
}


