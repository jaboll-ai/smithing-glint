package com.gmail.jaboll.mc.mixin.client;

import com.gmail.jaboll.mc.SmithingGlint;
import com.gmail.jaboll.mc.SmithingGlintClient;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.armortrim.TrimMaterial;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import static com.gmail.jaboll.mc.SmithingGlint.MOD_ID;
import static com.gmail.jaboll.mc.SmithingGlintClient.*;
import static com.gmail.jaboll.mc.compat.RuntimeTrimCompat.getPallete;

@Mixin(HumanoidArmorLayer.class)
public class RenderTypeMixin {

	@Unique ThreadLocal<ArmorTrim> armorTrimThreadLocal = new ThreadLocal<>();

    @ModifyVariable(method = "renderArmorPiece(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;ILnet/minecraft/client/model/HumanoidModel;)V",
			at = @At("STORE"), ordinal = 0)
    private ArmorTrim storeArmorTrim(ArmorTrim value){
		armorTrimThreadLocal.set(value);
		return value;
	}

    @ModifyArg(method = "renderGlint", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/MultiBufferSource;getBuffer(Lnet/minecraft/client/renderer/RenderType;)Lcom/mojang/blaze3d/vertex/VertexConsumer;"), index = 0)
    private RenderType modifyRenderType(RenderType value) {
        if (armorTrimThreadLocal.get() == null) return value;
        TrimMaterial material = armorTrimThreadLocal.get().material().value();
        if (material.assetName().equals("dynamic") && runtimeTrimsLoaded) {
            Item item = material.ingredient().value();
            int[] colors = getPallete(item);
            getOrGenerateType(Minecraft.getInstance().getResourceManager(), ResourceLocation.fromNamespaceAndPath(MOD_ID, item.getDescriptionId()), colors, true);
            return getCustomType(item.getDescriptionId());
        }
        return getCustomType(material.assetName());
    }


}