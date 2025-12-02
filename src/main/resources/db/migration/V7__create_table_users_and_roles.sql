CREATE TABLE role (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(150) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    failed_attempts INTEGER NOT NULL DEFAULT 0,
    locked_until TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES role(id) ON DELETE CASCADE
);

ALTER TABLE product ADD COLUMN created_by VARCHAR(100);
ALTER TABLE product ADD COLUMN updated_by VARCHAR(100);

ALTER TABLE storage_location ADD COLUMN created_by VARCHAR(100);
ALTER TABLE storage_location ADD COLUMN updated_by VARCHAR(100);

ALTER TABLE inventory ADD COLUMN created_by VARCHAR(100);
ALTER TABLE inventory ADD COLUMN updated_by VARCHAR(100);

ALTER TABLE stock_movement ADD COLUMN created_by VARCHAR(100);
ALTER TABLE stock_movement ADD COLUMN updated_by VARCHAR(100);

ALTER TABLE supplier ADD COLUMN created_by VARCHAR(100);
ALTER TABLE supplier ADD COLUMN updated_by VARCHAR(100);
