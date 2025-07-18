package com.artillexstudios.axcosmetics.gui;

import com.artillexstudios.axapi.config.YamlConfiguration;
import com.artillexstudios.axapi.context.ContextKey;
import com.artillexstudios.axapi.gui.configuration.ConfigurationBackedGui;
import com.artillexstudios.axapi.gui.configuration.ConfigurationBackedGuiBuilder;
import com.artillexstudios.axapi.gui.inventory.Gui;
import com.artillexstudios.axapi.gui.inventory.GuiItem;
import com.artillexstudios.axapi.gui.inventory.GuiKeys;
import com.artillexstudios.axapi.gui.inventory.provider.implementation.AsyncGuiItemProvider;
import com.artillexstudios.axapi.items.WrappedItemStack;
import com.artillexstudios.axapi.items.component.DataComponents;
import com.artillexstudios.axapi.libs.snakeyaml.DumperOptions;
import com.artillexstudios.axapi.utils.MessageUtils;
import com.artillexstudios.axcosmetics.AxCosmeticsPlugin;
import com.artillexstudios.axcosmetics.api.AxCosmeticsAPI;
import com.artillexstudios.axcosmetics.api.cosmetics.Cosmetic;
import com.artillexstudios.axcosmetics.api.user.User;
import com.artillexstudios.axcosmetics.config.Language;
import com.artillexstudios.axcosmetics.utils.FileUtils;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class Guis {
    public static ContextKey<User> OTHER_PLAYER = new ContextKey<>("other_player", User.class);
    private static final YamlConfiguration<?> COSMETIC_CONFIG = YamlConfiguration.of(FileUtils.PLUGIN_DIRECTORY.resolve("gui.yml"))
            .withDefaults(AxCosmeticsPlugin.instance().getResource("gui.yml"))
            .withDumperOptions(options -> {
                options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
                options.setSplitLines(false);
            })
            .build();
    public static final ConfigurationBackedGui<Gui> GUI = ConfigurationBackedGuiBuilder.builder(COSMETIC_CONFIG, true)
            .withProvider(Cosmetic.class, cosmetic -> {
                return new AsyncGuiItemProvider(new GuiItem(ctx -> {
                    User user = AxCosmeticsAPI.instance().getUserIfLoadedImmediately(ctx.get(GuiKeys.PLAYER));

                    WrappedItemStack stack = cosmetic.config().guiItem(cosmetic.data());
                    if (user.isEquipped(cosmetic)) {
                        stack.set(DataComponents.enchantmentGlintOverride(), true);
                    }

                    return stack;
                }, (ctx, event) -> {
                    Player player = ctx.get(GuiKeys.PLAYER);
                    User user = AxCosmeticsAPI.instance().getUserIfLoadedImmediately(player);
                    Gui inv = ctx.get(GuiKeys.GUI);
                    if (user.isEquipped(cosmetic)) {
                        user.unequipCosmetic(cosmetic);
                        MessageUtils.sendMessage(player, Language.prefix, Language.unequip, Placeholder.unparsed("cosmetic", cosmetic.config().name()));
                        inv.open();
                    } else {
                        user.equipCosmetic(cosmetic);
                        MessageUtils.sendMessage(player, Language.prefix, Language.equip, Placeholder.unparsed("cosmetic", cosmetic.config().name()));
                        inv.open();
                    }
                }));
            }).withValues(ctx -> {
                Player player = ctx.get(GuiKeys.PLAYER);
                User user = AxCosmeticsAPI.instance().getUserIfLoadedImmediately(player);
                return () -> new ArrayList<>(user.getCosmetics());
            }).build();
    public static final ConfigurationBackedGui<Gui> ADMIN_GUI = ConfigurationBackedGuiBuilder.builder(COSMETIC_CONFIG, true)
            .withProvider(Cosmetic.class, cosmetic -> {
                return new AsyncGuiItemProvider(new GuiItem(ctx -> {
                    User user = ctx.get(OTHER_PLAYER);

                    WrappedItemStack stack = cosmetic.config().guiItem(cosmetic.data());
                    if (user.isEquipped(cosmetic)) {
                        stack.set(DataComponents.enchantmentGlintOverride(), true);
                    }

                    return stack;
                }, (ctx, event) -> {
                    User other = ctx.get(OTHER_PLAYER);
                    Player player = ctx.get(GuiKeys.PLAYER);
                    Gui inv = ctx.get(GuiKeys.GUI);
                    if (other.isEquipped(cosmetic)) {
                        other.unequipCosmetic(cosmetic);
                        MessageUtils.sendMessage(player, Language.prefix, Language.unequip, Placeholder.unparsed("cosmetic", cosmetic.config().name()));
                        inv.open();
                    } else {
                        other.equipCosmetic(cosmetic);
                        MessageUtils.sendMessage(player, Language.prefix, Language.equip, Placeholder.unparsed("cosmetic", cosmetic.config().name()));
                        inv.open();
                    }
                }));
            }).withValues(ctx -> {
                User user = ctx.get(OTHER_PLAYER);
                return () -> new ArrayList<>(user.getCosmetics());
            }).onOpen(event -> {
                MessageUtils.sendMessage(event.getPlayer(), Language.prefix, Language.adminGuiMessage);
            }).build();
}
