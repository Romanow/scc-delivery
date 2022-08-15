package ru.romanow.delivery.service

import org.apache.commons.lang3.RandomStringUtils.randomAlphabetic
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.client.RestTemplate
import ru.romanow.delivery.domain.Delivery
import ru.romanow.delivery.exceptions.RestRequestException
import ru.romanow.delivery.model.DeliveryRequest
import ru.romanow.delivery.repository.DeliveryRepository
import java.util.*


@ActiveProfiles("test")
@SpringBootTest(
    classes = [DeliveryManageServiceTest.TestConfiguration::class],
    properties = ["warehouse.service.url=http://localhost:8070"]
)
@AutoConfigureStubRunner(
    ids = ["ru.romanow.scc:warehouse:[1.0.0,2.0.0):stubs:8070"],
    repositoryRoot = "https://maven.pkg.github.com/Romanow/scc-warehouse/",
    mappingsOutputFolder = "build/mappings",
    stubsMode = StubRunnerProperties.StubsMode.REMOTE
)
internal class DeliveryManageServiceTest {

    @Autowired
    private lateinit var deliveryManageService: DeliveryManageService

    @Autowired
    private lateinit var deliveryRepository: DeliveryRepository

    @Value("\${warehouse.service.url}")
    private lateinit var warehouseUrl: String

    @Test
    fun deliverSuccess() {
        val request = DeliveryRequest(
            firstName = randomAlphabetic(10),
            lastName = randomAlphabetic(10),
            address = randomAlphabetic(10)
        )

        `when`(deliveryRepository.save(any(Delivery::class.java)))
            .thenAnswer { it.getArgument<Delivery>(0) }
        deliveryManageService.deliver(ORDER_UID_SUCCESS, request)
    }

    @Test
    fun deliverOrderNotFound() {
        val orderUid = ORDER_UID_NOT_FOUND
        val request = DeliveryRequest(
            firstName = randomAlphabetic(10),
            lastName = randomAlphabetic(10),
            address = randomAlphabetic(10)
        )
        try {
            deliveryManageService.deliver(orderUid, request)
        } catch (exception: RestRequestException) {
            val url = "${warehouseUrl}/api/v1/items/$orderUid/checkout"
            val errorResponse = "OrderItem '$orderUid' not found"
            val message = "Error request to '$url': 404:$errorResponse"
            Assertions.assertEquals(message, exception.message)
            return
        }
        Assertions.fail<Any>()
    }

    companion object {
        private val ORDER_UID_SUCCESS = UUID.fromString("3affedc8-7338-4f5c-9462-b3579ec84652")
        private val ORDER_UID_NOT_FOUND = UUID.fromString("36856fc6-d6ec-47cb-bbee-d20e78299eb9")
    }

    @Configuration
    internal class TestConfiguration {

        @Bean
        fun restTemplate() = RestTemplate()

        @Bean
        fun deliveryRepository(): DeliveryRepository = mock(DeliveryRepository::class.java)

        @Bean
        fun deliveryManageService(@Value("\${warehouse.service.url}") warehouseUrl: String) =
            DeliveryManageServiceImpl(restTemplate(), deliveryRepository(), warehouseUrl)
    }

    private fun <T> any(cls: Class<T>): T = Mockito.any(cls)
}