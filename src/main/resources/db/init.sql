DROP TABLE IF EXISTS user_info;
DROP TABLE IF EXISTS address;
DROP TABLE IF EXISTS zone_schedule;
DROP TABLE IF EXISTS announcement;

CREATE TABLE user_info
(
    chat_id       BIGINT PRIMARY KEY NOT NULL,
    session_state VARCHAR            NOT NULL,
    user_city     VARCHAR,
    user_street   VARCHAR,
    user_house    VARCHAR
);

CREATE TABLE address
(
    id             SERIAL PRIMARY KEY NOT NULL,
    city           VARCHAR            NOT NULL,
    street         VARCHAR            NOT NULL,
    house          VARCHAR            NOT NULL,
    shutdown_group SMALLINT           NOT NULL
);

CREATE TABLE zone_schedule
(
    zone          VARCHAR PRIMARY KEY NOT NULL,
    schedule_json VARCHAR             NOT NULL,
    expire_date   DATE                NOT NULL
);

CREATE TABLE announcement
(
    id           SERIAL PRIMARY KEY NOT NULL,
    text         VARCHAR            NOT NULL,
    is_announced BOOLEAN DEFAULT FALSE
);