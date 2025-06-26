SELECT $table_prefixaxcosmetics_users.id, $table_prefixaxcosmetics_users.uuid, $table_prefixaxcosmetics_cosmetics.id, $table_prefixaxcosmetics_cosmetics.cosmetic_type_id, $table_prefixaxcosmetics_cosmetics.counter, $table_prefixaxcosmetics_cosmetics.color, $table_prefixaxcosmetics_cosmetics.acquired, $table_prefixaxcosmetics_cosmetics.equipped
FROM $table_prefixaxcosmetics_users
LEFT JOIN $table_prefixaxcosmetics_cosmetics ON $table_prefixaxcosmetics_cosmetics.owner_id = $table_prefixaxcosmetics_users.id
WHERE $table_prefixaxcosmetics_users.uuid = ?;