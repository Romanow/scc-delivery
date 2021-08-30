-- V100: create delivery table
CREATE TABLE delivery
(
    id         SERIAL PRIMARY KEY,
    order_uid  uuid         NOT NULL,
    first_name VARCHAR(80)  NOT NULL,
    last_name  VARCHAR(80),
    address    VARCHAR(255) NOT NULL,
    state      VARCHAR(80)  NOT NULL
);

CREATE UNIQUE INDEX idx_delivery_order_uid ON delivery (order_uid);