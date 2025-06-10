package com.artillexstudios.axcosmetics.command;

import com.artillexstudios.axapi.AxPlugin;
import com.artillexstudios.axapi.scheduler.Scheduler;
import com.artillexstudios.axcosmetics.api.AxCosmeticsAPI;
import com.artillexstudios.axcosmetics.api.cosmetics.CosmeticData;
import com.artillexstudios.axcosmetics.api.user.User;
import com.artillexstudios.axcosmetics.cosmetics.config.ArmorCosmeticConfig;
import com.artillexstudios.axcosmetics.cosmetics.config.FirstPersonBackpackConfig;
import com.artillexstudios.axcosmetics.cosmetics.type.ArmorCosmetic;
import com.artillexstudios.axcosmetics.cosmetics.type.FirstPersonBackpackCosmetic;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import dev.jorel.commandapi.CommandTree;
import org.bukkit.entity.Player;

import java.util.Map;

public class AxCosmeticsCommand {

    public static void load(AxPlugin plugin) {
        CommandAPI.onLoad(new CommandAPIBukkitConfig(plugin)
                .skipReloadDatapacks(true)
                .setNamespace("axcosmetics")
        );
    }

    public static void enable() {
        CommandAPI.onEnable();
    }

    public static void disable() {
        CommandAPI.onDisable();
    }

    public static void register() {
        new CommandTree("cosmetics")
                .executesPlayer((sender, args) -> {
                    User user = AxCosmeticsAPI.instance().userRepository().getUserIfLoadedImmediately(sender.getUniqueId());

//                    firstPersonBackpack(sender, user);
                    helmet(user);
                })
                .register();
    }

    private static void helmet(User user) {
        ArmorCosmetic cosmetic = new ArmorCosmetic(user, new CosmeticData(0, 0, 0), new ArmorCosmeticConfig(Map.of("slot", "helmet", "item-stack", Map.of("material", "paper", "custom-model-data", 1002))));
        user.addCosmetic(cosmetic);
        user.equipCosmetic(cosmetic);
    }

    private static void firstPersonBackpack(Player sender, User user) {
        FirstPersonBackpackCosmetic cosmetic = new FirstPersonBackpackCosmetic(user, new CosmeticData(0, 0, 0), new FirstPersonBackpackConfig(Map.of("slot", "backpack", "height", 1.5, "item-stack", Map.of("material", "paper", "custom-model-data", 1000), "first-person-item-stack", Map.of("material", "paper", "custom-model-data", 11000))));
        user.addCosmetic(cosmetic);
        cosmetic.spawn();

        Scheduler.get().runTimer(task -> {
            if (!sender.isOnline()) {
                task.cancel();
                cosmetic.despawn();
                return;
            }

            cosmetic.update();
        }, 1L, 1L);
    }
}
