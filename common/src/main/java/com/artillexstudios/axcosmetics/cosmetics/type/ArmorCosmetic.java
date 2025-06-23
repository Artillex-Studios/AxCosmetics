package com.artillexstudios.axcosmetics.cosmetics.type;

import com.artillexstudios.axapi.items.WrappedItemStack;
import com.artillexstudios.axapi.nms.wrapper.ServerPlayerWrapper;
import com.artillexstudios.axapi.packet.wrapper.clientbound.ClientboundSetEquipmentWrapper;
import com.artillexstudios.axapi.scheduler.Scheduler;
import com.artillexstudios.axapi.utils.EquipmentSlot;
import com.artillexstudios.axapi.utils.Pair;
import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axcosmetics.api.cosmetics.Cosmetic;
import com.artillexstudios.axcosmetics.api.cosmetics.CosmeticData;
import com.artillexstudios.axcosmetics.api.cosmetics.CosmeticSlot;
import com.artillexstudios.axcosmetics.api.cosmetics.CosmeticSlots;
import com.artillexstudios.axcosmetics.api.user.User;
import com.artillexstudios.axcosmetics.config.Config;
import com.artillexstudios.axcosmetics.cosmetics.config.ArmorCosmeticConfig;
import com.artillexstudios.axcosmetics.utils.ThreadUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class ArmorCosmetic extends Cosmetic<ArmorCosmeticConfig> {
    private int resendTicks = -1;
    private boolean updateInventory = false;
    private boolean equipped = false;
    private Player player;

    public ArmorCosmetic(User user, CosmeticData data, ArmorCosmeticConfig config) {
        super(user, data, config);
        if (Config.debug) {
            LogUtils.debug("Created armor cosmetic for user {} with data: {}", user, data);
        }
    }

    @Override
    public void spawn() {
        ThreadUtils.ensureMain("ArmorCosmetic spawn");
        if (Config.debug) {
            LogUtils.debug("Armor equip!");
        }
        this.player = this.user().onlinePlayer();
        if (this.player == null) {
            throw new IllegalStateException();
        }

        for (Player tracker : this.player.getTrackedBy()) {
            sendEquipmentPacket(this.player, tracker, this.config().equipmentSlot());
        }
        sendEquipmentPacket(this.player, this.player, this.config().equipmentSlot());
        this.equipped = true;
    }

    @Override
    public void update() {
        if (!this.equipped) {
            return;
        }

        if (this.resendTicks > 0) {
            this.resendTicks--;
        } else if (this.resendTicks == 0) {
            sendEquipmentPacket(this.player, this.player, this.config().equipmentSlot());
            this.resendTicks = -1;
        }

        if (this.updateInventory) {
            this.updateInventory = false;
            Scheduler.get().run(this.player, task -> {
                this.player.updateInventory();
            }, () -> {});
        }
    }

    @Override
    public void despawn() {
        if (Config.debug) {
            LogUtils.debug("Armor unequip!");
        }
        if (!this.equipped) {
            return;
        }
        ThreadUtils.ensureMain("ArmorCosmetic despawn");

        this.equipped = false;
        if (this.player == null) {
            throw new IllegalStateException();
        }

        for (Player tracker : this.player.getTrackedBy()) {
            resendEquipmentPacket(this.player, tracker, this.config().equipmentSlot());
        }
        resendEquipmentPacket(this.player, this.player, this.config().equipmentSlot());
        this.player = null;
    }

    public static void sendEquipmentPacket(Player sender, Player receiver, EquipmentSlot slot) {
        sendEquipmentPacket(sender.getEntityId(), receiver, slot, new ItemStack(Material.AIR));
    }

    public static void resendEquipmentPacket(Player sender, Player receiver, EquipmentSlot slot) {
        ItemStack item = sender.getEquipment().getItem(org.bukkit.inventory.EquipmentSlot.values()[slot.ordinal()]);
        sendEquipmentPacket(sender.getEntityId(), receiver, slot, item);
    }

    public static void sendEquipmentPacket(int entityId, Player receiver, EquipmentSlot slot, ItemStack stack) {
        List<Pair<EquipmentSlot, WrappedItemStack>> equipment = new ArrayList<>();
        equipment.add(Pair.of(slot, WrappedItemStack.wrap(stack)));

        ServerPlayerWrapper serverPlayerWrapper = ServerPlayerWrapper.wrap(receiver);
        if (Config.debug) {
            LogUtils.debug("Sending packet to player {}, id: {}, entityid: {}", receiver, receiver.getEntityId(), entityId);
        }
        serverPlayerWrapper.sendPacket(new ClientboundSetEquipmentWrapper(entityId, equipment));
    }

    public void markResend() {
        this.resendTicks = Config.armorResendFrequency;
    }

    public void updateInventory() {
        this.updateInventory = true;
    }

    @Override
    public Collection<CosmeticSlot> validSlots() {
        return List.of(CosmeticSlots.HELMET, CosmeticSlots.CHEST_PLATE, CosmeticSlots.LEGGINGS, CosmeticSlots.BOOTS, CosmeticSlots.MAIN_HAND, CosmeticSlots.OFF_HAND);
    }
}
