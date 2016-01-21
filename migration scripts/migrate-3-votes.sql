CREATE TABLE votes (
	user_id int references users(user_id), 
	rest_id int references restaurants(rest_id), 
	date DATE NOT NULL, 
	timestamp TIMESTAMP NOT NULL, 
	UNIQUE(user_id, rest_id, date));

