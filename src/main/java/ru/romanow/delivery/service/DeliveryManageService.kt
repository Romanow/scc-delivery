package ru.romanow.delivery.service

import ru.romanow.delivery.model.DeliveryRequest
import java.util.*
import javax.annotation.Nonnull

interface DeliveryManageService {
    fun deliver(@Nonnull orderUid: UUID, @Nonnull request: DeliveryRequest)
}