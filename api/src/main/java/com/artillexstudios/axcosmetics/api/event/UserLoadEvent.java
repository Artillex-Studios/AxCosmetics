package com.artillexstudios.axcosmetics.api.event;

import com.artillexstudios.axcosmetics.api.user.User;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * An event called when the user is loaded.
 */
public final class UserLoadEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final User user;

    public UserLoadEvent(User user) {
        super(true);
        this.user = user;
    }

    public User user() {
        return this.user;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }
}
