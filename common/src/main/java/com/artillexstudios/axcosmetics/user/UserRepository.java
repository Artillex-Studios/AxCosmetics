package com.artillexstudios.axcosmetics.user;

import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axcosmetics.api.LoadContext;
import com.artillexstudios.axcosmetics.api.exception.UserAlreadyLoadedException;
import com.artillexstudios.axcosmetics.api.user.User;
import com.artillexstudios.axcosmetics.config.Config;
import com.artillexstudios.axcosmetics.database.DatabaseAccessor;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public final class UserRepository implements com.artillexstudios.axcosmetics.api.user.UserRepository {
    private final DatabaseAccessor accessor;
    private final ConcurrentHashMap<UUID, User> loadedUsers = new ConcurrentHashMap<>();
    private final HashMap<UUID, CompletableFuture<User>> loadingUsers = new HashMap<>();
    private final ConcurrentHashMap<Integer, User> idLoadedUsers = new ConcurrentHashMap<>();
    private final Cache<UUID, User> tempUsers = Caffeine.newBuilder()
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .maximumSize(200)
            .build();
    private final Cache<UUID, User> joiningUsers = Caffeine.newBuilder()
            .expireAfterAccess(10, TimeUnit.SECONDS)
            .maximumSize(200)
            .build();

    public UserRepository(DatabaseAccessor accessor) {
        this.accessor = accessor;
    }

    @Override
    public User getUserIfLoadedImmediately(UUID uuid) {
        User user = this.loadedUsers.get(uuid);

        Player onlinePlayer = Bukkit.getPlayer(uuid);
        if (onlinePlayer != null && user != null) {
            if (Config.debug) {
                LogUtils.debug("getUserIfLoadedImmediately - Player object is not null");
            }
            // Update the user's player and onlineplayer instances, if the entityId is new
            if (this.idLoadedUsers.put(onlinePlayer.getEntityId(), user) == null) {
                if (Config.debug) {
                    LogUtils.debug("Added entityId of player");
                }
                ((com.artillexstudios.axcosmetics.user.User) user).player(onlinePlayer);
                ((com.artillexstudios.axcosmetics.user.User) user).onlinePlayer(onlinePlayer);
            }
        }

        if (user != null) {
            if (Config.debug) {
                LogUtils.debug("Returning loaded user!");
            }
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
    public CompletableFuture<User> asyncLoadUser(UUID uuid) throws UserAlreadyLoadedException {
        if (this.loadedUsers.containsKey(uuid) || this.joiningUsers.asMap().containsKey(uuid)) {
            throw new UserAlreadyLoadedException();
        }

        User user = this.tempUsers.getIfPresent(uuid);
        if (user != null) {
            this.tempUsers.invalidate(uuid);
            this.joiningUsers.put(uuid, user);
            Player onlinePlayer = Bukkit.getPlayer(uuid);
            if (onlinePlayer != null) {
                ((com.artillexstudios.axcosmetics.user.User) user).player(onlinePlayer);
                this.idLoadedUsers.put(onlinePlayer.getEntityId(), user);
            }
            return this.accessor.loadUser(uuid).thenApply(newUser -> {
                ((com.artillexstudios.axcosmetics.user.User) user).updateDataFrom(newUser);
                return user;
            });
        }

        return this.getUser(uuid, LoadContext.FULL);
    }

    @Override
    public User joinUser(UUID uuid) {
        User user = this.joiningUsers.getIfPresent(uuid);
        if (user == null) {
            return null;
        }

        this.loadedUsers.put(uuid, user);
        return user;
    }

    @Override
    public CompletableFuture<User> getUser(UUID uuid, LoadContext loadContext) {
        User user = this.getUserIfLoadedImmediately(uuid);
        if (user != null) {
            return CompletableFuture.completedFuture(user);
        }

        synchronized (this.loadingUsers) {
            CompletableFuture<User> userCompletableFuture = this.loadingUsers.get(uuid);
            if (userCompletableFuture != null) {
                return userCompletableFuture;
            }

            // Deal with funny loading order
            CompletableFuture<User> future = this.accessor.loadUser(uuid).thenApply(loaded -> {
                User temp;
                if (loadContext == LoadContext.FULL) {
                    temp = this.joiningUsers.asMap().putIfAbsent(uuid, loaded);
                } else {
                    temp = this.tempUsers.asMap().putIfAbsent(uuid, loaded);
                }

                if (Config.debug) {
                    LogUtils.debug("Temp: {}, loaded: {}", temp, loaded);
                }

                synchronized (this.loadingUsers) {
                    this.loadingUsers.remove(uuid);
                }
                return temp == null ? loaded : temp;
            });

            this.loadingUsers.put(uuid, future);
            return future;
        }
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
