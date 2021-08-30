package ru.romanow.delivery.service;

import ru.romanow.delivery.model.DeliveryRequest;

import javax.annotation.Nonnull;
import java.util.UUID;

public interface DeliveryManageService {
    void deliver(@Nonnull UUID orderUid, @Nonnull DeliveryRequest request);
}
