package ru.romanow.delivery.web;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.romanow.delivery.model.DeliveryRequest;
import ru.romanow.delivery.service.DeliveryManageService;

import javax.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/delivery")
@RequiredArgsConstructor
public class DeliveryController {
    private final DeliveryManageService deliveryManageService;

    @ResponseStatus(HttpStatus.ACCEPTED)
    @PostMapping(value = "/{orderUid}/deliver",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void deliver(@PathVariable UUID orderUid, @Valid @RequestBody DeliveryRequest request) {
        deliveryManageService.deliver(orderUid, request);
    }
}
