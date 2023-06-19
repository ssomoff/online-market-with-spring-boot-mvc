package com.somoff.shopproject.repositories;

import com.somoff.shopproject.entities.Cart;
import com.somoff.shopproject.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUser(User user);
}
