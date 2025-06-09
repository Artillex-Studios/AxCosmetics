package com.artillexstudios.axcosmetics.api.user;

import com.artillexstudios.axcosmetics.api.LoadContext;
import com.artillexstudios.axcosmetics.api.exception.UserAlreadyLoadedException;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface UserRepository {

    User getUserIfLoadedImmediately(UUID uuid);

    CompletableFuture<User> loadUser(UUID uuid) throws UserAlreadyLoadedException;

    CompletableFuture<User> getUser(UUID uuid, LoadContext loadContext);

    Collection<User> onlineUsers();

    User disconnect(UUID uuid);
}
