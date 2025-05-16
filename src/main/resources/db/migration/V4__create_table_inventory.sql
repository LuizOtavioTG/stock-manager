CREATE TABLE inventory (
                           id SERIAL PRIMARY KEY,
                           product_id BIGINT NOT NULL,
                           storage_location_id BIGINT NOT NULL,
                           quantity INTEGER NOT NULL DEFAULT 0,
                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           CONSTRAINT fk_inventory_product FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE CASCADE,
                           CONSTRAINT fk_inventory_storage_location FOREIGN KEY (storage_location_id) REFERENCES storage_location(id) ON DELETE CASCADE
);