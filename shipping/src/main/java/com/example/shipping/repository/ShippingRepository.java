package com.example.shipping.repository;

import com.example.shipping.entity.Shipping;
import org.springframework.data.repository.CrudRepository;

public interface ShippingRepository extends CrudRepository<Shipping, Long> {
}
