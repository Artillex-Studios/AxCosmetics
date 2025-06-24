CREATE TABLE IF NOT EXISTS $table_prefixaxcosmetics_users (id INTEGER PRIMARY KEY AUTOINCREMENT, uuid VARCHAR(36));

CREATE TABLE IF NOT EXISTS $table_prefixaxcosmetics_cosmetic_configs (id INTEGER PRIMARY KEY AUTOINCREMENT, name VARCHAR(255));

CREATE TABLE IF NOT EXISTS $table_prefixaxcosmetics_cosmetics (id INTEGER PRIMARY KEY AUTOINCREMENT, owner_id INTEGER, cosmetic_type_id INTEGER, counter INTEGER, color INTEGER, acquired BIGINT, equipped BOOLEAN);