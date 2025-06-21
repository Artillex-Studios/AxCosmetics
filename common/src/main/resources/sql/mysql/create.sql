CREATE TABLE IF NOT EXISTS $table_prefixaxcosmetics_users (id INTEGER AUTO_INCREMENT PRIMARY KEY, uuid VARCHAR(36), name VARCHAR(16));

CREATE TABLE IF NOT EXISTS $table_prefixaxcosmetics_cosmetic_configs (id INTEGER AUTO_INCREMENT PRIMARY KEY, name VARCHAR(255));

CREATE TABLE IF NOT EXISTS $table_prefixaxcosmetics_cosmetics (id INTEGER AUTO_INCREMENT PRIMARY KEY, owner_id INTEGER, cosmetic_type_id INTEGER, counter INTEGER, color INTEGER, acquired BIGINT, equipped BOOLEAN);