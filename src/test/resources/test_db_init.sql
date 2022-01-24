DROP DATABASE IF EXISTS TestDB;

CREATE DATABASE TestDB;

DROP TABLE IF EXISTS Users CASCADE;
DROP TABLE IF EXISTS Tasks;

CREATE TABLE Users (
        id serial PRIMARY KEY, 
	username varchar(255),
	password varchar(255),
	email varchar(255)
);




CREATE TABLE Tasks ( 
        id serial PRIMARY KEY,
	title varchar(255),
	description varchar(1000),
	id_user int,
	deadline date,
	done bool,
	FOREIGN KEY (id_user) REFERENCES Users (id)
);

COPY Users FROM '/db/fake-data/sample_user.csv' DELIMITER ',' CSV HEADER;

COPY Tasks FROM '/db/fake-data/sample_task.csv' DELIMITER ',' CSV HEADER;
