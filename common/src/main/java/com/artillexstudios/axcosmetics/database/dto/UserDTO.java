package com.artillexstudios.axcosmetics.database.dto;

import java.util.UUID;

public record UserDTO(int userId, UUID uuid, int cosmeticId, int cosmeticTypeId, int counter, int color, long timeStamp,
                      boolean equipped) {

    public UserDTO(int userId, String uuid, int cosmeticId, int cosmeticTypeId, int counter, int color, long timeStamp, boolean equipped) {
        this(userId, UUID.fromString(uuid), cosmeticId, cosmeticTypeId, counter, color, timeStamp, equipped);
    }
}
