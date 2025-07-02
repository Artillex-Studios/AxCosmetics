package com.artillexstudios.axcosmetics.database.dto;

import java.util.UUID;

public record UserDTO(Integer userId, UUID uuid, Integer cosmeticId, Integer cosmeticTypeId, Integer counter, Integer color, Long timeStamp,
                      Boolean equipped, Boolean permission) {

    public UserDTO(Integer userId, String uuid, Integer cosmeticId, Integer cosmeticTypeId, Integer counter, Integer color, Long timeStamp, Boolean equipped, Boolean permission) {
        this(userId, UUID.fromString(uuid), cosmeticId, cosmeticTypeId, counter, color, timeStamp, equipped, permission);
    }
}
