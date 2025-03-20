package com.gmail.jaboll.mc.mixin.client;


import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBuffers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Minecraft.class)
public interface MinecraftMixin {
    @Accessor
    RenderBuffers getRenderBuffers();
}

