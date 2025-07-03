package com.artillexstudios.axcosmetics.gui.implementation;

import com.artillexstudios.axapi.config.YamlConfiguration;
import com.artillexstudios.axapi.items.WrappedItemStack;
import com.artillexstudios.axapi.items.component.DataComponents;
import com.artillexstudios.axapi.libs.snakeyaml.DumperOptions;
import com.artillexstudios.axapi.utils.MessageUtils;
import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axcosmetics.AxCosmeticsPlugin;
import com.artillexstudios.axcosmetics.api.cosmetics.Cosmetic;
import com.artillexstudios.axcosmetics.api.user.User;
import com.artillexstudios.axcosmetics.config.Language;
import com.artillexstudios.axcosmetics.gui.GuiBase;
import com.artillexstudios.axcosmetics.utils.FileUtils;
import dev.triumphteam.gui.guis.GuiItem;
import org.bukkit.entity.Player;

public class CosmeticAdminGui extends GuiBase {
    public static final YamlConfiguration<?> COSMETIC_CONFIG = YamlConfiguration.of(FileUtils.PLUGIN_DIRECTORY.resolve("gui.yml"))
            .withDefaults(AxCosmeticsPlugin.instance().getResource("gui.yml"))
            .withDumperOptions(options -> {
                options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
                options.setSplitLines(false);
            })
            .build();

    static {
        COSMETIC_CONFIG.load();
    }

    private final User other;

    public CosmeticAdminGui(User user, User other) {
        super(user, COSMETIC_CONFIG, true, false);
        this.other = other;
    }

    @Override
    public void open() {
        Player player = this.user().onlinePlayer();

        if (player == null) {
            LogUtils.warn("Attempted to open admin gui for offline player {} ({})", this.user().player().getName(), this.user().player().getUniqueId());
            return;
        }

        MessageUtils.sendMessage(player, Language.prefix, Language.adminGuiMessage);
        for (Cosmetic<?> cosmetic : this.other.getCosmetics()) {
            WrappedItemStack stack = cosmetic.config().guiItem(cosmetic.data());

            if (this.other.isEquipped(cosmetic)) {
                stack.set(DataComponents.enchantmentGlintOverride(), true);
                this.gui().addItem(new GuiItem(stack.toBukkit(), event -> {
                    if (event.isShiftClick()) {
                        this.other.deleteCosmetic(cosmetic);
                    } else {
                        this.other.unequipCosmetic(cosmetic);
                    }

                    new CosmeticAdminGui(this.user(), this.other).open();
                }));
            } else {
                this.gui().addItem(new GuiItem(stack.toBukkit(), event -> {
                    if (event.isShiftClick()) {
                        this.other.deleteCosmetic(cosmetic);
                    } else {
                        this.other.equipCosmetic(cosmetic);
                    }

                    new CosmeticAdminGui(this.user(), this.other).open();
                }));
            }
        }

        this.gui().open(player);
    }
}
