CREATE TABLE users (
	user_id serial, 
	name VARCHAR(255) NOT NULL, 
	email VARCHAR(255) NOT NULL, 
	created_on TIMESTAMP NOT NULL,
	last_login TIMESTAMP NOT NULL, 
	UNIQUE(user_id), 
	PRIMARY KEY(email));
