package ru.romanow.delivery

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import ru.romanow.delivery.config.DatabaseTestConfiguration
import ru.romanow.delivery.web.DeliveryController

@ActiveProfiles("test")
@SpringBootTest
@Import(DatabaseTestConfiguration::class)
internal class DeliveryApplicationTest {

    @Autowired
    private lateinit var deliveryController: DeliveryController

    @Test
    fun test() {
        assertThat(deliveryController).isNotNull
    }
}