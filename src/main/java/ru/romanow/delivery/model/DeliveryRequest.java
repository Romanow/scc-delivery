package ru.romanow.delivery.model;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;

@Data
@Accessors(chain = true)
public class DeliveryRequest {

    @NotEmpty(message = "{field.is.empty")
    private String address;

    @NotEmpty(message = "{field.is.empty")
    private String firstName;

    private String lastName;
}
