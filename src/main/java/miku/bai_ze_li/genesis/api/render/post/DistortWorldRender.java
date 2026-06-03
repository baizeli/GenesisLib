package miku.bai_ze_li.genesis.api.render.post;

import miku.bai_ze_li.genesis.GenesisLib;
import miku.bai_ze_li.genesis.api.render.particle.CrescentBladeParticle;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class DistortWorldRender {
    public static final ResourceLocation DISTORT =
            new ResourceLocation(GenesisLib.MODID, "shaders/post/distort.json");
    public static final ResourceLocation VECTOR_DISTORT =
            new ResourceLocation(GenesisLib.MODID, "shaders/post/vector_distort.json");
    public static final ResourceLocation SPHERE_CHAIN =
            new ResourceLocation(GenesisLib.MODID, "shaders/post/sphere_chain.json");
    public static PostChain distortChain;
    public static PostChain vectorDistort;
    public static PostChain sphereChain;
    private static int width;
    private static int height;
    private static int distortWidth;
    private static int distortHeight;
    private static int vectorWidth;
    private static int vectorHeight;

    public static void processMyPostChain(float partialTicks) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.cameraEntity == null) return;

        if (vectorDistort != null) {
            vectorDistort.process(partialTicks);
        }

        if (sphereChain != null) {
            RenderTarget swapTarget = sphereChain.getTempTarget("swap");
            if (swapTarget != null) {
                swapTarget.copyDepthFrom(mc.getMainRenderTarget());
            }

            int _width = mc.getWindow().getWidth(), _height = mc.getWindow().getHeight();
            if (_width != width || _height != height) {
                width = _width;
                height = _height;
                sphereChain.resize(_width, _height);
            }

            Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();
            Quaternionf cameraRotation = mc.gameRenderer.getMainCamera().rotation();
            Matrix4f viewToWorldRotMat = new Matrix4f().rotation(cameraRotation);

            List<PostPass> passes = getPasses(sphereChain);
            for (PostPass pass : passes) {
                Uniform cameraPosUniform = pass.getEffect().getUniform("CameraPos");
                if (cameraPosUniform != null) {
                    cameraPosUniform.set((float) cameraPos.x, (float) cameraPos.y, (float) cameraPos.z);
                }
                Uniform projMatUniform = pass.getEffect().getUniform("ProjMat");
                if (projMatUniform != null) {
                    projMatUniform.set(RenderSystem.getProjectionMatrix());
                }
                Uniform rotMatUniform = pass.getEffect().getUniform("IViewRotMat");
                if (rotMatUniform != null) {
                    rotMatUniform.set(viewToWorldRotMat);
                }
            }

//            sphereChain.process(partialTicks);
        }
    }
    public static void initChain(Minecraft mc) {
        if (distortChain != null) distortChain.close();
        try {
            distortChain = new PostChain(mc.getTextureManager(), mc.getResourceManager(), mc.getMainRenderTarget(), DISTORT);
            distortChain.resize(mc.getWindow().getWidth(), mc.getWindow().getHeight());
            distortWidth = mc.getWindow().getWidth();
            distortHeight = mc.getWindow().getHeight();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (vectorDistort != null) vectorDistort.close();
        try {
            vectorDistort = new PostChain(mc.getTextureManager(), mc.getResourceManager(), mc.getMainRenderTarget(), VECTOR_DISTORT);
            vectorDistort.resize(mc.getWindow().getWidth(), mc.getWindow().getHeight());
            vectorWidth = mc.getWindow().getWidth();
            vectorHeight = mc.getWindow().getHeight();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (sphereChain != null) sphereChain.close();
        try {
            sphereChain = new PostChain(mc.getTextureManager(), mc.getResourceManager(), mc.getMainRenderTarget(), SPHERE_CHAIN);
            sphereChain.resize(mc.getWindow().getWidth(), mc.getWindow().getHeight());
            width = mc.getWindow().getWidth();
            height = mc.getWindow().getHeight();
        } catch (Exception e) {
            e.printStackTrace();
        }



    }
    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;
//        handleDistortRT(event, mc);
        handleVectorDistortRT(event, mc);
    }

    private static void handleDistortRT(RenderLevelStageEvent event, Minecraft mc) {
        PostChain chain = distortChain;
        if (chain == null) return;
        int currentWidth = mc.getWindow().getWidth();
        int currentHeight = mc.getWindow().getHeight();
        if (currentWidth != distortWidth || currentHeight != distortHeight) {
            distortWidth = currentWidth;
            distortHeight = currentHeight;
            distortChain.resize(currentWidth, currentHeight);
        }
        var rt = chain.getTempTarget("genesis_api_distort");
        if (rt == null) return;
        rt.copyDepthFrom(mc.getMainRenderTarget());
        rt.bindWrite(true);
        RenderSystem.clearColor(0, 0, 0, 0);
        RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT, false);

        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(GL11.GL_LEQUAL);

        // 渲染各种几何体
//        renderGeometries(event, mc);

        renderParticlesInVectorBuffer(event.getPoseStack(), mc);

        //  切回主屏幕
        mc.getMainRenderTarget().bindWrite(true);

        //测试rt
//         rt.blitToScreen(
//                mc.getWindow().getWidth(),
//                mc.getWindow().getHeight(),
//                false
//        );
    }

    private static void handleVectorDistortRT(RenderLevelStageEvent event, Minecraft mc) {
        PostChain chain = vectorDistort;
        if (chain == null) return;
        int currentWidth = mc.getWindow().getWidth();
        int currentHeight = mc.getWindow().getHeight();
        if (currentWidth != vectorWidth || currentHeight != vectorHeight) {
            vectorWidth = currentWidth;
            vectorHeight = currentHeight;
            vectorDistort.resize(currentWidth, currentHeight);
        }
        var rt = chain.getTempTarget("genesis_api_vector_buffer");
        if (rt == null) return;

        rt.copyDepthFrom(mc.getMainRenderTarget());

        rt.bindWrite(true);

        RenderSystem.clearColor(0, 0, 0, 0);
        RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT, false);

        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(GL11.GL_LEQUAL);

        // 渲染各种几何体
//        renderGeometries(event, mc);

        renderParticlesInVectorBuffer(event.getPoseStack(), mc);

        //  切回主屏幕
        mc.getMainRenderTarget().bindWrite(true);

        //测试rt
//         rt.blitToScreen(
//                mc.getWindow().getWidth(),
//                mc.getWindow().getHeight(),
//                false
//        );
    }

    private static void renderGeometries(RenderLevelStageEvent event, Minecraft mc) {
        var poseStack = event.getPoseStack();
        poseStack.pushPose();

        Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();
        Vec3 pos = mc.player.position().add(mc.player.getLookAngle().scale(5.0));

        poseStack.translate(pos.x - cam.x, pos.y - cam.y, pos.z - cam.z);
        poseStack.mulPose(mc.gameRenderer.getMainCamera().rotation());

        renderQuad(poseStack, mc);

        poseStack.popPose();
    }

    private static void renderQuad(PoseStack poseStack, Minecraft mc) {
        Matrix4f mat = poseStack.last().pose();

        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        BufferBuilder buf = Tesselator.getInstance().getBuilder();
        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        float s = 5f;
        buf.vertex(mat, -s, -s, 0).color(255, 255, 255, 255).endVertex();
        buf.vertex(mat, s, -s, 0).color(255, 255, 255, 255).endVertex();
        buf.vertex(mat, s, s, 0).color(255, 255, 255, 255).endVertex();
        buf.vertex(mat, -s, s, 0).color(255, 255, 255, 255).endVertex();

        BufferUploader.drawWithShader(buf.end());
    }

    private static final ResourceLocation VECTOR_DISTORT_TEX =
            ResourceLocation.fromNamespaceAndPath(GenesisLib.MODID, "textures/misc/vector_distort.png");


    private static void renderParticlesInVectorBuffer(PoseStack poseStack, Minecraft mc) {
        Camera camera = mc.gameRenderer.getMainCamera();
        Vec3 camPos = camera.getPosition();

        PoseStack renderStack = new PoseStack();
        renderStack.mulPose(Axis.XP.rotationDegrees(camera.getXRot()));
        renderStack.mulPose(Axis.YP.rotationDegrees(camera.getYRot() + 180.0F));

        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.disableCull();
        RenderSystem.setShaderTexture(0, VECTOR_DISTORT_TEX);

        BufferBuilder buf = Tesselator.getInstance().getBuilder();
        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

        for (CrescentBladeParticle cbp : CrescentBladeParticle.activeParticles()) {
                float partialTicks = mc.getFrameTime();

                double x = cbp.renderX(partialTicks);
                double y = cbp.renderY(partialTicks);
                double z = cbp.renderZ(partialTicks);

                float relX = (float) (x - camPos.x);
                float relY = (float) (y - camPos.y);
                float relZ = (float) (z - camPos.z);

                renderStack.pushPose();

                renderStack.translate(relX, relY, relZ);
                Vec3 velocity = cbp.velocity();
                double dx = velocity.x;
                double dy = velocity.y;
                double dz = velocity.z;
                double speedSq = dx * dx + dy * dy + dz * dz;

                Vector3f forward = new Vector3f();

                if (speedSq < 1.0E-6D) {
                    Vec3 lastVelocity = cbp.getLastVelocity();
                    if (lastVelocity.lengthSqr() > 1.0E-6D) {
                        forward.set((float) lastVelocity.x, (float) lastVelocity.y, (float) lastVelocity.z).normalize();
                    } else {
                        float seed = (float) ((cbp.hashCode() % 100) / 100.0);
                        forward.set(Mth.sin(seed * 3.1415f), Mth.cos(seed * 3.1415f), 0.5f).normalize();
                    }
                } else {
                    float speed = (float) Math.sqrt(speedSq);
                    forward.set((float) (dx / speed), (float) (dy / speed), (float) (dz / speed));
                }

                Vector3f temp = new Vector3f(0, 1, 0);
                if (Math.abs(forward.dot(temp)) > 0.99f) {
                    temp.set(1, 0, 0);
                }

                Vector3f right = new Vector3f();
                forward.cross(temp, right);
                right.normalize();

                Vector3f up = new Vector3f();
                right.cross(forward, up);
                up.normalize();

                float s = cbp.radius;
                Matrix4f mat = renderStack.last().pose();

                int r = 255, g = 255, b = 255, a = 255;

//                renderStack.mulPose(Axis.XP.rotationDegrees(90.0F));
                buf.vertex(mat, (-right.x() - up.x()) * s, (-right.y() - up.y()) * s, (-right.z() - up.z()) * s)
                        .uv(0.0F, 1.0F)
                        .color(r, g, b, a)
                        .endVertex();

                buf.vertex(mat, (right.x() - up.x()) * s, (right.y() - up.y()) * s, (right.z() - up.z()) * s)
                        .uv(1.0F, 1.0F)
                        .color(r, g, b, a)
                        .endVertex();

                buf.vertex(mat, (right.x() + up.x()) * s, (right.y() + up.y()) * s, (right.z() + up.z()) * s)
                        .uv(1.0F, 0.0F)
                        .color(r, g, b, a)
                        .endVertex();

                buf.vertex(mat, (-right.x() + up.x()) * s, (-right.y() + up.y()) * s, (-right.z() + up.z()) * s)
                        .uv(0.0F, 0.0F)
                        .color(r, g, b, a)
                        .endVertex();

                renderStack.popPose();
        }

        BufferUploader.drawWithShader(buf.end());
        RenderSystem.enableCull();
    }

    @SuppressWarnings("unchecked")
    private static List<PostPass> getPasses(PostChain chain) {
        try {
            Field field = PostChain.class.getDeclaredField("passes");
            field.setAccessible(true);
            Object value = field.get(chain);
            if (value instanceof List<?> list) {
                return (List<PostPass>) list;
            }
        } catch (ReflectiveOperationException ignored) {
        }
        return Collections.emptyList();
    }


}
