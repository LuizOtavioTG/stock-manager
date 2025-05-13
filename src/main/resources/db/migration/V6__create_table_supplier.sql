CREATE TABLE supplier (
                          id SERIAL PRIMARY KEY,
                          name VARCHAR(255) NOT NULL,
                          contact_name VARCHAR(255),
                          phone_number VARCHAR(20),
                          email VARCHAR(255),
                          address TEXT,
                          active BOOLEAN,
                          created_at TIMESTAMP,
                          updated_at TIMESTAMP
);