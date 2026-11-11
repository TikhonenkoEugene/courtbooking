CREATE TABLE payments (
    id         BIGSERIAL PRIMARY KEY,
    booking_id BIGINT         NOT NULL UNIQUE,
    user_id    BIGINT         NOT NULL,
    amount     NUMERIC(10, 2) NOT NULL,
    status     VARCHAR(20)    NOT NULL,
    created_at TIMESTAMP      NOT NULL
);
