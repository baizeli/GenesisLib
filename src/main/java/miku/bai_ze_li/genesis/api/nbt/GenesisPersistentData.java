package miku.bai_ze_li.genesis.api.nbt;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

import java.util.UUID;

public final class GenesisPersistentData {
    private GenesisPersistentData() {
    }

    public static boolean getBoolean(Entity entity, PersistentDataKey key) {
        return entity.getPersistentData().getBoolean(key.value());
    }

    public static void putBoolean(Entity entity, PersistentDataKey key, boolean value) {
        entity.getPersistentData().putBoolean(key.value(), value);
    }

    public static int getInt(Entity entity, PersistentDataKey key, int fallback) {
        CompoundTag tag = entity.getPersistentData();
        return tag.contains(key.value(), CompoundTag.TAG_INT) ? tag.getInt(key.value()) : fallback;
    }

    public static void putInt(Entity entity, PersistentDataKey key, int value) {
        entity.getPersistentData().putInt(key.value(), value);
    }

    public static long getLong(Entity entity, PersistentDataKey key, long fallback) {
        CompoundTag tag = entity.getPersistentData();
        return tag.contains(key.value(), CompoundTag.TAG_LONG) ? tag.getLong(key.value()) : fallback;
    }

    public static void putLong(Entity entity, PersistentDataKey key, long value) {
        entity.getPersistentData().putLong(key.value(), value);
    }

    public static float getFloat(Entity entity, PersistentDataKey key, float fallback) {
        CompoundTag tag = entity.getPersistentData();
        return tag.contains(key.value(), CompoundTag.TAG_FLOAT) ? tag.getFloat(key.value()) : fallback;
    }

    public static void putFloat(Entity entity, PersistentDataKey key, float value) {
        entity.getPersistentData().putFloat(key.value(), value);
    }

    public static UUID getUUID(Entity entity, PersistentDataKey key, UUID fallback) {
        CompoundTag tag = entity.getPersistentData();
        return tag.hasUUID(key.value()) ? tag.getUUID(key.value()) : fallback;
    }

    public static void putUUID(Entity entity, PersistentDataKey key, UUID value) {
        entity.getPersistentData().putUUID(key.value(), value);
    }

    public static void remove(Entity entity, PersistentDataKey key) {
        entity.getPersistentData().remove(key.value());
    }

    public static boolean contains(Entity entity, PersistentDataKey key, int tagType) {
        return entity.getPersistentData().contains(key.value(), tagType);
    }
}
