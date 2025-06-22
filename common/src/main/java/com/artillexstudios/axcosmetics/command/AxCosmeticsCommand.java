package com.artillexstudios.axcosmetics.command;

import com.artillexstudios.axapi.AxPlugin;
import com.artillexstudios.axapi.utils.MessageUtils;
import com.artillexstudios.axcosmetics.api.AxCosmeticsAPI;
import com.artillexstudios.axcosmetics.api.cosmetics.Cosmetic;
import com.artillexstudios.axcosmetics.api.user.User;
import com.artillexstudios.axcosmetics.config.Config;
import com.artillexstudios.axcosmetics.config.Language;
import com.artillexstudios.axcosmetics.gui.implementation.CosmeticsGui;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.AsyncOfflinePlayerArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
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
                                                            user.addCosmetic(cosmetic).thenRun(() -> {
                                                                MessageUtils.sendMessage(sender, Language.prefix, Language.give,
                                                                        Placeholder.unparsed("edition", String.valueOf(cosmetic.data().counter())),
                                                                        Placeholder.unparsed("cosmetic", String.valueOf(cosmetic.config().name())),
                                                                        Placeholder.unparsed("player", player.getName())
                                                                );

                                                                Player onlinePlayer = player.getPlayer();
                                                                if (onlinePlayer != null) {
                                                                    MessageUtils.sendMessage(onlinePlayer, Language.prefix, Language.receive,
                                                                            Placeholder.unparsed("edition", String.valueOf(cosmetic.data().counter())),
                                                                            Placeholder.unparsed("cosmetic", String.valueOf(cosmetic.config().name()))
                                                                    );
                                                                }
                                                            });
                                                        });
                                                    });
                                                })
                                        )
                                )
                        ).then(new LiteralArgument("reload")
                                .withPermission("axcosmetics.command.admin.reload")
                                .executes((sender, args) -> {
                                    long start = System.nanoTime();
                                    List<String> failed = new ArrayList<>();

                                    if (!Config.reload()) {
                                        failed.add("config.yml");
                                    }

                                    if (!Language.reload()) {
                                        failed.add("language/" + Language.lastLanguage + ".yml");
                                    }

                                    if (failed.isEmpty()) {
                                        MessageUtils.sendMessage(sender, Language.prefix, Language.reload.success, Placeholder.unparsed("time", Long.toString((System.nanoTime() - start) / 1_000_000)));
                                    } else {
                                        MessageUtils.sendMessage(sender, Language.prefix, Language.reload.fail, Placeholder.unparsed("time", Long.toString((System.nanoTime() - start) / 1_000_000)), Placeholder.unparsed("files", String.join(", ", failed)));
                                    }
                                })
                        )
                )
                .register();
    }

}
