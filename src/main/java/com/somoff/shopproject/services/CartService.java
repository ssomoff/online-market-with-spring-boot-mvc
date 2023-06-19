package com.somoff.shopproject.services;

import com.somoff.shopproject.entities.*;
import com.somoff.shopproject.repositories.CartProductRepository;
import com.somoff.shopproject.repositories.CartRepository;
import com.somoff.shopproject.repositories.OrderRepository;
import com.somoff.shopproject.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class CartService {

    @Value("${deliveryPrice}")
    private Double deliveryPrice;

    @Value("${freeDeliveryPrice}")
    private Double freeDeliveryPrice;

    @Value("${firstDeliveryTime}")
    private int firstDeliveryTime;

    @Value("${secondDeliveryTime}")
    private int secondDeliveryTime;

    private final CartRepository cartRepository;
    private final CartProductRepository cartProductRepository;
    private final ProductService productService;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;


    @Autowired
    public CartService(CartRepository cartRepository, CartProductRepository cartProductRepository,
                       ProductService productService, UserRepository userRepository, OrderRepository orderRepository) {
        this.cartRepository = cartRepository;
        this.cartProductRepository = cartProductRepository;
        this.productService = productService;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public void addCartProduct(User user, Integer amount, Long productId) {
        Cart cart = getUserCart(user);
        addProductInCart(cart, amount, productId, user);
        // update in Stack
        Product product = productService.findProductById(productId, user);
        Integer amountInStack = productService.getProductAmountById(productId).getAmount();
        productService.updateProductAmount(product, amountInStack - amount);
    }

    @Transactional
    public void deleteCartProduct(User user, Long productId) {
        Cart cart = getUserCart(user);
        CartProduct cartProduct = deleteProductInCart(cart, productId);
        Integer amount = cartProduct.getAmount();
        // update in Stack
        Product product = productService.findProductByIdForReturnInStack(productId, user);
        Integer amount1 = productService.getProductAmountById(productId).getAmount();
        productService.updateProductAmount(product, amount1 + amount);
    }

    @Transactional(readOnly = true)
    public Double getCartPrice(User user) {
        Cart cart = getCartByUser(user);
        return cart.getCartPrice();
    }

    @Transactional(readOnly = true)
    public List<CartProduct> getCartProductList(User user) {
        Cart cart = getCartByUser(user);
        return cart.getProductList();
    }
    public void createOrder(User user) {
        Cart cart = getUserCart(user);
        List<Date> list = getDate();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");
        String d1 = simpleDateFormat.format(list.get(1));
        String d2 = simpleDateFormat.format(list.get(2));
        String date = d1 + " - " + d2;
        Order order = new Order();
        order.setDate(date);
        order.setProductList(cart.getProductList());
        order.setStatus(OrderStatus.confirmed);
        //price delivery
        if (cart.getCartPrice() >= freeDeliveryPrice) {
            order.setOrderPrice(cart.getCartPrice());
        } else {
            order.setOrderPrice(cart.getCartPrice() + deliveryPrice);
        }
        order.setUserAddress(user.getAddress());
        order.setUserPhone(user.getPhone());
        order.setUserName(user.getFirstName());
        order.setUser(userRepository.findByEmail(user.getEmail()).orElse(null));
        orderRepository.save(order);
        clearCart(user);
    }

    public Cart createCart(User user) {
        Cart cart = new Cart();
        cart.setUser(user);
        cart.setCartPrice(0.0);
        List<CartProduct> productList = new ArrayList<>();
        cart.setProductList(productList);
        return addCart(cart);
    }

    public void clearCart(User user) {
        Cart userCart = getUserCart(user);
        for (CartProduct cartProduct : userCart.getProductList()) {
            Integer amount = cartProduct.getAmount();
            // update in Stack
            Product pr = productService.findProductByIdForReturnInStack(cartProduct.getProduct().getId(), user);
            Integer amount1 = productService.getProductAmountById(cartProduct.getProduct().getId()).getAmount();
            productService.updateProductAmount(pr, amount1 + amount);
        }
        userCart.clearProductList();
        addCart(userCart);
    }

    public List<Date> getDate() {
        List<Date> list = new ArrayList<>();
        Date localDate = new Date();
        list.add(localDate);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(localDate);
        calendar.add(Calendar.WEEK_OF_MONTH, firstDeliveryTime);
        list.add(calendar.getTime());
        calendar.add(Calendar.WEEK_OF_MONTH, secondDeliveryTime);
        list.add(calendar.getTime());
        return list;
    }

    private void addProductInCart(Cart cart, Integer amount, Long productId, User user) {
        List<CartProduct> list = cart.getProductList();
        CartProduct cartProduct = list.stream()
                .filter(e -> e.getProduct().getId().equals(productId))
                .peek(e -> {
                    Integer i = e.getAmount();
                    e.setAmount(i + amount);
                })
                .findFirst().orElse(null);
        if (cartProduct == null) {
            cartProduct = new CartProduct();
            cartProduct.setCart(cart);
            cartProduct.setProduct(productService.findProductById(productId, user));
            cartProduct.setAmount(amount);
        }
        cartProductRepository.save(cartProduct);
        list.removeIf(el -> el.getProduct().getId().equals(productId));
        list.add(cartProduct);
        cart.setProductList(list);
        Double i = list.stream().map(CartProduct::getAmountPrice).reduce(0.0, Double::sum);
        cart.setCartPrice(i);
        addCart(cart);
    }

    private CartProduct deleteProductInCart(Cart cart, Long productId) {
        List<CartProduct> list = cart.getProductList();
        Double cartPrice = cart.getCartPrice();
        CartProduct cartProduct = list.stream().filter(e -> e.getProduct().getId().equals(productId))
                .findFirst().orElse(null);
        assert cartProduct != null;
        list.remove(cartProduct);
        cart.setProductList(list);
        cart.setCartPrice(cartPrice - cartProduct.getAmountPrice());
        addCart(cart);
        cartProductRepository.delete(cartProduct);
        return cartProduct;
    }

    private Cart addCart(Cart cart) {
        return cartRepository.save(cart);
    }

    private Cart getCartByUser(User user) {
        return cartRepository.findByUser(user).orElse(new Cart());
    }

    private Cart getUserCart(User user) {
        User authUser = userRepository.findByEmail(user.getEmail()).orElse(null);
        assert authUser != null;
        return authUser.getCart();
    }
}
