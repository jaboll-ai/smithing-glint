package com.gmail.jaboll.mc;

import com.gmail.jaboll.mc.mixin.client.BufferMixins;
import com.gmail.jaboll.mc.mixin.client.MinecraftMixin;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.event.registry.DynamicRegistrySetupCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.ARGB;
import net.minecraft.util.TriState;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.SequencedMap;

import static com.gmail.jaboll.mc.SmithingGlint.LOGGER;
import static com.gmail.jaboll.mc.SmithingGlint.MOD_ID;
import static net.minecraft.client.renderer.RenderStateShard.*;

public class SmithingGlintClient implements ClientModInitializer {

	private static Map<String, ResourceLocation> permutations;
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
				.setTransparencyState(GLINT_TRANSPARENCY)
				.setTexturingState(ENTITY_GLINT_TEXTURING)
				.setLayeringState(VIEW_OFFSET_Z_LAYERING)
				.createCompositeState(false)
		);
	}

	private static Map<String, RenderType> CUSTOM_TYPES = new HashMap<String, RenderType>();

	public static RenderType getCustomType(String key){
		return CUSTOM_TYPES.getOrDefault(key, RenderType.armorEntityGlint());
	}

	public static void registerAllRenderTypes(){
		if (CUSTOM_TYPES.isEmpty()) {
			logic();
		}
		MultiBufferSource.BufferSource bufferSource = ((MinecraftMixin)Minecraft.getInstance()).getRenderBuffers().bufferSource();
		SequencedMap<RenderType, ByteBufferBuilder> map = ((BufferMixins) bufferSource).getFixedBuffers();
		for (Map.Entry<String, RenderType> entry : CUSTOM_TYPES.entrySet()) {
			if (!map.containsKey(entry.getValue())){
				map.put(entry.getValue(), new ByteBufferBuilder(entry.getValue().bufferSize()));
			}
		}
	}

	public static ResourceLocation getPermutation(String key) {
		return permutations.get(key);
	}

	public static void setPermutations(Map<String, ResourceLocation> map){
		permutations = map;
	}

	public static NativeImage applyTintMultiply(NativeImage image, int mainColor){
		NativeImage tintedImage = new NativeImage(image.getWidth(), image.getHeight(), false);
		for (int x = 0; x < image.getWidth(); x++) {
			for (int y = 0; y < image.getHeight(); y++) {
				int greyscaled = ARGB.greyscale(image.getPixel(x, y));
				int tintedColor = ARGB.multiply(greyscaled, mainColor);
				float[] hsv = Color.RGBtoHSB(ARGB.red(tintedColor), ARGB.green(tintedColor), ARGB.blue(tintedColor), null);
				int finalColor = ARGB.opaque(Color.HSBtoRGB(hsv[0], hsv[1] > 0.15f ? 1 : hsv[1], Math.min(hsv[2]*2.3f, 1)));
				tintedImage.setPixel(x, y, finalColor);
			}
		}
		return tintedImage;
	}

	public static void logic(){
		ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
		int mostSaturated = 0xFF612C9E;
		float maxSaturation = 0;
		NativeImage nativeImageGlint = null;
		try {
			InputStream defaultGlint = resourceManager.open(ResourceLocation.withDefaultNamespace("textures/misc/enchanted_glint_entity.png"));
			nativeImageGlint = NativeImage.read(defaultGlint);
			for (int pixel : nativeImageGlint.getPixels()) {
				float[] hsb = Color.RGBtoHSB(ARGB.red(pixel), ARGB.green(pixel), ARGB.blue(pixel), null);
				if (hsb[1] > maxSaturation) {
					maxSaturation = hsb[1];
					mostSaturated = pixel;
				}
			}
		} catch (IOException ex) {
			LOGGER.error("Something went wrong: ", ex);
		}
		for (Map.Entry<String, ResourceLocation> entry : permutations.entrySet()) {
			String material = entry.getKey();
			ResourceLocation resourceLocation = entry.getValue();
			int[] pixels = {};
			try {
				InputStream inputStream = resourceManager.open(ResourceLocation.fromNamespaceAndPath(resourceLocation.getNamespace(), resourceLocation.getPath()+".png"));
				pixels = NativeImage.read(inputStream).getPixels();
			} catch (IOException e) {
				try {
					InputStream inputStream = resourceManager.open(ResourceLocation.fromNamespaceAndPath(resourceLocation.getNamespace(), "textures/"+resourceLocation.getPath()+".png"));
					pixels = NativeImage.read(inputStream).getPixels();
				} catch (IOException ex) {
					LOGGER.error("Something went wrong: ", ex);
				}
			}
			if (pixels.length > 0){
//				float calculatedShift = computeHueShift(mostSaturated, pixels[0]);
				if(nativeImageGlint != null) {
//					NativeImage shiftedImage = applyHueShift(nativeImageGlint, calculatedShift);
					NativeImage tintedImage = applyTintMultiply(nativeImageGlint, pixels[0]);
					int avgColor = (int) Arrays.stream(tintedImage.getPixels()).average().orElse(0);
					float[] avgHSV = Color.RGBtoHSB(ARGB.red(avgColor), ARGB.green(avgColor), ARGB.blue(avgColor), null);
					System.out.println(material+ " brightness " +avgHSV[2]);
					TextureStateShard shard = TextureHelper.createTextureStateFromNativeImage(tintedImage, material);
					RenderType type = createArmorRenderType(material, shard);
					CUSTOM_TYPES.put(material, type);
				}

			}
		};
	}

	@Override
	public void onInitializeClient() {
		DynamicRegistrySetupCallback.EVENT.register((a) -> {
			registerAllRenderTypes();
		});
	}

	public static class TextureHelper {
		public static TextureStateShard createTextureStateFromNativeImage(NativeImage image, String assetName) {
//            try {
//                image.writeToFile(Paths.get("C:\\Users\\janni\\AppData\\Local\\Temp\\devmc\\"+assetName+".png"));
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
            TextureManager textureManager = Minecraft.getInstance().getTextureManager();
			DynamicTexture dynamicTexture = new DynamicTexture(image);
			ResourceLocation textureID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "dynamic/"+assetName);
			textureManager.register(textureID, dynamicTexture);

			return new TextureStateShard(textureID, TriState.DEFAULT, false);
		}
	}
}