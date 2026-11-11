CREATE TABLE messages (
    id         BIGSERIAL PRIMARY KEY,
    phone      VARCHAR(20)  NOT NULL,
    type       VARCHAR(20)  NOT NULL,
    text       VARCHAR(500) NOT NULL,
    created_at TIMESTAMP    NOT NULL
);
