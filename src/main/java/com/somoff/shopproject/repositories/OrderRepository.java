package com.somoff.shopproject.repositories;

import com.somoff.shopproject.entities.Order;
import com.somoff.shopproject.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findAllByUser(User user);
}
