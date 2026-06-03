package miku.bai_ze_li.genesis.api.codec;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CodecUtils {

    public static Codec<Map<EntityType<?>, Integer>> createEntityTypeCountCodec() {
        return Codec.list(
                Codec.pair(
                        BuiltInRegistries.ENTITY_TYPE.byNameCodec(),
                        Codec.INT
                )
        ).xmap(
                // List<Pair<EntityType, Integer>> -> Map
                list -> {
                    Map<EntityType<?>, Integer> map = new HashMap<>();
                    for (var pair : list) {
                        map.merge(pair.getFirst(), pair.getSecond(), Integer::sum);
                    }
                    return map;
                },
                map -> {
                    List<Pair<EntityType<?>, Integer>> list = new ArrayList<>();
                    for (var entry : map.entrySet()) {
                        list.add(Pair.of(entry.getKey(), entry.getValue()));
                    }
                    return list;
                });
    }

    public static <V> Codec<Map<EntityType<?>, V>> createEntityTypeMapCodec(Codec<V> valueCodec) {
        return Codec.list(
                Codec.pair(
                        BuiltInRegistries.ENTITY_TYPE.byNameCodec(),
                        valueCodec
                )
        ).xmap(
                // List<Pair<EntityType, V>> -> Map
                list -> {
                    Map<EntityType<?>, V> map = new HashMap<>();
                    for (var pair : list) {
                        map.put(pair.getFirst(), pair.getSecond());
                    }
                    return map;
                },
                // Map -> List<Pair>
                map -> {
                    List<Pair<EntityType<?>, V>> list = new ArrayList<>();
                    for (var entry : map.entrySet()) {
                        list.add(Pair.of(entry.getKey(), entry.getValue()));
                    }
                    return list;
                });
    }
    public static <K, V> Codec<Map<K, V>> createMapCodec(Codec<K> keyCodec, Codec<V> valueCodec) {
        return Codec.list(
                Codec.pair(keyCodec, valueCodec)
        ).xmap(
                // List<Pair<K, V>> -> Map
                list -> {
                    Map<K, V> map = new HashMap<>();
                    for (var pair : list) {
                        map.put(pair.getFirst(), pair.getSecond());
                    }
                    return map;
                },
                // Map -> List<Pair>
                map -> {
                    List<Pair<K, V>> list = new ArrayList<>();
                    for (var entry : map.entrySet()) {
                        list.add(Pair.of(entry.getKey(), entry.getValue()));
                    }
                    return list;
                });
    }

    public static Codec<Map<EntityType<?>, List<CompoundTag>>> createEntityTypeToListCodec() {
        return createEntityTypeMapCodec(CompoundTag.CODEC.listOf());
    }
}