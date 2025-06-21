package com.artillexstudios.axcosmetics.command;

import com.artillexstudios.axapi.AxPlugin;
import com.artillexstudios.axcosmetics.api.AxCosmeticsAPI;
import com.artillexstudios.axcosmetics.api.cosmetics.Cosmetic;
import com.artillexstudios.axcosmetics.api.user.User;
import com.artillexstudios.axcosmetics.gui.implementation.CosmeticsGui;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.AsyncOfflinePlayerArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import org.bukkit.OfflinePlayer;

import java.util.concurrent.CompletableFuture;

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
                .then(new LiteralArgument("gui")
                        .executesPlayer((sender, args) -> {
                            User user = AxCosmeticsAPI.instance().getUserIfLoadedImmediately(sender);
                            new CosmeticsGui(user).open();
                        })
                )
                .then(new LiteralArgument("admin")
                        .withPermission("axcosmetics.command.admin")
                        .then(new LiteralArgument("equip")
                                .withPermission("axcosmetics.command.admin.equip")
                                .then(CosmeticArgumentType.cosmetic("cosmetic")
                                        .executesPlayer((sender, args) -> {
                                            User user = AxCosmeticsAPI.instance().getUserIfLoadedImmediately(sender.getUniqueId());
                                            Cosmetic<?> cosmetic = args.getByClass("cosmetic", Cosmetic.class);
                                            if (cosmetic == null) {
                                                System.out.println("Null cosmetic!");
                                                return;
                                            }

                                            user.equipCosmetic(cosmetic);
                                        })
                                )
                        ).then(new LiteralArgument("give")
                                .withPermission("axcosmetics.command.admin.equip")
                                .then(new AsyncOfflinePlayerArgument("player")
                                        .then(CosmeticArgumentType.cosmetic("cosmetic")
                                                .executes((sender, args) -> {
                                                    CompletableFuture<OfflinePlayer> playerFuture = args.getUnchecked("player");
                                                    if (playerFuture == null) {
                                                        return;
                                                    }

                                                    Cosmetic<?> cosmetic = args.getByClass("cosmetic", Cosmetic.class);
                                                    if (cosmetic == null) {
                                                        return;
                                                    }

                                                    playerFuture.thenAccept(player -> {
                                                        AxCosmeticsAPI.instance().getUser(player).thenAccept(user -> {
                                                            user.addCosmetic(cosmetic);
                                                        });
                                                    });
                                                })
                                        )
                                )
                        )
                )
                .register();
    }

}
