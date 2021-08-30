package ru.romanow.delivery.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.romanow.delivery.domain.Delivery

interface DeliveryRepository : JpaRepository<Delivery, Int>