package ru.romanow.delivery

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
class DeliveryApplication

fun main(args: Array<String>) {
    SpringApplication.run(DeliveryApplication::class.java, *args)
}