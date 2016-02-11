INSERT INTO restaurants (name, added_by, timestamp) SELECT restaurant, "Zomato", current_date from lunchvotes;
