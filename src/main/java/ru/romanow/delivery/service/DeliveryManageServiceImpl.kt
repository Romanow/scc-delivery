package ru.romanow.delivery.service

import com.google.gson.Gson
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.RestTemplate
import ru.romanow.delivery.domain.Delivery
import ru.romanow.delivery.domain.enums.DeliveryState
import ru.romanow.delivery.exceptions.OrderNotReadyException
import ru.romanow.delivery.exceptions.RestRequestException
import ru.romanow.delivery.model.DeliveryRequest
import ru.romanow.delivery.model.ErrorResponse
import ru.romanow.delivery.model.OrderItemResponse
import ru.romanow.delivery.model.enums.OrderState
import ru.romanow.delivery.repository.DeliveryRepository
import java.util.*
import javax.annotation.Nonnull

@Service
class DeliveryManageServiceImpl(
    private val restTemplate: RestTemplate,
    private val deliveryRepository: DeliveryRepository,
    @Value("\${warehouse.service.url}")
    private val warehouseUrl: String,
) : DeliveryManageService {
    private val logger = LoggerFactory.getLogger(DeliveryManageServiceImpl::class.java)

    override fun deliver(@Nonnull orderUid: UUID, @Nonnull request: DeliveryRequest) {
        val response: OrderItemResponse = makeWarehouseCheckoutRequest(orderUid)
        if (response.state !== OrderState.READY_FOR_DELIVERY) {
            throw OrderNotReadyException("Order '$orderUid' has invalid state")
        }

        var delivery = Delivery(
            orderUid = orderUid,
            state = DeliveryState.DELIVERED,
            firstName = request.firstName,
            lastName = request.lastName,
            address = request.address
        )
        delivery = deliveryRepository.save(delivery)
        logger.info("Created new delivery '{}'", delivery)
    }

    @Nonnull
    private fun makeWarehouseCheckoutRequest(@Nonnull orderUid: UUID): OrderItemResponse {
        val url = "${warehouseUrl}/api/v1/items/$orderUid/checkout"
        return try {
            Optional.ofNullable(restTemplate.postForObject(url, null, OrderItemResponse::class.java))
                .orElseThrow { RestRequestException("Warehouse returned empty response") }
        } catch (exception: RestClientResponseException) {
            val response = Gson().fromJson(exception.responseBodyAsString, ErrorResponse::class.java)
            throw RestRequestException("Error request to '$url': ${exception.rawStatusCode}:${response.message}")
        }
    }
}