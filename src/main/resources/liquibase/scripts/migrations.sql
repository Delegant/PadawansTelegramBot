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
    rename column chatId to chat_id;

--changeset algmironov:6

ALTER TABLE reports
    rename column reportDate to report_date;

ALTER TABLE reports
    rename column reportText to report_text;

ALTER TABLE reports
    rename column reportUpdateDate to report_update_date;

--changeset algmironov:7

ALTER TABLE pictures
    rename column fileSize to file_size;

ALTER TABLE pictures
    rename column mediaType to media_type;

-- changeset algmironov:8
CREATE TABLE pictureNames
(
    id                  SERIAL NOT NULL PRIMARY KEY,
    file_name           TEXT,
    report_id           bigint NOT NULL REFERENCES reports(id)

);

-- changeset algmironov:9
ALTER TABLE pictureNames
    ADD CONSTRAINT UQ_picturenames_file_name UNIQUE(file_name);

ALTER TABLE reports
    add column picture_names TEXT REFERENCES pictureNames(file_name);

-- changeset algmironov:10
ALTER TABLE pictureNames
    rename column file_name to filename;

-- changeset algmironov:11
ALTER TABLE pictures
    ADD COLUMN file_path TEXT;

-- changeset algmironov:12
ALTER TABLE pictures
    ADD CONSTRAINT UQ_file_path_name UNIQUE(file_path);

-- changeset algmironov:13
CREATE TABLE messages
(
    message_id      SERIAL NOT NULL PRIMARY KEY,
    sender_id       bigint NOT NULL REFERENCES users(chat_id),
    message_text    TEXT NOT NULL,
    sent_date       timestamp NOT NULL,
    read_status     varchar(255) NOT NULL DEFAULT 'UNREAD'
);

-- changeset algmironov:14
CREATE TABLE trial_periods
(
    id  SERIAL NOT NULL PRIMARY KEY,
    user_id bigint NOT NULL REFERENCES users(chat_id),
    start_date timestamp NOT NULL,
    end_date timestamp,
    additional_days int,
    set_by bigint NOT NULL REFERENCES users(chat_id),
    accepted_by bigint REFERENCES users(chat_id),
    prolonged_by bigint REFERENCES users(chat_id),
    denied_by bigint REFERENCES users(chat_id),
    status  varchar(255) NOT NULL DEFAULT 'STARTED'
);

-- changeset algmironov:15
ALTER TABLE trial_periods
    rename column user_id to user_id_id;

-- changeset algmironov:16
ALTER TABLE trial_periods
    rename column user_id_id to chat_id;

-- changeset algmironov:17
ALTER TABLE trial_periods
    rename column chat_id to user_id;

-- changeset algmironov:18
ALTER TABLE trial_periods
    drop column user_id;

ALTER TABLE trial_periods
    ADD COLUMN user_id bigint references users(id);

-- changeset anton:19

CREATE TABLE menuStack
(
    id SERIAL NOT NULL PRIMARY KEY,
    user_id  bigint NOT NULL CONSTRAINT users_ref REFERENCES users,
    text_pack_key varchar(255) DEFAULT 'DOG',
    text_key varchar(255) NOT NULL DEFAULT 'DEFAULT_MENU_TEXT',
    menu_state varchar(255) NOT NULL DEFAULT 'SPECIES_PET_SELECTION_MENU'
);

-- changeset anton:20

ALTER TABLE menuStack
    ADD COLUMN expect varchar(255) NOT NULL DEFAULT 'COMMAND';

ALTER TABLE users
    ADD COLUMN companion bigint;

-- changeset algmironov:21

ALTER TABLE reports
    ADD COLUMN read_status varchar(255) NOT NULL DEFAULT 'UNREAD';