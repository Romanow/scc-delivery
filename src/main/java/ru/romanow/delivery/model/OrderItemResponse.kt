package ru.romanow.delivery.model

import ru.romanow.delivery.model.enums.OrderState
import java.util.*

data class OrderItemResponse(
    val orderUid: UUID? = null,
    val state: OrderState? = null,
    val items: List<ItemsInfo>? = null
)