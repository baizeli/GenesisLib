package miku.bai_ze_li.genesis.api.render.tooltip;

import miku.bai_ze_li.genesis.GenesisLib;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

import java.util.*;

@OnlyIn(Dist.CLIENT)
public class TooltipParticleSystem
{
    private static final TooltipParticleSystem INSTANCE = new TooltipParticleSystem();
    private final List<TooltipParticle> particles = new ArrayList<>();
    private final Random random = new Random();
    private long lastUpdateTime = System.currentTimeMillis();
    private long lastRenderTime = System.nanoTime();

    public static class ParticleTextureInfo {
        public final ResourceLocation texture;
        public final int frameCount;
        public final int frameInterval;
        public final int framesPerRow;

        public ParticleTextureInfo(ResourceLocation texture) {
            this(texture, 1, 0, 1);
        }
        public ParticleTextureInfo(ResourceLocation texture, int frameCount, int frameInterval) {this(texture, frameCount, frameInterval, calculateFramesPerRow(frameCount));}

        public ParticleTextureInfo(ResourceLocation texture, int frameCount, int frameInterval, int framesPerRow)
        {
            this.texture = texture;
            this.frameCount = frameCount;
            this.frameInterval = frameInterval;
            this.framesPerRow = framesPerRow;
        }

        private static int calculateFramesPerRow(int frameCount)
        {
            if (frameCount <= 1) return 1;
            // 计算最接近平方根的整数
            int sqrt = (int)Math.ceil(Math.sqrt(frameCount));
            return sqrt;
        }

        // 指定帧的UV坐标
        public UVCoords getFrameUV(int frameIndex)
        {
            if (frameCount <= 1) return new UVCoords(0, 0, 1, 1);

            frameIndex = frameIndex % frameCount;
            int col = frameIndex % framesPerRow;
            int row = frameIndex / framesPerRow;

            float frameWidth = 1.0f / framesPerRow;
            float frameHeight = 1.0f / (int)Math.ceil((float)frameCount / framesPerRow);

            float u1 = col * frameWidth;
            float v1 = row * frameHeight;
            float u2 = u1 + frameWidth;
            float v2 = v1 + frameHeight;

            return new UVCoords(u1, v1, u2, v2);
        }
    }

    // UV坐标
    public static class UVCoords
    {
        public final float u1, v1, u2, v2;

        public UVCoords(float u1, float v1, float u2, float v2) {
            this.u1 = u1;
            this.v1 = v1;
            this.u2 = u2;
            this.v2 = v2;
        }
    }

    public static final Map<String, ParticleTextureInfo> PARTICLE_TEXTURES = new HashMap<>();

    static
    {
        registerParticle(PTID.Star_0, 4, 40, 1);
        registerParticle(PTID.Star_1, 3, 30, 1);
        registerParticle(PTID.Star_2, 6, 60, 1);
        registerParticle(PTID.Star_3, 5, 20, 1);
        registerParticle(PTID.Star_4, 4, 40, 1);
        registerParticle(PTID.Star_5, 1, 0, 1);
        registerParticle(PTID.Star_6, 6, 20, 1);
        registerParticle(PTID.Star_7, 4, 40, 1);
        registerParticle(PTID.Star_8, 7, 60, 1);
        registerParticle(PTID.Star_9, 4, 40, 1);

        registerParticle(PTID.Leaf_0, 1, 0, 1);
        registerParticle(PTID.Leaf_1, 1, 0, 1);
        registerParticle(PTID.Leaf_2, 1, 0, 1);
        registerParticle(PTID.Leaf_3, 1, 0, 1);

        registerParticle(PTID.Smoke_0, 1, 0, 1);
        registerParticle(PTID.Smoke_1, 1, 0, 1);
        registerParticle(PTID.Smoke_2, 1, 0, 1);
        registerParticle(PTID.Smoke_3, 1, 0, 1);
        registerParticle(PTID.Smoke_4, 1, 0, 1);
    }

    // 注册纹理
    private static void registerParticle(String name, int frames, int duration, int loops)
    {
        PARTICLE_TEXTURES.put(name, new ParticleTextureInfo(ResourceLocation.fromNamespaceAndPath(GenesisLib.MODID, "textures/misc/" + name + ".png"), frames, duration, loops));
    }

    public static void registerParticleTexture(String key, ResourceLocation texture)
    {
        PARTICLE_TEXTURES.put(key, new ParticleTextureInfo(texture));
    }

    public static void registerParticleTexture(String key, ResourceLocation texture, int frameCount, int frameInterval)
    {
        PARTICLE_TEXTURES.put(key, new ParticleTextureInfo(texture, frameCount, frameInterval));
    }

    public static void registerParticleTexture(String key, ResourceLocation texture, int frameCount, int frameInterval, int framesPerRow)
    {
        PARTICLE_TEXTURES.put(key, new ParticleTextureInfo(texture, frameCount, frameInterval, framesPerRow));
    }

    public static TooltipParticleSystem getInstance() {
        return INSTANCE;
    }

    // 粒子运动类型
    public enum MotionType {
        LINEAR,          // 直线运动
        SINE_WAVE,       // 正弦波运动
        CIRCULAR,        // 圆形运动
        FLOATING,        // 上下飘动
        SPIRAL,          // 螺旋运动
        RANDOM_WALK,     // 随机游走
        ORBIT,           // 轨道运动
        RAIN,            // 下雨效果
        STORM,           // 暴风雨效果
        SNOW,            // 雪花飘落
        LEAVES,          // 树叶飘落
        LEAF_WIND_RIGHT,
        FIRE_SPARKS,     // 火花四溅
        MAGIC_SWIRL,     // 魔法漩涡
        GALAXY_SPIRAL,   // 银河螺旋
        PULSE,           // 脉冲扩散
        WAVE_RIPPLE,     // 波纹扩散
        TORNADO,         // 龙卷风
        BUTTERFLY,       // 蝴蝶飞舞
        FIREWORKS,       // 烟花爆炸
        PHOENIX_FLIGHT,  // 凤凰飞翔
        DRAGON_BREATH,   // 龙息
        TELEPORT,        // 传送特效
        HEALING_AURA,    // 治疗光环
        CURSE_ENERGY,    // 诅咒能量
        TIME_DISTORTION, // 时间扭曲
        SPACE_FOLD       // 空间折叠
    }

    // 贝塞尔曲线类型
    public enum BezierCurveType
    {
        NONE,           // 无曲线
        EASE_IN,        // 缓入
        EASE_OUT,       // 缓出
        EASE_IN_OUT,    // 缓入缓出
        BOUNCE,         // 弹跳
        ELASTIC,        // 弹性
        BACK,           // 回弹
        STAR_TWINKLE,   // 星星闪烁曲线
        STAR_EXPAND,    // 星星膨胀曲线
        STAR_MOVEMENT,  // 星星运动曲线
        HEART_BEAT,     // 心跳曲线
        BREATH,         // 呼吸曲线
        MAGIC_PULSE     // 魔法脉冲曲线
    }

    public static class ParticleConfig
    {
        // 基础
        public String[] textureKeys = {"vansh_glow"};
        public int minParticles = 3;
        public int maxParticles = 8;
        public int maxTotalParticles = 100; // 最大总粒子数
        public float minSize = 2.0f;
        public float maxSize = 6.0f;
        public boolean randomSize = false; // 是否随机大小
        public float minLife = 1.0f;
        public float maxLife = 3.0f;
        public float minSpeed = 5.0f;
        public float maxSpeed = 15.0f;

        // 颜色
        public int[] colors = {0xFF00FFFF, 0xFF00FF00, 0xFFFF00FF};
        public boolean enableColorGradient = false; // 颜色渐变模式
        public boolean enableRainbow = false; // 彩虹模式
        public float rainbowSpeed = 1.0f; // 彩虹变化速度
        public float colorTransitionSpeed = 1.0f; // 颜色过渡速度

        // 物理
        public boolean enableGravity = false;
        public float gravityStrength = 2.0f;
        public boolean enableWind = false;
        public float windStrengthX = 0.0f;
        public float windStrengthY = 0.0f;
        public float airResistance = 0.0f; // 空气阻力
        public float bounciness = 0.0f; // 弹性系数

        // 旋转
        public boolean enableRotation = true;
        public float minRotationSpeed = -90.0f;
        public float maxRotationSpeed = 90.0f;
        public float initialRotationMin = 0.0f; // 初始旋转角度范围
        public float initialRotationMax = 360.0f;

        // 运动
        public MotionType motionType = MotionType.LINEAR;
        public float motionAmplitude = 10.0f;
        public float motionFrequency = 2.0f;
        public float centerX = 0, centerY = 0;
        public float radius = 50.0f;

        // 出入场
        public boolean enableFadeIn = true; // 淡入效果
        public float fadeInDuration = 0.3f; // 淡入持续时间
        public boolean enableFadeOut = true; // 淡出效果
        public float fadeOutDuration = 0.5f; // 淡出持续时间

        // 闪烁
        public boolean enableTwinkling = false;
        public float twinkleSpeed = 2.0f;
        public float twinkleIntensity = 0.5f;

        // 脉冲
        public boolean enablePulsing = false;
        public float pulseSpeed = 1.0f;
        public float pulseScale = 0.3f;

        public boolean enableTrail = false;
        public int trailLength = 5;
        public float trailFadeSpeed = 0.8f;

        // 层次
        public boolean enableDepthLayers = false;
        public int depthLayers = 16;
        public float depthDarkening = 0.1f; // 每层变暗程度

        // 贝塞尔曲线
        public BezierCurveType sizeCurve = BezierCurveType.NONE;
        public BezierCurveType alphaCurve = BezierCurveType.NONE;
        public BezierCurveType speedCurve = BezierCurveType.NONE;
        public BezierCurveType rotationCurve = BezierCurveType.NONE;

        // 一些实验性的
        public boolean enableMagicAura = false;
        public boolean enableEnergyField = false;
        public boolean enableSparkles = false;
        public boolean enableGlow = false;
        public float glowIntensity = 1.0f;
        public boolean enableDistortion = false;
        public float distortionStrength = 0.1f;

        public ParticleConfig setTextures(String... textureKeys)
        {
            this.textureKeys = textureKeys;
            return this;
        }

        public ParticleConfig setParticleCount(int min, int max)
        {
            this.minParticles = min;
            this.maxParticles = max;
            return this;
        }

        public ParticleConfig setMaxTotalParticles(int max)
        {
            this.maxTotalParticles = max;
            return this;
        }

        public ParticleConfig setSize(float min, float max)
        {
            this.minSize = min;
            this.maxSize = max;
            return this;
        }

        public ParticleConfig setRandomSize(boolean enable)
        {
            this.randomSize = enable;
            return this;
        }

        public ParticleConfig setLife(float min, float max)
        {
            this.minLife = min;
            this.maxLife = max;
            return this;
        }

        public ParticleConfig setSpeed(float min, float max)
        {
            this.minSpeed = min;
            this.maxSpeed = max;
            return this;
        }

        public ParticleConfig setColors(int... colors)
        {
            this.colors = colors;
            this.enableColorGradient = false;
            this.enableRainbow = false;
            return this;
        }

        public ParticleConfig setColors(boolean enableGradient, int... colors)
        {
            this.colors = colors;
            this.enableColorGradient = enableGradient;
            this.enableRainbow = false;
            return this;
        }

        public ParticleConfig setRainbowColors(boolean enable, float speed)
        {
            this.enableRainbow = enable;
            this.rainbowSpeed = speed;
            this.enableColorGradient = false;
            return this;
        }

        public ParticleConfig setColorTransitionSpeed(float speed)
        {
            this.colorTransitionSpeed = speed;
            return this;
        }

        public ParticleConfig setGravity(boolean enable, float strength)
        {
            this.enableGravity = enable;
            this.gravityStrength = strength;
            return this;
        }

        public ParticleConfig setWind(boolean enable, float strengthX, float strengthY)
        {
            this.enableWind = enable;
            this.windStrengthX = strengthX;
            this.windStrengthY = strengthY;
            return this;
        }

        public ParticleConfig setAirResistance(float resistance)
        {
            this.airResistance = resistance;
            return this;
        }

        public ParticleConfig setBounciness(float bounciness)
        {
            this.bounciness = bounciness;
            return this;
        }

        public ParticleConfig setRotation(boolean enable, float minSpeed, float maxSpeed)
        {
            this.enableRotation = enable;
            this.minRotationSpeed = minSpeed;
            this.maxRotationSpeed = maxSpeed;
            return this;
        }

        public ParticleConfig setInitialRotation(float min, float max)
        {
            this.initialRotationMin = min;
            this.initialRotationMax = max;
            return this;
        }

        public ParticleConfig setMotionType(MotionType type)
        {
            this.motionType = type;
            return this;
        }

        public ParticleConfig setMotionProperties(float amplitude, float frequency)
        {
            this.motionAmplitude = amplitude;
            this.motionFrequency = frequency;
            return this;
        }

        public ParticleConfig setCenter(float x, float y)
        {
            this.centerX = x;
            this.centerY = y;
            return this;
        }

        public ParticleConfig setRadius(float radius)
        {
            this.radius = radius;
            return this;
        }

        public ParticleConfig setFadeIn(boolean enable, float duration)
        {
            this.enableFadeIn = enable;
            this.fadeInDuration = duration;
            return this;
        }

        public ParticleConfig setFadeOut(boolean enable, float duration)
        {
            this.enableFadeOut = enable;
            this.fadeOutDuration = duration;
            return this;
        }

        public ParticleConfig setTwinkling(boolean enable, float speed, float intensity)
        {
            this.enableTwinkling = enable;
            this.twinkleSpeed = speed;
            this.twinkleIntensity = intensity;
            return this;
        }

        public ParticleConfig setPulsing(boolean enable, float speed, float scale)
        {
            this.enablePulsing = enable;
            this.pulseSpeed = speed;
            this.pulseScale = scale;
            return this;
        }

        // 别看了拖尾没写好
        public ParticleConfig setTrail(boolean enable, int length, float fadeSpeed)
        {
            this.enableTrail = enable;
            this.trailLength = length;
            this.trailFadeSpeed = fadeSpeed;
            return this;
        }

        public ParticleConfig setDepthLayers(boolean enable, int layers, float darkening)
        {
            this.enableDepthLayers = enable;
            this.depthLayers = layers;
            this.depthDarkening = darkening;
            return this;
        }

        public ParticleConfig setSizeCurve(BezierCurveType curve)
        {
            this.sizeCurve = curve;
            return this;
        }

        public ParticleConfig setAlphaCurve(BezierCurveType curve)
        {
            this.alphaCurve = curve;
            return this;
        }

        public ParticleConfig setSpeedCurve(BezierCurveType curve)
        {
            this.speedCurve = curve;
            return this;
        }

        public ParticleConfig setRotationCurve(BezierCurveType curve)
        {
            this.rotationCurve = curve;
            return this;
        }

        public ParticleConfig setMagicAura(boolean enable)
        {
            this.enableMagicAura = enable;
            return this;
        }

        public ParticleConfig setEnergyField(boolean enable)
        {
            this.enableEnergyField = enable;
            return this;
        }

        public ParticleConfig setSparkles(boolean enable)
        {
            this.enableSparkles = enable;
            return this;
        }

        public ParticleConfig setGlow(boolean enable, float intensity)
        {
            this.enableGlow = enable;
            this.glowIntensity = intensity;
            return this;
        }

        public ParticleConfig setDistortion(boolean enable, float strength)
        {
            this.enableDistortion = enable;
            this.distortionStrength = strength;
            return this;
        }
    }

    public void spawnParticlesInTooltip(int tooltipX, int tooltipY, int tooltipWidth, int tooltipHeight, ParticleConfig config)
    {
        // 粒子数量限制
        if (particles.size() >= config.maxTotalParticles) return;

        int particleCount = random.nextInt(config.maxParticles - config.minParticles + 1) + config.minParticles;
        particleCount = Math.min(particleCount, config.maxTotalParticles - particles.size());

        for (int i = 0; i < particleCount; i++)
        {
            String textureKey = config.textureKeys[random.nextInt(config.textureKeys.length)];
            ParticleTextureInfo textureInfo = PARTICLE_TEXTURES.get(textureKey);
            if (textureInfo == null) continue;

            float x, y, vx, vy;

            switch (config.motionType) {
                case RAIN:
                    x = tooltipX + random.nextFloat() * tooltipWidth;
                    y = tooltipY - 50; // 从屏幕上方开始
                    vx = (random.nextFloat() - 0.5f) * 20.0f;
                    vy = config.minSpeed + random.nextFloat() * (config.maxSpeed - config.minSpeed);
                    break;
                case STORM:
                    x = tooltipX + tooltipWidth + 50; // 从右边开始
                    y = tooltipY + random.nextFloat() * (tooltipHeight * 0.3f);
                    vx = -(config.minSpeed + random.nextFloat() * (config.maxSpeed - config.minSpeed)) * 0.7f;
                    vy = config.minSpeed * 0.5f + random.nextFloat() * config.maxSpeed * 0.5f;
                    break;
                case SNOW:
                    x = tooltipX + random.nextFloat() * tooltipWidth;
                    y = tooltipY - 30;
                    vx = (random.nextFloat() - 0.5f) * 10.0f;
                    vy = config.minSpeed * 0.3f + random.nextFloat() * config.maxSpeed * 0.3f;
                    break;
                case LEAVES:
                    x = tooltipX + random.nextFloat() * tooltipWidth;
                    y = tooltipY - 20;
                    vx = (random.nextFloat() - 0.3f) * 30.0f;
                    vy = config.minSpeed * 0.4f + random.nextFloat() * config.maxSpeed * 0.6f;
                    break;
                case LEAF_WIND_RIGHT:
                    x = tooltipX - 48.0f - random.nextFloat() * 96.0f;
                    y = tooltipY + random.nextFloat() * tooltipHeight;
                    vx = config.minSpeed + random.nextFloat() * (config.maxSpeed - config.minSpeed);
                    vy = (random.nextFloat() - 0.5f) * 20.0f;
                    break;
                case FIRE_SPARKS:
                    // 从底部中心向上喷射
                    x = tooltipX + tooltipWidth * 0.5f + (random.nextFloat() - 0.5f) * 20.0f;
                    y = tooltipY + tooltipHeight;
                    float sparkAngle = (float)((Math.PI * 0.3f) + random.nextFloat() * (Math.PI * 0.4f));
                    float sparkSpeed = config.minSpeed + random.nextFloat() * (config.maxSpeed - config.minSpeed);
                    vx = (float)Math.cos(sparkAngle) * sparkSpeed * (random.nextBoolean() ? 1 : -1);
                    vy = -(float)Math.sin(sparkAngle) * sparkSpeed;
                    break;
                case CIRCULAR:
                case ORBIT:
                    float angle = (float)(2 * Math.PI * i / particleCount);
                    x = config.centerX + (float)Math.cos(angle) * config.radius;
                    y = config.centerY + (float)Math.sin(angle) * config.radius;
                    float speed = config.minSpeed + random.nextFloat() * (config.maxSpeed - config.minSpeed);
                    vx = -(float)Math.sin(angle) * speed;
                    vy = (float)Math.cos(angle) * speed;
                    break;
                case FLOATING:
                    x = tooltipX + random.nextFloat() * tooltipWidth;
                    y = tooltipY + tooltipHeight;
                    vx = (random.nextFloat() - 0.5f) * 10.0f;
                    vy = -(config.minSpeed + random.nextFloat() * (config.maxSpeed - config.minSpeed));
                    break;
                default:
                    x = tooltipX + random.nextFloat() * tooltipWidth;
                    y = tooltipY + random.nextFloat() * tooltipHeight;
                    float randomAngle = random.nextFloat() * 2 * (float)Math.PI;
                    float randomSpeed = config.minSpeed + random.nextFloat() * (config.maxSpeed - config.minSpeed);
                    vx = (float)Math.cos(randomAngle) * randomSpeed;
                    vy = (float)Math.sin(randomAngle) * randomSpeed;
                    break;
            }

            float size = config.minSize + random.nextFloat() * (config.maxSize - config.minSize);
            if (config.randomSize) size *= 0.5f + random.nextFloat() * 1.5f; // 随机大小

            float life = config.minLife + random.nextFloat() * (config.maxLife - config.minLife);

            int color;
            if (config.enableRainbow) color = 0xFFFFFFFF;
            else color = config.colors[random.nextInt(config.colors.length)];

            float rotationSpeed = 0;
            float initialRotation = 0;
            if (config.enableRotation)
            {
                rotationSpeed = config.minRotationSpeed + random.nextFloat() * (config.maxRotationSpeed - config.minRotationSpeed);
                initialRotation = config.initialRotationMin + random.nextFloat() * (config.initialRotationMax - config.initialRotationMin);
            }

            int depthLayer = 0;
            if (config.enableDepthLayers) depthLayer = random.nextInt(config.depthLayers);

            TooltipParticle particle = new TooltipParticle(x, y, vx, vy, life, size, color, textureInfo, rotationSpeed, initialRotation, config, depthLayer);
            particles.add(particle);
        }
    }

    public void update(float deltaTime) {
        particles.removeIf(particle -> !particle.update(deltaTime));
    }

    public void render(GuiGraphics graphics, float partialTicks)
    {
        if (particles.isEmpty()) return;

        long currentTime = System.nanoTime();
        float frameTime = (currentTime - lastRenderTime) / 1_000_000_000.0f;
        lastRenderTime = currentTime;

        float actualPartialTicks = Math.min(frameTime * 20.0f, 1.0f);

        particles.sort((a, b) -> Integer.compare(b.depthLayer, a.depthLayer));

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);

        for (TooltipParticle particle : particles) renderParticle(graphics, particle, actualPartialTicks);

        // 恢复
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private void renderParticle(GuiGraphics graphics, TooltipParticle particle, float partialTicks)
    {
        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();

        float renderX = particle.getRenderX(partialTicks);
        float renderY = particle.getRenderY(partialTicks);

        poseStack.translate(renderX, renderY, 0);

        // 旋转
        if (particle.rotationSpeed != 0)
        {
            float renderRotation = particle.rotation + particle.rotationSpeed * partialTicks;
            poseStack.mulPose(Axis.ZP.rotationDegrees(renderRotation));
        }

        // 计算颜色
        int renderColor = particle.getCurrentColor();
        float r = ((renderColor >> 16) & 0xFF) / 255.0f;
        float g = ((renderColor >> 8) & 0xFF) / 255.0f;
        float b = (renderColor & 0xFF) / 255.0f;

        // 深度层级明度调整
        if (particle.config.enableDepthLayers && particle.depthLayer > 0)
        {
            float darkening = 1.0f - (particle.depthLayer * particle.config.depthDarkening);
            r *= darkening;
            g *= darkening;
            b *= darkening;
        }

        RenderSystem.setShaderColor(r, g, b, particle.getCurrentAlpha());
        RenderSystem.setShaderTexture(0, particle.textureInfo.texture);

        // 渲染大小
        float renderSize = particle.getCurrentSize();
        float halfSize = renderSize / 2.0f;

        UVCoords uv = particle.getCurrentFrameUV();
        renderTexturedQuadWithUV(graphics, -halfSize, -halfSize, renderSize, renderSize, uv);

        poseStack.popPose();
    }

    private void renderTexturedQuadWithUV(GuiGraphics graphics, float x, float y, float width, float height, UVCoords uv)
    {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        Matrix4f matrix = graphics.pose().last().pose();
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        bufferBuilder.vertex(matrix, x, y + height, 0).uv(uv.u1, uv.v2).endVertex();
        bufferBuilder.vertex(matrix, x + width, y + height, 0).uv(uv.u2, uv.v2).endVertex();
        bufferBuilder.vertex(matrix, x + width, y, 0).uv(uv.u2, uv.v1).endVertex();
        bufferBuilder.vertex(matrix, x, y, 0).uv(uv.u1, uv.v1).endVertex();

        BufferUploader.drawWithShader(bufferBuilder.end());
    }

    public void clear() {particles.clear();}

    // 贝塞尔曲线
    public static float calculateBezierValue(BezierCurveType type, float t)
    {
        t = Math.max(0, Math.min(1, t));

        switch (type)
        {
            case EASE_IN:
                return t * t;
            case EASE_OUT:
                return 1 - (1 - t) * (1 - t);
            case EASE_IN_OUT:
                return t < 0.5f ? 2 * t * t : 1 - 2 * (1 - t) * (1 - t);
            case BOUNCE:
                if (t < 1/2.75f) return 7.5625f * t * t;
                else if (t < 2/2.75f)
                {
                    t -= 1.5f/2.75f;
                    return 7.5625f * t * t + 0.75f;
                }
                else if (t < 2.5f/2.75f)
                {
                    t -= 2.25f/2.75f;
                    return 7.5625f * t * t + 0.9375f;
                }
                else
                {
                    t -= 2.625f/2.75f;
                    return 7.5625f * t * t + 0.984375f;
                }
            case ELASTIC:
                if (t == 0) return 0;
                if (t == 1) return 1;
                return (float)(Math.pow(2, -10 * t) * Math.sin((t - 0.075f) * (2 * Math.PI) / 0.3f) + 1);
            case BACK:
                float c1 = 1.70158f;
                float c3 = c1 + 1;
                return c3 * t * t * t - c1 * t * t;
            case STAR_TWINKLE:
                return (float)(0.5f + 0.5f * Math.sin(t * Math.PI * 8));
            case STAR_EXPAND:
                return (float)(1.0f + 0.3f * Math.sin(t * Math.PI * 4));
            case STAR_MOVEMENT:
                return (float)(t + 0.1f * Math.sin(t * Math.PI * 6));
            case HEART_BEAT:
                return (float)(1.0f + 0.2f * Math.sin(t * Math.PI * 10));
            case BREATH:
                return (float)(0.8f + 0.2f * Math.sin(t * Math.PI * 2));
            case MAGIC_PULSE:
                return (float)(1.0f + 0.5f * Math.sin(t * Math.PI * 3) * Math.exp(-t * 2));
            default:
                return t;
        }
    }
}