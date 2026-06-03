package miku.bai_ze_li.genesis.api.render.shader;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.io.IOException;

@OnlyIn(Dist.CLIENT)
public class GenesisShaderInstance extends ShaderInstance {
    public final @Nullable Uniform colorUniform = this.getUniform("color");
    public final @Nullable Uniform timeUniform = this.getUniform("iTime");
    public final @Nullable Uniform intensityUniform = this.getUniform("intensity");

    public GenesisShaderInstance(ResourceProvider pResourceProvider, String pName, VertexFormat pVertexFormat) throws IOException {
        super(pResourceProvider, pName, pVertexFormat);
    }

    public void setColor(float red, float green, float blue, float alpha) {
        if (this.colorUniform != null) {
            this.colorUniform.set(red, green, blue, alpha);
        }
    }

    public void setTime(float time) {
        if (this.timeUniform != null) {
            this.timeUniform.set(time);
        }
    }

    public void setIntensity(float intensity) {
        if (this.intensityUniform != null) {
            this.intensityUniform.set(intensity);
        }
    }

    public void setUniform(String uniformName, float width, float height) {
        Uniform uniform = this.getUniform(uniformName);
        if (uniform != null) {
            uniform.set(width, height);
        }
    }
    public void setUniform(String uniformName, float value) {
        Uniform uniform = this.getUniform(uniformName);
        if (uniform != null) {
            uniform.set(value);
        }
    }
}