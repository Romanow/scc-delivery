package ru.romanow.delivery.web

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import ru.romanow.delivery.model.DeliveryRequest
import ru.romanow.delivery.model.ErrorResponse
import ru.romanow.delivery.model.ValidationErrorResponse
import ru.romanow.delivery.service.DeliveryManageService
import java.util.*
import javax.validation.Valid

@Tag(name = "Delivery")
@RestController
@RequestMapping("/api/v1/delivery")
class DeliveryController(
    private val deliveryManageService: DeliveryManageService
) {

    @Operation(
        summary = "Deliver order",
        responses = [
            ApiResponse(
                responseCode = "202",
                description = "Order apply for delivery"
            ),
            ApiResponse(
                responseCode = "400",
                description = "Wrong request",
                content = [Content(schema = Schema(implementation = ValidationErrorResponse::class))]
            ),
            ApiResponse(
                responseCode = "406",
                description = "Order already delivered",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            ),
            ApiResponse(
                responseCode = "409",
                description = "Request to external system failed",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            )
        ]
    )
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PostMapping(value = ["/{orderUid}/deliver"], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun deliver(@PathVariable orderUid: UUID, @Valid @RequestBody request: DeliveryRequest) {
        deliveryManageService.deliver(orderUid, request)
    }
}