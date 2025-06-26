package com.artillexstudios.axcosmetics.listener;

import com.artillexstudios.axapi.items.WrappedItemStack;
import com.artillexstudios.axapi.packet.ClientboundPacketTypes;
import com.artillexstudios.axapi.packet.PacketEvent;
import com.artillexstudios.axapi.packet.PacketListener;
import com.artillexstudios.axapi.packet.ServerboundPacketTypes;
import com.artillexstudios.axapi.packet.wrapper.clientbound.ClientboundAddEntityWrapper;
import com.artillexstudios.axapi.packet.wrapper.clientbound.ClientboundContainerSetContentWrapper;
import com.artillexstudios.axapi.packet.wrapper.clientbound.ClientboundContainerSetSlotWrapper;
import com.artillexstudios.axapi.packet.wrapper.clientbound.ClientboundSetEquipmentWrapper;
import com.artillexstudios.axapi.utils.EquipmentSlot;
import com.artillexstudios.axapi.utils.Pair;
import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axcosmetics.api.AxCosmeticsAPI;
import com.artillexstudios.axcosmetics.api.cosmetics.Cosmetic;
import com.artillexstudios.axcosmetics.api.cosmetics.CosmeticSlot;
import com.artillexstudios.axcosmetics.api.cosmetics.CosmeticSlots;
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
                if (Config.debug) {
                    LogUtils.debug("Equipment null player!");
                }
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
                if (Config.debug) {
                    LogUtils.debug("Container set slot packet, user null!");
                }
                return;
            }

            ClientboundContainerSetSlotWrapper wrapper = new ClientboundContainerSetSlotWrapper(event);
            int containerId = wrapper.containerId();
            if (containerId != 0) {
                if (Config.debug) {
                    LogUtils.debug("Container set slot packet, not player inventory");
                }
                return;
            }

            CosmeticSlot slot = getCosmeticSlot(wrapper.slot());
            if (slot == null) {
                if (Config.debug) {
                    LogUtils.debug("Not a cosmetic slot!");
                }
                return;
            }

            Cosmetic<?> cosmetic = user.getCosmetic(slot);

            if (cosmetic instanceof ArmorCosmetic armor) {
                if (Config.debug) {
                    LogUtils.debug("Updating item!");
                }
                wrapper.stack(armor.config().itemStack(armor.data()));
            }
        } else if (event.type() == ClientboundPacketTypes.CONTAINER_CONTENT) {
            User user = AxCosmeticsAPI.instance().getUserIfLoadedImmediately(event.player().getUniqueId());
            if (user == null) {
                if (Config.debug) {
                    LogUtils.debug("Container set content, user null!");
                }
                return;
            }

            ClientboundContainerSetContentWrapper wrapper = new ClientboundContainerSetContentWrapper(event);
            int containerId = wrapper.containerId();
            if (containerId != 0) {
                if (Config.debug) {
                    LogUtils.debug("Container set content packet, not player inventory");
                }
                return;
            }

            if (Config.debug) {
                LogUtils.debug("SET CONTENT! Items: {}", wrapper.items());
            }
            for (Cosmetic<?> equippedCosmetic : user.getEquippedCosmetics()) {
                if (!(equippedCosmetic instanceof ArmorCosmetic armorCosmetic)) {
                    continue;
                }

                int index = equipmentSlot(armorCosmetic.slot(), user.onlinePlayer());
                wrapper.items().set(index, armorCosmetic.config().itemStack(armorCosmetic.data()));
            }
            wrapper.markDirty();
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
        sendUserArmorUpdate(user, eventPlayer, false);
    }

    public static void sendUserArmorUpdate(User user, Player eventPlayer, boolean inventory) {
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
                if (inventory) {
                    armorCosmetic.updateInventory();
                } else {
                    armorCosmetic.markResend();
                }
            }

            return;
        }
    }

    public static CosmeticSlot getCosmeticSlot(int slot) {
        return switch (slot) {
            case 5 -> CosmeticSlots.HELMET;
            case 6 -> CosmeticSlots.CHEST_PLATE;
            case 7 -> CosmeticSlots.LEGGINGS;
            case 8 -> CosmeticSlots.BOOTS;
            default -> null;
        };
    }

    public static int equipmentSlot(CosmeticSlot slot, Player player) {
        return switch (slot.name()) {
            case "helmet" -> 5;
            case "chest_plate" -> 6;
            case "leggings" -> 7;
            case "boots" -> 8;
            case "main_hand" -> heldItemSlot(player);
            case "off_hand" -> 45;
            case null, default -> throw new IllegalStateException();
        };
    }

    public static int heldItemSlot(Player player) {
        int slot = player.getInventory().getHeldItemSlot();
        if (slot < 0 || slot > 8) {
            throw new IllegalArgumentException();
        }

        return slot + 36;
    }
}
