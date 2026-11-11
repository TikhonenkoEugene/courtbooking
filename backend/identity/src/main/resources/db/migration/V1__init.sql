CREATE TABLE users (
    id         BIGSERIAL PRIMARY KEY,
    phone      VARCHAR(20) NOT NULL UNIQUE,
    created_at TIMESTAMP   NOT NULL
);

CREATE TABLE otp_codes (
    id         BIGSERIAL PRIMARY KEY,
    phone      VARCHAR(20) NOT NULL,
    code       VARCHAR(6)  NOT NULL,
    used       BOOLEAN     NOT NULL,
    expires_at TIMESTAMP   NOT NULL,
    created_at TIMESTAMP   NOT NULL
);
