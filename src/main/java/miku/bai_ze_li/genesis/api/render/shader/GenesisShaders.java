package miku.bai_ze_li.genesis.api.render.shader;

import miku.bai_ze_li.genesis.api.render.cosmic.AvaritiaShaders;
import miku.bai_ze_li.genesis.GenesisLib;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
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
    private static ShaderInstance guiBackgroundBlur;
    @Nullable
    private static ShaderInstance heatWavePostprocessShader;
    @Nullable
    private static ShaderInstance waterRefractionShader;

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

    @javax.annotation.Nullable
    public static ShaderInstance getGuiBackgroundBlur() {
        return guiBackgroundBlur;
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

    @Nullable
    public static ShaderInstance getWaterRefractionShader() {
        return waterRefractionShader;
    }

    public static ShaderInstance getWaterRefractionShaderOrFallback() {
        return waterRefractionShader != null ? waterRefractionShader : GameRenderer.getRendertypeTranslucentShader();
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

        ShaderInstance tooltipBackgroundBlur = new ShaderInstance(
                event.getResourceProvider(),
                new ResourceLocation(GenesisLib.MODID, "gui_background_blur"),
                DefaultVertexFormat.POSITION_TEX
        );
        event.registerShader(tooltipBackgroundBlur, s -> guiBackgroundBlur = s);

        ShaderInstance waterRefraction = new ShaderInstance(
                event.getResourceProvider(),
                new ResourceLocation(GenesisLib.MODID, "source_water_refraction"),
                DefaultVertexFormat.BLOCK
        );
        event.registerShader(waterRefraction, s -> waterRefractionShader = s);
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

    public static void setSlashColors(ShaderInstance shader, float glowR, float glowG, float glowB,
                                      float coreR, float coreG, float coreB) {
        shader.safeGetUniform("slashColor").set(glowR, glowG, glowB, 1.0F);
        shader.safeGetUniform("slashCoreColor").set(coreR, coreG, coreB, 1.0F);
    }

    public static boolean setScreenSize(ShaderInstance shader) {
        shader.safeGetUniform("screenSize").set((float) getMinecraft().getWindow().getWidth(), (float) getMinecraft().getWindow().getHeight());
        return true;
    }
}
