package com.somoff.shopproject.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "carts")
public class Cart implements Serializable {

    private static final String SEQ_NAME = "cart_seq";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQ_NAME)
    @SequenceGenerator(name = SEQ_NAME, sequenceName = SEQ_NAME, allocationSize = 1)
    @Column(name = "cart_id")
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "cart_price")
    private Double cartPrice;

    @OneToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "carts_cart_product", joinColumns = @JoinColumn(name = "cart_id"),
            inverseJoinColumns = @JoinColumn(name = "cart_product_id"))
    private List<CartProduct> productList = new ArrayList<>();

    public void clearProductList() {
        this.getProductList().clear();
        this.cartPrice = 0.0;
    }

}
