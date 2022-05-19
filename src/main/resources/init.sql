CREATE TABLE IF NOT EXISTS status_aliases
(
    name VARCHAR(255) NOT NULL,
    url  VARCHAR(255) NOT NULL,
    type VARCHAR(255) NOT NULL,
    primary key (name)
);
CREATE TABLE IF NOT EXISTS presence_status
(
    id     BIGINT       NOT NULL,
    status VARCHAR(255) NOT NULL,
    primary key (id)
);
CREATE TABLE IF NOT EXISTS offline_checker
(
    id         BIGINT  NOT NULL,
    suppressed BOOLEAN NOT NULL,
    banned     BOOLEAN NOT NULL,
    type       VARCHAR(64),
    primary key (id)
);
CREATE TABLE IF NOT EXISTS channel_barriers
(
    name       VARCHAR(255) NOT NULL,
    channel_id BIGINT       NOT NULL,
    guild_id   BIGINT       NOT NULL,
    primary key (name)
);
CREATE TABLE IF NOT EXISTS music
(
    name  VARCHAR(255)  NOT NULL,
    url   VARCHAR(1023) NOT NULL,
    state VARCHAR(63)   NOT NULL,
    topic VARCHAR(255)  NOT NULL,
    primary key (name)
);
CREATE TABLE IF NOT EXISTS event_data
(
    guild_id   BIGINT       NOT NULL,
    channel_id BIGINT       NOT NULL,
    type       VARCHAR(255) NOT NULL,
    data       JSON         NOT NULL,
    CONSTRAINT event_data_constraint UNIQUE (guild_id, channel_id, type)
);