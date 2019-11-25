package ru.romanow.delivery.domain;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import lombok.Data;
import lombok.experimental.Accessors;
import ru.romanow.delivery.domain.enums.DeliveryState;

import javax.persistence.*;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Entity
@Table(name = "delivery")
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "item_uid", nullable = false, updatable = false)
    private UUID orderUid;

    @Column(name = "first_name", length = 80)
    private String firstName;

    @Column(name = "last_name", length = 80)
    private String lastName;

    @Column(name = "address")
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    private DeliveryState state;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Delivery delivery = (Delivery) o;
        return Objects.equal(orderUid, delivery.orderUid);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(orderUid);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("orderUid", orderUid)
                .add("firstName", firstName)
                .add("lastName", lastName)
                .add("address", address)
                .toString();
    }
}
