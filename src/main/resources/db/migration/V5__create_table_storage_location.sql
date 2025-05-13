CREATE TABLE storage_location (
                                  id SERIAL PRIMARY KEY,
                                  name VARCHAR(255),
                                  type VARCHAR(50),
                                  address TEXT,
                                  phone_number VARCHAR(20),
                                  email VARCHAR(255),
                                  responsible_name VARCHAR(255),
                                  notes TEXT,
                                  capacity INTEGER,
                                  default_location BOOLEAN,
                                  active BOOLEAN,
                                  latitude DOUBLE PRECISION,
                                  longitude DOUBLE PRECISION,
                                  created_at TIMESTAMP,
                                  updated_at TIMESTAMP
);