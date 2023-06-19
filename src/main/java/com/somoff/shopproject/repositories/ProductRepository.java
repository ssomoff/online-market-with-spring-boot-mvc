package com.somoff.shopproject.repositories;

import com.somoff.shopproject.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("select e from Product e where e.name like ?1%")
    List<Product> findProductsByName(String name);

    @Query("select e from Product e where e.name like ?1% and e.active=true")
    List<Product> findProductsByNameAndActive(String name);

    @Query("select e from Product e where e.popularity = ?1 and e.active=true")
    List<Product> findProductsByPopularityAndActive(String popularity);

    @Query("select e from Product e where e.id = ?1 and e.active=true")
    Optional<Product> findProductByIdAndActive(Long id);

    List<Product> findAllByActive(boolean active);

}
