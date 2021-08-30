package ru.romanow.delivery.web

import io.swagger.v3.oas.annotations.Hidden
import org.slf4j.LoggerFactory
import ru.romanow.delivery.service.DeliveryManageService
import java.util.UUID
import org.springframework.web.bind.MethodArgumentNotValidException
import ru.romanow.delivery.web.ExceptionController
import ru.romanow.delivery.exceptions.OrderNotReadyException
import ru.romanow.delivery.exceptions.RestRequestException
import org.springframework.validation.FieldError
import java.util.stream.Collectors
import javax.validation.constraints.NotEmpty
import ru.romanow.delivery.model.enums.OrderState
import org.springframework.web.client.RestTemplate
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Enumerated
import ru.romanow.delivery.domain.enums.DeliveryState
import ru.romanow.delivery.domain.Delivery
import ru.romanow.delivery.repository.DeliveryRepository
import ru.romanow.delivery.service.DeliveryManageServiceImpl
import org.springframework.web.client.RestClientResponseException
import java.lang.RuntimeException
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.boot.autoconfigure.SpringBootApplication
import kotlin.jvm.JvmStatic
import org.springframework.boot.SpringApplication
import org.springframework.http.HttpStatus
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*
import ru.romanow.delivery.model.*
import java.lang.Exception

@Hidden
@RestControllerAdvice
class ExceptionController {
    private val logger = LoggerFactory.getLogger(ExceptionController::class.java)

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun badRequest(exception: MethodArgumentNotValidException): ValidationErrorResponse {
        val bindingResult = exception.bindingResult
        return ValidationErrorResponse(buildMessage(bindingResult), buildErrors(bindingResult))
    }

    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    @ExceptionHandler(OrderNotReadyException::class)
    fun notAcceptable(exception: OrderNotReadyException): ErrorResponse {
        return ErrorResponse(exception.message!!)
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(RestRequestException::class)
    fun conflict(exception: RestRequestException): ErrorResponse {
        logger.warn(exception.message)
        return ErrorResponse(exception.message!!)
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(RuntimeException::class)
    fun error(exception: RuntimeException): ErrorResponse {
        logger.error("", exception)
        return ErrorResponse(exception.message!!)
    }

    private fun buildMessage(bindingResult: BindingResult): String {
        return String.format("Error on %s, rejected errors [%s]",
            bindingResult.target,
            bindingResult.allErrors
                .stream()
                .map { it.defaultMessage }
                .collect(Collectors.joining(",")))
    }

    private fun buildErrors(bindingResult: BindingResult): List<ErrorDescription> {
        return bindingResult.fieldErrors
            .stream()
            .map { ErrorDescription(it.field, it.defaultMessage!!) }
            .collect(Collectors.toList())
    }
}