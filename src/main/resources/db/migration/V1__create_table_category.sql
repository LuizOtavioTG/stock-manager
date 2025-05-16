CREATE TABLE category (
                          id SERIAL PRIMARY KEY,
                          name VARCHAR(255) NOT NULL UNIQUE,
                          description TEXT,
                          active BOOLEAN DEFAULT TRUE,
                          parent_category_id BIGINT,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          created_by VARCHAR(100),
                          updated_by VARCHAR(100),
                          version INTEGER DEFAULT 0,
                          CONSTRAINT fk_category_parent FOREIGN KEY (parent_category_id)
                              REFERENCES category(id) ON DELETE SET NULL
);