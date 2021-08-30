package ru.romanow.delivery.web;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.RegexPattern;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import ru.romanow.delivery.exceptions.OrderNotReadyException;
import ru.romanow.delivery.exceptions.RestRequestException;
import ru.romanow.delivery.model.ErrorResponse;
import ru.romanow.delivery.service.DeliveryManageService;
import ru.romanow.delivery.model.DeliveryRequest;

import java.util.UUID;
import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static java.lang.String.format;
import static java.util.UUID.fromString;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.junit.jupiter.params.provider.Arguments.of;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.springframework.cloud.contract.wiremock.restdocs.SpringCloudContractRestDocs.dslContract;
import static org.springframework.cloud.contract.wiremock.restdocs.WireMockRestDocs.verify;
import static org.springframework.data.util.ReflectionUtils.findConstructor;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(DeliveryController.class)
@AutoConfigureRestDocs
class DeliveryControllerTest {
    private static final UUID ORDER_UID_SUCCESS = fromString("1a1f775c-4f31-4256-bec1-c3d4e9bf1b52");
    private static final UUID ORDER_UID_NOT_READY = fromString("fc1f6904-6a27-4fa0-ac05-64233d111a23");
    private static final UUID ORDER_UID_WH_ERROR = fromString("b7db3e82-7ade-464f-a336-dd3b91709781");
    private static final String STRING_VALUE = "";

    @MockBean
    private DeliveryManageService deliveryManageService;

    @Autowired
    private MockMvc mockMvc;

    private static final Gson gson = new Gson();

    @Test
    void deliverSuccess()
            throws Exception {
        final DeliveryRequest request = new DeliveryRequest()
                .setAddress(randomAlphanumeric(10))
                .setFirstName(randomAlphabetic(10))
                .setLastName(randomAlphabetic(10));
        mockMvc.perform(post(format("/api/v1/delivery/%s/deliver", ORDER_UID_SUCCESS))
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(gson.toJson(request)))
                .andExpect(status().isAccepted())
                .andDo(verify()
                        .wiremock(WireMock.post(
                                urlMatching(format("/api/v1/delivery/%s/deliver", ORDER_UID_SUCCESS)))
                                .withRequestBody(matchingJsonPath("$.firstName", new RegexPattern("\\S{10}")))
                                .withRequestBody(matchingJsonPath("$.lastName", new RegexPattern("\\S{10}")))
                                .withRequestBody(matchingJsonPath("$.address", new RegexPattern("\\S{10}")))
                        )
                        .stub("deliverSuccess"))
                .andDo(document("deliverySuccess",
                        requestFields(
                                fieldWithPath("address").description("Delivery address").type(JsonFieldType.STRING),
                                fieldWithPath("firstName").description("First name").type(JsonFieldType.STRING),
                                fieldWithPath("lastName").description("Last name").optional().type(JsonFieldType.STRING)
                        ),
                        dslContract())
                );
    }

    @ParameterizedTest
    @MethodSource("provideErrorRequest")
    <T extends RuntimeException> void testErrorRequest(String name, Class<T> cls, UUID orderUid, String message, HttpStatus httpStatus)
            throws Exception {
        final DeliveryRequest request = new DeliveryRequest()
                .setAddress(randomAlphanumeric(10))
                .setFirstName(randomAlphabetic(10))
                .setLastName(randomAlphabetic(10));

        final T exception = (T) findConstructor(cls, STRING_VALUE).get().newInstance(message);
        doThrow(exception)
                .when(deliveryManageService)
                .deliver(eq(orderUid), eq(request));

        mockMvc.perform(post(format("/api/v1/delivery/%s/deliver", orderUid))
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(gson.toJson(request)))
                .andExpect(status().is(httpStatus.value()))
                .andExpect(jsonPath("$.message").value(message))
                .andDo(verify()
                        .wiremock(WireMock.post(
                                urlMatching(format("/api/v1/delivery/%s/deliver", orderUid)))
                                .withRequestBody(matchingJsonPath("$.address", new RegexPattern("\\S{10}")))
                                .withRequestBody(matchingJsonPath("$.firstName", new RegexPattern("\\S{10}")))
                                .withRequestBody(matchingJsonPath("$.lastName", new RegexPattern("\\S{10}")))
                        )
                        .stub(name))
                .andDo(document(name,
                        requestFields(
                                fieldWithPath("address").description("Delivery address").type(JsonFieldType.STRING),
                                fieldWithPath("firstName").description("First name").type(JsonFieldType.STRING),
                                fieldWithPath("lastName").description("Last name").optional().type(JsonFieldType.STRING)
                        ),
                        responseFields(
                                fieldWithPath("message").description("Error description").type(JsonFieldType.STRING)
                        ),
                        dslContract())
                );
    }

    private static Stream<Arguments> provideErrorRequest() {
        final String url = "/items/" + ORDER_UID_WH_ERROR + "/checkout";
        final ErrorResponse errorResponse = new ErrorResponse(format("Order '%s' not found", ORDER_UID_NOT_READY));
        final String deliverRequestErrorMessage = format("Error request to '%s': %d:%s", url, NOT_FOUND.value(), gson.toJson(errorResponse));
        final String deliveryNotReadyMessage = format("Order '%s' has invalid state", ORDER_UID_WH_ERROR);

        return Stream.of(
                of("deliveryNotReady", OrderNotReadyException.class, ORDER_UID_NOT_READY, deliveryNotReadyMessage, NOT_ACCEPTABLE),
                of("deliveryRequestError", RestRequestException.class, ORDER_UID_WH_ERROR, deliverRequestErrorMessage, CONFLICT)
        );
    }
}