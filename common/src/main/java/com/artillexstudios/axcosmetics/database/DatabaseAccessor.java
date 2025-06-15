package com.artillexstudios.axcosmetics.database;

import com.artillexstudios.axapi.database.DatabaseHandler;
import com.artillexstudios.axapi.database.DatabaseQuery;
import com.artillexstudios.axapi.database.handler.ListHandler;
import com.artillexstudios.axapi.database.handler.TransformerHandler;
import com.artillexstudios.axapi.utils.AsyncUtils;
import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axcosmetics.api.cosmetics.Cosmetic;
import com.artillexstudios.axcosmetics.api.cosmetics.config.CosmeticConfig;
import com.artillexstudios.axcosmetics.database.dto.UserDTO;
import com.artillexstudios.axcosmetics.user.User;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class DatabaseAccessor {
    private final DatabaseHandler handler;
    private final DatabaseQuery<List<UserDTO>> userSelect;
    private final DatabaseQuery<Integer> userInsert;
    private final DatabaseQuery<Integer> cosmeticConfigSelect;
    private final DatabaseQuery<Integer> cosmeticConfigInsert;
    private final DatabaseQuery<Integer> cosmeticInsert;

    public DatabaseAccessor(DatabaseHandler handler) {
        this.handler = handler;
        this.userSelect = handler.query("user_select", new ListHandler<>(new TransformerHandler<>(UserDTO.class)));
        this.userInsert = handler.query("user_insert");
        this.cosmeticConfigSelect = handler.query("cosmetic_config_select");
        this.cosmeticConfigInsert = handler.query("cosmetic_config_insert");
        this.cosmeticInsert = handler.query("cosmetic_insert");
    }

    public CompletableFuture<?> create() {
        return CompletableFuture.runAsync(() -> {
            this.handler.query("create")
                    .createAsync()
                    .update();
        }, AsyncUtils.executor()).exceptionally(throwable -> {
            LogUtils.error("Failed to run create query!", throwable);
            return null;
        });
    }

    public CompletableFuture<User> loadUser(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            List<UserDTO> userDTOS = this.userSelect.create()
                    .query(uuid);

            if (userDTOS != null && !userDTOS.isEmpty()) {
                return new User(userDTOS.getFirst().userId(), Bukkit.getOfflinePlayer(uuid), userDTOS, this);
            }

            OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
            Integer userId = this.userInsert.create().execute(player.getName(), uuid);

            if (userId == null) {
                LogUtils.error("Failed to create account for user!");
                return null;
            }

            return new com.artillexstudios.axcosmetics.user.User(userId, player, List.of(), this);
        }, AsyncUtils.executor()).exceptionally(throwable -> {
            LogUtils.error("Failed to run user load query!", throwable);
            return null;
        });
    }

    public CompletableFuture<?> insertCosmetic(User user, Cosmetic<?> cosmetic) {
        return CompletableFuture.supplyAsync(() -> {

            return null;
        }, AsyncUtils.executor());
    }

    public CompletableFuture<Integer> registerCosmeticConfig(CosmeticConfig config) {
        return CompletableFuture.supplyAsync(() -> {
            Integer cosmeticConfigId = this.cosmeticConfigSelect.create()
                    .query(config.name());

            if (cosmeticConfigId != null) {
                return cosmeticConfigId;
            }

            cosmeticConfigId = this.cosmeticConfigInsert.create()
                    .query(config.name());
            if (cosmeticConfigId == null) {
                LogUtils.error("Failed to insert cosmetic config {}!", config.name());
                return null;
            }

            return cosmeticConfigId;
        }, AsyncUtils.executor()).exceptionally(throwable -> {
            LogUtils.error("Failed to run cosmetic config register query!", throwable);
            return null;
        });
    }
}
