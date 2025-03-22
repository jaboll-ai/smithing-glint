package com.gmail.jaboll.mc;

import com.gmail.jaboll.mc.mixin.client.BufferMixins;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.ARGB;
import net.minecraft.util.TriState;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.SequencedMap;

import static com.gmail.jaboll.mc.SmithingGlint.LOGGER;
import static com.gmail.jaboll.mc.SmithingGlint.MOD_ID;
import static net.minecraft.client.renderer.RenderStateShard.*;

public class SmithingGlintClient implements ClientModInitializer {

	private static NativeImage enchantmentGlintImage = null;
	public static boolean runtimeTrimsLoaded;
	private static SequencedMap<RenderType, ByteBufferBuilder> buffersMap = null;
	private static final Map<String, RenderType> CUSTOM_TYPES = new HashMap<>();

	@Override
	public void onInitializeClient() {
		if (FabricLoader.getInstance().isModLoaded("runtimetrims")){
			runtimeTrimsLoaded = true;
		}
	}

	private static void generateCustomType(ResourceManager resourceManager, String material, int[] pixels){
		if (enchantmentGlintImage == null) {
			try {
				InputStream inputStream = resourceManager.open(ResourceLocation.withDefaultNamespace("textures/misc/enchanted_glint_entity.png"));
				enchantmentGlintImage = NativeImage.read(inputStream);
			} catch (IOException e) {
				LOGGER.error("Failed to load glint texture: ", e);
				enchantmentGlintImage = new NativeImage(16, 16, false);
			}
		}
		NativeImage tintedImage = applyTintMultiply(pixels[0]);
		try {tintedImage.writeToFile(Paths.get("glints/"+material+".png"));} catch (IOException ignored) {}
		TextureStateShard shard = createTextureStateFromNativeImage(tintedImage, material);
		RenderType type = createArmorRenderType(material, shard);
		registerRenderType(type);
		CUSTOM_TYPES.put(material, type);
	}

	public static RenderType getOrGenerateType(ResourceManager resourceManager, ResourceLocation palette, int[] pixels) {
		String[] path = palette.getPath().split("/");
		String material = path[path.length-1];
		if (CUSTOM_TYPES.containsKey(material)){
			return getCustomType(material);
		}
		generateCustomType(resourceManager, material, pixels);
		return getCustomType(material);
	}

	public static RenderType getCustomType(String key){
		return CUSTOM_TYPES.getOrDefault(key, RenderType.armorEntityGlint());
	}

	public static TextureStateShard createTextureStateFromNativeImage(NativeImage image, String assetName) {
		DynamicTexture dynamicTexture = new DynamicTexture(image);
		ResourceLocation textureID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "dynamic/"+assetName);
		Minecraft.getInstance().getTextureManager().register(textureID, dynamicTexture);

		return new TextureStateShard(textureID, TriState.DEFAULT, false);
	}

	private static RenderType createArmorRenderType(String name, TextureStateShard textureStateShard){
		return RenderType.create(
				name+"_armor_entity_glint",
				DefaultVertexFormat.POSITION_TEX,
				VertexFormat.Mode.QUADS,
				1536,
				RenderType.CompositeState.builder()
						.setShaderState(RENDERTYPE_ARMOR_ENTITY_GLINT_SHADER)
						.setTextureState(textureStateShard)
						.setWriteMaskState(COLOR_WRITE)
						.setCullState(NO_CULL)
						.setDepthTestState(EQUAL_DEPTH_TEST)
						.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
						.setTexturingState(ENTITY_GLINT_TEXTURING)
						.setLayeringState(VIEW_OFFSET_Z_LAYERING)
						.createCompositeState(false)
		);
	}

	public static void registerRenderType(RenderType renderType){
		if (buffersMap == null){
			MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
			buffersMap = ((BufferMixins) bufferSource).getFixedBuffers();
		}
		if (!buffersMap.containsKey(renderType)){
			buffersMap.put(renderType, new ByteBufferBuilder(renderType.bufferSize()));
		}
	}

	public static NativeImage applyTintMultiply(int mainColor){
		NativeImage tintedImage = new NativeImage(enchantmentGlintImage.getWidth(), enchantmentGlintImage.getHeight(), false);
		int min = 255;
		int max = 0;
		float maxV = 0;
		for (int x = 0; x < enchantmentGlintImage.getWidth(); x++) {
			for (int y = 0; y < enchantmentGlintImage.getHeight(); y++) {
				int pixel = enchantmentGlintImage.getPixel(x, y);
				int greyVal = Math.round(ARGB.red(pixel) * 0.3F + ARGB.green(pixel) * 0.59F + ARGB.blue(pixel) * 0.11F);
				int greyscaled = ARGB.color(greyVal, greyVal, greyVal);
				int tintedColor = ARGB.multiply(greyscaled, mainColor);
				float[] hsv = Color.RGBtoHSB(ARGB.red(tintedColor), ARGB.green(tintedColor), ARGB.blue(tintedColor), null);
				int saturated = Color.HSBtoRGB(hsv[0], hsv[1] > 0.15f ? 1 : hsv[1], hsv[2]);
				int finalColor = ARGB.color(greyVal, saturated);
				tintedImage.setPixel(x, y, finalColor);
				min = Math.min(min, greyVal);
				max = Math.max(max, greyVal);
				maxV = Math.max(hsv[2], maxV);
			}
		}
		for (int x = 0; x < enchantmentGlintImage.getWidth(); x++) {
			for (int y = 0; y < enchantmentGlintImage.getHeight(); y++) {
				int color = tintedImage.getPixel(x, y);
				int limit = maxV > 0.15 ? 100 : 220;
				int newAlpha = Math.round((ARGB.alpha(color) - min) / (max - min + 0.5F) * (limit - 26) + 26);
				tintedImage.setPixel(x, y, ARGB.color(newAlpha, color));
			}
		}
		return tintedImage;
	}
}