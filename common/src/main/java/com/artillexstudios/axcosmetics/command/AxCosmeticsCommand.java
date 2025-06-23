package com.artillexstudios.axcosmetics.command;

import com.artillexstudios.axapi.AxPlugin;
import com.artillexstudios.axapi.config.YamlConfiguration;
import com.artillexstudios.axapi.database.handler.ListHandler;
import com.artillexstudios.axapi.database.handler.SimpleHandler;
import com.artillexstudios.axapi.database.handler.TransformerHandler;
import com.artillexstudios.axapi.items.WrappedItemStack;
import com.artillexstudios.axapi.libs.snakeyaml.DumperOptions;
import com.artillexstudios.axapi.utils.ItemBuilder;
import com.artillexstudios.axapi.utils.MessageUtils;
import com.artillexstudios.axapi.utils.Pair;
import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axcosmetics.AxCosmeticsPlugin;
import com.artillexstudios.axcosmetics.api.AxCosmeticsAPI;
import com.artillexstudios.axcosmetics.api.cosmetics.Cosmetic;
import com.artillexstudios.axcosmetics.api.cosmetics.CosmeticData;
import com.artillexstudios.axcosmetics.api.cosmetics.config.CosmeticConfig;
import com.artillexstudios.axcosmetics.api.user.User;
import com.artillexstudios.axcosmetics.config.Config;
import com.artillexstudios.axcosmetics.config.Language;
import com.artillexstudios.axcosmetics.gui.implementation.CosmeticsGui;
import com.artillexstudios.axcosmetics.utils.FileUtils;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.AsyncOfflinePlayerArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.apache.commons.lang3.function.TriFunction;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
                                            Pair<TriFunction<User, CosmeticData, CosmeticConfig, Cosmetic<CosmeticConfig>>, CosmeticConfig> cosmeticBuilder = args.getUnchecked("cosmetic");
                                            if (cosmeticBuilder == null) {
                                                return;
                                            }

                                            Cosmetic<?> cosmetic = cosmeticBuilder.first().apply(user, new CosmeticData(0, 0, 0, System.currentTimeMillis()), cosmeticBuilder.second());
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

                                                    Pair<TriFunction<User, CosmeticData, CosmeticConfig, Cosmetic<CosmeticConfig>>, CosmeticConfig> cosmeticBuilder = args.getUnchecked("cosmetic");
                                                    if (cosmeticBuilder == null) {
                                                        return;
                                                    }

                                                    playerFuture.thenAccept(player -> {
                                                        AxCosmeticsAPI.instance().getUser(player).thenAccept(user -> {
                                                            Cosmetic<?> cosmetic = cosmeticBuilder.first().apply(user, new CosmeticData(0, 0, 0, System.currentTimeMillis()), cosmeticBuilder.second());

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
                        ).then(new LiteralArgument("convert")
                                .withPermission("axcosmetics.command.admin.convert")
                                .executes((sender, args) -> {
                                    Path resolve = FileUtils.PLUGIN_DIRECTORY.resolve("../").resolve("HMCCosmetics").resolve("cosmetics");
                                    if (!Files.exists(resolve)) {
                                        sender.sendMessage("Folder doesn't exist!");
                                        return;
                                    }

                                    if (!Files.isDirectory(resolve)) {
                                        sender.sendMessage("File is not a directory!");
                                        return;
                                    }

                                    Path convertedPath = FileUtils.PLUGIN_DIRECTORY.resolve("cosmetics").resolve("converted.yml");
                                    YamlConfiguration<?> newConfig = YamlConfiguration.of(convertedPath)
                                            .withDumperOptions(options -> {
                                                options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
                                                options.setSplitLines(false);
                                            }).build();
                                    List<Pair<String, String>> permissions = new ArrayList<>();
                                    for (File file : resolve.toFile().listFiles()) {
                                        YamlConfiguration<?> configuration = YamlConfiguration.of(file.toPath())
                                                .withDumperOptions(options -> {
                                                    options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
                                                    options.setSplitLines(false);
                                                }).build();

                                        for (String key : configuration.keys()) {
                                            String slot = configuration.getString(key + ".slot");
                                            permissions.add(Pair.of(key, configuration.getString(key + ".permission")));
                                            if (slot.equalsIgnoreCase("backpack")) {
                                                WrappedItemStack stack = new ItemBuilder((Map<Object, Object>) configuration.getMap(key + ".item"))
                                                        .wrapped();
                                                
                                                WrappedItemStack firstPersonStack = new ItemBuilder((Map<Object, Object>) configuration.getMap(key + ".item"))
                                                        .wrapped();
                                                newConfig.set(key + ".slot", "backpack");
                                                newConfig.set(key + ".type", "backpack");
                                                newConfig.set(key + ".height", 1.5);
                                                newConfig.set(key + ".item-stack", stack);
                                                newConfig.set(key + ".first-person-item-stack", firstPersonStack);
                                            } else if (slot.equalsIgnoreCase("HELMET")) {
                                                WrappedItemStack stack = new ItemBuilder((Map<Object, Object>) configuration.getMap(key + ".item"))
                                                        .wrapped();

                                                newConfig.set(key + ".slot", "helmet");
                                                newConfig.set(key + ".type", "armor");
                                                newConfig.set(key + ".item-stack", stack);
                                            } else {
                                                sender.sendMessage("Don't know how to convert slot: " + slot);
                                            }
                                        }
                                    }

                                    newConfig.save();

                                    List<CompletableFuture<?>> completableFutures = AxCosmeticsPlugin.instance().configLoader().loadFile(convertedPath.toFile());
                                    CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0])).thenRun(() -> {
                                        sender.sendMessage("Converted cosmetics! Starting user data conversion!");

                                        for (Pair<String, String> permission : permissions) {
                                            List<String> uuids = AxCosmeticsPlugin.instance().handler()
                                                    .rawQuery("SELECT uuid FROM luckperms_user_permissions WHERE permission = ?;", new ListHandler<>(new SimpleHandler<String>()))
                                                    .create()
                                                    .query(permission.second());

                                            List<CosmeticConvertData> datas = AxCosmeticsPlugin.instance().handler()
                                                    .rawQuery("SELECT acted_uuid, time FROM luckperms_actions WHERE action LIKE ? ORDER BY time DESC;", new ListHandler<>(new TransformerHandler<>(CosmeticConvertData.class)))
                                                    .create()
                                                    .query("permission set " + permission.second() + " %");

                                            for (CosmeticConvertData data : datas) {
                                                if (!uuids.contains(data.uuid)) {
                                                    sender.sendMessage("Skipping, due to not being in uuids!");
                                                    continue;
                                                }

                                                AxCosmeticsAPI.instance().getUser(UUID.fromString(data.uuid)).thenAccept(user -> {
                                                    CosmeticConfig fetch = AxCosmeticsAPI.instance().cosmeticConfigs().fetch(permission.first());
                                                    if (fetch == null) {
                                                        LogUtils.error("No fetched with id {}", permission.first());
                                                        return;
                                                    }

                                                    TriFunction<User, CosmeticData, CosmeticConfig, Cosmetic<CosmeticConfig>> fetch1 = AxCosmeticsAPI.instance().cosmeticTypes().fetch(fetch.type());
                                                    if (fetch1 == null) {
                                                        LogUtils.error("No other fetched with id {}", fetch.type());
                                                        return;
                                                    }

                                                    Cosmetic<?> cosmetic = fetch1.apply(user, new CosmeticData(0, 0, 0, Instant.ofEpochSecond(data.time).toEpochMilli()), fetch);
                                                    user.addCosmetic(cosmetic);
                                                });
                                            }
                                            sender.sendMessage("Converted: " + permission.first());
                                        }
                                    });
                                })
                        )
                )
                .register();
    }

    public record CosmeticConvertData(String uuid, long time) {

    }
}
