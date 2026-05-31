package miku.bai_ze_li.genesis.render.api.shader;

import miku.bai_ze_li.genesis.render.api.cosmic.AvaritiaShaders;
import miku.bai_ze_li.genesis.GenesisLib;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Objects;

@Mod.EventBusSubscriber(modid = GenesisLib.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class GenesisShaders {
    @Nullable
    public static ShaderInstance haloShader;
    @Nullable
    private static ShaderInstance ribbonShader;
    @Nullable
    private static ShaderInstance colorfulShader;
    @Nullable
    private static ShaderInstance floridShader;
    @Nullable
    private static ShaderInstance rainbowShader;
    @Nullable
    private static ShaderInstance heatWaveShader;
    @Nullable
    private static ShaderInstance trailMaskShader;
    @Nullable
    public static ShaderInstance genesisOutline;
    @Nullable
    public static ShaderInstance genesisBloomBlur;
    @Nullable
    public static ShaderInstance genesisBloom;
    @Nullable
    private static ShaderInstance heatWavePostprocessShader;

    public static ShaderInstance getHaloShader() {
        return Objects.requireNonNull(haloShader, "Halo shader not registered");
    }

    @javax.annotation.Nullable
    public static ShaderInstance getGenesisOutline() {
        return genesisOutline;
    }

    @javax.annotation.Nullable
    public static ShaderInstance getGenesisBloomBlur() {
        return genesisBloomBlur;
    }

    @javax.annotation.Nullable
    public static ShaderInstance getGenesisBloom() {
        return genesisBloom;
    }

    public static ShaderInstance getColorfulShader() {
        return Objects.requireNonNull(colorfulShader, "Colorful shader not registered");
    }

    public static ShaderInstance getFloridShader() {
        return Objects.requireNonNull(floridShader, "Florid shader not registered");
    }
    
    public static ShaderInstance getRainbowShader() {
        return Objects.requireNonNull(rainbowShader, "Rainbow shader not registered");
    }
    
    public static ShaderInstance getHeatWaveShader() {
        return Objects.requireNonNull(heatWaveShader, "HeatWave shader not registered");
    }
    public static ShaderInstance getRibbonShader() {
        return Objects.requireNonNull(ribbonShader, "Ribbon shader not registered");
    }

    public static ShaderInstance getTrailMaskShader() {
        return Objects.requireNonNull(trailMaskShader, "Trail mask shader not registered");
    }

    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent event) throws IOException {
        ResourceProvider resourceProvider = event.getResourceProvider();

        ShaderInstance halo = new ShaderInstance(
                resourceProvider,
                new ResourceLocation(GenesisLib.MODID, "halo"),
                DefaultVertexFormat.POSITION_COLOR_NORMAL
        );
        event.registerShader(halo, shader -> haloShader = shader);

        GenesisShaderInstance colorful = new GenesisShaderInstance(
                resourceProvider,
                new ResourceLocation(GenesisLib.MODID, "colorful_shader").toString(),
                DefaultVertexFormat.POSITION_COLOR_TEX
        );
        event.registerShader(colorful, shaderInstance -> colorfulShader = shaderInstance);


        GenesisShaderInstance florid = new GenesisShaderInstance(
                resourceProvider,
                new ResourceLocation(GenesisLib.MODID, "florid_shader").toString(),
                DefaultVertexFormat.POSITION_TEX
        );
        event.registerShader(florid, shaderInstance -> floridShader = shaderInstance);

        GenesisShaderInstance rainbow = new GenesisShaderInstance(
                resourceProvider,
                new ResourceLocation(GenesisLib.MODID, "rainbow_shader").toString(),
                DefaultVertexFormat.POSITION_TEX
        );
        event.registerShader(rainbow, shaderInstance -> rainbowShader = shaderInstance);

        GenesisShaderInstance heat_wave = new GenesisShaderInstance(
                resourceProvider,
                new ResourceLocation(GenesisLib.MODID, "heat_wave").toString(),
                DefaultVertexFormat.POSITION_TEX
        );
        event.registerShader(heat_wave, shaderInstance -> heatWaveShader = shaderInstance);

        GenesisShaderInstance ribbon = new GenesisShaderInstance(
                resourceProvider,
                new ResourceLocation(GenesisLib.MODID, "ribbon").toString(),
                DefaultVertexFormat.POSITION_COLOR_TEX
        );
        event.registerShader(ribbon, shaderInstance -> ribbonShader = shaderInstance);

        GenesisShaderInstance trailMask = new GenesisShaderInstance(
                resourceProvider,
                new ResourceLocation(GenesisLib.MODID, "trail_mask").toString(),
                DefaultVertexFormat.NEW_ENTITY
        );
        event.registerShader(trailMask, shaderInstance -> trailMaskShader = shaderInstance);

        ShaderInstance outline = new ShaderInstance(
                event.getResourceProvider(),
                new ResourceLocation(GenesisLib.MODID, "held_item_outline"),
                DefaultVertexFormat.POSITION_TEX
        );
        event.registerShader(outline, s -> genesisOutline = s);

        ShaderInstance bloomBlur = new ShaderInstance(
                event.getResourceProvider(),
                new ResourceLocation(GenesisLib.MODID, "held_item_bloom_blur"),
                DefaultVertexFormat.POSITION_TEX
        );
        event.registerShader(bloomBlur, s -> genesisBloomBlur = s);

        ShaderInstance bloomComposite = new ShaderInstance(
                event.getResourceProvider(),
                new ResourceLocation(GenesisLib.MODID, "held_item_bloom"),
                DefaultVertexFormat.POSITION_TEX
        );
        event.registerShader(bloomComposite, s -> genesisBloom = s);
    }

    public static Minecraft getMinecraft() {
        return Minecraft.getInstance();
    }

    public static boolean setTime(ShaderInstance shader) {
        shader.safeGetUniform("time").set((float) AvaritiaShaders.renderTime);
        return true;
    }

    public static boolean setTime(ShaderInstance shader, float pk) {
        shader.safeGetUniform("time").set(pk);
        return true;
    }

    public static boolean setScreenSize(ShaderInstance shader) {
        shader.safeGetUniform("screenSize").set((float) getMinecraft().getWindow().getWidth(), (float) getMinecraft().getWindow().getHeight());
        return true;
    }
}
