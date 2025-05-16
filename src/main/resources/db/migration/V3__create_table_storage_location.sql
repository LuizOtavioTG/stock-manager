CREATE TABLE storage_location (
                                  id SERIAL PRIMARY KEY,
                                  name VARCHAR(255) NOT NULL,
                                  type VARCHAR(50),
                                  address TEXT,
                                  phone_number VARCHAR(20),
                                  email VARCHAR(255),
                                  responsible_name VARCHAR(255),
                                  notes TEXT,
                                  capacity INTEGER,
                                  default_location BOOLEAN DEFAULT FALSE,
                                  active BOOLEAN DEFAULT TRUE,
                                  latitude DOUBLE PRECISION,
                                  longitude DOUBLE PRECISION,
                                  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);