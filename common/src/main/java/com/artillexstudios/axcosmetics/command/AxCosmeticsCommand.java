package com.artillexstudios.axcosmetics.command;

import com.artillexstudios.axapi.AxPlugin;
import com.artillexstudios.axapi.items.WrappedItemStack;
import com.artillexstudios.axapi.items.component.DataComponents;
import com.artillexstudios.axapi.items.component.type.CustomModelData;
import com.artillexstudios.axapi.scheduler.Scheduler;
import com.artillexstudios.axcosmetics.api.cosmetics.CosmeticData;
import com.artillexstudios.axcosmetics.api.user.User;
import com.artillexstudios.axcosmetics.cosmetics.config.FirstPersonBackpackConfig;
import com.artillexstudios.axcosmetics.cosmetics.type.FirstPersonBackpackCosmetic;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import dev.jorel.commandapi.CommandTree;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.List;

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
                    User user = new User() {
                        @Override
                        public int id() {
                            return 0;
                        }

                        @Override
                        public OfflinePlayer player() {
                            return sender;
                        }

                        @Override
                        public @Nullable Player onlinePlayer() {
                            return sender;
                        }
                    };

                    FirstPersonBackpackCosmetic cosmetic = new FirstPersonBackpackCosmetic(user, new CosmeticData(0, 0, 0), new FirstPersonBackpackConfig() {

                        @Override
                        public WrappedItemStack firstPersonItemStack() {
                            return WrappedItemStack.edit(new ItemStack(Material.PAPER), stack -> {
                                stack.set(DataComponents.customModelData(), new CustomModelData(List.of(), List.of(), List.of(Integer.valueOf(11000).floatValue()), List.of()));
                                return stack;
                            });
                        }

                        @Override
                        public WrappedItemStack itemStack() {
                            return WrappedItemStack.edit(new ItemStack(Material.PAPER), stack -> {
                                stack.set(DataComponents.customModelData(), new CustomModelData(List.of(), List.of(), List.of(Integer.valueOf(1000).floatValue()), List.of()));
                                return stack;
                            });
                        }

                        @Override
                        public double height() {
                            return 1.5;
                        }
                    });

                    cosmetic.spawn();

                    Scheduler.get().runTimer(task -> {
                        if (!sender.isOnline()) {
                            task.cancel();
                            cosmetic.despawn();
                            return;
                        }

                        cosmetic.update();
                    }, 1L, 1L);
                })
                .register();
    }
}
