UPDATE product
SET sku = 'SKU-' || id
WHERE sku IS NULL OR TRIM(sku) = '';

ALTER TABLE product
    ALTER COLUMN sku TYPE VARCHAR(50),
    ALTER COLUMN sku SET NOT NULL;

ALTER TABLE product
    ADD CONSTRAINT uk_product_sku UNIQUE (sku);
