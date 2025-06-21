CREATE TABLE IF NOT EXISTS $table_prefixaxcosmetics_users (id INTEGER AUTOINCREMENT PRIMARY KEY, uuid VARCHAR(36), name VARCHAR(16));

CREATE TABLE IF NOT EXISTS $table_prefixaxcosmetics_cosmetic_configs (id INTEGER AUTOINCREMENT PRIMARY KEY, name VARCHAR(255));

CREATE TABLE IF NOT EXISTS $table_prefixaxcosmetics_cosmetics (id INTEGER AUTOINCREMENT PRIMARY KEY, owner_id INTEGER, cosmetic_type_id INTEGER, counter INTEGER, color INTEGER, equipped BOOLEAN);