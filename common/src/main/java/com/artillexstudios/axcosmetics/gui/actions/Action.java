package com.artillexstudios.axcosmetics.gui.actions;

import com.artillexstudios.axcosmetics.api.user.User;
import com.artillexstudios.axcosmetics.gui.GuiBase;

import java.util.Objects;

public abstract class Action<T> {
    private final String id;

    public Action(String id) {
        this.id = id;
    }

    public abstract T evaluate(String input);

    public abstract void execute(User user, GuiBase base, T value);

    public String id() {
        return this.id;
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof Action<?> action)) {
            return false;
        }

        return Objects.equals(this.id, action.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.id);
    }
}