package ru.romanow.delivery.domain

import ru.romanow.delivery.domain.enums.DeliveryState
import java.util.*
import javax.persistence.*

@Entity
@Table(
    name = "delivery", indexes = [
        Index(name = "idx_delivery_order_uid", columnList = "order_uid", unique = true)
    ]
)
data class Delivery(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val id: Int? = null,

    @Column(name = "order_uid", nullable = false, updatable = false)
    private val orderUid: UUID? = null,

    @Column(name = "first_name", length = 80, nullable = false)
    private val firstName: String? = null,

    @Column(name = "last_name", length = 80)
    private val lastName: String? = null,

    @Column(name = "address", nullable = false)
    private val address: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    private val state: DeliveryState? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Delivery

        if (orderUid != other.orderUid) return false
        if (state != other.state) return false

        return true
    }

    override fun hashCode(): Int {
        var result = orderUid?.hashCode() ?: 0
        result = 31 * result + (state?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "Delivery(id=$id, orderUid=$orderUid, firstName=$firstName, lastName=$lastName, address=$address, state=$state)"
    }
}