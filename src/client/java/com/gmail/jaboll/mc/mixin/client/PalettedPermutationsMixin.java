package com.gmail.jaboll.mc.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.atlas.sources.PalettedPermutations;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.gmail.jaboll.mc.SmithingGlintClient.getOrGenerateType;


@Mixin(PalettedPermutations.class)
public class PalettedPermutationsMixin {
    @Inject(method = "loadPaletteEntryFromImage", at = @At("RETURN"))
    private static void genGlint(ResourceManager resourceManager, ResourceLocation palette, CallbackInfoReturnable<int[]> cir){
        Minecraft.getInstance().execute(()->getOrGenerateType(resourceManager, palette, cir.getReturnValue(), false)); //Dispatch to render thread
    }
}
