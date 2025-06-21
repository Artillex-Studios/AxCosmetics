package com.artillexstudios.axcosmetics.gui.implementation;

import com.artillexstudios.axapi.config.YamlConfiguration;
import com.artillexstudios.axapi.items.WrappedItemStack;
import com.artillexstudios.axapi.items.component.DataComponents;
import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axcosmetics.AxCosmeticsPlugin;
import com.artillexstudios.axcosmetics.api.cosmetics.Cosmetic;
import com.artillexstudios.axcosmetics.api.user.User;
import com.artillexstudios.axcosmetics.cosmetics.type.ArmorCosmetic;
import com.artillexstudios.axcosmetics.cosmetics.type.FirstPersonBackpackCosmetic;
import com.artillexstudios.axcosmetics.gui.GuiBase;
import com.artillexstudios.axcosmetics.utils.FileUtils;
import dev.triumphteam.gui.guis.GuiItem;
import org.bukkit.entity.Player;

public class CosmeticsGui extends GuiBase {
    public static final YamlConfiguration<?> COSMETIC_CONFIG = YamlConfiguration.of(FileUtils.PLUGIN_DIRECTORY.resolve("gui.yml"))
            .withDefaults(AxCosmeticsPlugin.instance().getResource("gui.yml"))
            .build();

    static {
        COSMETIC_CONFIG.load();
    }

    public CosmeticsGui(User user) {
        super(user, COSMETIC_CONFIG, true, false);
    }

    @Override
    public void open() {
        Player player = this.user().onlinePlayer();

        if (player == null) {
            LogUtils.warn("Attempted to open main gui for offline player {} ({})", this.user().player().getName(), this.user().player().getUniqueId());
            return;
        }

        for (Cosmetic<?> cosmetic : this.user().getCosmetics()) {
            WrappedItemStack stack;
            if (cosmetic instanceof FirstPersonBackpackCosmetic backpackCosmetic) {
                stack = backpackCosmetic.config().itemStack().copy();
            } else if (cosmetic instanceof ArmorCosmetic armorCosmetic) {
                stack = armorCosmetic.config().itemStack().copy();
            } else {
                continue;
            }

            // TODO: Message on equip/unequip
            if (this.user().isEquipped(cosmetic)) {
                stack.set(DataComponents.enchantmentGlintOverride(), true);
                stack.finishEdit();
                this.gui().addItem(new GuiItem(stack.toBukkit(), event -> {
                    this.user().unequipCosmetic(cosmetic);
                    new CosmeticsGui(this.user()).open();
                }));
            } else {
                this.gui().addItem(new GuiItem(stack.toBukkit(), event -> {
                    this.user().equipCosmetic(cosmetic);
                    new CosmeticsGui(this.user()).open();
                }));
            }
        }

        this.gui().open(player);
    }
}
