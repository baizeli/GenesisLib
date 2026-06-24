package miku.bai_ze_li.genesis.api.render.tooltip.background;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import miku.bai_ze_li.genesis.GenesisLib;
import miku.bai_ze_li.genesis.api.render.shader.GenesisShaders;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import org.joml.Vector2ic;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

@Mod.EventBusSubscriber(modid = GenesisLib.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class GenesisTooltipBackgroundRenderer {
    private static final int TOOLTIP_Z = 399;
    private static TextureTarget sourceBuffer;
    private static TextureTarget blurBufferA;
    private static TextureTarget blurBufferB;

    private GenesisTooltipBackgroundRenderer() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRenderTooltipPre(RenderTooltipEvent.Pre event) {
        GenesisTooltipBackgroundStyle style = GenesisTooltipBackgroundRegistry.resolve(event.getItemStack());
        if (style == null) {
            return;
        }

        TooltipArea area = calculateArea(event, style);
        GuiGraphics graphics = event.getGraphics();
        graphics.flush();

        boolean renderedBlur = renderBlurredBackground(area, style);
        int fillColor = renderedBlur ? style.overlayColor() : style.fallbackColor();
        drawTintAndBorder(graphics, area, style, fillColor);
        graphics.flush();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRenderTooltipColor(RenderTooltipEvent.Color event) {
        if (GenesisTooltipBackgroundRegistry.resolve(event.getItemStack()) == null) {
            return;
        }

        event.setBackgroundStart(0x00000000);
        event.setBackgroundEnd(0x00000000);
        event.setBorderStart(0x00000000);
        event.setBorderEnd(0x00000000);
    }

    private static TooltipArea calculateArea(RenderTooltipEvent.Pre event, GenesisTooltipBackgroundStyle style) {
        Font font = event.getFont();
        int tooltipWidth = 0;
        int tooltipHeight = event.getComponents().size() == 1 ? -2 : 0;
        for (ClientTooltipComponent component : event.getComponents()) {
            tooltipWidth = Math.max(tooltipWidth, component.getWidth(font));
            tooltipHeight += component.getHeight();
        }

        Vector2ic position = event.getTooltipPositioner().positionTooltip(
                event.getScreenWidth(),
                event.getScreenHeight(),
                event.getX(),
                event.getY(),
                tooltipWidth,
                tooltipHeight
        );

        int padding = style.padding();
        return new TooltipArea(
                position.x() - padding,
                position.y() - padding,
                tooltipWidth + padding * 2,
                tooltipHeight + padding * 2
        );
    }

    private static boolean renderBlurredBackground(TooltipArea area, GenesisTooltipBackgroundStyle style) {
        ShaderInstance blurShader = GenesisShaders.getGuiBackgroundBlur();
        if (blurShader == null) {
            return false;
        }

        Minecraft minecraft = Minecraft.getInstance();
        RenderTarget main = minecraft.getMainRenderTarget();
        int blurWidth = Math.max(16, main.width / style.downscale());
        int blurHeight = Math.max(16, main.height / style.downscale());

        sourceBuffer = syncBuffer(sourceBuffer, blurWidth, blurHeight);
        blurBufferA = syncBuffer(blurBufferA, blurWidth, blurHeight);
        blurBufferB = syncBuffer(blurBufferB, blurWidth, blurHeight);

        try {
            copyMainColor(main, sourceBuffer);
            int blurredTexture = runBlur(blurShader, sourceBuffer.getColorTextureId(), blurWidth, blurHeight, style);
            main.bindWrite(true);
            drawBlurRegion(blurredTexture, area);
            return true;
        } catch (Throwable ignored) {
            main.bindWrite(false);
            restoreCommonState();
            return false;
        }
    }

    private static void copyMainColor(RenderTarget main, TextureTarget target) {
        int previousRead = GL30.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
        int previousDraw = GL30.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);
        try {
            GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, main.frameBufferId);
            GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, target.frameBufferId);
            GL30.glBlitFramebuffer(
                    0, 0, main.width, main.height,
                    0, 0, target.width, target.height,
                    GL11.GL_COLOR_BUFFER_BIT,
                    GL11.GL_LINEAR
            );
        } finally {
            GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, previousRead);
            GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, previousDraw);
        }
    }

    private static int runBlur(ShaderInstance shader, int sourceTexture, int width, int height,
                               GenesisTooltipBackgroundStyle style) {
        int currentTexture = sourceTexture;
        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        if (shader.getUniform("ScreenSize") != null) {
            shader.getUniform("ScreenSize").set((float) width, (float) height);
        }

        float radius = Math.max(1.0F, style.blurRadius() / style.blurPasses());
        for (int i = 0; i < style.blurPasses(); i++) {
            if (shader.getUniform("BlurRadius") != null) {
                shader.getUniform("BlurRadius").set(radius);
            }

            blurBufferA.bindWrite(true);
            RenderSystem.setShader(() -> shader);
            shader.setSampler("DiffuseSampler", currentTexture);
            if (shader.getUniform("BlurDirection") != null) {
                shader.getUniform("BlurDirection").set(1.0F, 0.0F);
            }
            drawFullscreenQuad();

            blurBufferB.bindWrite(true);
            RenderSystem.setShader(() -> shader);
            shader.setSampler("DiffuseSampler", blurBufferA.getColorTextureId());
            if (shader.getUniform("BlurDirection") != null) {
                shader.getUniform("BlurDirection").set(0.0F, 1.0F);
            }
            drawFullscreenQuad();
            currentTexture = blurBufferB.getColorTextureId();
        }

        return currentTexture;
    }

    private static void drawBlurRegion(int texture, TooltipArea area) {
        Minecraft minecraft = Minecraft.getInstance();
        int guiWidth = Math.max(1, minecraft.getWindow().getGuiScaledWidth());
        int guiHeight = Math.max(1, minecraft.getWindow().getGuiScaledHeight());

        float x0 = area.x() / (float) guiWidth * 2.0F - 1.0F;
        float x1 = area.right() / (float) guiWidth * 2.0F - 1.0F;
        float y0 = 1.0F - area.y() / (float) guiHeight * 2.0F;
        float y1 = 1.0F - area.bottom() / (float) guiHeight * 2.0F;

        float u0 = area.x() / (float) guiWidth;
        float u1 = area.right() / (float) guiWidth;
        float v0 = 1.0F - area.bottom() / (float) guiHeight;
        float v1 = 1.0F - area.y() / (float) guiHeight;

        RenderSystem.backupProjectionMatrix();
        Matrix4f identity = new Matrix4f().identity();
        RenderSystem.setProjectionMatrix(identity, com.mojang.blaze3d.vertex.VertexSorting.ORTHOGRAPHIC_Z);
        com.mojang.blaze3d.vertex.PoseStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushPose();
        modelViewStack.setIdentity();
        RenderSystem.applyModelViewMatrix();

        try {
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShader(net.minecraft.client.renderer.GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, texture);

            BufferBuilder builder = Tesselator.getInstance().getBuilder();
            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            builder.vertex(x0, y1, 0.0F).uv(u0, v0).endVertex();
            builder.vertex(x1, y1, 0.0F).uv(u1, v0).endVertex();
            builder.vertex(x1, y0, 0.0F).uv(u1, v1).endVertex();
            builder.vertex(x0, y0, 0.0F).uv(u0, v1).endVertex();
            BufferUploader.drawWithShader(builder.end());
        } finally {
            modelViewStack.popPose();
            RenderSystem.restoreProjectionMatrix();
            RenderSystem.applyModelViewMatrix();
            restoreCommonState();
        }
    }

    private static void drawTintAndBorder(GuiGraphics graphics, TooltipArea area,
                                          GenesisTooltipBackgroundStyle style, int fillColor) {
        graphics.fill(area.x(), area.y(), area.right(), area.bottom(), TOOLTIP_Z, fillColor);

        int start = style.borderStartColor();
        int end = style.borderEndColor();
        drawGradientHorizontal(graphics, area.x(), area.right(), area.y(), start, end);
        drawGradientHorizontal(graphics, area.x(), area.right(), area.bottom() - 1, multiplyAlpha(end, 0.72F), multiplyAlpha(start, 0.72F));
        drawGradientVertical(graphics, area.x(), area.y(), area.bottom(), start, end);
        drawGradientVertical(graphics, area.right() - 1, area.y(), area.bottom(), end, start);

        if (area.width() > 3 && area.height() > 3) {
            int softStart = multiplyAlpha(start, 0.36F);
            int softEnd = multiplyAlpha(end, 0.36F);
            drawGradientHorizontal(graphics, area.x() + 1, area.right() - 1, area.y() + 1, softStart, softEnd);
            drawGradientHorizontal(graphics, area.x() + 1, area.right() - 1, area.bottom() - 2, softEnd, softStart);
            drawGradientVertical(graphics, area.x() + 1, area.y() + 1, area.bottom() - 1, softStart, softEnd);
            drawGradientVertical(graphics, area.right() - 2, area.y() + 1, area.bottom() - 1, softEnd, softStart);
        }
    }

    private static void drawGradientHorizontal(GuiGraphics graphics, int x0, int x1, int y, int startColor, int endColor) {
        int width = x1 - x0;
        if (width <= 0) {
            return;
        }

        if (width == 1) {
            graphics.fill(x0, y, x1, y + 1, TOOLTIP_Z, startColor);
            return;
        }

        for (int x = x0; x < x1; x++) {
            float progress = (x - x0) / (float) (width - 1);
            graphics.fill(x, y, x + 1, y + 1, TOOLTIP_Z, interpolateColor(startColor, endColor, progress));
        }
    }

    private static void drawGradientVertical(GuiGraphics graphics, int x, int y0, int y1, int startColor, int endColor) {
        int height = y1 - y0;
        if (height <= 0) {
            return;
        }

        if (height == 1) {
            graphics.fill(x, y0, x + 1, y1, TOOLTIP_Z, startColor);
            return;
        }

        for (int y = y0; y < y1; y++) {
            float progress = (y - y0) / (float) (height - 1);
            graphics.fill(x, y, x + 1, y + 1, TOOLTIP_Z, interpolateColor(startColor, endColor, progress));
        }
    }

    private static int interpolateColor(int startColor, int endColor, float progress) {
        int startA = startColor >>> 24 & 255;
        int startR = startColor >>> 16 & 255;
        int startG = startColor >>> 8 & 255;
        int startB = startColor & 255;
        int endA = endColor >>> 24 & 255;
        int endR = endColor >>> 16 & 255;
        int endG = endColor >>> 8 & 255;
        int endB = endColor & 255;
        int alpha = interpolate(startA, endA, progress);
        int red = interpolate(startR, endR, progress);
        int green = interpolate(startG, endG, progress);
        int blue = interpolate(startB, endB, progress);
        return alpha << 24 | red << 16 | green << 8 | blue;
    }

    private static int interpolate(int start, int end, float progress) {
        return Math.round(start + (end - start) * progress);
    }

    private static int multiplyAlpha(int color, float alphaMultiplier) {
        int alpha = Math.round((color >>> 24 & 255) * alphaMultiplier);
        return alpha << 24 | color & 0x00FFFFFF;
    }

    private static void drawFullscreenQuad() {
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        builder.vertex(-1.0D, -1.0D, 0.0D).uv(0.0F, 0.0F).endVertex();
        builder.vertex(1.0D, -1.0D, 0.0D).uv(1.0F, 0.0F).endVertex();
        builder.vertex(1.0D, 1.0D, 0.0D).uv(1.0F, 1.0F).endVertex();
        builder.vertex(-1.0D, 1.0D, 0.0D).uv(0.0F, 1.0F).endVertex();
        BufferUploader.drawWithShader(builder.end());
    }

    private static TextureTarget syncBuffer(TextureTarget buffer, int width, int height) {
        if (buffer == null) {
            TextureTarget created = new TextureTarget(width, height, false, Minecraft.ON_OSX);
            created.setFilterMode(GL11.GL_LINEAR);
            return created;
        }
        if (buffer.width != width || buffer.height != height) {
            buffer.resize(width, height, Minecraft.ON_OSX);
            buffer.setFilterMode(GL11.GL_LINEAR);
        }
        return buffer;
    }

    private static void restoreCommonState() {
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.activeTexture(org.lwjgl.opengl.GL13.GL_TEXTURE0);
        RenderSystem.bindTexture(0);
    }

    private record TooltipArea(int x, int y, int width, int height) {
        private int right() {
            return x + width;
        }

        private int bottom() {
            return y + height;
        }
    }
}
