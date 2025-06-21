package com.artillexstudios.axcosmetics.gui.actions.implementation;

import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axcosmetics.api.AxCosmeticsAPI;
import com.artillexstudios.axcosmetics.api.cosmetics.CosmeticSlot;
import com.artillexstudios.axcosmetics.api.user.User;
import com.artillexstudios.axcosmetics.gui.GuiBase;
import com.artillexstudios.axcosmetics.gui.actions.Action;

import java.util.ArrayList;
import java.util.List;

public final class ActionUnequip extends Action<List<CosmeticSlot>> {

    public ActionUnequip() {
        super("unequip");
    }

    @Override
    public List<CosmeticSlot> evaluate(String input) {
        String[] split = input.replace(" ", "").split(",");
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
    public void execute(User user, GuiBase base, List<CosmeticSlot> value) {
        for (CosmeticSlot cosmeticSlot : value) {
            user.unequipCosmetic(cosmeticSlot);
        }
    }
}
