-- liquibase formatted sql

-- changeset algmironov:1

create table users
(
    id                   SERIAL NOT NULL PRIMARY KEY,
    chatId               bigint NOT NULL,
    name                 varchar(255) NOT NULL,
    role                 varchar(255) NOT NULL DEFAULT 'USER'
);

-- changeset algmironov:2

create table reports
(
    id                  SERIAL NOT NULL PRIMARY KEY,
    reportText          TEXT NOT NULL,
    status              varchar(255) NOT NULL DEFAULT 'MAIN',
    reportDate          timestamp NOT NULL,
    reportUpdateDate    timestamp,
    user_id             bigint NOT NULL REFERENCES users(id)
);

-- changeset algmironov:3

create table pictures
(
    id          SERIAL NOT NULL PRIMARY KEY,
    fileSize    bigint NOT NULL,
    mediaType   text NOT NULL,
    data        bytea NOT NULL,
    report_id   bigint NOT NULL REFERENCES reports(id)
);

-- changeset anton:4

ALTER TABLE users
    ADD CONSTRAINT UQ_users_chatId UNIQUE(chatId);

-- changeset anton:5

ALTER TABLE users
    rename column chatId to chat_id
