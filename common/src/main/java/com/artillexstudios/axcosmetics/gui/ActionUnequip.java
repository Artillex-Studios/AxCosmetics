package com.artillexstudios.axcosmetics.gui;

import com.artillexstudios.axapi.context.HashMapContext;
import com.artillexstudios.axapi.gui.configuration.actions.Action;
import com.artillexstudios.axapi.gui.inventory.GuiKeys;
import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axcosmetics.api.AxCosmeticsAPI;
import com.artillexstudios.axcosmetics.api.cosmetics.CosmeticSlot;
import com.artillexstudios.axcosmetics.api.user.User;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class ActionUnequip extends Action<List<CosmeticSlot>> {

    public ActionUnequip(String data) {
        super(data);
    }

    @Override
    public List<CosmeticSlot> transform(String data) {
        String[] split = data.replace(" ", "").split(",");
        List<CosmeticSlot> slots = new ArrayList<>();
        for (String slot : split) {
            if (slot.equals("*") || slot.equals("all")) {
                slots.addAll(AxCosmeticsAPI.instance().cosmeticSlots().registered());
                break;
            }

            CosmeticSlot cosmeticSlot = AxCosmeticsAPI.instance().cosmeticSlots().fetch(slot);
            if (cosmeticSlot == null) {
                LogUtils.warn("Failed to find CosmeticSlot with id {}! Valid slots are: {}", slot, String.join(", ", AxCosmeticsAPI.instance().cosmeticSlots().names()));
                continue;
            }

            slots.add(cosmeticSlot);
        }

        return slots;
    }

    @Override
    public void run(Player player, HashMapContext context) {
        User user = context.get(Guis.OTHER_PLAYER);
        if (user == null) {
            user = AxCosmeticsAPI.instance().getUserIfLoadedImmediately(context.get(GuiKeys.PLAYER));
        }

        for (CosmeticSlot cosmeticSlot : this.value()) {
            user.unequipCosmetic(cosmeticSlot);
        }
    }
}
