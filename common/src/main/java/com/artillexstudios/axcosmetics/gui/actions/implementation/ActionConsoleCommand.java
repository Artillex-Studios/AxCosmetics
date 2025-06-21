package com.artillexstudios.axcosmetics.gui.actions.implementation;

import com.artillexstudios.axapi.scheduler.Scheduler;
import com.artillexstudios.axcosmetics.api.user.User;
import com.artillexstudios.axcosmetics.gui.GuiBase;
import com.artillexstudios.axcosmetics.gui.actions.Action;
import org.bukkit.Bukkit;

public final class ActionConsoleCommand extends Action<String> {

    public ActionConsoleCommand() {
        super("console");
    }

    @Override
    public String evaluate(String input) {
        return input;
    }

    @Override
    public void execute(User user, GuiBase base, String value) {
        Scheduler.get().run(() -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), value));
    }
}
