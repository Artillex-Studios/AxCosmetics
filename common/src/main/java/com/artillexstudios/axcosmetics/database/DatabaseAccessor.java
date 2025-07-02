package com.artillexstudios.axcosmetics.database;

import com.artillexstudios.axapi.database.DatabaseHandler;
import com.artillexstudios.axapi.database.DatabaseQuery;
import com.artillexstudios.axapi.database.handler.ListHandler;
import com.artillexstudios.axapi.database.handler.TransformerHandler;
import com.artillexstudios.axapi.utils.AsyncUtils;
import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axcosmetics.api.cosmetics.Cosmetic;
import com.artillexstudios.axcosmetics.api.cosmetics.CosmeticData;
import com.artillexstudios.axcosmetics.api.cosmetics.config.CosmeticConfig;
import com.artillexstudios.axcosmetics.config.Config;
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
    private final DatabaseQuery<Number> userInsert;
    private final DatabaseQuery<Number> cosmeticConfigSelect;
    private final DatabaseQuery<Number> cosmeticConfigInsert;
    private final DatabaseQuery<Number> cosmeticInsert;
    private final DatabaseQuery<Object> cosmeticDelete;
    private final DatabaseQuery<Number> cosmeticEditionGenerate;
    private final DatabaseQuery<Number> cosmeticUpdate;

    public DatabaseAccessor(DatabaseHandler handler) {
        this.handler = handler;
        this.userSelect = handler.query("user_select", new ListHandler<>(new TransformerHandler<>(UserDTO.class)));
        this.userInsert = handler.query("user_insert");
        this.cosmeticConfigSelect = handler.query("cosmetic_config_select");
        this.cosmeticConfigInsert = handler.query("cosmetic_config_insert");
        this.cosmeticInsert = handler.query("cosmetic_insert");
        this.cosmeticDelete = handler.query("cosmetic_delete");
        this.cosmeticEditionGenerate = handler.query("cosmetic_edition_generate");
        this.cosmeticUpdate = handler.query("cosmetic_update");
    }

    public CompletableFuture<?> create() {
        return CompletableFuture.runAsync(() -> {
            this.handler.query("create")
                    .create()
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
                if (Config.debug) {
                    LogUtils.debug("Loading already existing user with uuid {}, id: {}", uuid, userDTOS.getFirst().userId());
                }

                return new User(userDTOS.getFirst().userId(), Bukkit.getOfflinePlayer(uuid), userDTOS, this);
            }

            OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
            Number userId = this.userInsert.create()
                    .execute(uuid);

            if (Config.debug) {
                LogUtils.debug("Creating new user with uuid {}, id: {}", uuid, userId);
            }

            if (userId == null) {
                LogUtils.error("Failed to create account for user!");
                return null;
            }

            return new User(userId.intValue(), player, List.of(), this);
        }, AsyncUtils.executor()).exceptionally(throwable -> {
            LogUtils.error("Failed to run user load query!", throwable);
            return null;
        });
    }

    public CompletableFuture<?> deleteCosmetic(Cosmetic<?> cosmetic) {
        return CompletableFuture.supplyAsync(() -> {
            if (cosmetic.data().id() == 0) {
                return null;
            }

            return this.cosmeticDelete.create()
                    .update(cosmetic.data().id());
        }, AsyncUtils.executor()).exceptionally(throwable -> {
            LogUtils.error("Failed to run cosmetic delete query!", throwable);
            return null;
        });
    }

    public CompletableFuture<?> updateCosmetic(Cosmetic<?> cosmetic, boolean equipped) {
        return CompletableFuture.runAsync(() -> {
            if (cosmetic.data().id() == 0) {
                throw new RuntimeException();
            }

            if (Config.debug) {
                LogUtils.debug("Updating cosmetic: {}, equipped: {}", cosmetic, equipped);
            }

            this.cosmeticUpdate.create()
                    .update(equipped, cosmetic.data().color(), cosmetic.data().id());
        }, AsyncUtils.executor()).exceptionally(throwable -> {
            LogUtils.error("Failed to run cosmetic update query!", throwable);
            return null;
        });
    }

    public CompletableFuture<?> insertCosmetic(User user, Cosmetic<?> cosmetic) {
        return CompletableFuture.runAsync(() -> {
            int edition = cosmetic.data().counter();
            if (edition == 0) {
                // generate new edition from the database
                Number query = this.cosmeticEditionGenerate.create()
                        .query(cosmetic.config().id());
                query = query == null ? 0 : query;
                edition = query.intValue() + 1;
            }

            Number cosmeticId = this.cosmeticInsert.create()
                    .execute(user.id(), cosmetic.config().id(), edition, cosmetic.data().color(), cosmetic.data().timeStamp(), false, cosmetic.data().permission());

            if (Config.debug) {
                LogUtils.debug("Inserting cosmetic: {}, id: {}, edition: {}, user: {}", cosmetic, cosmetic, edition, user);
            }

            cosmetic.data(new CosmeticData(cosmeticId.intValue(), edition, cosmetic.data().color(), cosmetic.data().timeStamp(), cosmetic.data().permission()));
        }, AsyncUtils.executor()).exceptionally(throwable -> {
            LogUtils.error("Failed to run cosmetic insert query!", throwable);
            return null;
        });
    }

    public CompletableFuture<Integer> registerCosmeticConfig(CosmeticConfig config) {
        return CompletableFuture.supplyAsync(() -> {
            LogUtils.debug(config.name());
            Number cosmeticConfigId = this.cosmeticConfigSelect.create()
                    .query(config.name());

            if (cosmeticConfigId != null) {
                LogUtils.debug("Found cosmetic!");
                return cosmeticConfigId.intValue();
            }

            cosmeticConfigId = this.cosmeticConfigInsert.create()
                    .execute(config.name());

            if (cosmeticConfigId == null) {
                LogUtils.error("Failed to insert cosmetic config {}!", config.name());
                return null;
            }

            LogUtils.debug("Inserted cosmetic!", cosmeticConfigId);
            return cosmeticConfigId.intValue();
        }, AsyncUtils.executor()).exceptionally(throwable -> {
            LogUtils.error("Failed to run cosmetic config register query!", throwable);
            return null;
        });
    }
}
