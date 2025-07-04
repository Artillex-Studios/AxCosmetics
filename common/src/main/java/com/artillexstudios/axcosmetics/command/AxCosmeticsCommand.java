package com.artillexstudios.axcosmetics.command;

import com.artillexstudios.axapi.AxPlugin;
import com.artillexstudios.axapi.config.YamlConfiguration;
import com.artillexstudios.axapi.database.handler.ListHandler;
import com.artillexstudios.axapi.database.handler.SimpleHandler;
import com.artillexstudios.axapi.database.handler.TransformerHandler;
import com.artillexstudios.axapi.database.impl.MySQLDatabaseType;
import com.artillexstudios.axapi.libs.snakeyaml.DumperOptions;
import com.artillexstudios.axapi.scheduler.Scheduler;
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
import com.artillexstudios.axcosmetics.gui.implementation.CosmeticAdminGui;
import com.artillexstudios.axcosmetics.gui.implementation.CosmeticsGui;
import com.artillexstudios.axcosmetics.utils.FileUtils;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import dev.jorel.commandapi.CommandAPICommand;
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
        for (String alias : Config.aliases) {
            new CommandAPICommand(alias)
                    .withPermission("axcosmetics.command.gui")
                    .executesPlayer((sender, args) -> {
                        User user = AxCosmeticsAPI.instance().getUserIfLoadedImmediately(sender);
                        new CosmeticsGui(user).open();
                    })
                    .register();
        }

        new CommandTree("cosmetics")
                .then(new LiteralArgument("gui")
                        .withPermission("axcosmetics.command.gui")
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

                                            Cosmetic<?> cosmetic = cosmeticBuilder.first().apply(user, new CosmeticData(0, 0, 0, System.currentTimeMillis(), false), cosmeticBuilder.second());
                                            if (cosmetic == null) {
                                                System.out.println("Null cosmetic!");
                                                return;
                                            }

                                            user.equipCosmetic(cosmetic);
                                        })
                                )
                        ).then(new LiteralArgument("give")
                                .withPermission("axcosmetics.command.admin.give")
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
                                                            Cosmetic<?> cosmetic = cosmeticBuilder.first().apply(user, new CosmeticData(0, 0, 0, System.currentTimeMillis(), false), cosmeticBuilder.second());

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
                        ).then(new LiteralArgument("view")
                                .withPermission("axcosmetics.command.admin.view")
                                .then(new AsyncOfflinePlayerArgument("player")
                                        .executesPlayer((sender, args) -> {
                                            User user = AxCosmeticsAPI.instance().getUserIfLoadedImmediately(sender.getUniqueId());
                                            if (user == null) {
                                                return;
                                            }

                                            CompletableFuture<OfflinePlayer> playerFuture = args.getUnchecked("player");
                                            if (playerFuture == null) {
                                                return;
                                            }

                                            playerFuture.thenAccept(offlinePlayer -> {
                                                AxCosmeticsAPI.instance().getUser(offlinePlayer).thenAccept(otherUser -> {
                                                   new CosmeticAdminGui(user, otherUser).open();
                                                });
                                            });
                                        })
                                )
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

                                    newConfig.load();
                                    List<Pair<String, String>> permissions = new ArrayList<>();
                                    for (File file : resolve.toFile().listFiles()) {
                                        YamlConfiguration<?> configuration = YamlConfiguration.of(file.toPath())
                                                .withDumperOptions(options -> {
                                                    options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
                                                    options.setSplitLines(false);
                                                }).build();
                                        configuration.load();

                                        for (String key : configuration.keys()) {
                                            String slot = configuration.getString(key + ".slot");
                                            String permission = configuration.getString(key + ".permission");
                                            permissions.add(Pair.of(key, configuration.getString(key + ".permission")));
                                            if (slot.equalsIgnoreCase("backpack")) {
                                                newConfig.set(key + ".slot", "backpack");
                                                newConfig.set(key + ".type", "backpack");
                                                newConfig.set(key + ".height", 1.5);
                                                String material = configuration.getString(key + ".item.material");
                                                String name = configuration.getString(key + ".item.name");
                                                Integer customModelData = configuration.getInteger(key + ".item.model-data");

                                                String firstPersonMaterial = configuration.getString(key + ".firstperson-item.material");
                                                String firstPersonName = configuration.getString(key + ".firstperson-item.name");
                                                Integer firstPersonCustomModelData = configuration.getInteger(key + ".firstperson-item.model-data");

                                                newConfig.set(key + ".item-stack.material", material);
                                                newConfig.set(key + ".item-stack.name", name);
                                                newConfig.set(key + ".item-stack.custom-model-data", customModelData);
                                                newConfig.set(key + ".first-person-item-stack.material", firstPersonMaterial);
                                                newConfig.set(key + ".first-person-item-stack.name", firstPersonName);
                                                newConfig.set(key + ".first-person-item-stack.custom-model-data", firstPersonCustomModelData);
                                                if (permission != null && !permission.isBlank()) {
                                                    newConfig.set(key + ".permission", permission);
                                                }
                                            } else if (slot.equalsIgnoreCase("HELMET")) {
                                                newConfig.set(key + ".slot", "helmet");
                                                newConfig.set(key + ".type", "armor");
                                                String material = configuration.getString(key + ".item.material");
                                                String name = configuration.getString(key + ".item.name");
                                                Integer customModelData = configuration.getInteger(key + ".item.custom-model-data");
                                                newConfig.set(key + ".item-stack.material", material);
                                                newConfig.set(key + ".item-stack.name", name);
                                                newConfig.set(key + ".item-stack.custom-model-data", customModelData);
                                                if (permission != null && !permission.isBlank()) {
                                                    newConfig.set(key + ".permission", permission);
                                                }
                                            } else {
                                                sender.sendMessage("Don't know how to convert slot: " + slot);
                                            }
                                        }
                                    }

                                    newConfig.save();
                                    LogUtils.warn("Permissions: {}", permissions);
                                    List<CompletableFuture<?>> completableFutures = AxCosmeticsPlugin.instance().configLoader().loadFile(convertedPath.toFile());
                                    CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0])).thenRunAsync(() -> {
                                        if (!(Config.database.type instanceof MySQLDatabaseType)) {
                                            return;
                                        }

                                        sender.sendMessage("Converted cosmetics! Starting user data conversion!");

                                        for (Pair<String, String> permission : permissions) {
                                            List<UUID> uuids = new ArrayList<>(AxCosmeticsPlugin.instance().handler()
                                                    .rawQuery("SELECT uuid FROM luckperms_user_permissions WHERE permission = ?;", new ListHandler<>(new SimpleHandler<String>()))
                                                    .create()
                                                    .query(permission.second()).stream()
                                                    .map(UUID::fromString)
                                                    .toList());
                                            LogUtils.info("Permission: {}, UUIDS: {}", permission.first(), uuids);

                                            List<UUIDCosmeticConvertData> datas = AxCosmeticsPlugin.instance().handler()
                                                    .rawQuery("SELECT acted_uuid, time FROM luckperms_actions WHERE action LIKE ? ORDER BY time ASC;", new ListHandler<>(new TransformerHandler<>(CosmeticConvertData.class)))
                                                    .create()
                                                    .query("permission set " + permission.second() + " %").stream()
                                                    .filter(data -> data.uuid != null &&
                                                            !data.uuid.trim().isEmpty() &&
                                                            !"null".equalsIgnoreCase(data.uuid))
                                                    .map(data -> {
                                                        return new UUIDCosmeticConvertData(UUID.fromString(data.uuid), data.time);
                                                    }).toList();

                                            int converted = 0;
                                            int fixed = 0;
                                            for (UUIDCosmeticConvertData data : datas) {
                                                if (!uuids.remove(data.uuid)) {
                                                    sender.sendMessage("Skipping, due to not being in uuids!");
                                                    continue;
                                                }
                                                converted++;

                                                AxCosmeticsAPI.instance().getUser(data.uuid).thenAccept(user -> {
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

                                                    Cosmetic<?> cosmetic = fetch1.apply(user, new CosmeticData(0, 0, 0, Instant.ofEpochSecond(data.time).toEpochMilli(), false), fetch);
                                                    user.addCosmetic(cosmetic);
                                                });
                                            }

                                            for (UUID uuid : uuids) {
                                                LogUtils.info("No action data for uuids: {}", uuid);
                                                fixed++;
                                                AxCosmeticsAPI.instance().getUser(uuid).thenAccept(user -> {
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

                                                    Cosmetic<?> cosmetic = fetch1.apply(user, new CosmeticData(0, 0, 0, System.currentTimeMillis(), false), fetch);
                                                    user.addCosmetic(cosmetic);
                                                }).exceptionally(throwable -> {
                                                    LogUtils.error("Error occurred!", throwable);
                                                    return null;
                                                });
                                            }
                                            sender.sendMessage("Converted: " + permission.first() + " count: " + converted + " fixed: " + fixed);
                                        }
                                    }, runnable -> Scheduler.get().run(task -> runnable.run())).exceptionally(throwable -> {
                                        LogUtils.error("Error!", throwable);
                                        return null;
                                    });
                                })
                        )
                )
                .register();
    }

    public record CosmeticConvertData(String uuid, long time) {

    }

    public record UUIDCosmeticConvertData(UUID uuid, long time) {

    }
}
