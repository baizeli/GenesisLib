package miku.bai_ze_li.genesis.api.render.shader;

import miku.bai_ze_li.genesis.api.render.cosmic.AvaritiaShaders;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GenesisRenderType extends RenderType {
    public GenesisRenderType(String p_173178_, VertexFormat p_173179_, VertexFormat.Mode p_173180_, int p_173181_, boolean p_173182_, boolean p_173183_, Runnable p_173184_, Runnable p_173185_) {super(p_173178_, p_173179_, p_173180_, p_173181_, p_173182_, p_173183_, p_173184_, p_173185_);}
    private static final Map<ResourceLocation, RenderType> DELAYED_TRAILS = new ConcurrentHashMap<>();

    public static RenderType delayedTrail(ResourceLocation texture) {
        return DELAYED_TRAILS.computeIfAbsent(texture, location -> RenderType.create(
                "genesis_delayed_trail",
                DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.QUADS,
                1024,
                true,
                true,
                RenderType.CompositeState.builder()
                        .setShaderState(new ShaderStateShard(GenesisShaders::getTrailMaskShader))
                        .setTextureState(new TextureStateShard(location, false, false))
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setCullState(NO_CULL)
                        .setLightmapState(LIGHTMAP)
                        .setOverlayState(OVERLAY)
                        .setDepthTestState(LEQUAL_DEPTH_TEST)
                        .setWriteMaskState(COLOR_WRITE)
                        .createCompositeState(false)
        ));
    }

    public static final RenderType cosmic_world = RenderType.create(
            "cosmic_world",
            DefaultVertexFormat.POSITION_COLOR_NORMAL,
            VertexFormat.Mode.TRIANGLES,
            2097149,
            true,
            false,
            RenderType.CompositeState.builder()
                    .setShaderState(new ShaderStateShard(() -> AvaritiaShaders.cosmicShader))
                    .setTextureState(new TextureStateShard(InventoryMenu.BLOCK_ATLAS, false, false))
                    .setDepthTestState(LEQUAL_DEPTH_TEST)
                    .setLightmapState(AvaritiaShaders.RenderStateShardAccess.LIGHT_MAP)
                    .setTransparencyState(NO_TRANSPARENCY)
                    .setCullState(NO_CULL)
                    .setWriteMaskState(COLOR_DEPTH_WRITE)
                    .createCompositeState(true)
    );

    public static final RenderType halo = RenderType.create(
            "halo",
            DefaultVertexFormat.POSITION_COLOR_NORMAL,
            VertexFormat.Mode.TRIANGLES,
            2097149,
            true,
            false,
            RenderType.CompositeState.builder()
                    .setShaderState(new ShaderStateShard(GenesisShaders::getHaloShader))
                    .setTextureState(new TextureStateShard(InventoryMenu.BLOCK_ATLAS, false, false))
                    .setDepthTestState(LEQUAL_DEPTH_TEST)
                    .setLightmapState(AvaritiaShaders.RenderStateShardAccess.LIGHT_MAP)
                    .setTransparencyState(NO_TRANSPARENCY)
                    .setCullState(NO_CULL)
                    .setWriteMaskState(COLOR_DEPTH_WRITE)
                    .createCompositeState(true)
    );

    public static final RenderType ribbon = RenderType.create(
            "ribbon",
            DefaultVertexFormat.POSITION_COLOR_TEX,
            VertexFormat.Mode.TRIANGLES,
            256,
            true,
            true,
            RenderType.CompositeState.builder()
                    .setShaderState(new ShaderStateShard(GenesisShaders::getRibbonShader))
                    .setTextureState(NO_TEXTURE)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setCullState(NO_CULL)
                    .setLightmapState(NO_LIGHTMAP)
                    .setDepthTestState(LEQUAL_DEPTH_TEST)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(true)
    );
}
