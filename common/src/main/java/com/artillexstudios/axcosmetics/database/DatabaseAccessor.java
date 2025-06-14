package com.artillexstudios.axcosmetics.database;

import com.artillexstudios.axapi.database.DatabaseHandler;
import com.artillexstudios.axapi.database.DatabaseQuery;
import com.artillexstudios.axapi.database.RunnableQuery;
import com.artillexstudios.axapi.database.handler.ListHandler;
import com.artillexstudios.axapi.database.handler.TransformerHandler;
import com.artillexstudios.axapi.utils.AsyncUtils;
import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axcosmetics.database.dto.UserDTO;
import com.artillexstudios.axcosmetics.user.User;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class DatabaseAccessor {
    private final DatabaseHandler handler;
    private final DatabaseQuery<List<UserDTO>> userSelect;

    public DatabaseAccessor(DatabaseHandler handler) {
        this.handler = handler;
        this.userSelect = handler.query("user_select", new ListHandler<>(new TransformerHandler<>(UserDTO.class)));
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

    // TODO: Figure out how we want to deal with equipped cosmetics, and API-only, non-stored cosmetics
    public CompletableFuture<User> loadUser(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            List<UserDTO> userDTOS = this.userSelect.create()
                    .query(uuid);

            if (userDTOS != null && !userDTOS.isEmpty()) {
                for (UserDTO userDTO : userDTOS) {
                    userDTO.cosmeticTypeId();
                }
            }
            return (User) null;
        }, AsyncUtils.executor()).exceptionally(throwable -> {
            LogUtils.error("Failed to run create query!", throwable);
            return (com.artillexstudios.axcosmetics.user.User) null;
        });
    }

    public CompletableFuture<Integer> registerCosmeticConfig() {
        return CompletableFuture.completedFuture(0);
    }
}
