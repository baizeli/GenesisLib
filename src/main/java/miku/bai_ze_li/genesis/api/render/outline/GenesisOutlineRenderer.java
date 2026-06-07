package miku.bai_ze_li.genesis.api.render.outline;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.mojang.math.Axis;
import miku.bai_ze_li.genesis.mixin.client.GameRendererAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Camera;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import miku.bai_ze_li.genesis.api.render.shader.GenesisShaders;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL30;

import java.lang.reflect.Field;
import java.util.Arrays;

public class GenesisOutlineRenderer {
    private static final int EFFECT_COUNT = 3;
    private static final float FIXED_OUTLINE_WIDTH = 0.5F;
    private static final TextureTarget[] maskBuffers = new TextureTarget[EFFECT_COUNT];
    private static final boolean[] hasCapturedWorld = new boolean[EFFECT_COUNT];
    private static final boolean[] needsWorldColorComposite = new boolean[EFFECT_COUNT];
    private static final boolean[] hasCapturedGui = new boolean[EFFECT_COUNT];
    private static final int[] EFFECT_LOOKUP = new int[3];

    private static TextureTarget worldBlurA, worldBlurB;
    private static TextureTarget guiBlurA, guiBlurB;
    private static boolean isCapturingWorld = false;
    private static boolean isCapturingGui = false;
    private static boolean isCapturingHandMask = false;
    private static GenesisEffect activeGuiEffect = null;
    private static InteractionHand activeHandMaskFilter = null;
    private static int previousWorldFramebuffer = -1;
    private static int previousGuiFramebuffer = -1;
    private static DeferredHandOutline deferredHandOutline = null;
    private static DeferredWorldContext deferredWorldContext = null;
    private static Object oculusHandRendererInstance = null;
    private static Field oculusHandActiveField = null;
    private static Field oculusHandRenderingSolidField = null;
    private static Minecraft cachedMinecraft;

    private static final Matrix4f IDENTITY_MATRIX = new Matrix4f();
    private static final Matrix4f SAVED_PROJ_CACHE = new Matrix4f();

    static {
        EFFECT_LOOKUP[GenesisEffect.BLACK_RED.ordinal()] = 0;
        EFFECT_LOOKUP[GenesisEffect.BLUE_WHITE.ordinal()] = 1;
        EFFECT_LOOKUP[GenesisEffect.RAINBOW.ordinal()] = 2;
        IDENTITY_MATRIX.identity();
    }

    private static int getEffectIndex(GenesisEffect effect) {
        return EFFECT_LOOKUP[effect.ordinal()];
    }

    private static boolean isEffectEnabled(int effectIdx) {
        return true;
    }

    private static float getEffectWidth(int effectIdx) {
        return 1.0F;
    }

    private static Minecraft getMc() {
        if (cachedMinecraft == null) cachedMinecraft = Minecraft.getInstance();
        return cachedMinecraft;
    }

    private static TextureTarget getOrCreateMaskBuffer(int idx) {
        Minecraft mc = getMc();
        RenderTarget main = mc.getMainRenderTarget();
        TextureTarget buf = maskBuffers[idx];
        if (buf == null) {
            buf = new TextureTarget(main.width, main.height, true, Minecraft.ON_OSX);
            buf.setFilterMode(9729);
            maskBuffers[idx] = buf;
        } else if (buf.width != main.width || buf.height != main.height) {
            buf.resize(main.width, main.height, Minecraft.ON_OSX);
        }
        return buf;
    }

    public static void beginWorldPass() {
        Arrays.fill(hasCapturedWorld, false);
        Arrays.fill(needsWorldColorComposite, false);
    }

    public static void startWorldCapture(ItemStack stack, GenesisEffect effect) {
        if (!beginWorldMaskCapture(effect)) return;
        needsWorldColorComposite[getEffectIndex(effect)] = true;
    }

    public static boolean beginWorldMaskCapture(GenesisEffect effect) {
        int idx = getEffectIndex(effect);
        if (!isEffectEnabled(idx)) return false;
        TextureTarget mb = getOrCreateMaskBuffer(idx);
        previousWorldFramebuffer = GL30.glGetInteger(GL30.GL_FRAMEBUFFER_BINDING);

        if (!hasCapturedWorld[idx]) {
            mb.bindWrite(false);
            RenderSystem.depthMask(true);
            RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 0.0F);
            RenderSystem.clear(org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT | org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
            hasCapturedWorld[idx] = true;
            needsWorldColorComposite[idx] = false;
        } else {
            mb.bindWrite(false);
        }
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        isCapturingWorld = true;
        return true;
    }

    public static boolean beginHandMaskCapture(GenesisEffect effect, InteractionHand hand) {
        if (getMc().screen != null) {
            return false;
        }
        if (!beginWorldMaskCapture(effect)) {
            return false;
        }
        isCapturingHandMask = true;
        activeHandMaskFilter = hand;
        return true;
    }

    public static void stopHandMaskCapture() {
        isCapturingHandMask = false;
        activeHandMaskFilter = null;
        stopWorldMaskCapture();
    }

    public static boolean isHandMaskCaptureActive() {
        return isCapturingHandMask;
    }

    public static boolean shouldSkipHandMask(InteractionHand hand) {
        return isCapturingHandMask && activeHandMaskFilter != null && activeHandMaskFilter != hand;
    }

    public static void queueDeferredHandOutline(Matrix4f pose, Matrix3f normal, Matrix4f projection,
                                                float partialTick, int packedLight) {
        Minecraft mc = getMc();
        LocalPlayer player = mc.player;
        if (player == null || mc.screen != null) {
            deferredHandOutline = null;
            return;
        }

        boolean main = GenesisRegistry.getTargetEffect(player.getMainHandItem().getItem()) != null;
        boolean off = GenesisRegistry.getTargetEffect(player.getOffhandItem().getItem()) != null;
        if (!main && !off) {
            deferredHandOutline = null;
            return;
        }

        deferredHandOutline = new DeferredHandOutline(
                new Matrix4f(pose),
                new Matrix3f(normal),
                new Matrix4f(projection),
                partialTick,
                packedLight
        );
    }

    public static void clearDeferredHandOutline() {
        deferredHandOutline = null;
    }

    public static void captureLevelRenderContext(PoseStack poseStack, float partialTick,
                                                 Camera camera, Matrix4f projectionMatrix) {
        deferredWorldContext = new DeferredWorldContext(
                new Matrix4f(poseStack.last().pose()),
                new Matrix4f(projectionMatrix),
                partialTick,
                camera.getPosition()
        );
    }

    public static void renderDeferredOutlines(float partialTicks, ItemInHandRenderer itemInHandRenderer,
                                              LightTexture lightTexture) {
        Minecraft mc = getMc();
        if (mc.level == null || mc.player == null) {
            clearDeferredHandOutline();
            deferredWorldContext = null;
            beginWorldPass();
            return;
        }
        if (mc.screen != null) {
            clearDeferredHandOutline();
            deferredWorldContext = null;
            beginWorldPass();
            return;
        }

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        bufferSource.endBatch();
        flushWorldPass(OutlineCompositePass.HAND);
        beginWorldPass();

        RenderSystem.backupProjectionMatrix();
        PoseStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushPose();

        lightTexture.turnOnLightLayer();
        try {
            renderDeferredDroppedItems(mc, partialTicks, bufferSource, modelViewStack);
            bufferSource.endBatch();
            flushWorldPass(OutlineCompositePass.FIXED_WORLD);
            beginWorldPass();

            renderDeferredHandOutline(mc, itemInHandRenderer, bufferSource, modelViewStack);
            bufferSource.endBatch();
            flushWorldPass(OutlineCompositePass.HAND);
        } finally {
            lightTexture.turnOffLightLayer();
            modelViewStack.popPose();
            RenderSystem.restoreProjectionMatrix();
            RenderSystem.applyModelViewMatrix();
            clearDeferredHandOutline();
            deferredWorldContext = null;
        }
    }

    private static void renderDeferredDroppedItems(Minecraft mc, float partialTicks,
                                                   MultiBufferSource.BufferSource bufferSource,
                                                   PoseStack modelViewStack) {
        Camera camera = mc.gameRenderer.getMainCamera();
        DeferredWorldContext context = deferredWorldContext;
        float framePartialTick = context != null ? context.partialTick : partialTicks;
        Vec3 cameraPos = context != null ? context.cameraPos : camera.getPosition();
        Matrix4f projection = context != null
                ? new Matrix4f(context.projection)
                : createProjectionMatrix(mc, camera, partialTicks);
        RenderSystem.setProjectionMatrix(projection, VertexSorting.DISTANCE_TO_ORIGIN);

        modelViewStack.setIdentity();
        modelViewStack.mulPoseMatrix(createModelViewMatrix(context, camera));
        RenderSystem.applyModelViewMatrix();

        EntityRenderDispatcher dispatcher = mc.getEntityRenderDispatcher();
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof ItemEntity itemEntity)) {
                continue;
            }

            ItemStack stack = itemEntity.getItem();
            GenesisEffect effect = stack.isEmpty() ? null : GenesisRegistry.getTargetEffect(stack.getItem());
            if (effect == null) {
                continue;
            }

            EntityRenderer<? super ItemEntity> renderer = dispatcher.getRenderer(itemEntity);
            if (!(renderer instanceof ItemEntityRenderer itemRenderer)) {
                continue;
            }

            double x = Mth.lerp(framePartialTick, itemEntity.xOld, itemEntity.getX()) - cameraPos.x;
            double y = Mth.lerp(framePartialTick, itemEntity.yOld, itemEntity.getY()) - cameraPos.y;
            double z = Mth.lerp(framePartialTick, itemEntity.zOld, itemEntity.getZ()) - cameraPos.z;

            PoseStack poseStack = new PoseStack();
            poseStack.translate(x, y, z);
            int packedLight = dispatcher.getPackedLightCoords(itemEntity, framePartialTick);

            bufferSource.endBatch();
            if (!beginWorldMaskCapture(effect)) {
                continue;
            }
            try {
                itemRenderer.render(itemEntity, 0.0F, framePartialTick, poseStack, bufferSource, packedLight);
                bufferSource.endBatch();
            } finally {
                stopWorldMaskCapture();
            }
        }
    }

    private static Matrix4f createProjectionMatrix(Minecraft mc, Camera camera, float partialTicks) {
        double fov = ((GameRendererAccessor) mc.gameRenderer).callGetFov(camera, partialTicks, true);
        return mc.gameRenderer.getProjectionMatrix(fov);
    }

    private static Matrix4f createModelViewMatrix(DeferredWorldContext context, Camera camera) {
        if (context != null) {
            return new Matrix4f(context.modelView);
        }

        PoseStack viewPoseStack = new PoseStack();
        viewPoseStack.mulPose(Axis.XP.rotationDegrees(camera.getXRot()));
        viewPoseStack.mulPose(Axis.YP.rotationDegrees(camera.getYRot() + 180.0F));
        return new Matrix4f(viewPoseStack.last().pose());
    }

    private static void renderDeferredHandOutline(Minecraft mc, ItemInHandRenderer itemInHandRenderer,
                                                  MultiBufferSource.BufferSource bufferSource,
                                                  PoseStack modelViewStack) {
        DeferredHandOutline frame = deferredHandOutline;
        LocalPlayer player = mc.player;
        if (frame == null || player == null) {
            return;
        }

        RenderSystem.setProjectionMatrix(new Matrix4f(frame.projection), VertexSorting.DISTANCE_TO_ORIGIN);
        modelViewStack.setIdentity();
        RenderSystem.applyModelViewMatrix();

        renderDeferredHandMask(player, itemInHandRenderer, bufferSource, frame, InteractionHand.MAIN_HAND);
        renderDeferredHandMask(player, itemInHandRenderer, bufferSource, frame, InteractionHand.OFF_HAND);
    }

    private static void renderDeferredHandMask(LocalPlayer player, ItemInHandRenderer itemInHandRenderer,
                                               MultiBufferSource.BufferSource bufferSource,
                                               DeferredHandOutline frame, InteractionHand hand) {
        ItemStack stack = hand == InteractionHand.MAIN_HAND ? player.getMainHandItem() : player.getOffhandItem();
        GenesisEffect effect = stack.isEmpty() ? null : GenesisRegistry.getTargetEffect(stack.getItem());
        if (effect == null) {
            return;
        }

        bufferSource.endBatch();
        if (!beginHandMaskCapture(effect, hand)) {
            return;
        }

        PoseStack handPose = new PoseStack();
        handPose.last().pose().set(frame.pose);
        handPose.last().normal().set(frame.normal);
        OculusHandState oculusState = beginOculusHandMaskState();
        try {
            itemInHandRenderer.renderHandsWithItems(frame.partialTick, handPose, bufferSource, player, frame.packedLight);
            bufferSource.endBatch();
        } finally {
            restoreOculusHandMaskState(oculusState);
            stopHandMaskCapture();
        }
    }

    private static OculusHandState beginOculusHandMaskState() {
        try {
            if (oculusHandRendererInstance == null || oculusHandActiveField == null || oculusHandRenderingSolidField == null) {
                Class<?> handRendererClass = Class.forName("net.irisshaders.iris.pathways.HandRenderer");
                Field instanceField = handRendererClass.getField("INSTANCE");
                oculusHandActiveField = handRendererClass.getDeclaredField("ACTIVE");
                oculusHandRenderingSolidField = handRendererClass.getDeclaredField("renderingSolid");
                oculusHandActiveField.setAccessible(true);
                oculusHandRenderingSolidField.setAccessible(true);
                oculusHandRendererInstance = instanceField.get(null);
            }

            boolean previousActive = oculusHandActiveField.getBoolean(oculusHandRendererInstance);
            boolean previousRenderingSolid = oculusHandRenderingSolidField.getBoolean(oculusHandRendererInstance);
            oculusHandActiveField.setBoolean(oculusHandRendererInstance, false);
            oculusHandRenderingSolidField.setBoolean(oculusHandRendererInstance, true);
            return new OculusHandState(previousActive, previousRenderingSolid);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static void restoreOculusHandMaskState(OculusHandState state) {
        if (state == null || oculusHandRendererInstance == null
                || oculusHandActiveField == null || oculusHandRenderingSolidField == null) {
            return;
        }

        try {
            oculusHandActiveField.setBoolean(oculusHandRendererInstance, state.active);
            oculusHandRenderingSolidField.setBoolean(oculusHandRendererInstance, state.renderingSolid);
        } catch (Throwable ignored) {
        }
    }

    public static void stopWorldCapture() {
        stopWorldMaskCapture();
    }

    public static void stopWorldMaskCapture() {
        if (!isCapturingWorld) return;
        restoreFramebuffer(previousWorldFramebuffer);
        previousWorldFramebuffer = -1;
        restoreCommonState();
        isCapturingWorld = false;
    }

    public static void flushWorldPass() {
        flushWorldPass(OutlineCompositePass.HAND);
    }

    private static void flushWorldPass(OutlineCompositePass pass) {
        if (getMc().screen != null) {
            beginWorldPass();
            return;
        }

        ShaderInstance outlineShader = GenesisShaders.getGenesisOutline();
        ShaderInstance bloomShader = GenesisShaders.getGenesisBloom();
        ShaderInstance blurShader = GenesisShaders.getGenesisBloomBlur();

        if (outlineShader == null) {
            beginWorldPass(); return;
        }

        Minecraft mc = getMc();
        RenderTarget main = mc.getMainRenderTarget();
        boolean anyToRender = false;
        for (boolean b : hasCapturedWorld) if (b) { anyToRender = true; break; }
        if (!anyToRender) return;

        SAVED_PROJ_CACHE.set(RenderSystem.getProjectionMatrix());
        IDENTITY_MATRIX.identity();
        RenderSystem.setProjectionMatrix(IDENTITY_MATRIX, com.mojang.blaze3d.vertex.VertexSorting.ORTHOGRAPHIC_Z);

        com.mojang.blaze3d.vertex.PoseStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushPose(); modelViewStack.setIdentity(); RenderSystem.applyModelViewMatrix();

        main.bindWrite(true);
        RenderSystem.enableBlend(); RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest(); RenderSystem.depthMask(false);

        for (int i = 0; i < EFFECT_COUNT; i++) {
            if (!hasCapturedWorld[i]) continue;
            TextureTarget maskBuf = maskBuffers[i];
            int colorTexId = maskBuf.getColorTextureId();
            int depthTexId = maskBuf.getDepthTextureId();

            if (needsWorldColorComposite[i]) {
                drawTexture(colorTexId);
            }

            RenderSystem.setShader(() -> outlineShader);
            outlineShader.setSampler("DiffuseSampler", colorTexId);
            outlineShader.setSampler("DepthSampler", depthTexId);
            applyGlobalUniforms(outlineShader, i, main, pass);
            drawQuad();

            if (bloomShader != null && blurShader != null) {
                runBloomPass(mc, main, i, pass, bloomShader, blurShader);
            }
        }

        modelViewStack.popPose(); RenderSystem.applyModelViewMatrix();
        RenderSystem.setProjectionMatrix(SAVED_PROJ_CACHE, com.mojang.blaze3d.vertex.VertexSorting.ORTHOGRAPHIC_Z);

        RenderSystem.enableDepthTest(); RenderSystem.depthMask(true);
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend(); RenderSystem.defaultBlendFunc();
        RenderSystem.activeTexture(org.lwjgl.opengl.GL13.GL_TEXTURE0);
        RenderSystem.bindTexture(0);

        beginWorldPass();
    }

    public static void startGuiCapture(ItemStack stack, GenesisEffect effect) {
        startGuiMaskCapture(effect);
    }

    public static void startGuiMaskCapture(GenesisEffect effect) {
        beginGuiMaskCapture(effect);
    }

    public static boolean beginGuiMaskCapture(GenesisEffect effect) {
        int effectIdx = getEffectIndex(effect);
        if (!isEffectEnabled(effectIdx)) return false;

        TextureTarget mask = getOrCreateMaskBuffer(effectIdx);
        previousGuiFramebuffer = GL30.glGetInteger(GL30.GL_FRAMEBUFFER_BINDING);
        if (!hasCapturedGui[effectIdx]) {
            mask.bindWrite(false);
            RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 0.0F);
            RenderSystem.clear(org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT | org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
            hasCapturedGui[effectIdx] = true;
        } else {
            mask.bindWrite(false);
        }

        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        isCapturingGui = true;
        activeGuiEffect = effect;
        return true;
    }

    public static void stopGuiCapture() {
        stopGuiMaskCapture();
    }

    public static void stopGuiMaskCapture() {
        if (!isCapturingGui) return;
        isCapturingGui = false;
        activeGuiEffect = null;
        restoreFramebuffer(previousGuiFramebuffer);
        previousGuiFramebuffer = -1;
        restoreCommonState();
    }

    public static void flushGuiCapture() {
        stopGuiCapture();
        flushGuiPass();
    }

    public static void flushGuiPass() {
        if (isCapturingGui) {
            stopGuiCapture();
        }

        boolean anyToRender = false;
        for (boolean b : hasCapturedGui) if (b) { anyToRender = true; break; }
        if (!anyToRender) return;

        ShaderInstance outlineShader = GenesisShaders.getGenesisOutline();
        if (outlineShader == null) {
            resetGuiPass();
            getMc().getMainRenderTarget().bindWrite(false);
            return;
        }

        Minecraft mc = getMc();
        RenderTarget main = mc.getMainRenderTarget();

        pushFullscreenState();
        try {
            main.bindWrite(false);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);

            for (int i = 0; i < EFFECT_COUNT; i++) {
                if (!hasCapturedGui[i]) continue;
                TextureTarget mask = maskBuffers[i];
                if (mask == null) continue;

                int colorTexId = mask.getColorTextureId();
                int depthTexId = mask.getDepthTextureId();

                RenderSystem.setShader(() -> outlineShader);
                outlineShader.setSampler("DiffuseSampler", colorTexId);
                outlineShader.setSampler("DepthSampler", depthTexId);
                applyGlobalUniforms(outlineShader, i, main, OutlineCompositePass.GUI);
                drawQuad();
            }
        } finally {
            popFullscreenState();
            resetGuiPass();
            main.bindWrite(false);
            restoreCommonState();
        }
    }

    private static void pushFullscreenState() {
        SAVED_PROJ_CACHE.set(RenderSystem.getProjectionMatrix());
        IDENTITY_MATRIX.identity();
        RenderSystem.setProjectionMatrix(IDENTITY_MATRIX, com.mojang.blaze3d.vertex.VertexSorting.ORTHOGRAPHIC_Z);

        com.mojang.blaze3d.vertex.PoseStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushPose();
        modelViewStack.setIdentity();
        RenderSystem.applyModelViewMatrix();
    }

    private static void popFullscreenState() {
        com.mojang.blaze3d.vertex.PoseStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.popPose();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.setProjectionMatrix(SAVED_PROJ_CACHE, com.mojang.blaze3d.vertex.VertexSorting.ORTHOGRAPHIC_Z);
    }

    private static void drawTexture(int textureId) {
        RenderSystem.setShader(net.minecraft.client.renderer.GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, textureId);
        drawQuad();
    }

    private static void resetGuiPass() {
        Arrays.fill(hasCapturedGui, false);
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

    private static void restoreFramebuffer(int framebuffer) {
        if (framebuffer >= 0) {
            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebuffer);
        } else {
            getMc().getMainRenderTarget().bindWrite(false);
        }
    }

    private static void runBloomPass(Minecraft mc, RenderTarget main, int effectIdx, OutlineCompositePass pass,
                                     ShaderInstance bloomShader, ShaderInstance blurShader) {
        if (blurShader == null || bloomShader == null) return;
        boolean isGui = pass == OutlineCompositePass.GUI;
        if (isGui) return;
        int bw = Math.max(64, main.width / (isGui ? 4 : 2));
        int bh = Math.max(64, main.height / (isGui ? 4 : 2));

        TextureTarget activeBlurA = isGui ? guiBlurA : worldBlurA;
        TextureTarget activeBlurB = isGui ? guiBlurB : worldBlurB;

        activeBlurA = syncBuffer(activeBlurA, bw, bh);
        activeBlurB = syncBuffer(activeBlurB, bw, bh);

        if (isGui) { guiBlurA = activeBlurA; guiBlurB = activeBlurB; }
        else { worldBlurA = activeBlurA; worldBlurB = activeBlurB; }

        RenderSystem.disableBlend();
        if (blurShader.getUniform("ScreenSize") != null) blurShader.getUniform("ScreenSize").set((float) bw, (float) bh);

        float radius = isGui ? 0.6F : 2.5F;
        int passes = isGui ? 1 : 4;
        int srcTex = maskBuffers[effectIdx].getColorTextureId();
        int blurResult = runBlur(blurShader, srcTex, activeBlurA, activeBlurB, radius / passes, passes);

        main.bindWrite(true);
        RenderSystem.enableBlend(); RenderSystem.defaultBlendFunc();

        RenderSystem.setShader(() -> bloomShader);
        bloomShader.setSampler("DiffuseSampler", srcTex);
        bloomShader.setSampler("NearBlurSampler", srcTex);
        bloomShader.setSampler("FarBlurSampler", blurResult);
        applyGlobalUniforms(bloomShader, effectIdx, main, pass);

        if (bloomShader.getUniform("BloomStrength") != null) bloomShader.getUniform("BloomStrength").set(0.0F);
        if (bloomShader.getUniform("BloomRadius") != null) bloomShader.getUniform("BloomRadius").set(radius);
        drawQuad();
    }

    private static int runBlur(ShaderInstance shader, int tex, TextureTarget bufA, TextureTarget bufB, float radius, int passes) {
        int current = tex;
        for (int i = 0; i < passes; i++) {
            if (shader.getUniform("BlurRadius") != null) shader.getUniform("BlurRadius").set(radius);
            bufA.bindWrite(true);
            shader.setSampler("DiffuseSampler", current);
            if (shader.getUniform("BlurDirection") != null) shader.getUniform("BlurDirection").set(1.0F, 0.0F);
            drawQuad();

            bufB.bindWrite(true);
            shader.setSampler("DiffuseSampler", bufA.getColorTextureId());
            if (shader.getUniform("BlurDirection") != null) shader.getUniform("BlurDirection").set(0.0F, 1.0F);
            drawQuad();
            current = bufB.getColorTextureId();
        }
        return current;
    }

    private static TextureTarget syncBuffer(TextureTarget buffer, int w, int h) {
        if (buffer == null) {
            TextureTarget newBuf = new TextureTarget(w, h, false, Minecraft.ON_OSX);
            newBuf.setFilterMode(9729); return newBuf;
        } else if (buffer.width != w || buffer.height != h) {
            buffer.resize(w, h, Minecraft.ON_OSX); buffer.setFilterMode(9729);
        }
        return buffer;
    }

    private static void applyGlobalUniforms(ShaderInstance shader, int effectIdx, RenderTarget target, OutlineCompositePass pass) {
        boolean isGui = pass == OutlineCompositePass.GUI;
        if (shader.getUniform("ScreenSize") != null) shader.getUniform("ScreenSize").set((float) target.width, (float) target.height);
        if (shader.getUniform("Time") != null) shader.getUniform("Time").set((float) (System.currentTimeMillis() % 24000L) / 1000.0F);

        switch (effectIdx) {
            case 0:
                setVec4(shader, "OutlineColor", 0.8F, 0.05F, 0.05F);
                setVec4(shader, "SecondaryColor", 0.02F, 0.02F, 0.02F);
                if (shader.getUniform("ColorMode") != null) shader.getUniform("ColorMode").set(1.0F);
                break;
            case 1:
                setVec4(shader, "OutlineColor", 0.1F, 0.5F, 1.0F);
                setVec4(shader, "SecondaryColor", 0.7F, 0.9F, 1.0F);
                if (shader.getUniform("ColorMode") != null) shader.getUniform("ColorMode").set(1.0F);
                break;
            default:
                setVec4(shader, "OutlineColor", 1.0F, 1.0F, 1.0F);
                if (shader.getUniform("ColorMode") != null) shader.getUniform("ColorMode").set(2.0F);
        }

        float outlineWidth = pass == OutlineCompositePass.HAND ? getEffectWidth(effectIdx) : FIXED_OUTLINE_WIDTH;
        if (shader.getUniform("OutlineWidth") != null) shader.getUniform("OutlineWidth").set(outlineWidth);
        if (shader.getUniform("Softness") != null) shader.getUniform("Softness").set(isGui ? 0.65F : 1.35F);
        if (shader.getUniform("DepthWeight") != null) shader.getUniform("DepthWeight").set(isGui ? 0.0F : 1.15F);
        if (shader.getUniform("GlowStrength") != null) shader.getUniform("GlowStrength").set(isGui ? 0.18F : 0.55F);
        if (shader.getUniform("DistanceScale") != null) shader.getUniform("DistanceScale").set(1.0F);
        if (shader.getUniform("Opacity") != null) shader.getUniform("Opacity").set(isGui ? 0.9F : 0.95F);
    }

    private static void setVec4(ShaderInstance shader, String uniform, float r, float g, float b) {
        if (shader.getUniform(uniform) != null) shader.getUniform(uniform).set(r, g, b, 1.0F);
    }

    private static void drawQuad() {
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        builder.vertex(-1.0D, -1.0D, 0.0D).uv(0.0F, 0.0F).endVertex();
        builder.vertex(1.0D, -1.0D, 0.0D).uv(1.0F, 0.0F).endVertex();
        builder.vertex(1.0D, 1.0D, 0.0D).uv(1.0F, 1.0F).endVertex();
        builder.vertex(-1.0D, 1.0D, 0.0D).uv(0.0F, 1.0F).endVertex();
        BufferUploader.drawWithShader(builder.end());
    }

    private static final class DeferredHandOutline {
        private final Matrix4f pose;
        private final Matrix3f normal;
        private final Matrix4f projection;
        private final float partialTick;
        private final int packedLight;

        private DeferredHandOutline(Matrix4f pose, Matrix3f normal, Matrix4f projection,
                                    float partialTick, int packedLight) {
            this.pose = pose;
            this.normal = normal;
            this.projection = projection;
            this.partialTick = partialTick;
            this.packedLight = packedLight;
        }
    }

    private static final class DeferredWorldContext {
        private final Matrix4f modelView;
        private final Matrix4f projection;
        private final float partialTick;
        private final Vec3 cameraPos;

        private DeferredWorldContext(Matrix4f modelView, Matrix4f projection, float partialTick, Vec3 cameraPos) {
            this.modelView = modelView;
            this.projection = projection;
            this.partialTick = partialTick;
            this.cameraPos = cameraPos;
        }
    }

    private static final class OculusHandState {
        private final boolean active;
        private final boolean renderingSolid;

        private OculusHandState(boolean active, boolean renderingSolid) {
            this.active = active;
            this.renderingSolid = renderingSolid;
        }
    }

    private enum OutlineCompositePass {
        FIXED_WORLD,
        HAND,
        GUI
    }
}

