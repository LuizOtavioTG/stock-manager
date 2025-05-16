CREATE TABLE stock_movement (
                                id SERIAL PRIMARY KEY,
                                product_id BIGINT,
                                storage_location_id BIGINT,
                                quantity INTEGER,
                                movement_type VARCHAR(50),
                                reason TEXT,
                                movement_date TIMESTAMP,
                                reference VARCHAR(255),
                                responsible VARCHAR(255),
                                notes TEXT,
                                CONSTRAINT fk_stock_movement_product FOREIGN KEY (product_id) REFERENCES product(id),
                                CONSTRAINT fk_stock_movement_storage_location FOREIGN KEY (storage_location_id) REFERENCES storage_location(id)
);