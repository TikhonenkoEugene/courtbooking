CREATE TABLE courts (
    id             BIGSERIAL PRIMARY KEY,
    name           VARCHAR(100)   NOT NULL,
    sport          VARCHAR(20)    NOT NULL,
    city           VARCHAR(100)   NOT NULL,
    price_per_hour NUMERIC(10, 2) NOT NULL
);

INSERT INTO courts (name, sport, city, price_per_hour) VALUES
    ('Tennis Court 1', 'TENNIS', 'Moscow', 2000),
    ('Tennis Court 2', 'TENNIS', 'Moscow', 2500),
    ('Padel Court 1', 'PADEL', 'Moscow', 3000),
    ('Padel Court 2', 'PADEL', 'Saint Petersburg', 2800);
