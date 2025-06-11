package com.artillexstudios.axcosmetics.cosmetics.type;

import com.artillexstudios.axapi.items.WrappedItemStack;
import com.artillexstudios.axapi.nms.wrapper.ServerPlayerWrapper;
import com.artillexstudios.axapi.packet.wrapper.clientbound.ClientboundSetEquipmentWrapper;
import com.artillexstudios.axapi.utils.EquipmentSlot;
import com.artillexstudios.axapi.utils.Pair;
import com.artillexstudios.axcosmetics.api.cosmetics.Cosmetic;
import com.artillexstudios.axcosmetics.api.cosmetics.CosmeticData;
import com.artillexstudios.axcosmetics.api.cosmetics.CosmeticSlot;
import com.artillexstudios.axcosmetics.api.user.User;
import com.artillexstudios.axcosmetics.cosmetics.config.ArmorCosmeticConfig;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class ArmorCosmetic extends Cosmetic<ArmorCosmeticConfig> {
    private static final org.bukkit.inventory.EquipmentSlot[] slots = new org.bukkit.inventory.EquipmentSlot[]{org.bukkit.inventory.EquipmentSlot.HAND, org.bukkit.inventory.EquipmentSlot.OFF_HAND, org.bukkit.inventory.EquipmentSlot.FEET, org.bukkit.inventory.EquipmentSlot.LEGS, org.bukkit.inventory.EquipmentSlot.CHEST, org.bukkit.inventory.EquipmentSlot.HEAD};
    private Player player;

    public ArmorCosmetic(User user, CosmeticData data, ArmorCosmeticConfig config) {
        super(user, data, config);
    }

    @Override
    public void spawn() {
        this.player = this.user().onlinePlayer();
        if (this.player == null) {
            throw new IllegalStateException();
        }

        for (Player tracker : this.player.getTrackedBy()) {
            sendEquipmentPacket(this.player.getEntityId(), tracker);
        }
        sendEquipmentPacket(this.player.getEntityId(), this.player);
    }

    @Override
    public void update() {
        // do nothing
    }

    @Override
    public void despawn() {
        if (this.player == null) {
            throw new IllegalStateException();
        }

        for (Player tracker : this.player.getTrackedBy()) {
            sendEquipmentPacket(this.player.getEntityId(), tracker);
        }
        sendEquipmentPacket(this.player.getEntityId(), this.player);
        this.player = null;
    }

    // TODO: Maybe rework
    public static void sendEquipmentPacket(int entityId, Player player) {
        List<Pair<EquipmentSlot, WrappedItemStack>> equipment = new ArrayList<>();
        for (org.bukkit.inventory.EquipmentSlot value : slots) {
            ItemStack item = player.getInventory().getItem(value);
            if (item == null || item.getType().isAir()) {
                continue;
            }


            equipment.add(Pair.of(EquipmentSlot.values()[value.ordinal()], WrappedItemStack.wrap(item)));
        }

        if (equipment.isEmpty()) {
            equipment.add(Pair.of(EquipmentSlot.HELMET, WrappedItemStack.wrap(new ItemStack(Material.AIR))));
        }

        ServerPlayerWrapper serverPlayerWrapper = ServerPlayerWrapper.wrap(player);
        serverPlayerWrapper.sendPacket(new ClientboundSetEquipmentWrapper(entityId, equipment));
    }

    @Override
    public Collection<CosmeticSlot> validSlots() {
        return List.of();
    }
}
