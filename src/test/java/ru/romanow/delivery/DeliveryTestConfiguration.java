package ru.romanow.delivery;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import ru.romanow.delivery.repository.DeliveryRepository;

@Configuration
@ComponentScan(basePackages = "ru.romanow.delivery",
        excludeFilters = @ComponentScan.Filter(Configuration.class))
public class DeliveryTestConfiguration {

    @MockBean
    public DeliveryRepository deliveryRepository;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplateBuilder().build();
    }
}
