package com.artillexstudios.axcosmetics.api.cosmetics;

/**
 * The data representing a cosmetic.
 * @param id The id of the cosmetic in the database.
 * @param counter The edition of the cosmetic.
 * @param color The color, currently unused.
 * @param timeStamp The timestamp at which this cosmetic was acquired.
 * @param permission If the cosmetic is a cosmetic gained from a permission.
 */
public record CosmeticData(int id, int counter, int color, long timeStamp, boolean permission) {

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof CosmeticData that)) {
            return false;
        }

        return this.id() == that.id();
    }

    @Override
    public int hashCode() {
        return this.id();
    }
}
