package ru.romanow.delivery.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.romanow.delivery.domain.Delivery;

public interface DeliveryRepository
        extends JpaRepository<Delivery, Integer> {}
