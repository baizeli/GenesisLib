package miku.bai_ze_li.genesis.api.render.effect;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SlashEffectManager {

    private static final int MAX_SLASH_EFFECTS = 256;
    private static final List<BaiZeLiSlashEffect> EFFECTS = new ArrayList<>();

    public static void add(BaiZeLiSlashEffect effect) {
        while (EFFECTS.size() >= MAX_SLASH_EFFECTS) {
            EFFECTS.remove(0);
        }
        EFFECTS.add(effect);
    }

    public static void tick() {
        Iterator<BaiZeLiSlashEffect> it = EFFECTS.iterator();
        while (it.hasNext()) {
            BaiZeLiSlashEffect e = it.next();
            e.tick();
            if (e.isFinished()) {
                it.remove();
            }
        }
    }

    public static void render(PoseStack poseStack, MultiBufferSource buffer, float partialTick) {
        for (BaiZeLiSlashEffect e : EFFECTS) {
            e.render(poseStack, buffer, partialTick);
        }
    }

    public static void renderDeferred(PoseStack poseStack, MultiBufferSource buffer, float partialTick) {
        render(poseStack, buffer, partialTick);
    }

    public static void clear() {
        EFFECTS.clear();
    }
}
