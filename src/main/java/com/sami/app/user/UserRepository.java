package com.sami.app.user;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.sami.app.account.Role;

public interface UserRepository extends JpaRepository<User, Long> {

    @EntityGraph(attributePaths = "account")
    Optional<User> findByAccount_Id(UUID accountId);

    @EntityGraph(attributePaths = "account")
    Optional<User> findWithAccountById(Long id);

    @EntityGraph(attributePaths = "account")
    Page<User> findAllBy(Pageable pageable);

    @EntityGraph(attributePaths = "account")
    Page<User> findByAccount_Role(Role role, Pageable pageable);
}
