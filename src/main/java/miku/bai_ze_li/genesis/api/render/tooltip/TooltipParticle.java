package miku.bai_ze_li.genesis.api.render.tooltip;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class TooltipParticle
{
    public float x, y;
    public float vx, vy;
    public float life; // 当前生命周期 (0-1)
    public float maxLife;
    public float baseSize;
    public float alpha;
    public int baseColor;
    public TooltipParticleSystem.ParticleTextureInfo textureInfo;
    public float rotation;
    public float rotationSpeed;
    public int depthLayer;

    public final TooltipParticleSystem.ParticleConfig config;
    private float age = 0;
    private final float initialX, initialY;
    private final float motionPhase;
    private final float twinklePhase;
    private final float pulsePhase;
    private final int colorIndex;
    private float colorTransition = 0;

    // 序列帧
    private int currentFrame = 0;
    private int frameTimer = 0;

    private final List<Vector2f> trailPoints = new ArrayList<>();
    // 获取当前帧的UV坐标
    public TooltipParticleSystem.UVCoords getCurrentFrameUV() {return textureInfo.getFrameUV(currentFrame);}

    public TooltipParticle(float x, float y, float vx, float vy, float maxLife, float size, int color, TooltipParticleSystem.ParticleTextureInfo textureInfo, float rotationSpeed, float initialRotation, TooltipParticleSystem.ParticleConfig config, int depthLayer)
    {
        this.x = x;
        this.y = y;
        this.initialX = x;
        this.initialY = y;
        this.vx = vx;
        this.vy = vy;
        this.life = 1.0f;
        this.maxLife = maxLife;
        this.baseSize = size;
        this.baseColor = color;
        this.textureInfo = textureInfo;
        this.alpha = config.enableFadeIn ? 0.0f : 1.0f;
        this.rotationSpeed = rotationSpeed;
        this.rotation = initialRotation;
        this.config = config;
        this.depthLayer = depthLayer;
        this.motionPhase = (float)(Math.random() * 2 * Math.PI);
        this.twinklePhase = (float)(Math.random() * 2 * Math.PI);
        this.pulsePhase = (float)(Math.random() * 2 * Math.PI);
        this.colorIndex = (int)(Math.random() * config.colors.length);

        // 随机初始帧
        if (textureInfo.frameCount > 1) this.currentFrame = (int)(Math.random() * textureInfo.frameCount);
    }

    public boolean update(float deltaTime)
    {
        age += deltaTime;

        // 更新序列帧
        updateFrame();

        if (config.enableTrail)
        {
            trailPoints.add(new Vector2f(x, y));
            if (trailPoints.size() > config.trailLength) trailPoints.remove(0);
        }

        // 更新粒子位置
        updatePosition(deltaTime);

        if (rotationSpeed != 0)
        {
            float rotationMultiplier = TooltipParticleSystem.calculateBezierValue(config.rotationCurve, 1.0f - life);
            rotation += rotationSpeed * deltaTime * rotationMultiplier;
            rotation = rotation % 360;
        }

        applyPhysics(deltaTime);

        life -= deltaTime / maxLife;
        if (life <= 0) return false;

        updateAlpha();
        if (config.enableColorGradient || config.enableRainbow) updateColor();

        return true;
    }

    private void updateFrame()
    {
        if (textureInfo.frameCount > 1 && textureInfo.frameInterval > 0)
        {
            frameTimer++;
            if (frameTimer >= textureInfo.frameInterval)
            {
                frameTimer = 0;
                currentFrame = (currentFrame + 1) % textureInfo.frameCount;
            }
        }
    }

    private void updatePosition(float deltaTime)
    {
        float speedMultiplier = TooltipParticleSystem.calculateBezierValue(config.speedCurve, 1.0f - life);

        switch (config.motionType)
        {
            case LINEAR:
                x += vx * deltaTime * speedMultiplier;
                y += vy * deltaTime * speedMultiplier;
                break;

            case SINE_WAVE:
                x += vx * deltaTime * speedMultiplier;
                y += vy * deltaTime * speedMultiplier +
                        (float)Math.sin(age * config.motionFrequency + motionPhase) * config.motionAmplitude * deltaTime;
                break;

            case CIRCULAR:
                float circularAngle = age * config.motionFrequency + motionPhase;
                x = config.centerX + (float)Math.cos(circularAngle) * config.radius;
                y = config.centerY + (float)Math.sin(circularAngle) * config.radius;
                break;

            case FLOATING:
                x += vx * deltaTime * speedMultiplier;
                y += vy * deltaTime * speedMultiplier;
                x += (float)Math.sin(age * config.motionFrequency + motionPhase) * config.motionAmplitude * deltaTime;
                break;

            case SPIRAL:
                float spiralAngle = age * config.motionFrequency + motionPhase;
                float spiralRadius = config.radius * (1.0f - age / maxLife);
                x = initialX + (float)Math.cos(spiralAngle) * spiralRadius;
                y = initialY + (float)Math.sin(spiralAngle) * spiralRadius + vy * age * speedMultiplier;
                break;

            case RANDOM_WALK:
                vx += (float)(Math.random() - 0.5) * 20.0f * deltaTime;
                vy += (float)(Math.random() - 0.5) * 20.0f * deltaTime;
                float maxSpeed = Math.max(config.maxSpeed, 50.0f);
                vx = Math.max(-maxSpeed, Math.min(maxSpeed, vx));
                vy = Math.max(-maxSpeed, Math.min(maxSpeed, vy));
                x += vx * deltaTime * speedMultiplier;
                y += vy * deltaTime * speedMultiplier;
                break;

            case ORBIT:
                float orbitAngle = age * config.motionFrequency + motionPhase;
                x = config.centerX + (float)Math.cos(orbitAngle) * config.radius;
                y = config.centerY + (float)Math.sin(orbitAngle) * config.radius * 0.6f;
                break;

            case RAIN:
            case SNOW:
                x += vx * deltaTime * speedMultiplier;
                y += vy * deltaTime * speedMultiplier;
                // 添加轻微的水平摇摆
                x += (float)Math.sin(age * 3.0f + motionPhase) * 5.0f * deltaTime;
                break;

            case STORM:
                x += vx * deltaTime * speedMultiplier;
                y += vy * deltaTime * speedMultiplier;
                // 添加强烈的水平风力和垂直波动
                x += (float)Math.sin(age * 5.0f + motionPhase) * 15.0f * deltaTime;
                y += (float)Math.cos(age * 3.0f + motionPhase) * 8.0f * deltaTime;
                break;

            case LEAVES:
                x += vx * deltaTime * speedMultiplier;
                y += vy * deltaTime * speedMultiplier;
                // 树叶飘落的复杂运动
                x += (float)Math.sin(age * 2.0f + motionPhase) * 20.0f * deltaTime;
                y += (float)Math.sin(age * 1.5f + motionPhase * 0.7f) * 5.0f * deltaTime;
                break;

            case LEAF_WIND_RIGHT:
                x += vx * deltaTime * speedMultiplier;
                y += vy * deltaTime * speedMultiplier;
                x += (float)Math.sin(age * config.motionFrequency + motionPhase) * config.motionAmplitude * deltaTime;
                y += (float)Math.cos(age * config.motionFrequency * 0.7f + motionPhase) * config.motionAmplitude * 0.45f * deltaTime;
                break;

            case FIRE_SPARKS:
                x += vx * deltaTime * speedMultiplier;
                y += vy * deltaTime * speedMultiplier;
                // 火花效果：随时间减速并添加随机扰动
                vx *= (1.0f - deltaTime * 2.0f);
                vy *= (1.0f - deltaTime * 2.0f);
                x += (float)(Math.random() - 0.5) * 10.0f * deltaTime;
                y += (float)(Math.random() - 0.5) * 10.0f * deltaTime;
                break;

            case MAGIC_SWIRL:
                float swirlAngle = age * config.motionFrequency * 3.0f + motionPhase;
                float swirlRadius = config.radius * (float)Math.sin(age * 2.0f);
                x = initialX + (float)Math.cos(swirlAngle) * swirlRadius;
                y = initialY + (float)Math.sin(swirlAngle) * swirlRadius + vy * age * speedMultiplier;
                break;

            case GALAXY_SPIRAL:
                float galaxyAngle = age * config.motionFrequency + motionPhase;
                float galaxyRadius = config.radius * (float)Math.sqrt(age / maxLife);
                x = config.centerX + (float)Math.cos(galaxyAngle) * galaxyRadius;
                y = config.centerY + (float)Math.sin(galaxyAngle) * galaxyRadius;
                break;

            case PULSE:
                float pulseRadius = config.radius * (age / maxLife);
                float pulseAngle = motionPhase;
                x = initialX + (float)Math.cos(pulseAngle) * pulseRadius;
                y = initialY + (float)Math.sin(pulseAngle) * pulseRadius;
                break;

            case WAVE_RIPPLE:
                float waveRadius = config.radius * (age / maxLife);
                x = initialX + (float)Math.cos(motionPhase) * waveRadius;
                y = initialY + (float)Math.sin(motionPhase) * waveRadius;
                break;

            case TORNADO:
                float tornadoAngle = age * config.motionFrequency * 5.0f + motionPhase;
                float tornadoRadius = config.radius * (1.0f - age / maxLife);
                float tornadoHeight = age * config.maxSpeed * speedMultiplier;
                x = config.centerX + (float)Math.cos(tornadoAngle) * tornadoRadius;
                y = config.centerY - tornadoHeight + (float)Math.sin(tornadoAngle * 0.5f) * 10.0f;
                break;

            case BUTTERFLY:
                float butterflyT = age * config.motionFrequency;
                x = initialX + (float)(Math.sin(butterflyT) * Math.cos(butterflyT)) * config.radius;
                y = initialY + (float)(Math.sin(butterflyT)) * config.radius * 0.5f + vy * age * speedMultiplier;
                break;

            case FIREWORKS:
                if (age < maxLife * 0.3f) {
                    // 上升阶段
                    y += vy * deltaTime * speedMultiplier * 2.0f;
                    x += vx * deltaTime * speedMultiplier * 0.1f;
                } else {
                    // 爆炸阶段
                    x += vx * deltaTime * speedMultiplier;
                    y += vy * deltaTime * speedMultiplier;
                }
                break;

            default:
                x += vx * deltaTime * speedMultiplier;
                y += vy * deltaTime * speedMultiplier;
                break;
        }
    }

    private void applyPhysics(float deltaTime) {
        // 重力
        if (config.enableGravity) vy += config.gravityStrength * deltaTime;

        // 风力
        if (config.enableWind)
        {
            vx += config.windStrengthX * deltaTime;
            vy += config.windStrengthY * deltaTime;
        }

        // 空气阻力
        if (config.airResistance > 0)
        {
            vx *= (1.0f - config.airResistance * deltaTime);
            vy *= (1.0f - config.airResistance * deltaTime);
        }

//        if (config.bounciness > 0) {
//        }
    }

    private void updateAlpha()
    {
        float lifeRatio = life;

        // 淡入效果
        if (config.enableFadeIn && age < config.fadeInDuration) alpha = age / config.fadeInDuration;
        else alpha = 1.0f;

        // 淡出效果
        if (config.enableFadeOut && lifeRatio < config.fadeOutDuration) alpha *= lifeRatio / config.fadeOutDuration;

        // 闪烁效果
        if (config.enableTwinkling)
        {
            float twinkle = (float)(0.5f + 0.5f * Math.sin(age * config.twinkleSpeed + twinklePhase));
            alpha *= (1.0f - config.twinkleIntensity) + config.twinkleIntensity * twinkle;
        }

        // 贝塞尔曲线
        alpha *= TooltipParticleSystem.calculateBezierValue(config.alphaCurve, 1.0f - life);
    }

    private void updateColor()
    {
        if (config.enableRainbow)
        {
            float hue = (float) ((age * config.rainbowSpeed + motionPhase) % (2 * Math.PI));
            baseColor = Color.HSBtoRGB(hue / (2 * (float)Math.PI), 1.0f, 1.0f) | 0xFF000000;
        }
        else if (config.enableColorGradient && config.colors.length > 1)
        {
            colorTransition += config.colorTransitionSpeed * 0.05f; // deltaTime的近似值
            if (colorTransition >= 1.0f) colorTransition = 0.0f;

            int nextIndex = (colorIndex + 1) % config.colors.length;

            int currentColor = config.colors[colorIndex];
            int nextColor = config.colors[nextIndex];

            // 线性插值颜色
            float r1 = ((currentColor >> 16) & 0xFF) / 255.0f;
            float g1 = ((currentColor >> 8) & 0xFF) / 255.0f;
            float b1 = (currentColor & 0xFF) / 255.0f;

            float r2 = ((nextColor >> 16) & 0xFF) / 255.0f;
            float g2 = ((nextColor >> 8) & 0xFF) / 255.0f;
            float b2 = (nextColor & 0xFF) / 255.0f;

            float r = r1 + (r2 - r1) * colorTransition;
            float g = g1 + (g2 - g1) * colorTransition;
            float b = b1 + (b2 - b1) * colorTransition;

            baseColor = (0xFF << 24) | ((int)(r * 255) << 16) | ((int)(g * 255) << 8) | (int)(b * 255);
        }
    }

    public float getRenderX(float partialTicks) {return x;}
    public float getRenderY(float partialTicks) {return y;}

    public int getCurrentColor() {return baseColor;}
    public float getCurrentAlpha() {return alpha;}

    public float getCurrentSize()
    {
        float size = baseSize;

        if (config.enablePulsing)
        {
            float pulse = (float)(1.0f + config.pulseScale * Math.sin(age * config.pulseSpeed + pulsePhase));
            size *= pulse;
        }

        // 贝塞尔曲线
        //size *= TooltipParticleSystem.calculateBezierValue(config.sizeCurve, 1.0f - life);

        return size;
    }

    public static class Vector2f
    {
        public float x, y;

        public Vector2f(float x, float y)
        {
            this.x = x;
            this.y = y;
        }
    }
}