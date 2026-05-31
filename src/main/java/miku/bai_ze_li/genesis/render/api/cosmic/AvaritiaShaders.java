package miku.bai_ze_li.genesis.render.api.cosmic;

import miku.bai_ze_li.genesis.render.api.cosmic.CCShaderInstance;
import miku.bai_ze_li.genesis.render.api.cosmic.CCUniform;
import miku.bai_ze_li.genesis.GenesisLib;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Objects;

@Mod.EventBusSubscriber(modid = GenesisLib.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class AvaritiaShaders {
    public static class RenderStateShardAccess extends RenderStateShard {
        private static final DepthTestStateShard EQUAL_DEPTH_TEST = RenderStateShard.EQUAL_DEPTH_TEST;
        public static final LightmapStateShard LIGHT_MAP = RenderStateShard.LIGHTMAP;
        public static final DepthTestStateShard NO_DEPTH_TEST = RenderStateShard.NO_DEPTH_TEST;
        private static final TransparencyStateShard TRANSLUCENT_TRANSPARENCY = RenderStateShard.TRANSLUCENT_TRANSPARENCY;
        private static final TextureStateShard BLOCK_SHEET_MIPPED = RenderStateShard.BLOCK_SHEET_MIPPED;
        public static final TextureStateShard COSMIC_TEXTURE_ISOLATED = new RenderStateShard.TextureStateShard(InventoryMenu.BLOCK_ATLAS, false, false) {
            public void setupRenderState() {
                super.setupRenderState();
                RenderSystem.activeTexture(33984);
                Minecraft mc = Minecraft.getInstance();
                TextureAtlas textureAtlas = mc.getModelManager().getAtlas(InventoryMenu.BLOCK_ATLAS);
                RenderSystem.bindTexture(textureAtlas.getId());
            }

            public void clearRenderState() {
                super.clearRenderState();
            }
        };

        private RenderStateShardAccess(String pName, Runnable pSetupState, Runnable pClearState) {
            super(pName, pSetupState, pClearState);
        }
    }

    public static final float[] COSMIC_UVS = new float[40];
    public static boolean inventoryRender = false;
    public static int renderTime;
    public static float tick;
    public static float renderFrame;
    public static CCShaderInstance cosmicShader;
    public static CCUniform useType;
    public static CCUniform cosmicTime;
    public static CCUniform cosmicYaw;
    public static CCUniform cosmicPitch;
    public static CCUniform cosmicExternalScale;
    public static CCUniform cosmicOpacity;
    public static CCUniform cosmicUVs;
    public static CCUniform cosmicColor;
    public static CCUniform cosmicScreenSize;
    public static CCUniform cosmicIs2D;
    public static final RenderType COSMIC_RENDER_TYPE = RenderType.create(
            GenesisLib.MODID + ":cosmic",
            DefaultVertexFormat.BLOCK,
            VertexFormat.Mode.QUADS,
            2097152,
            true,
            false, RenderType.CompositeState.builder()
                    .setShaderState(new RenderStateShard.ShaderStateShard(() -> cosmicShader))
                    .setDepthTestState(RenderStateShardAccess.EQUAL_DEPTH_TEST)
                    .setLightmapState(RenderStateShardAccess.LIGHT_MAP)
                    .setTransparencyState(RenderStateShardAccess.TRANSLUCENT_TRANSPARENCY)
                    .setTextureState(RenderStateShardAccess.COSMIC_TEXTURE_ISOLATED)
                    .createCompositeState(true)
    );
    public static final RenderType COSMIC_HAND_RENDER_TYPE = RenderType.create(
            GenesisLib.MODID + ":cosmic_hand",
            DefaultVertexFormat.BLOCK,
            VertexFormat.Mode.QUADS,
            2097152,
            true,
            false, RenderType.CompositeState.builder()
                    .setShaderState(new RenderStateShard.ShaderStateShard(() -> cosmicShader))
                    .setDepthTestState(RenderStateShardAccess.NO_DEPTH_TEST)
                    .setLightmapState(RenderStateShardAccess.LIGHT_MAP)
                    .setTransparencyState(RenderStateShardAccess.TRANSLUCENT_TRANSPARENCY)
                    .setTextureState(RenderStateShardAccess.COSMIC_TEXTURE_ISOLATED)
                    .createCompositeState(true)
    );
    //public static final RenderType COSMIC_RENDER_TYPE_2 = RenderType.create(EternisStarrySky.MOD_ID + ":cosmic_1", DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 2097152, true, false, RenderType.CompositeState.builder().setShaderState(new RenderStateShard.ShaderStateShard(() -> cosmicShader)).setDepthTestState(RenderStateShardAccess.EQUAL_DEPTH_TEST).setLightmapState(RenderStateShardAccess.LIGHT_MAP).setTransparencyState(RenderStateShardAccess.TRANSLUCENT_TRANSPARENCY).setTextureState(RenderStateShardAccess.BLOCK_SHEET_MIPPED).createCompositeState(true));

    public static void onRegisterShaders(RegisterShadersEvent event) {
        event.registerShader(CCShaderInstance.create(event.getResourceProvider(), ResourceLocation.fromNamespaceAndPath(GenesisLib.MODID, "cosmic"), DefaultVertexFormat.BLOCK), e -> {
            cosmicShader = (CCShaderInstance) e;
            cosmicTime = Objects.requireNonNull(cosmicShader.getUniform("time"));
            cosmicYaw = Objects.requireNonNull(cosmicShader.getUniform("yaw"));
            cosmicPitch = Objects.requireNonNull(cosmicShader.getUniform("pitch"));
            cosmicExternalScale = Objects.requireNonNull(cosmicShader.getUniform("externalScale"));
            cosmicOpacity = Objects.requireNonNull(cosmicShader.getUniform("opacity"));
            cosmicUVs = Objects.requireNonNull(cosmicShader.getUniform("cosmicuvs"));
            useType = Objects.requireNonNull(cosmicShader.getUniform("useCosmicType"));
            cosmicColor = Objects.requireNonNull(cosmicShader.getUniform("cosmicColor0"));
            cosmicScreenSize = Objects.requireNonNull(cosmicShader.getUniform("screenSize"));
            cosmicIs2D = Objects.requireNonNull(cosmicShader.getUniform("is2D"));
            cosmicTime.set((float) renderTime + renderFrame);
            cosmicShader.onApply(() -> cosmicTime.set((float) renderTime + renderFrame));
        });
        /*event.registerShader(CCShaderInstance.create(event.getResourceProvider(), ResourceLocation.fromNamespaceAndPath(EternisStarrySky.MOD_ID, "cosmic_1"), DefaultVertexFormat.BLOCK), e -> {
            cosmicShader = (CCShaderInstance) e;
            cosmicTime = Objects.requireNonNull(cosmicShader.getUniform("time"));
            cosmicYaw = Objects.requireNonNull(cosmicShader.getUniform("yaw"));
            cosmicPitch = Objects.requireNonNull(cosmicShader.getUniform("pitch"));
            cosmicExternalScale = Objects.requireNonNull(cosmicShader.getUniform("externalScale"));
            cosmicOpacity = Objects.requireNonNull(cosmicShader.getUniform("opacity"));
            cosmicUVs = Objects.requireNonNull(cosmicShader.getUniform("cosmicuvs"));
            //useType = Objects.requireNonNull(cosmicShader.getUniform("useType"));
            //currentTime = Objects.requireNonNull(cosmicShader.getUniform("currentTime"));
            cosmicTime.set((float) renderTime + renderFrame);
            cosmicShader.onApply(() -> cosmicTime.set((float) renderTime + renderFrame));
        });*/
    }

    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        if (!Minecraft.getInstance().isPaused() && event.phase == TickEvent.Phase.END) {
            ++renderTime;
            tick += 1F;
            if (tick >= 720.0f) {
                tick = 0.0F;
            }
        }
    }

    @SubscribeEvent
    public static void renderTick(TickEvent.RenderTickEvent event) {
        if (!Minecraft.getInstance().isPaused() && event.phase == TickEvent.Phase.START) {
            renderFrame = event.renderTickTime;
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void drawScreenPre(final ScreenEvent.Render.Pre e) {
        AvaritiaShaders.inventoryRender = true;
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void drawScreenPost(final ScreenEvent.Render.Post e) {
        AvaritiaShaders.inventoryRender = false;
    }
}
