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
import java.util.HashMap;
import java.util.Map;
import java.util.SequencedMap;

import static com.gmail.jaboll.mc.SmithingGlint.LOGGER;
import static com.gmail.jaboll.mc.SmithingGlint.MOD_ID;
import static net.minecraft.client.renderer.RenderStateShard.*;

public class SmithingGlintClient implements ClientModInitializer {

	public static boolean modEnabled = true;
	public static boolean runtimeTrimsLoaded;
	private static NativeImage enchantmentGlintImage = null;
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
				modEnabled = false;
				enchantmentGlintImage = new NativeImage(16, 16, false);
			}
		}
		NativeImage tintedImage = applyTintMultiply(pixels[1]);
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
//		float[] hsv = Color.RGBtoHSB(ARGB.red(mainColor), ARGB.green(mainColor), ARGB.blue(mainColor), null);
		int mainLuminance = Math.round(ARGB.red(mainColor) * 0.299F + ARGB.green(mainColor) * 0.587F + ARGB.blue(mainColor) * 0.0722F);
		for (int x = 0; x < enchantmentGlintImage.getWidth(); x++) {
			for (int y = 0; y < enchantmentGlintImage.getHeight(); y++) {
				int pixel = enchantmentGlintImage.getPixel(x, y);
				int luminance = Math.round(ARGB.red(pixel) * 0.299F + ARGB.green(pixel) * 0.587F + ARGB.blue(pixel) * 0.0722F);
				int diffrence = Math.round((127-mainLuminance)/255f * luminance);
				int finalColor = ARGB.color(Math.min(255, luminance+diffrence+26), mainColor); //26 is the 10% transparency threshold of 255
				tintedImage.setPixel(x, y, finalColor);
			}
		}
//		try {tintedImage.writeToFile(Paths.get("glints/"+material+"_final.png"));} catch (IOException ignored) {}
		return tintedImage;
	}
}