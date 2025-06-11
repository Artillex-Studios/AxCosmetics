package com.artillexstudios.axcosmetics.database;

import com.artillexstudios.axapi.database.DatabaseHandler;
import com.artillexstudios.axcosmetics.user.User;
import org.bukkit.Bukkit;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class DatabaseAccessor {
    private final DatabaseHandler handler;

    public DatabaseAccessor(DatabaseHandler handler) {
        this.handler = handler;
    }

    public CompletableFuture<User> loadUser(UUID uuid) {
        return CompletableFuture.completedFuture(new User(0, Bukkit.getOfflinePlayer(uuid), this));
    }
}
