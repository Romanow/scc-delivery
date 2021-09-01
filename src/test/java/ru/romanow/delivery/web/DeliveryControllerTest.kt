package ru.romanow.delivery.web

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.matching.RegexPattern
import com.google.gson.Gson
import org.apache.commons.lang3.RandomStringUtils
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties
import org.springframework.cloud.contract.wiremock.restdocs.SpringCloudContractRestDocs.dslContract
import org.springframework.cloud.contract.wiremock.restdocs.WireMockRestDocs.verify
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus.CONFLICT
import org.springframework.http.MediaType
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.payload.JsonFieldType.STRING
import org.springframework.restdocs.payload.PayloadDocumentation.*
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional
import ru.romanow.delivery.config.DatabaseTestConfiguration
import ru.romanow.delivery.model.DeliveryRequest
import java.util.*

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureStubRunner(
    ids = ["ru.romanow.scc:warehouse:[1.0.0,2.0.0):stubs:8070"],
    mappingsOutputFolder = "build/mappings",
    stubsMode = StubRunnerProperties.StubsMode.LOCAL
)
@Transactional
@AutoConfigureTestEntityManager
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Import(DatabaseTestConfiguration::class)
internal class DeliveryControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Value("\${warehouse.service.url}")
    private lateinit var warehouseUrl: String

    @Test
    fun deliverSuccess() {
        val request = DeliveryRequest(
            firstName = RandomStringUtils.randomAlphabetic(10),
            lastName = RandomStringUtils.randomAlphabetic(10),
            address = RandomStringUtils.randomAlphabetic(10)
        )

        mockMvc.perform(
            post("/api/v1/delivery/$ORDER_UID_SUCCESS/deliver")
                .contentType(MediaType.APPLICATION_JSON)
                .content(Gson().toJson(request))
        )
            .andExpect(status().isAccepted)
            .andDo(
                verify()
                    .wiremock(
                        post(urlEqualTo("/api/v1/delivery/$ORDER_UID_SUCCESS/deliver"))
                            .withRequestBody(matchingJsonPath("$.firstName", RegexPattern("\\S+")))
                            .withRequestBody(matchingJsonPath("$.lastName", RegexPattern("\\S+")))
                            .withRequestBody(matchingJsonPath("$.address", RegexPattern("\\S+")))
                    )
            )
            .andDo(
                document(
                    "deliverySuccess",
                    requestFields(
                        fieldWithPath("address").description("Delivery address").type(STRING),
                        fieldWithPath("firstName").description("First name").type(STRING),
                        fieldWithPath("lastName").description("Last name").optional().type(STRING)
                    ),
                    dslContract()
                )
            )
    }

    @Test
    fun testErrorRequest() {
        val request = DeliveryRequest(
            firstName = RandomStringUtils.randomAlphabetic(10),
            lastName = RandomStringUtils.randomAlphabetic(10),
            address = RandomStringUtils.randomAlphabetic(10)
        )

        val url = "$warehouseUrl/api/v1/items/$ORDER_UID_NOT_FOUND/checkout"
        val errorResponse = "OrderItem '$ORDER_UID_NOT_FOUND' not found"
        val message = "Error request to '$url': 404:$errorResponse"

        mockMvc.perform(
            post("/api/v1/delivery/$ORDER_UID_NOT_FOUND/deliver")
                .contentType(MediaType.APPLICATION_JSON)
                .content(Gson().toJson(request))
        )
            .andExpect(status().`is`(CONFLICT.value()))
            .andExpect(jsonPath("$.message").value(message))
            .andDo(
                verify()
                    .wiremock(
                        post(urlEqualTo("/api/v1/delivery/$ORDER_UID_NOT_FOUND/deliver"))
                            .withRequestBody(matchingJsonPath("$.address", RegexPattern("\\S+")))
                            .withRequestBody(matchingJsonPath("$.firstName", RegexPattern("\\S+")))
                            .withRequestBody(matchingJsonPath("$.lastName", RegexPattern("\\S+")))
                    )
            )
            .andDo(
                document(
                    "deliveryNotFound",
                    requestFields(
                        fieldWithPath("address").description("Delivery address").type(STRING),
                        fieldWithPath("firstName").description("First name").type(STRING),
                        fieldWithPath("lastName").description("Last name").optional().type(STRING)
                    ),
                    responseFields(fieldWithPath("message").description("Error description").type(STRING)),
                    dslContract()
                )
            )
    }

    companion object {
        private val ORDER_UID_SUCCESS = UUID.fromString("3affedc8-7338-4f5c-9462-b3579ec84652")
        private val ORDER_UID_NOT_FOUND = UUID.fromString("36856fc6-d6ec-47cb-bbee-d20e78299eb9")
    }
}