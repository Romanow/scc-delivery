package ru.romanow.delivery.model

import javax.validation.constraints.NotEmpty

data class DeliveryRequest(
    @field:NotEmpty(message = "{field.is.empty")
    val address: String? = null,
    @field:NotEmpty(message = "{field.is.empty")
    val firstName: String? = null,
    val lastName: String? = null
)