package miku.bai_ze_li.genesis.api.render.tooltip;

import miku.bai_ze_li.genesis.GenesisLib;
import miku.bai_ze_li.genesis.api.annotation.GenesisTooltipParticles;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = GenesisLib.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class TooltipParticleHandler
{
    private static int tickCounter = 0;
    private static ItemStack lastTooltipItem = ItemStack.EMPTY;
    private static int tooltipX = 0, tooltipY = 0, tooltipWidth = 0, tooltipHeight = 0;
    private static long lastFrameTime = System.nanoTime();

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event)
    {
        if (event.phase == TickEvent.Phase.END) tickCounter++;
    }

    @SubscribeEvent
    public static void onRenderTooltip(RenderTooltipEvent.Pre event)
    {
        ItemStack stack = event.getItemStack();
        TooltipParticleSystem.ParticleConfig config = particleConfig(stack);
        int spawnRate = particleSpawnRate(stack);

        if (config != null)
        {
            tooltipX = 0;
            tooltipY = 0;
            tooltipWidth = event.getScreenWidth();
            tooltipHeight = event.getScreenHeight();
            lastTooltipItem = stack;

            if (shouldSpawnParticles(stack) && tickCounter % spawnRate == 0) {
                TooltipParticleSystem.getInstance().spawnParticlesInTooltip(tooltipX, tooltipY, tooltipWidth, tooltipHeight, config);
            }
        }
    }

    @SubscribeEvent
    public static void onRenderTooltipPost(RenderTooltipEvent event)
    {
        ItemStack stack = event.getItemStack();

        if (hasTooltipParticles(stack))
        {
            long currentTime = System.nanoTime();
            float deltaTime = (currentTime - lastFrameTime) / 1_000_000_000.0f;
            lastFrameTime = currentTime;
            deltaTime = Math.min(deltaTime, 0.05f);

            // 更新粒子
            TooltipParticleSystem.getInstance().update(deltaTime);
            TooltipParticleSystem.getInstance().render(event.getGraphics(), 1.0f);
        }
    }

    private static boolean hasTooltipParticles(ItemStack stack) {
        return particleConfig(stack) != null;
    }

    private static TooltipParticleSystem.ParticleConfig particleConfig(ItemStack stack) {
        GenesisTooltipParticles annotation = stack.getItem().getClass().getAnnotation(GenesisTooltipParticles.class);
        if (annotation != null) {
            return GenesisTooltipParticlePresets.create(annotation.preset());
        }
        if (stack.getItem() instanceof ITooltipParticleItem particleItem) {
            return particleItem.getParticleConfig();
        }
        return null;
    }

    private static boolean shouldSpawnParticles(ItemStack stack) {
        if (stack.getItem().getClass().isAnnotationPresent(GenesisTooltipParticles.class)) {
            return true;
        }
        return !(stack.getItem() instanceof ITooltipParticleItem particleItem) || particleItem.shouldSpawnParticles(stack);
    }

    private static int particleSpawnRate(ItemStack stack) {
        GenesisTooltipParticles annotation = stack.getItem().getClass().getAnnotation(GenesisTooltipParticles.class);
        if (annotation != null) {
            return Math.max(1, annotation.spawnRate());
        }
        if (stack.getItem() instanceof ITooltipParticleItem particleItem) {
            return Math.max(1, particleItem.getParticleSpawnRate());
        }
        return 5;
    }

    @SubscribeEvent
    public static void onRenderGameOverlay(RenderGuiOverlayEvent.Post event)
    {
        if (event.getOverlay() == VanillaGuiOverlay.HOTBAR.type())
        {
            Minecraft mc = Minecraft.getInstance();
            if (mc.screen == null)
            {
                long currentTime = System.nanoTime();
                float deltaTime = (currentTime - lastFrameTime) / 1_000_000_000.0f;
                lastFrameTime = currentTime;
                deltaTime = Math.min(deltaTime, 0.05f);

                // 依旧更新
                TooltipParticleSystem.getInstance().update(deltaTime);
                TooltipParticleSystem.getInstance().render(event.getGuiGraphics(), 1.0f);
            }
        }
    }
}
