package ru.romanow.delivery.service;

import groovy.json.JsonOutput;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.romanow.delivery.exceptions.RestRequestException;
import ru.romanow.delivery.model.ErrorResponse;
import ru.romanow.delivery.DeliveryTestConfiguration;
import ru.romanow.delivery.model.DeliveryRequest;

import java.util.UUID;

import static groovy.json.JsonOutput.toJson;
import static java.lang.String.format;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@DirtiesContext
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = DeliveryTestConfiguration.class)
@AutoConfigureStubRunner(
        ids = {
                "ru.romanow.scc:warehouse:[1.0.0,2.0.0):stubs:8070",
        },
        mappingsOutputFolder = "build/mappings",
        repositoryRoot = "https://dl.bintray.com/ronin/scc-microservices",
        stubsMode = StubRunnerProperties.StubsMode.REMOTE)
class DeliveryManageServiceTest {
    private static final String WAREHOUSE_URL = "http://warehouse:8070/api/v1/items/";
    private static final String CHECKOUT_PATH = "/checkout";

    private static final UUID ORDER_UID_SUCCESS = UUID.fromString("1a1f775c-4f31-4256-bec1-c3d4e9bf1b52");
    private static final UUID ORDER_UID_NOT_FOUND = UUID.fromString("36856fc6-d6ec-47cb-bbee-d20e78299eb9");

    @Autowired
    private DeliveryManageService deliveryManageService;

    @Test
    void deliverSuccess() {
        final DeliveryRequest request =
                new DeliveryRequest()
                .setFirstName(randomAlphabetic(10))
                .setLastName(randomAlphabetic(10))
                .setAddress(randomAlphabetic(10));
        deliveryManageService.deliver(ORDER_UID_SUCCESS, request);
    }

    @Test
    void deliverOrderNotFound() {
        final UUID orderUid = ORDER_UID_NOT_FOUND;
        final DeliveryRequest request =
                new DeliveryRequest()
                        .setFirstName(randomAlphabetic(10))
                        .setLastName(randomAlphabetic(10))
                        .setAddress(randomAlphabetic(10));
        try {
            deliveryManageService.deliver(orderUid, request);
        } catch (RestRequestException exception) {
            final String url = format("%s%s%s", WAREHOUSE_URL, orderUid, CHECKOUT_PATH);
            final String responseMessage = JsonOutput.toJson(new ErrorResponse(format("OrderItem '%s' not found", orderUid)));
            final String message = format("Error request to '%s': %d:%s", url, NOT_FOUND.value(), responseMessage);
            assertEquals(message, exception.getMessage());
            return;
        }

        Assertions.fail();
    }

}