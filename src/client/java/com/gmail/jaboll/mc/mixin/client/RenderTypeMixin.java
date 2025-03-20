package com.gmail.jaboll.mc.mixin.client;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.world.item.armortrim.ArmorTrim;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import static com.gmail.jaboll.mc.SmithingGlintClient.getCustomType;

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
        String materialKey = armorTrimThreadLocal.get().material().value().assetName();
        return getCustomType(materialKey);
    }

}