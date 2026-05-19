CREATE TABLE product (
                         id SERIAL PRIMARY KEY,
                         sku VARCHAR(255),
                         name VARCHAR(255) NOT NULL,
                         description TEXT,
                         brand VARCHAR(255),
                         category_id BIGINT,
                         unit_of_measure VARCHAR(50),
                         cost_price DOUBLE PRECISION,
                         sale_price DOUBLE PRECISION,
                         active BOOLEAN DEFAULT TRUE,
                         expiration_date DATE,
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES category(id)
                             ON DELETE SET NULL
);