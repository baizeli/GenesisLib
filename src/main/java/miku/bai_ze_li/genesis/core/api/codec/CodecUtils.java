package miku.bai_ze_li.genesis.core.api.codec;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 鎻愪緵鍙噸鐢ㄧ殑Codec鍒涘缓宸ュ叿鏂规硶
 */
public class CodecUtils {

    /**
     * 鍒涘缓EntityType鍒癐nteger鐨勬槧灏凜odec
     * 
     * @return Codec<Map<EntityType<?>, Integer>>
     */
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
                // Map -> List<Pair>
                map -> {
                    List<Pair<EntityType<?>, Integer>> list = new ArrayList<>();
                    for (var entry : map.entrySet()) {
                        list.add(Pair.of(entry.getKey(), entry.getValue()));
                    }
                    return list;
                });
    }

    /**
     * 鍒涘缓EntityType鍒颁换鎰忕被鍨嬬殑鏄犲皠Codec
     * 
     * @param valueCodec 鍊肩殑缂栬В鐮佸櫒
     * @param <V> 鍊肩殑绫诲瀷
     * @return Codec<Map<EntityType<?>, V>>
     */
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

    /**
     * 鍒涘缓閫氱敤鐨凪apCodec
     * 
     * @param keyCodec 閿殑缂栬В鐮佸櫒
     * @param valueCodec 鍊肩殑缂栬В鐮佸櫒
     * @param <K> 閿殑绫诲瀷
     * @param <V> 鍊肩殑绫诲瀷
     * @return Codec<Map<K, V>>
     */
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

    /**
     * 鍒涘缓EntityType鍒癈ompoundTag鍒楄〃鐨勬槧灏凜odec
     * 
     * @return Codec<Map<EntityType<?>, List<CompoundTag>>>
     */
    public static Codec<Map<EntityType<?>, List<CompoundTag>>> createEntityTypeToListCodec() {
        return createEntityTypeMapCodec(CompoundTag.CODEC.listOf());
    }
}
