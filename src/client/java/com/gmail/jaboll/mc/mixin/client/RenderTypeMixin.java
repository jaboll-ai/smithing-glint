package com.gmail.jaboll.mc.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import static com.gmail.jaboll.mc.SmithingGlintClient.getCustomType;

@Mixin(EquipmentLayerRenderer.class)
public class RenderTypeMixin {

    @ModifyVariable(method = "renderLayers(Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/client/model/Model;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/resources/ResourceLocation;)V",
            at = @At("STORE"), ordinal = 0)
    private VertexConsumer modifyVertexConsumer(VertexConsumer value,
		EquipmentClientInfo.LayerType layerType,
		ResourceKey<EquipmentAsset> resourceKey,
		Model model,
		ItemStack itemStack,
		PoseStack poseStack,
		MultiBufferSource multiBufferSource,
		int i,
		@Nullable ResourceLocation resourceLocation,
		@Local(ordinal = 1) ResourceLocation resourceLocation2,
		@Local boolean bl
	) {
		RenderType renderType = RenderType.armorCutoutNoCull(resourceLocation2);
		ArmorTrim armorTrim = itemStack.get(DataComponents.TRIM);
		if (!bl){
			return multiBufferSource.getBuffer(renderType);
		}
		if (armorTrim != null) {
			String materialKey = armorTrim.material().value().assetName();


			return VertexMultiConsumer.create(multiBufferSource.getBuffer(getCustomType(materialKey)), multiBufferSource.getBuffer(renderType));
		}
        return VertexMultiConsumer.create(multiBufferSource.getBuffer(RenderType.armorEntityGlint()), multiBufferSource.getBuffer(renderType));
    }
}