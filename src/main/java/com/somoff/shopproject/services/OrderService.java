package com.somoff.shopproject.services;

import com.somoff.shopproject.entities.*;
import com.somoff.shopproject.repositories.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductService productService;

    @Autowired
    public OrderService(OrderRepository orderRepository, ProductService productService) {
        this.orderRepository = orderRepository;
        this.productService = productService;
    }

    @Transactional(readOnly = true)
    public List<Order> getOrders(User user) {
        if (user.getRoles().contains(Role.USER)) {
            return orderRepository.findAllByUser(user);
        } else if (user.getRoles().contains(Role.ADMIN) || user.getRoles().contains(Role.MANAGER)) {
            return orderRepository.findAll();
        }
        return null;
    }

    @Transactional
    public void updateOrder(User user, Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId).orElse(null);
        assert order != null;
        OrderStatus initStatus  = order.getStatus();
            if (order.getUser().getEmail().equals(user.getEmail())) {
                if (status.equals(OrderStatus.canceled) && (initStatus.equals(OrderStatus.confirmed) || initStatus.equals(OrderStatus.in_processing))) {
                    order.setStatus(status);
                    returnItems(user, orderId);
                }
                if (status.equals(OrderStatus.completed) && (initStatus.equals(OrderStatus.sent) || initStatus.equals(OrderStatus.delivered))) {
                    order.setStatus(status);
                }
                orderRepository.save(order);
            } else if (user.getRoles().contains(Role.ADMIN) || user.getRoles().contains(Role.MANAGER)) {
                if (status.equals(OrderStatus.canceled) && (!initStatus.equals(OrderStatus.completed))) {
                    order.setStatus(status);
                    returnItems(user, orderId);
                }
                if (status.equals(OrderStatus.in_processing) && (initStatus.equals(OrderStatus.confirmed))) {
                    order.setStatus(status);
                }
                if (status.equals(OrderStatus.sent) && (initStatus.equals(OrderStatus.in_processing))) {
                    order.setStatus(status);
                }
                if (status.equals(OrderStatus.delivered) && (initStatus.equals(OrderStatus.sent))) {
                    order.setStatus(status);
                }
                if (status.equals(OrderStatus.completed) && (initStatus.equals(OrderStatus.delivered))) {
                    order.setStatus(status);
                }
                orderRepository.save(order);
            }

    }

    private void returnItems(User user, Long orderId) {
        Order userOrder = getUserOrder(orderId);
        for (CartProduct cartProduct : userOrder.getProductList()) {
            Integer amount = cartProduct.getAmount();
            // update in Stack
            Product product = productService.findProductByIdForReturnInStack(cartProduct.getProduct().getId(), user);
            Integer amount1 = productService.getProductAmountById(cartProduct.getProduct().getId()).getAmount();
            productService.updateProductAmount(product, amount1 + amount);
        }
    }

    private Order getUserOrder(Long orderId) {
        return orderRepository.findById(orderId).orElse(null);

    }
}
