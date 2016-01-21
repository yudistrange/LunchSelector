CREATE TABLE restaurants (
	rest_id serial, 
	name VARCHAR(255) NOT NULL, 
	added_by VARCHAR(255) NOT NULL, 
	created_on TIMESTAMP NOT NULL, 
	last_voted TIMESTAMP NOT NULL, 
	UNIQUE(rest_id), 
	PRIMARY KEY(name));
