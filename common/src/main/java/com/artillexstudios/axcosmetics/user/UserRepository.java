package com.artillexstudios.axcosmetics.user;

import com.artillexstudios.axcosmetics.api.LoadContext;
import com.artillexstudios.axcosmetics.api.exception.UserAlreadyLoadedException;
import com.artillexstudios.axcosmetics.api.user.User;
import com.artillexstudios.axcosmetics.database.DatabaseAccessor;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class UserRepository implements com.artillexstudios.axcosmetics.api.user.UserRepository {
    private final DatabaseAccessor accessor;
    private final ConcurrentHashMap<UUID, User> loadedUsers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, User> idLoadedUsers = new ConcurrentHashMap<>();
    private final Cache<UUID, User> tempUsers = Caffeine.newBuilder()
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .maximumSize(200)
            .build();

    public UserRepository(DatabaseAccessor accessor) {
        this.accessor = accessor;
    }

    @Override
    public User getUserIfLoadedImmediately(UUID uuid) {
        User user = this.loadedUsers.get(uuid);
        if (user != null) {
            return user;
        }

        user = this.tempUsers.getIfPresent(uuid);
        return user;
    }

    @Override
    public User getUserIfLoadedImmediately(int id) {
        return this.idLoadedUsers.get(id);
    }

    @Override
    public CompletableFuture<User> loadUser(UUID uuid) throws UserAlreadyLoadedException {
        if (this.loadedUsers.containsKey(uuid)) {
            throw new UserAlreadyLoadedException();
        }

        User user = this.tempUsers.getIfPresent(uuid);
        if (user != null) {
            this.tempUsers.invalidate(uuid);
            this.loadedUsers.put(uuid, user);
            Player onlinePlayer = user.onlinePlayer();
            if (onlinePlayer != null) {
                this.idLoadedUsers.put(onlinePlayer.getEntityId(), user);
            }
            return CompletableFuture.completedFuture(user);
        }

        return this.getUser(uuid, LoadContext.FULL);
    }

    @Override
    public CompletableFuture<User> getUser(UUID uuid, LoadContext loadContext) {
        User user = this.getUserIfLoadedImmediately(uuid);
        if (user != null) {
            return CompletableFuture.completedFuture(user);
        }

        // Deal with funny loading order
        return this.accessor.loadUser(uuid).thenApply(loaded -> {
            User temp;
            if (loadContext == LoadContext.FULL) {
                temp = this.loadedUsers.putIfAbsent(uuid, loaded);
            } else {
                temp = this.tempUsers.asMap().putIfAbsent(uuid, loaded);
            }

            Player onlinePlayer = loaded.onlinePlayer();
            if (onlinePlayer != null) {
                this.idLoadedUsers.put(onlinePlayer.getEntityId(), loaded);
            }

            return temp == null ? loaded : temp;
        });
    }

    @Override
    public Collection<User> onlineUsers() {
        return Collections.unmodifiableCollection(this.loadedUsers.values());
    }

    @Override
    public User disconnect(UUID uuid) {
        User user = this.loadedUsers.remove(uuid);

        if (user != null) {
            Player onlinePlayer = user.onlinePlayer();
            if (onlinePlayer != null) {
                this.idLoadedUsers.remove(onlinePlayer.getEntityId());
            }

            this.tempUsers.put(uuid, user);
        }

        return user;
    }
}
