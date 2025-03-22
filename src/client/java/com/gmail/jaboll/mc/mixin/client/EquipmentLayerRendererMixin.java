package com.gmail.jaboll.mc.mixin.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.gmail.jaboll.mc.SmithingGlint.MOD_ID;
import static com.gmail.jaboll.mc.SmithingGlintClient.*;
import static com.gmail.jaboll.mc.SmithingGlintClient.getCustomType;
import static com.gmail.jaboll.mc.compat.RuntimeTrimCompat.getPallete;

@Mixin(EquipmentLayerRenderer.class)
public class EquipmentLayerRendererMixin {

    @Unique
    private static final ThreadLocal<ArmorTrim> armorTrimThreadLocal = new ThreadLocal<>();

    @Inject(method = "renderLayers(Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/client/model/Model;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/resources/ResourceLocation;)V",
            at = @At("HEAD"))
    private void storeArmorTrim(EquipmentClientInfo.LayerType layerType, ResourceKey<EquipmentAsset> equipmentAsset, Model armorModel, ItemStack item, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, @Nullable ResourceLocation playerTexture, CallbackInfo ci) {
        ArmorTrim armorTrim = item.get(DataComponents.TRIM);
        armorTrimThreadLocal.set(armorTrim);
    }

    @WrapOperation(method = "renderLayers(Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/client/model/Model;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/resources/ResourceLocation;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;getArmorFoilBuffer(Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/renderer/RenderType;Z)Lcom/mojang/blaze3d/vertex/VertexConsumer;"))
    private VertexConsumer modifyRenderType(MultiBufferSource bufferSource, RenderType renderType, boolean hasFoil, Operation<VertexConsumer> original) {
        if (armorTrimThreadLocal.get() == null || !hasFoil) return original.call(bufferSource, renderType, hasFoil);
        TrimMaterial material = armorTrimThreadLocal.get().material().value();
        if (material.assetName().equals("dynamic") && runtimeTrimsLoaded) {
            Item item = material.ingredient().value();
            int[] colors = getPallete(item);
            RenderType dynamicType = getOrGenerateType(Minecraft.getInstance().getResourceManager(), ResourceLocation.fromNamespaceAndPath(MOD_ID, item.getDescriptionId()), colors);
            return VertexMultiConsumer.create(bufferSource.getBuffer(dynamicType), bufferSource.getBuffer(renderType));

        }
        return VertexMultiConsumer.create(bufferSource.getBuffer(getCustomType(material.assetName())), bufferSource.getBuffer(renderType));
    }

}
