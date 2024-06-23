DROP TABLE IF EXISTS user_info;
DROP TABLE IF EXISTS address;
DROP TABLE IF EXISTS zone_schedule;

CREATE TABLE user_info
(
    chatId        BIGINT PRIMARY KEY NOT NULL,
    session_state VARCHAR            NOT NULL,
    user_city     VARCHAR,
    user_street   VARCHAR,
    user_house    VARCHAR
);

CREATE TABLE address
(
    id             INTEGER PRIMARY KEY NOT NULL,
    city           VARCHAR             NOT NULL,
    street         VARCHAR             NOT NULL,
    house          VARCHAR             NOT NULL,
    shutdown_group SMALLINT            NOT NULL
);

CREATE TABLE zone_schedule
(
    zone          VARCHAR PRIMARY KEY NOT NULL,
    schedule_json VARCHAR             NOT NULL,
    expire_date   DATE                NOT NULL
);