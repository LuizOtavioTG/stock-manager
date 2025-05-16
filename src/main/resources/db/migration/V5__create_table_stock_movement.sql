CREATE TABLE stock_movement (
                                id SERIAL PRIMARY KEY,
                                product_id BIGINT NOT NULL,
                                storage_location_id BIGINT NOT NULL,
                                quantity INTEGER NOT NULL,
                                movement_type VARCHAR(50) NOT NULL,
                                reason TEXT,
                                movement_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                reference VARCHAR(255),
                                responsible VARCHAR(255),
                                notes TEXT,
                                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                CONSTRAINT fk_stock_movement_product FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE CASCADE,
                                CONSTRAINT fk_stock_movement_storage_location FOREIGN KEY (storage_location_id) REFERENCES storage_location(id) ON DELETE CASCADE
);
