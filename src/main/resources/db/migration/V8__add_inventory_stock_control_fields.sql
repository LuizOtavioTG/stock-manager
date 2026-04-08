ALTER TABLE inventory
    ADD COLUMN minimum_stock INTEGER,
    ADD COLUMN maximum_stock INTEGER,
    ADD COLUMN reorder_point INTEGER;

ALTER TABLE inventory
    ADD CONSTRAINT chk_inventory_quantity_non_negative
        CHECK (quantity >= 0),
    ADD CONSTRAINT chk_inventory_minimum_stock_non_negative
        CHECK (minimum_stock IS NULL OR minimum_stock >= 0),
    ADD CONSTRAINT chk_inventory_maximum_stock_non_negative
        CHECK (maximum_stock IS NULL OR maximum_stock >= 0),
    ADD CONSTRAINT chk_inventory_reorder_point_non_negative
        CHECK (reorder_point IS NULL OR reorder_point >= 0),
    ADD CONSTRAINT chk_inventory_maximum_stock_greater_than_minimum
        CHECK (minimum_stock IS NULL OR maximum_stock IS NULL OR maximum_stock >= minimum_stock),
    ADD CONSTRAINT chk_inventory_reorder_point_greater_than_minimum
        CHECK (minimum_stock IS NULL OR reorder_point IS NULL OR reorder_point >= minimum_stock),
    ADD CONSTRAINT chk_inventory_reorder_point_less_than_maximum
        CHECK (maximum_stock IS NULL OR reorder_point IS NULL OR reorder_point <= maximum_stock);
