--liquibase formatted sql

--changeset makson:1
INSERT INTO users (id, username, password)
VALUES (100,'testUserOne', '{noop}password'),
       (101, 'testUserTwo', '{noop}password')
--rollback DELETE FROM users WHERE id IN (100, 101);;