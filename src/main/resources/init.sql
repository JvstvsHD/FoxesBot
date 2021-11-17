CREATE TABLE IF NOT EXISTS updates (url VARCHAR (1024) NOT NULL,type VARCHAR (100) NOT NULL,primary key(type));
CREATE TABLE IF NOT EXISTS update_tracker_subscriptions(id BIGINT NOT NULL,type CHAR (255) NOT NULL,CONSTRAINT update_tracker_subscriptions_constraint UNIQUE (type, id));
CREATE TABLE IF NOT EXISTS status_aliases(name CHAR (255) NOT NULL,url CHAR (255) NOT NULL,type CHAR (255) NOT NULL,primary key (name));
CREATE TABLE IF NOT EXISTS presence_status(id BIGINT NOT NULL,status CHAR (255) NOT NULL,primary key (id));
CREATE TABLE IF NOT EXISTS offline_checker(id BIGINT NOT NULL,suppressed BOOLEAN NOT NULL,banned BOOLEAN NOT NULL,type CHAR (64),primary key (id));
