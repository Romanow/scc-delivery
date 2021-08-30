package ru.romanow.delivery.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import ru.romanow.delivery.domain.Delivery;
import ru.romanow.delivery.model.OrderItemResponse;
import ru.romanow.delivery.model.enums.OrderState;
import ru.romanow.delivery.domain.enums.DeliveryState;
import ru.romanow.delivery.exceptions.OrderNotReadyException;
import ru.romanow.delivery.exceptions.RestRequestException;
import ru.romanow.delivery.model.DeliveryRequest;
import ru.romanow.delivery.repository.DeliveryRepository;

import javax.annotation.Nonnull;
import java.util.UUID;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static org.slf4j.LoggerFactory.getLogger;

@Service
@RequiredArgsConstructor
public class DeliveryManageServiceImpl
        implements DeliveryManageService {
    private static final Logger logger = getLogger(DeliveryManageServiceImpl.class);

    private static final String WAREHOUSE_URL = "http://warehouse:8070/api/v1/items/";
    private static final String CHECKOUT_PATH = "/checkout";

    private final RestTemplate restTemplate;
    private final DeliveryRepository deliveryRepository;

    @Override
    public void deliver(@Nonnull UUID orderUid, @Nonnull DeliveryRequest request) {
        final OrderItemResponse response = makeWarehouseCheckoutRequest(orderUid);
        if (response.getState() != OrderState.READY_FOR_DELIVERY) {
            throw new OrderNotReadyException(format("Order '%s' has invalid state", orderUid));
        }

        Delivery delivery = new Delivery()
                .setOrderUid(orderUid)
                .setState(DeliveryState.DELIVERED)
                .setFirstName(request.getFirstName())
                .setLastName(request.getLastName())
                .setAddress(request.getAddress());

        delivery = deliveryRepository.save(delivery);
        logger.debug("Created new delivery '{}'", delivery);
    }

    @Nonnull
    private OrderItemResponse makeWarehouseCheckoutRequest(@Nonnull UUID orderUid) {
        final String url = format("%s%s%s", WAREHOUSE_URL, orderUid, CHECKOUT_PATH);
        try {
            return ofNullable(restTemplate.postForObject(url, null, OrderItemResponse.class))
                    .orElseThrow(() -> new RestRequestException("Warehouse returned empty response"));
        } catch (RestClientResponseException exception) {
            final String message = format("Error request to '%s': %d:%s", url, exception.getRawStatusCode(), exception.getResponseBodyAsString());
            throw new RestRequestException(message);
        }

    }
}
