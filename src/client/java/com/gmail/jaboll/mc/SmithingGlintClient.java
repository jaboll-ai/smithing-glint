package com.gmail.jaboll.mc;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.TriState;

import java.util.Map;

import static com.gmail.jaboll.mc.SmithingGlint.MOD_ID;
import static net.minecraft.client.renderer.RenderStateShard.*;

public class SmithingGlintClient implements ClientModInitializer {

	private static RenderType createArmorRenderType(String name, String location){
		//"textures/misc/enchanted_glint_entity.png"
		return RenderType.create(
			name,
			DefaultVertexFormat.POSITION_TEX,
			VertexFormat.Mode.QUADS,
			1536,
			RenderType.CompositeState.builder()
				.setShaderState(RENDERTYPE_ARMOR_ENTITY_GLINT_SHADER)
				.setTextureState(new RenderStateShard.TextureStateShard(ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/misc/"+location+".png"), TriState.DEFAULT, false))
				.setWriteMaskState(COLOR_WRITE)
				.setCullState(NO_CULL)
				.setDepthTestState(EQUAL_DEPTH_TEST)
				.setTransparencyState(GLINT_TRANSPARENCY)
				.setTexturingState(ENTITY_GLINT_TEXTURING)
				.setLayeringState(VIEW_OFFSET_Z_LAYERING)
				.createCompositeState(false)
		);
	}

	private static final Map<String, RenderType> CUSTOM_TYPES = Map.ofEntries(
			Map.entry("amethyst", createArmorRenderType("amethyst_armor_entity_glint", "amethyst")),
			Map.entry("copper", createArmorRenderType("copper_armor_entity_glint", "copper")),
			Map.entry("diamond", createArmorRenderType("diamond_armor_entity_glint", "diamond")),
			Map.entry("emerald", createArmorRenderType("emerald_armor_entity_glint", "emerald")),
			Map.entry("gold", createArmorRenderType("gold_armor_entity_glint", "gold")),
			Map.entry("iron", createArmorRenderType("iron_armor_entity_glint", "iron")),
			Map.entry("lapis", createArmorRenderType("lapis_armor_entity_glint", "lapis")),
			Map.entry("quartz", createArmorRenderType("quartz_armor_entity_glint", "quartz")),
			Map.entry("netherite", createArmorRenderType("netherite_armor_entity_glint", "netherite")),
			Map.entry("redstone", createArmorRenderType("redstone_armor_entity_glint", "redstone")),
			Map.entry("resin", createArmorRenderType("resin_armor_entity_glint", "resin"))
	);

	public static RenderType getCustomType(String key){
		return CUSTOM_TYPES.getOrDefault(key, RenderType.armorEntityGlint());
	}

	public static void registerAllRenderTypes(Object2ObjectLinkedOpenHashMap<RenderType, ByteBufferBuilder> mapBuilders){
		for (Map.Entry<String, RenderType> entry : CUSTOM_TYPES.entrySet()) {
			if (!mapBuilders.containsKey(entry.getValue())){
				mapBuilders.put(entry.getValue(), new ByteBufferBuilder(entry.getValue().bufferSize()));
			}
		}
	}

	@Override
	public void onInitializeClient() {

	}
}