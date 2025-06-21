package com.artillexstudios.axcosmetics.gui.actions;

import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axcosmetics.api.user.User;
import com.artillexstudios.axcosmetics.config.Config;
import com.artillexstudios.axcosmetics.gui.GuiBase;
import com.artillexstudios.axcosmetics.gui.actions.implementation.ActionClose;
import com.artillexstudios.axcosmetics.gui.actions.implementation.ActionConsoleCommand;
import com.artillexstudios.axcosmetics.gui.actions.implementation.ActionMessage;
import com.artillexstudios.axcosmetics.gui.actions.implementation.ActionPage;
import com.artillexstudios.axcosmetics.gui.actions.implementation.ActionPlayerCommand;
import com.artillexstudios.axcosmetics.gui.actions.implementation.ActionUnequip;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public final class Actions {
    private static final HashMap<String, Action<?>> ACTIONS = new HashMap<>();
    private static final Action<?> PAGE = register(new ActionPage());
    private static final Action<?> PLAYER_COMMAND = register(new ActionPlayerCommand());
    private static final Action<?> CONSOLE_COMMAND = register(new ActionConsoleCommand());
    private static final Action<?> CLOSE = register(new ActionClose());
    private static final Action<?> MESSAGE = register(new ActionMessage());
    private static final Action<?> UNEQUIP = register(new ActionUnequip());

    public static Action<?> register(Action<?> action) {
        ACTIONS.put(action.id(), action);
        return action;
    }

    public static void execute(User user, GuiBase guiBase, List<Action<?>> actions, List<Object> parsed) {
        for (int i = 0; i < actions.size(); i++) {
            Action<Object> action = (Action<Object>) actions.get(i);
            Object value = parsed.get(i);
            if (Config.debug) {
                LogUtils.debug("Executing actions: {} with parameters: {}", action, parsed);
            }

            action.execute(user, guiBase, value);
        }
    }

    public static List<Action<?>> compile(List<String> actions) {
        ObjectArrayList<Action<?>> compiled = new ObjectArrayList<>();
        if (actions == null) {
            return compiled;
        }

        for (String rawAction : actions) {
            if (rawAction == null || rawAction.isBlank()) {
                continue;
            }

            String id = StringUtils.substringBetween(rawAction, "[", "]").toLowerCase(Locale.ENGLISH);

            Action<?> action = ACTIONS.get(id);
            if (action == null) {
                continue;
            }

            compiled.add(action);
        }

        return compiled;
    }

    public static List<Object> parseAll(List<String> actions) {
        ObjectArrayList<Object> parsed = new ObjectArrayList<>();
        if (actions == null) {
            return parsed;
        }

        for (String rawAction : actions) {
            if (rawAction == null || rawAction.isBlank()) {
                continue;
            }

            String id = StringUtils.substringBetween(rawAction, "[", "]").toLowerCase(Locale.ENGLISH);
            String arguments = StringUtils.substringAfter(rawAction, "] ");

            Action<?> action = ACTIONS.get(id);
            if (action == null) {
                LogUtils.warn("Found incorrect action: {}! Possible values are: {}", id, String.join(", ", ACTIONS.keySet()));
                continue;
            }

            parsed.add(action.evaluate(arguments));
        }

        return parsed;
    }
}
