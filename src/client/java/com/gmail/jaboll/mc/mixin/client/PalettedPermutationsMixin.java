package com.gmail.jaboll.mc.mixin.client;

import net.minecraft.client.renderer.texture.atlas.sources.PalettedPermutations;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

import static com.gmail.jaboll.mc.SmithingGlintClient.setPermutations;


@Mixin(PalettedPermutations.class)
public class PalettedPermutationsMixin {
    @Inject(method = "<init>", at = @At("RETURN"))
    private void grabPermutations(List textures, ResourceLocation paletteKey, Map<String, ResourceLocation> permutations, CallbackInfo ci){
        setPermutations(permutations);
    }
}
