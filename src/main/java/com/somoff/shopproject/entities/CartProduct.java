package com.somoff.shopproject.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cart_products")
public class CartProduct implements Serializable {

    private static final String SEQ_NAME = "cart_product_seq";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQ_NAME)
    @SequenceGenerator(name = SEQ_NAME, sequenceName = SEQ_NAME, allocationSize = 1)
    @Column(name = "cart_product_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "amount")
    private Integer amount;

    @Column(name = "amount_price")
    private Double amountPrice;


    public void setAmount(Integer amount) {
        this.amount = amount;
        this.amountPrice = product.getPrice() * amount;
    }
}
