CREATE TABLE refunds (
    id         BIGSERIAL PRIMARY KEY,
    payment_id BIGINT         NOT NULL UNIQUE REFERENCES payments (id),
    booking_id BIGINT         NOT NULL,
    user_id    BIGINT         NOT NULL,
    amount     NUMERIC(10, 2) NOT NULL,
    status     VARCHAR(20)    NOT NULL,
    created_at TIMESTAMP      NOT NULL
);

INSERT INTO refunds (payment_id, booking_id, user_id, amount, status, created_at)
SELECT id, booking_id, user_id, amount, 'COMPLETED', now()
FROM payments
WHERE status = 'REFUNDED';
