package com.artillexstudios.axcosmetics.listener;

import com.artillexstudios.axapi.items.WrappedItemStack;
import com.artillexstudios.axapi.packet.ClientboundPacketTypes;
import com.artillexstudios.axapi.packet.PacketEvent;
import com.artillexstudios.axapi.packet.PacketListener;
import com.artillexstudios.axapi.packet.ServerboundPacketTypes;
import com.artillexstudios.axapi.packet.wrapper.clientbound.ClientboundAddEntityWrapper;
import com.artillexstudios.axapi.packet.wrapper.clientbound.ClientboundSetEquipmentWrapper;
import com.artillexstudios.axapi.utils.EquipmentSlot;
import com.artillexstudios.axapi.utils.Pair;
import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axcosmetics.api.AxCosmeticsAPI;
import com.artillexstudios.axcosmetics.api.cosmetics.Cosmetic;
import com.artillexstudios.axcosmetics.api.user.User;
import com.artillexstudios.axcosmetics.config.Config;
import com.artillexstudios.axcosmetics.cosmetics.type.ArmorCosmetic;
import org.bukkit.entity.Player;

import java.util.List;

public final class CosmeticPacketListener extends PacketListener {

    @Override
    public void onPacketSending(PacketEvent event) {
        if (event.type() == ClientboundPacketTypes.ADD_ENTITY) {
            ClientboundAddEntityWrapper wrapper = new ClientboundAddEntityWrapper(event);
            User user = AxCosmeticsAPI.instance().getUserIfLoadedImmediately(wrapper.uuid());
            if (user == null) {
                if (Config.debug) {
                    LogUtils.debug("Add entity packet player null!");
                }
                return;
            }

            if (Config.debug) {
                LogUtils.debug("Add entity packet!");
            }
            sendUserArmorUpdate(user, event.player());
            // The backpacks are probably tracked before the player.
            // What can we do about it?

        } else if (event.type() == ClientboundPacketTypes.SET_EQUIPMENT) {
            // TODO: Maybe cache armor cosmetic items
            ClientboundSetEquipmentWrapper wrapper = new ClientboundSetEquipmentWrapper(event);
            User user = AxCosmeticsAPI.instance().getUserIfLoadedImmediately(wrapper.entityId());
            if (user == null) {
                LogUtils.warn("Null player!");
                return;
            }

            List<Pair<EquipmentSlot, WrappedItemStack>> items = wrapper.items();
            for (Cosmetic<?> cosmetic : user.getEquippedCosmetics()) {
                if (!(cosmetic instanceof ArmorCosmetic armorCosmetic)) {
                    continue;
                }

                EquipmentSlot equipmentSlot = armorCosmetic.config().equipmentSlot();
                boolean found = false;
                for (int i = 0; i < items.size(); i++) {
                    Pair<EquipmentSlot, WrappedItemStack> pair = items.get(i);
                    if (pair.first() == equipmentSlot) {
                        items.set(i, Pair.of(equipmentSlot, armorCosmetic.config().itemStack(cosmetic.data())));
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    items.add(Pair.of(equipmentSlot, armorCosmetic.config().itemStack(cosmetic.data())));
                }
            }

            wrapper.markDirty();
        } else if (event.type() == ClientboundPacketTypes.CONTAINER_SET_SLOT) {
            User user = AxCosmeticsAPI.instance().getUserIfLoadedImmediately(event.player().getUniqueId());
            if (user == null) {
                return;
            }

            sendUserArmorUpdate(user, event.player());
        }
    }

    @Override
    public void onPacketReceive(PacketEvent event) {
        if (event.type() == ServerboundPacketTypes.CONTAINER_CLICK) {
            User user = AxCosmeticsAPI.instance().getUserIfLoadedImmediately(event.player().getUniqueId());
            if (user == null) {
                return;
            }

            sendUserArmorUpdate(user, event.player());
        }
    }

    public static void sendUserArmorUpdate(User user, Player eventPlayer) {
        Player player = user.onlinePlayer();
        if (player == null) {
            return;
        }

        if (user.getEquippedCosmetics().isEmpty()) {
            return;
        }

        for (Cosmetic<?> equippedCosmetic : user.getEquippedCosmetics()) {
            if (!(equippedCosmetic instanceof ArmorCosmetic armorCosmetic)) {
                continue;
            }

            if (player != eventPlayer) {
                // Only send the packet if we know we have an armor cosmetic
                ArmorCosmetic.sendEquipmentPacket(player, eventPlayer, armorCosmetic.config().equipmentSlot());
            } else {
                // Mark as needs to be resent, this limits packet spam
                armorCosmetic.markResend();
            }

            return;
        }
    }
}
