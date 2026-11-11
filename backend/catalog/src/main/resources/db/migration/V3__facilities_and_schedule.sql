CREATE TABLE facilities (
    id       BIGSERIAL PRIMARY KEY,
    owner_id BIGINT,
    name     VARCHAR(100) NOT NULL,
    city     VARCHAR(100) NOT NULL,
    address  VARCHAR(255) NOT NULL
);

INSERT INTO facilities (owner_id, name, city, address)
SELECT owner_id, 'Спорткомплекс ' || city, city, ''
FROM courts
GROUP BY owner_id, city
ORDER BY min(id);

ALTER TABLE courts ADD COLUMN facility_id BIGINT REFERENCES facilities (id) ON DELETE CASCADE;

UPDATE courts c
SET facility_id = f.id
FROM facilities f
WHERE f.city = c.city
  AND f.owner_id IS NOT DISTINCT FROM c.owner_id;

ALTER TABLE courts ALTER COLUMN facility_id SET NOT NULL;
ALTER TABLE courts DROP COLUMN city;
ALTER TABLE courts DROP COLUMN owner_id;

CREATE TABLE court_schedule (
    id          BIGSERIAL PRIMARY KEY,
    court_id    BIGINT  NOT NULL REFERENCES courts (id) ON DELETE CASCADE,
    day_of_week INTEGER NOT NULL CHECK (day_of_week BETWEEN 1 AND 7),
    open_time   TIME    NOT NULL,
    close_time  TIME    NOT NULL,
    UNIQUE (court_id, day_of_week)
);

INSERT INTO court_schedule (court_id, day_of_week, open_time, close_time)
SELECT c.id, d, '08:00', '22:00'
FROM courts c
CROSS JOIN generate_series(1, 7) d;
