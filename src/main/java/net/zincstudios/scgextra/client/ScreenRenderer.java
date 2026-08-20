package net.zincstudios.scgextra.client;

import net.zincstudios.scgextra.SCGExtra;
import net.zincstudios.scgextra.effects.ModEffects;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SCGExtra.MOD_ID, value = Dist.CLIENT)
public class ScreenRenderer {
    private static float alpha = 1;
    private static final ResourceLocation INK =
            SCGExtra.asResource("textures/misc/ink_splat.png");
    private static final ResourceLocation INK_GLOWING =
            SCGExtra.asResource("textures/misc/ink_splat_glowing.png");
    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        if (!mc.player.hasEffect(ModEffects.INK_EFFECT.get()) && !mc.player.hasEffect(ModEffects.GLOWING_INK_EFFECT.get())){ alpha = 1; return;}
        GuiGraphics g = event.getGuiGraphics();

        int screenW = event.getWindow().getGuiScaledWidth();
        int screenH = event.getWindow().getGuiScaledHeight();

        // int imgW = 512;
        // int imgH = 512;

        // int x = (screenW - imgW) / 2;
        // int y = (screenH - imgH) / 2;

        if(mc.player.hasEffect(ModEffects.INK_EFFECT.get())){
            if(mc.player.getEffect(ModEffects.INK_EFFECT.get()).getDuration() <= 20){
                alpha = Math.min(1f, alpha - 0.01f);
            }
        }else{
            if(mc.player.getEffect(ModEffects.GLOWING_INK_EFFECT.get()).getDuration() <= 20){
                alpha = Math.min(1f, alpha - 0.01f);
            }
        }

        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1f, 1f, 1f, alpha);

        if(mc.player.hasEffect(ModEffects.INK_EFFECT.get())){
            g.blit(
                INK,
                0,
                0,
                0,
                0,
                screenW,
                screenH,
                screenW,
                screenH
            );
        }else{
            g.blit(
                INK_GLOWING,
                0,
                0,
                0,
                0,
                screenW,
                screenH,
                screenW,
                screenH
            );
        }

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        RenderSystem.disableBlend();
    }
}
