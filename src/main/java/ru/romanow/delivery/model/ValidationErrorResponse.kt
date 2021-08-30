package ru.romanow.delivery.model

data class ValidationErrorResponse(
    val message: String,
    val errors: List<ErrorDescription>
)