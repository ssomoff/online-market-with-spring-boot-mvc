package com.somoff.shopproject.repositories;

import com.somoff.shopproject.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    User findByActivationCode(String code);
}
