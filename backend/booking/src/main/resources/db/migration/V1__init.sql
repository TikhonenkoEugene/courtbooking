CREATE TABLE bookings (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT         NOT NULL,
    court_id   BIGINT         NOT NULL,
    start_time TIMESTAMP      NOT NULL,
    end_time   TIMESTAMP      NOT NULL,
    price      NUMERIC(10, 2) NOT NULL,
    status     VARCHAR(20)    NOT NULL,
    created_at TIMESTAMP      NOT NULL
);
