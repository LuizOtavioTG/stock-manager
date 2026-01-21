ALTER TABLE inventory
    ADD CONSTRAINT uk_inventory_product_storage_location
        UNIQUE (product_id, storage_location_id);
