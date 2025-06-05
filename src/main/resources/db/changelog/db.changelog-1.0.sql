--liquibase formatted sql

--changeset makson:1
CREATE TABLE IF NOT EXISTS users
(
    id       SERIAL PRIMARY KEY,
    username    VARCHAR(16) UNIQUE NOT NULL,
    password VARCHAR(512)       NOT NULL
);
--rollback DROP TABLE users;