CREATE TABLE category (
                          id SERIAL PRIMARY KEY,
                          name VARCHAR(255) NOT NULL,
                          description TEXT,
                          active BOOLEAN,
                          parent_category_id BIGINT,
                          CONSTRAINT fk_category_parent FOREIGN KEY (parent_category_id) REFERENCES category(id)
);