CREATE TABLE inventory (
                           id SERIAL PRIMARY KEY,
                           product_id BIGINT,
                           storage_location_id BIGINT,
                           quantity INTEGER,
                           last_updated TIMESTAMP,
                           CONSTRAINT fk_inventory_product FOREIGN KEY (product_id) REFERENCES product(id),
                           CONSTRAINT fk_inventory_storage_location FOREIGN KEY (storage_location_id) REFERENCES storage_location(id)
);