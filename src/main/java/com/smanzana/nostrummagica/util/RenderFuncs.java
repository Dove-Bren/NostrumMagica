package com.smanzana.nostrummagica.util;

import java.util.List;
import java.util.Random;
import java.util.function.Function;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import com.smanzana.nostrummagica.util.Curves.ICurve3d;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.EmptyModelData;

@OnlyIn(Dist.CLIENT)
public final class RenderFuncs {
	
	public static final Random RenderRandom(Random existing) {
		existing.setSeed(42); // Copied from Vanilla
		return existing;
	}
	
	public static final Random RenderRandom() {
		return RenderRandom(new Random());
	}
	
	public static void RenderModelWithColorNoBatch(PoseStack stack, BakedModel model, int color, int combinedLight, int combinedOverlay) {
		Tesselator tessellator = Tesselator.getInstance();
		BufferBuilder buffer = tessellator.getBuilder();
		buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK); // quads hardcode this internally. If not, would need to convert when rendering quad?
		RenderModelWithColor(stack, buffer, model, color, combinedLight, combinedOverlay);
		buffer.end();
		BufferUploader.end(buffer);
	}
	
	public static void RenderModelWithColor(PoseStack stack, VertexConsumer buffer, BakedModel model, int color, int combinedLight, int combinedOverlay) {
		final float colors[] = ColorUtil.ARGBToColor(color);
		RenderModel(stack, buffer, model, combinedLight, combinedOverlay, colors[0], colors[1], colors[2], colors[3]);
	}
	
	private static final Random RenderModelRandom = new Random();
	public static final int BrightPackedLight = 15728880;
	
	public static void RenderModel(PoseStack stack, VertexConsumer buffer, BakedModel model, int combinedLight, int combinedOverlay, float red, float green, float blue, float alpha) {
		RenderModel(stack.last(), buffer, model, combinedLight, combinedOverlay, red, green, blue, alpha);
	}
	
	public static void RenderModel(PoseStack.Pose stackLast, VertexConsumer buffer, BakedModel model, int combinedLight, int combinedOverlay, float red, float green, float blue, float alpha) {
		
		for(Direction side : Direction.values()) {
			List<BakedQuad> quads = model.getQuads(null, side, RenderRandom(RenderModelRandom), EmptyModelData.INSTANCE);
			if(!quads.isEmpty()) 
				for(BakedQuad quad : quads) {
					buffer.putBulkData(stackLast, quad, red, green, blue, alpha, combinedLight, combinedOverlay, true);
//					LightUtil.renderQuadColor(buffer, quad, color);
				}
		}
		List<BakedQuad> quads = model.getQuads(null, null, RenderRandom(RenderModelRandom), EmptyModelData.INSTANCE);
		if(!quads.isEmpty()) {
			for(BakedQuad quad : quads) 
				buffer.putBulkData(stackLast, quad, red, green, blue, alpha, combinedLight, combinedOverlay, true);
				//LightUtil.renderQuadColor(buffer, quad, color);
		}

	}
	
	public static void RenderBlockState(BlockState state, PoseStack stack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
		// Could get model and turn around and call RenderModel() on it
		 Minecraft.getInstance().getBlockRenderer().renderSingleBlock(state, stack, bufferIn, combinedLightIn, combinedOverlayIn, EmptyModelData.INSTANCE);
	}

	public static void RenderWorldItem(ItemStack stack, PoseStack matrix) {
		// light and overlay constants taken from ItemRenderer and GameRenderer
		final int combinedLight = BrightPackedLight;
		final int combinedOverlay = OverlayTexture.NO_OVERLAY;
		
		MultiBufferSource.BufferSource typebuffer = Minecraft.getInstance().renderBuffers().bufferSource();
		RenderWorldItem(stack, matrix, typebuffer, combinedLight, combinedOverlay);
		typebuffer.endBatch();
	}
	
	/**
	 * Renders an item. Basically a wrapper for rendering classes.
	 * Making now because transform type is deprecated but required :P and I'd rather have one warning than a bunch.
	 * @param world
	 * @param stack
	 */
	public static void RenderWorldItem(ItemStack stack, PoseStack matrix, MultiBufferSource typeBuffer, int combinedLight) {
		RenderWorldItem(stack, matrix, typeBuffer, combinedLight, OverlayTexture.NO_OVERLAY);
	}
	
	public static void RenderWorldItem(ItemStack stack, PoseStack matrix, MultiBufferSource typeBuffer, int combinedLight, int combinedOverlay) {
		Minecraft.getInstance().getItemRenderer()
			.renderStatic(stack, TransformType.GROUND, combinedLight, combinedOverlay, matrix, typeBuffer, 0);
	}
	
	public static void RenderGUIItem(ItemStack stack, PoseStack matrixStackIn) {
		final Minecraft mc = Minecraft.getInstance();
		final PoseStack actualStack = RenderSystem.getModelViewStack();
		actualStack.pushPose();
		actualStack.mulPoseMatrix(matrixStackIn.last().pose());
		//RenderSystem.pushMatrix();
		//RenderSystem.multMatrix(matrixStackIn.last().pose());
		mc.getItemRenderer().renderGuiItem(stack, 0, 0);
		actualStack.popPose();
		//RenderSystem.popMatrix();
	}
	
	public static void RenderGUIItem(ItemStack stack, PoseStack matrixStackIn, int x, int y, int z) {
		matrixStackIn.pushPose();
		matrixStackIn.translate(x, y, z);
		RenderGUIItem(stack, matrixStackIn);
		matrixStackIn.popPose();
	}
	
	public static void RenderGUIItem(ItemStack stack, PoseStack matrixStackIn, int x, int y) {
		RenderGUIItem(stack, matrixStackIn, x, y, 0);
	}
	
	// can use blit here: blit(x, y, 0, u, v, width, height, texWidth, texHeight)
	public static void drawModalRectWithCustomSizedTextureImmediate(PoseStack matrixStackIn, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight) {
		Screen.blit(matrixStackIn, x, y, u, v, width, height, textureWidth, textureHeight);
	}
	
	public static void drawModalRectWithCustomSizedTextureImmediate(PoseStack matrixStackIn, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight, float red, float green, float blue, float alpha) {
		// hack for now
		RenderSystem.setShaderColor(red, green, blue, alpha);
		drawModalRectWithCustomSizedTextureImmediate(matrixStackIn, x, y, u, v, width, height, textureWidth, textureHeight);
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
	}
	
	// Different from the above in that this includes scaling on what's drawn
	public static void drawScaledCustomSizeModalRectImmediate(PoseStack matrixStackIn, int x, int y, float u, float v, int uWidth, int vHeight, int width, int height, float tileWidth, float tileHeight) {
		drawScaledCustomSizeModalRectImmediate(matrixStackIn, x, y, u, v, uWidth, vHeight, width, height, tileWidth, tileHeight, 1f, 1f, 1f, 1f);
	}
	
	public static void drawScaledCustomSizeModalRectImmediate(PoseStack matrixStackIn, int x, int y, float u, float v, int uWidth, int vHeight, int width, int height, float tileWidth, float tileHeight, float red, float green, float blue, float alpha) {
		Tesselator tessellator = Tesselator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuilder();
		bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
		
		drawScaledCustomSizeModalRect(matrixStackIn, bufferbuilder, x, y, u, v, uWidth, vHeight, width, height, tileWidth, tileHeight,
				red, green, blue, alpha);

		bufferbuilder.end();
		BufferUploader.end(bufferbuilder);
	}
	
	public static void drawScaledCustomSizeModalRect(PoseStack matrixStackIn, VertexConsumer buffer, int x, int y, float u, float v, int uWidth, int vHeight, int width, int height, float tileWidth, float tileHeight, float red, float green, float blue, float alpha) {
		final int combinedLight = BrightPackedLight;
		final int combinedOverlay = OverlayTexture.NO_OVERLAY;
		drawScaledCustomSizeModalRect(matrixStackIn, buffer, x, y, u, v, uWidth, vHeight, width, height, tileWidth, tileHeight, combinedLight, combinedOverlay, red, green, blue, alpha);
	}
	
	public static void drawScaledCustomSizeModalRect(PoseStack matrixStackIn, VertexConsumer buffer, int x, int y, float u, float v, int uWidth, int vHeight, int width, int height, float tileWidth, float tileHeight, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
		final float f = 1.0F / tileWidth;
		final float f1 = 1.0F / tileHeight;
		final Matrix4f transform = matrixStackIn.last().pose();
		final Matrix3f normal = matrixStackIn.last().normal();
		
		buffer.vertex(transform, x, y + height, 0.0f).color(red, green, blue, alpha).uv(u * f, (v + vHeight) * f1).overlayCoords(packedOverlayIn).uv2(packedLightIn).normal(normal, 0, 0, 1).endVertex();
		buffer.vertex(transform, x + width, y + height, 0.0f).color(red, green, blue, alpha).uv((u + uWidth) * f, (v + vHeight) * f1).overlayCoords(packedOverlayIn).uv2(packedLightIn).normal(normal, 0, 0, 1).endVertex();
		buffer.vertex(transform, x + width, y, 0.0f).color(red, green, blue, alpha).uv((u + uWidth) * f, v * f1).overlayCoords(packedOverlayIn).uv2(packedLightIn).normal(normal, 0, 0, 1).endVertex();
		buffer.vertex(transform, x, y, 0.0f).color(red, green, blue, alpha).uv((u * f), (v * f1)).overlayCoords(packedOverlayIn).uv2(packedLightIn).normal(normal, 0, 0, 1).endVertex();
	}
	
	public static void drawRect(PoseStack stack, int minX, int minY, int maxX, int maxY, int colorARGB) {
		GuiComponent.fill(stack, minX, minY, maxX, maxY, colorARGB);
	}
	
	public static void drawGradientRect(PoseStack stack, int minX, int minY, int maxX, int maxY, int colorTopLeft, int colorTopRight, int colorBottomLeft, int colorBottomRight) {
		final Matrix4f transform = stack.last().pose();
		final float[] colorTR = ColorUtil.ARGBToColor(colorTopRight);
		final float[] colorTL = ColorUtil.ARGBToColor(colorTopLeft);
		final float[] colorBL = ColorUtil.ARGBToColor(colorBottomLeft);
		final float[] colorBR = ColorUtil.ARGBToColor(colorBottomRight);
		
		Tesselator tessellator = Tesselator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuilder();
		bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

		RenderSystem.disableTexture();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		//RenderSystem.shadeModel(GL11.GL_SMOOTH);
		{
			bufferbuilder.vertex(transform, minX, minY, 0).color(colorTL[0], colorTL[1], colorTL[2], colorTL[3]).endVertex();
			bufferbuilder.vertex(transform, minX, maxY, 0).color(colorBL[0], colorBL[1], colorBL[2], colorBL[3]).endVertex();
			bufferbuilder.vertex(transform, maxX, maxY, 0).color(colorBR[0], colorBR[1], colorBR[2], colorBR[3]).endVertex();
			bufferbuilder.vertex(transform, maxX, minY, 0).color(colorTR[0], colorTR[1], colorTR[2], colorTR[3]).endVertex();
		}

		bufferbuilder.end();
		BufferUploader.end(bufferbuilder);
		RenderSystem.disableBlend();
		RenderSystem.enableTexture();
		//RenderSystem.shadeModel(GL11.GL_FLAT);
	}

	public static final void renderSpaceQuad(PoseStack stack, VertexConsumer buffer,
			float radius,
			int combinedLightmapIn, int combinedOverlayIn, float red, float green, float blue, float alpha
			) {
		
		// Copied and adapted from vanilla particle instead of manually drawing a space quad.
		// One big difference is it pushes to global render state the transform first, and in it's render
		// func just draws the little particle.
		// We pass that through instead of using global state.
		Vector3f[] avector3f = new Vector3f[]{new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F)};

		for(int i = 0; i < 4; ++i) {
			Vector3f vector3f = avector3f[i];
			vector3f.mul(radius);
		}
		
		final Matrix4f transform = stack.last().pose();
		final Matrix3f normal = stack.last().normal();

		final float uMin = 0;
		final float uMax = 1;
		final float vMin = 0;
		final float vMax = 1;
		buffer.vertex(transform, avector3f[0].x(), avector3f[0].y(), avector3f[0].z()).color(red, green, blue, alpha).uv(uMax, vMax).overlayCoords(combinedOverlayIn).uv2(combinedLightmapIn).normal(normal, 0, 0, 1).endVertex();
		buffer.vertex(transform, avector3f[1].x(), avector3f[1].y(), avector3f[1].z()).color(red, green, blue, alpha).uv(uMax, vMin).overlayCoords(combinedOverlayIn).uv2(combinedLightmapIn).normal(normal, 0, 0, 1).endVertex();
		buffer.vertex(transform, avector3f[2].x(), avector3f[2].y(), avector3f[2].z()).color(red, green, blue, alpha).uv(uMin, vMin).overlayCoords(combinedOverlayIn).uv2(combinedLightmapIn).normal(normal, 0, 0, 1).endVertex();
		buffer.vertex(transform, avector3f[3].x(), avector3f[3].y(), avector3f[3].z()).color(red, green, blue, alpha).uv(uMin, vMax).overlayCoords(combinedOverlayIn).uv2(combinedLightmapIn).normal(normal, 0, 0, 1).endVertex();
	}
	
	public static final void renderSpaceQuadFacingCamera(PoseStack stack, VertexConsumer buffer, Camera renderInfo,
			float radius,
			int lightmap, int overlay,
			float red, float green, float blue, float alpha) {
		Quaternion rotation = renderInfo.rotation();
		
		stack.pushPose();
		stack.mulPose(rotation);
		
		renderSpaceQuad(stack, buffer,
				radius,
				lightmap, overlay,
				red, green, blue, alpha
				);
		stack.popPose();
	}

	// Note: renders in ENTITY vertex formate
	public static final void drawUnitCube(PoseStack stack, VertexConsumer buffer, int packedLightIn, int combinedOverlayIn, float red, float green, float blue, float alpha) {
		drawUnitCube(stack, buffer, 0, 1, 0, 1, packedLightIn, combinedOverlayIn, red, green, blue, alpha);
	}
	
	public static final void drawUnitCube(PoseStack stack, VertexConsumer buffer, float minU, float maxU, float minV, float maxV, int packedLightIn, int combinedOverlayIn,
			float red, float green, float blue, float alpha) {
		
		final float mind = -.5f;
		final float maxd = .5f;
		
		final float minn = -.5773f;
		final float maxn = .5773f;
		
		final Matrix4f transform = stack.last().pose();
		final Matrix3f normal = stack.last().normal();
		
		// Top
		buffer.vertex(transform, mind, maxd, mind).color(red, green, blue, alpha).uv(minU,minV).overlayCoords(combinedOverlayIn).uv2(packedLightIn).normal(normal, minn, maxn, minn).endVertex();
		buffer.vertex(transform, mind, maxd, maxd).color(red, green, blue, alpha).uv(minU,maxV).overlayCoords(combinedOverlayIn).uv2(packedLightIn).normal(normal, minn, maxn, maxn).endVertex();
		buffer.vertex(transform, maxd, maxd, maxd).color(red, green, blue, alpha).uv(maxU,maxV).overlayCoords(combinedOverlayIn).uv2(packedLightIn).normal(normal, maxn, maxn, maxn).endVertex();
		buffer.vertex(transform, maxd, maxd, mind).color(red, green, blue, alpha).uv(maxU,minV).overlayCoords(combinedOverlayIn).uv2(packedLightIn).normal(normal, maxn, maxn, minn).endVertex();
		
		// North
		buffer.vertex(transform, maxd, maxd, mind).color(red, green, blue, alpha).uv(maxU,minV).overlayCoords(combinedOverlayIn).uv2(packedLightIn).normal(normal, maxn, maxn, minn).endVertex();
		buffer.vertex(transform, maxd, mind, mind).color(red, green, blue, alpha).uv(maxU,maxV).overlayCoords(combinedOverlayIn).uv2(packedLightIn).normal(normal, maxn, minn, minn).endVertex();
		buffer.vertex(transform, mind, mind, mind).color(red, green, blue, alpha).uv(minU,maxV).overlayCoords(combinedOverlayIn).uv2(packedLightIn).normal(normal, minn, minn, minn).endVertex();
		buffer.vertex(transform, mind, maxd, mind).color(red, green, blue, alpha).uv(minU,minV).overlayCoords(combinedOverlayIn).uv2(packedLightIn).normal(normal, minn, maxn, minn).endVertex();
		
		// East
		buffer.vertex(transform, maxd, maxd, maxd).color(red, green, blue, alpha).uv(minU,minV).overlayCoords(combinedOverlayIn).uv2(packedLightIn).normal(normal, maxn, maxn, maxn).endVertex();
		buffer.vertex(transform, maxd, mind, maxd).color(red, green, blue, alpha).uv(minU,maxV).overlayCoords(combinedOverlayIn).uv2(packedLightIn).normal(normal, maxn, minn, maxn).endVertex();
		buffer.vertex(transform, maxd, mind, mind).color(red, green, blue, alpha).uv(maxU,maxV).overlayCoords(combinedOverlayIn).uv2(packedLightIn).normal(normal, maxn, minn, minn).endVertex();
		buffer.vertex(transform, maxd, maxd, mind).color(red, green, blue, alpha).uv(maxU,minV).overlayCoords(combinedOverlayIn).uv2(packedLightIn).normal(normal, maxn, maxn, minn).endVertex();
		
		// South
		buffer.vertex(transform, mind, maxd, maxd).color(red, green, blue, alpha).uv(minU,minV).overlayCoords(combinedOverlayIn).uv2(packedLightIn).normal(normal, minn, maxn, maxn).endVertex();
		buffer.vertex(transform, mind, mind, maxd).color(red, green, blue, alpha).uv(minU,maxV).overlayCoords(combinedOverlayIn).uv2(packedLightIn).normal(normal, minn, minn, maxn).endVertex();
		buffer.vertex(transform, maxd, mind, maxd).color(red, green, blue, alpha).uv(maxU,maxV).overlayCoords(combinedOverlayIn).uv2(packedLightIn).normal(normal, maxn, minn, maxn).endVertex();
		buffer.vertex(transform, maxd, maxd, maxd).color(red, green, blue, alpha).uv(maxU,minV).overlayCoords(combinedOverlayIn).uv2(packedLightIn).normal(normal, maxn, maxn, maxn).endVertex();
		
		// West
		buffer.vertex(transform, mind, maxd, mind).color(red, green, blue, alpha).uv(maxU,minV).overlayCoords(combinedOverlayIn).uv2(packedLightIn).normal(normal, minn, maxn, minn).endVertex();
		buffer.vertex(transform, mind, mind, mind).color(red, green, blue, alpha).uv(maxU,maxV).overlayCoords(combinedOverlayIn).uv2(packedLightIn).normal(normal, minn, minn, minn).endVertex();
		buffer.vertex(transform, mind, mind, maxd).color(red, green, blue, alpha).uv(minU,maxV).overlayCoords(combinedOverlayIn).uv2(packedLightIn).normal(normal, minn, minn, maxn).endVertex();
		buffer.vertex(transform, mind, maxd, maxd).color(red, green, blue, alpha).uv(minU,minV).overlayCoords(combinedOverlayIn).uv2(packedLightIn).normal(normal, minn, maxn, maxn).endVertex();
		
		// Bottom
		buffer.vertex(transform, mind, mind, mind).color(red, green, blue, alpha).uv(maxU,minV).overlayCoords(combinedOverlayIn).uv2(packedLightIn).normal(normal, minn, minn, minn).endVertex();
		buffer.vertex(transform, maxd, mind, mind).color(red, green, blue, alpha).uv(minU,minV).overlayCoords(combinedOverlayIn).uv2(packedLightIn).normal(normal, maxn, minn, minn).endVertex();
		buffer.vertex(transform, maxd, mind, maxd).color(red, green, blue, alpha).uv(minU,maxV).overlayCoords(combinedOverlayIn).uv2(packedLightIn).normal(normal, maxn, minn, maxn).endVertex();
		buffer.vertex(transform, mind, mind, maxd).color(red, green, blue, alpha).uv(maxU,maxV).overlayCoords(combinedOverlayIn).uv2(packedLightIn).normal(normal, minn, minn, maxn).endVertex();
	}
	
	// Assumes render type is LINES
	public static final void drawUnitCubeOutline(PoseStack stack, VertexConsumer buffer, int packedLightIn, int combinedOverlayIn,
			float red, float green, float blue, float alpha) {
		
		// REally tex makes no sense, but will provide it anyways.
		// I guess this means it will be the top left pixel of a texture if a texture is bound and used on this buffer!
		final float minU = 0f;
		final float maxU = 0f;
		final float minV = 0f;
		final float maxV = 0f; 
		
		final float mind = -.5f;
		final float maxd = .5f;
		
		final float minn = -.5773f;
		final float maxn = .5773f;
		
		final Matrix4f transform = stack.last().pose();
		final Matrix3f normal = stack.last().normal();
		
		// Top
		buffer.vertex(transform, mind, maxd, mind).color(red, green, blue, alpha).uv(minU,minV).overlayCoords(combinedOverlayIn).uv2(packedLightIn).normal(normal, minn, maxn, minn).endVertex();
		buffer.vertex(transform, mind, maxd, maxd).color(red, green, blue, alpha).uv(minU,maxV).overlayCoords(combinedOverlayIn).uv2(packedLightIn).normal(normal, minn, maxn, maxn).endVertex();
		// --
		buffer.vertex(transform, mind, maxd, maxd).color(red, green, blue, alpha).uv(minU,maxV).overlayCoords(combinedOverlayIn).uv2(packedLightIn).normal(normal, minn, maxn, maxn).endVertex();
		buffer.vertex(transform, maxd, maxd, maxd).color(red, green, blue, alpha).uv(maxU,maxV).overlayCoords(combinedOverlayIn).uv2(packedLightIn).normal(normal, maxn, maxn, maxn).endVertex();
		// --
		buffer.vertex(transform, maxd, maxd, maxd).color(red, green, blue, alpha).uv(maxU,maxV).overlayCoords(combinedOverlayIn).uv2(packedLightIn).normal(normal, maxn, maxn, maxn).endVertex();
		buffer.vertex(transform, maxd, maxd, mind).color(red, green, blue, alpha).uv(maxU,minV).overlayCoords(combinedOverlayIn).uv2(packedLightIn).normal(normal, maxn, maxn, minn).endVertex();
		// --
		buffer.vertex(transform, maxd, maxd, mind).color(red, green, blue, alpha).uv(maxU,minV).overlayCoords(combinedOverlayIn).uv2(packedLightIn).normal(normal, maxn, maxn, minn).endVertex();
		buffer.vertex(transform, mind, maxd, mind).color(red, green, blue, alpha).uv(minU,minV).overlayCoords(combinedOverlayIn).uv2(packedLightIn).normal(normal, minn, maxn, minn).endVertex();
		
		// North-West
		buffer.vertex(transform, mind, mind, mind).color(red, green, blue, alpha).uv(minU,maxV).overlayCoords(combinedOverlayIn).uv2(packedLightIn).normal(normal, minn, minn, minn).endVertex();
		buffer.vertex(transform, mind, maxd, mind).color(red, green, blue, alpha).uv(minU,minV).overlayCoords(combinedOverlayIn).uv2(packedLightIn).normal(normal, minn, maxn, minn).endVertex();
		
		// North-East
		buffer.vertex(transform, maxd, mind, mind).color(red, green, blue, alpha).uv(minU,minV).overlayCoords(combinedOverlayIn).uv2(packedLightIn).normal(normal, maxn, minn, minn).endVertex();
		buffer.vertex(transform, maxd, maxd, mind).color(red, green, blue, alpha).uv(maxU,minV).overlayCoords(combinedOverlayIn).uv2(packedLightIn).normal(normal, maxn, maxn, minn).endVertex();
		
		// South-West
		buffer.vertex(transform, mind, maxd, maxd).color(red, green, blue, alpha).uv(minU,maxV).overlayCoords(combinedOverlayIn).uv2(packedLightIn).normal(normal, minn, maxn, maxn).endVertex();
		buffer.vertex(transform, mind, mind, maxd).color(red, green, blue, alpha).uv(minU,minV).overlayCoords(combinedOverlayIn).uv2(packedLightIn).normal(normal, minn, minn, maxn).endVertex();
		
		// South-East
		buffer.vertex(transform, maxd, mind, maxd).color(red, green, blue, alpha).uv(maxU,minV).overlayCoords(combinedOverlayIn).uv2(packedLightIn).normal(normal, maxn, minn, maxn).endVertex();
		buffer.vertex(transform, maxd, maxd, maxd).color(red, green, blue, alpha).uv(maxU,maxV).overlayCoords(combinedOverlayIn).uv2(packedLightIn).normal(normal, maxn, maxn, maxn).endVertex();
		
		// Bottom
		buffer.vertex(transform, mind, mind, mind).color(red, green, blue, alpha).uv(maxU,minV).overlayCoords(combinedOverlayIn).uv2(packedLightIn).normal(normal, minn, minn, minn).endVertex();
		buffer.vertex(transform, maxd, mind, mind).color(red, green, blue, alpha).uv(minU,minV).overlayCoords(combinedOverlayIn).uv2(packedLightIn).normal(normal, maxn, minn, minn).endVertex();
		// --
		buffer.vertex(transform, maxd, mind, mind).color(red, green, blue, alpha).uv(minU,minV).overlayCoords(combinedOverlayIn).uv2(packedLightIn).normal(normal, maxn, minn, minn).endVertex();
		buffer.vertex(transform, maxd, mind, maxd).color(red, green, blue, alpha).uv(minU,maxV).overlayCoords(combinedOverlayIn).uv2(packedLightIn).normal(normal, maxn, minn, maxn).endVertex();
		// --
		buffer.vertex(transform, maxd, mind, maxd).color(red, green, blue, alpha).uv(minU,maxV).overlayCoords(combinedOverlayIn).uv2(packedLightIn).normal(normal, maxn, minn, maxn).endVertex();
		buffer.vertex(transform, mind, mind, maxd).color(red, green, blue, alpha).uv(maxU,maxV).overlayCoords(combinedOverlayIn).uv2(packedLightIn).normal(normal, minn, minn, maxn).endVertex();
		// -- 
		buffer.vertex(transform, mind, mind, maxd).color(red, green, blue, alpha).uv(maxU,maxV).overlayCoords(combinedOverlayIn).uv2(packedLightIn).normal(normal, minn, minn, maxn).endVertex();
		buffer.vertex(transform, mind, mind, mind).color(red, green, blue, alpha).uv(maxU,minV).overlayCoords(combinedOverlayIn).uv2(packedLightIn).normal(normal, minn, minn, minn).endVertex();
	}
	
	/**
	 * 
	 * @param width
	 * @param height
	 * @param matrixStackIn
	 * @param buffer should be set up to draw TRIANGLES
	 * @param packedLightIn
	 * @param red
	 * @param green
	 * @param blue
	 * @param alpha
	 */
	public static final void drawEllipse(float horizontalRadius, float verticalRadius, int points, PoseStack matrixStackIn, VertexConsumer buffer, int packedLightIn, float red, float green, float blue, float alpha) {
		drawEllipse(horizontalRadius, verticalRadius, points, 0f, matrixStackIn, buffer, packedLightIn, red, green, blue, alpha);
	}
	
	/**
	 * 
	 * @param width
	 * @param height
	 * @param rotationPercent float from 0 to 1 with how far the uvs should be rotated, with 1 being a full rotation
	 * @param matrixStackIn
	 * @param buffer should be set up to draw TRIANGLES
	 * @param packedLightIn
	 * @param red
	 * @param green
	 * @param blue
	 * @param alpha
	 */
	public static final void drawEllipse(float horizontalRadius, float verticalRadius, int points, float rotationPercent, PoseStack matrixStackIn, VertexConsumer buffer, int packedLightIn, float red, float green, float blue, float alpha) {
		
		final double angleOffset = rotationPercent * Math.PI;
		final Matrix4f transform = matrixStackIn.last().pose();
		final Matrix3f normal = matrixStackIn.last().normal();
		
		for (int i = 2; i < points; i++) {
			
			// For each (point-2) triangles, draw points for 0, i-1, and i
			// for (int j : new int[]{0, i-1, i}) {
			for (int j = i-2; j <= i; j++) {
				double angle = (2*Math.PI) * ((double) (j == i-2 ? 0 : j) / (double) points);
				float vx = (float) (Math.cos(angle) * horizontalRadius);
				float vy = (float) (Math.sin(angle) * verticalRadius);
				
				double aheadAngle = angle + angleOffset;
				double ux = Math.cos(aheadAngle) * horizontalRadius;
				double uy = Math.sin(aheadAngle) * verticalRadius;
				float u = (float) ((ux + (horizontalRadius)) / (horizontalRadius * 2));
				float v = (float) ((uy + (verticalRadius)) / (verticalRadius * 2));
				
				buffer.vertex(transform, vx, vy, 0f).color(red, green, blue, alpha).uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLightIn).normal(normal, 0, 0, -1f).endVertex();
				
			}
		}
	}
	
	public static final void drawOrb(PoseStack matrixStackIn, VertexConsumer buffer, int combinedLightIn, int combinedOverlayIn, float red, float green, float blue, float alpha,
			int rows, int columns, float xRadius, float yRadius, float zRadius) {
		final Matrix4f transform = matrixStackIn.last().pose();
		final Matrix3f normal = matrixStackIn.last().normal();
		
		for (int i = 1; i <= rows; i++) {
			final double yRad0 = Math.PI * (-0.5f + (float) (i - 1) / (float) rows);
			final double y0 = Math.sin(yRad0);
			final double yR0 = Math.cos(yRad0);
			final double yRad1 = Math.PI * (-0.5f + (float) (i) / (float) rows);
			final double y1 = Math.sin(yRad1);
			final double yR1 = Math.cos(yRad1);

			for (int j = 0; j <= columns; j++) {
				final double xRad = Math.PI * 2 * (double) ((float) (j-1) / (float) columns);
				final double x = Math.cos(xRad);
				final double z = Math.sin(xRad);
				
				final float nx0 = (float) (x * yR0);
				final float ny0 = (float) (y0);
				final float nz0 = (float) (z * yR0);
				final float nx1 = (float) (x * yR1);
				final float ny1 = (float) (y1);
				final float nz1 = (float) (z * yR1);
				
				final float px0 = (float) (xRadius * nx0);
				final float py0 = (float) (yRadius * ny0);
				final float pz0 = (float) (zRadius * nz0);
				final float px1 = (float) (xRadius * nx1);
				final float py1 = (float) (yRadius * ny1);
				final float pz1 = (float) (zRadius * nz1);
				
				// Repeat vertices (but backwards and first) for positions besides the edges to make quads into strips
				// without having to recalculate the last positions
				if (j != 0) {
					buffer.vertex(transform, px1, py1, pz1).color(red, green, blue, alpha).uv(1, 1).overlayCoords(combinedOverlayIn).uv2(combinedLightIn).normal(normal, nx1, ny1, nz1).endVertex();
					buffer.vertex(transform, px0, py0, pz0).color(red, green, blue, alpha).uv(1, 0).overlayCoords(combinedOverlayIn).uv2(combinedLightIn).normal(normal, nx0, ny0, nz0).endVertex();
				}

				if (j != columns) {
					buffer.vertex(transform, px0, py0, pz0).color(red, green, blue, alpha).uv(0, 0).overlayCoords(combinedOverlayIn).uv2(combinedLightIn).normal(normal, nx0, ny0, nz0).endVertex();
					buffer.vertex(transform, px1, py1, pz1).color(red, green, blue, alpha).uv(0, 1).overlayCoords(combinedOverlayIn).uv2(combinedLightIn).normal(normal, nx1, ny1, nz1).endVertex();
				}
			}
		}
	}
	
	public static final void renderLine(PoseStack matrixStackIn, VertexConsumer buffer, Vec3 start, Vec3 end,
			//float u1, float v1, float u2, float v2,
			int combinedOverlayIn, int combinedLightIn,
			float red, float green, float blue, float alpha) {
		renderLine(matrixStackIn, buffer, start, end, 1f,
				combinedOverlayIn, combinedLightIn,
				red, green, blue, alpha);
	}
	
	public static final void renderLine(PoseStack matrixStackIn, VertexConsumer buffer, Vec3 start, Vec3 end,
			//float u1, float v1, float u2, float v2,
			float width,
			int combinedOverlayIn, int combinedLightIn,
			float red, float green, float blue, float alpha) {
		
		final Vec3 diff = end.subtract(start);
		
		matrixStackIn.pushPose();
		matrixStackIn.translate(start.x(), start.y(), start.z());
		final Matrix4f transform = matrixStackIn.last().pose();
		final Matrix3f normal = matrixStackIn.last().normal();
		RenderSystem.lineWidth(width);
		buffer.vertex(transform, 0, 0, 0).color(red, green, blue, alpha).uv(0, 0).overlayCoords(combinedOverlayIn).uv2(combinedLightIn).normal(normal, 0, 1, 0).endVertex();
		buffer.vertex(transform, (float) diff.x(), (float) diff.y(), (float) diff.z()).color(red, green, blue, alpha).uv(1, 1).overlayCoords(combinedOverlayIn).uv2(combinedLightIn).normal(normal, 0, 1, 0).endVertex();
		//RenderSystem.lineWidth(1f);
		matrixStackIn.popPose();
	}
	
	public static final void renderCurve(PoseStack matrixStackIn, VertexConsumer buffer, Vec3 start, ICurve3d curve, int segments,
			//float u1, float v1, float u2, float v2,
			int combinedOverlayIn, int combinedLightIn,
			float red, float green, float blue, float alpha) {
		Vec3 last = curve.getPosition(0f);

		matrixStackIn.pushPose();
		matrixStackIn.translate(start.x(), start.y(), start.z());
		final Matrix4f transform = matrixStackIn.last().pose();
		final Matrix3f normal = matrixStackIn.last().normal();
		for (int i = 1; i <= segments; i++) {
			final float nextProg = ((float) i / (float) segments);
			Vec3 next = curve.getPosition(nextProg);
			
			buffer.vertex(transform, (float) last.x(), (float) last.y(), (float) last.z()).color(red, green, blue, alpha).uv(0, 0).overlayCoords(combinedOverlayIn).uv2(combinedLightIn).normal(normal, 0, 1, 0).endVertex();
			buffer.vertex(transform, (float) next.x(), (float) next.y(), (float) next.z()).color(red, green, blue, alpha).uv(1, 1).overlayCoords(combinedOverlayIn).uv2(combinedLightIn).normal(normal, 0, 1, 0).endVertex();
			
			last = next;
		}
		matrixStackIn.popPose();
	}
	
	public static final void renderHorizontalRibbon(PoseStack matrixStackIn, VertexConsumer buffer, Vec3 start, ICurve3d curve, int segments,
			float width, float texOffsetProg,
			int combinedOverlayIn, int combinedLightIn,
			float red, float green, float blue, float alpha) {
		
		Function<Vec3, Vec3> widthMapper = (segmentDiff) -> segmentDiff.multiply(1, 0, 1).normalize().scale(width/2).yRot(-90f);
		
		renderRibbon(matrixStackIn, buffer, start, curve, segments, widthMapper, texOffsetProg,
				combinedOverlayIn, combinedLightIn, red, green, blue, alpha);
	}
	
	public static final void renderVerticalRibbon(PoseStack matrixStackIn, VertexConsumer buffer, Vec3 start, ICurve3d curve, int segments,
			float width, float texOffsetProg,
			int combinedOverlayIn, int combinedLightIn,
			float red, float green, float blue, float alpha) {
		
		Function<Vec3, Vec3> widthMapper = (segmentDiff) -> new Vec3(0, width/2, 0);
		
		renderRibbon(matrixStackIn, buffer, start, curve, segments, widthMapper, texOffsetProg,
				combinedOverlayIn, combinedLightIn, red, green, blue, alpha);
	}
	
	public static final void renderRibbon(PoseStack matrixStackIn, VertexConsumer buffer, Vec3 start, ICurve3d curve, int segments,
			Function<Vec3, Vec3> widthSupplier, float texOffsetProg,
			int combinedOverlayIn, int combinedLightIn,
			float red, float green, float blue, float alpha) {
		
		Vec3 last = curve.getPosition(0f);
		//float lastV = lowV; 

		matrixStackIn.pushPose();
		matrixStackIn.translate(start.x(), start.y(), start.z());
		final Matrix4f transform = matrixStackIn.last().pose();
		final Matrix3f normal = matrixStackIn.last().normal();
		for (int i = 1; i <= segments; i++) {
			final float nextProg = ((float) i / (float) segments);
			final Vec3 next = curve.getPosition(nextProg);
			final Vec3 segmentDiff = next.subtract(last);
			final Vec3 widthOffset = widthSupplier.apply(segmentDiff);
			//final float nextV = lastV == lowV ? highV : lowV;
			
			buffer.vertex(transform, (float) (last.x() - widthOffset.x()), (float) (last.y() - widthOffset.y()), (float) (last.z() - widthOffset.z())).color(red, green, blue, alpha).uv(0, 1 + texOffsetProg).overlayCoords(combinedOverlayIn).uv2(combinedLightIn).normal(normal, 0, 1, 0).endVertex();
			buffer.vertex(transform, (float) (last.x() + widthOffset.x()), (float) (last.y() + widthOffset.y()), (float) (last.z() + widthOffset.z())).color(red, green, blue, alpha).uv(1, 1 + texOffsetProg).overlayCoords(combinedOverlayIn).uv2(combinedLightIn).normal(normal, 0, 1, 0).endVertex();
			
			buffer.vertex(transform, (float) (next.x() + widthOffset.x()), (float) (next.y() + widthOffset.y()), (float) (next.z() + widthOffset.z())).color(red, green, blue, alpha).uv(1, 0 + texOffsetProg).overlayCoords(combinedOverlayIn).uv2(combinedLightIn).normal(normal, 0, 1, 0).endVertex();
			buffer.vertex(transform, (float) (next.x() - widthOffset.x()), (float) (next.y() - widthOffset.y()), (float) (next.z() - widthOffset.z())).color(red, green, blue, alpha).uv(0, 0 + texOffsetProg).overlayCoords(combinedOverlayIn).uv2(combinedLightIn).normal(normal, 0, 1, 0).endVertex();
			
			last = next;
			//lastV = nextV;
		}
		matrixStackIn.popPose();
	}
	
	public static final void drawNameplate(PoseStack matrixStackIn, MultiBufferSource bufferIn, Entity entityIn, String info, Font fonter, int packedLightIn, float yOffsetExtra, Camera renderInfo) {
		final float offsetY = yOffsetExtra + (entityIn == null ? 0 : (entityIn.getBbHeight() + 0.5f));
		final boolean discrete = entityIn == null ? false : entityIn.isDiscrete();
		drawNameplate(matrixStackIn, bufferIn, info, fonter, packedLightIn, offsetY, discrete, renderInfo);
	}
	
	public static final void drawNameplate(PoseStack matrixStackIn, MultiBufferSource bufferIn, String info, Font fonter, int packedLightIn, float yOffset, boolean discrete, Camera renderInfo) {
		matrixStackIn.pushPose();
		matrixStackIn.translate(0.0D, yOffset, 0.0D);
		matrixStackIn.mulPose(renderInfo.rotation());
		drawNameplate(matrixStackIn, bufferIn, info, fonter, packedLightIn, discrete);
		matrixStackIn.popPose();
	}
	
	public static final void drawNameplate(PoseStack matrixStackIn, MultiBufferSource bufferIn, String info, Font fontrenderer, int packedLightIn, boolean discrete) {
		matrixStackIn.pushPose();
        matrixStackIn.scale(-0.025F, -0.025F, 0.025F);
		
		final Minecraft mc = Minecraft.getInstance();
		final Matrix4f matrix4f = matrixStackIn.last().pose();
		float f1 = mc.options.getBackgroundOpacity(0.25F);
		int j = (int)(f1 * 255.0F) << 24;
		float f2 = (float)(-fontrenderer.width(info) / 2);
		fontrenderer.drawInBatch(info, f2, 0, 553648127, false, matrix4f, bufferIn, !discrete, j, packedLightIn);
		if (!discrete) {
			fontrenderer.drawInBatch(info, f2, 0, -1, false, matrix4f, bufferIn, false, 0, packedLightIn);
		}
		matrixStackIn.popPose();
	}

	public static int drawSplitString(PoseStack matrixStackIn, Font fonter, String str, int x, int y, int width, int infoColor) {
		int offset = 0;
		int lineWidth = fonter.width(str);
		while (lineWidth > width) {
			int subWidth = 0;
			StringBuffer lineBuffer = new StringBuffer();
			StringBuffer wordBuffer = new StringBuffer();
			int i;
			boolean hasWord = false;
			
			for (i = 0; i < str.length(); i++) {
				final char c = str.charAt(i);
				if (Character.isSpaceChar(c)) {
					wordBuffer.append(c);
					lineBuffer.append(wordBuffer);
					wordBuffer = new StringBuffer();
					
					subWidth = fonter.width(lineBuffer.toString());
					hasWord = true;
				} else {
					wordBuffer.append(c);
					
					if (subWidth + fonter.width(wordBuffer.toString()) >= width) {
						if (!hasWord) {
							// Didn't find single word to split, so just add what we have and tear the string
							lineBuffer.append(wordBuffer);
						} else {
							// Word is too big and hasn't hit a space, so ditch it and just take current linebuffer and roll back i
							i -= wordBuffer.length()-1; // -1 chops off space
						}
						break;
					}
				}
			}
			
			fonter.draw(matrixStackIn, lineBuffer.toString(), x, y + offset, infoColor);
			str = str.substring(i);
			lineWidth = fonter.width(str);
			offset += fonter.lineHeight;
		}
		
		fonter.draw(matrixStackIn, str, x, y + offset, infoColor);
		return offset;
	}
	
	// Copies of Vanilla's blit but with color support
	public static final void blit(PoseStack matrixStack, int x, int y, int blitOffset, int width, int height, TextureAtlasSprite sprite, float red, float green, float blue, float alpha) {
		innerBlit(matrixStack.last().pose(), x, x + width, y, y + height, blitOffset, sprite.getU0(), sprite.getU1(), sprite.getV0(), sprite.getV1(), red, green, blue, alpha);
	}

	public static final void blit(PoseStack matrixStack, int x, int y, int uOffset, int vOffset, int uWidth, int vHeight, float red, float green, float blue, float alpha) {
		blit(matrixStack, x, y, 0, (float)uOffset, (float)vOffset, uWidth, vHeight, 256, 256, red, green, blue, alpha);
	}

	public static final void blit(PoseStack matrixStack, int x, int y, int blitOffset, float uOffset, float vOffset, int uWidth, int vHeight, int textureHeight, int textureWidth, float red, float green, float blue, float alpha) {
		innerBlit(matrixStack, x, x + uWidth, y, y + vHeight, blitOffset, uWidth, vHeight, uOffset, vOffset, textureWidth, textureHeight, red, green, blue, alpha);
	}

	public static final void blit(PoseStack matrixStack, int x, int y, int width, int height, float uOffset, float vOffset, int uWidth, int vHeight, int textureWidth, int textureHeight, float red, float green, float blue, float alpha) {
		innerBlit(matrixStack, x, x + width, y, y + height, 0, uWidth, vHeight, uOffset, vOffset, textureWidth, textureHeight, red, green, blue, alpha);
	}

	public static final void blit(PoseStack matrixStack, int x, int y, float uOffset, float vOffset, int width, int height, int textureWidth, int textureHeight, float red, float green, float blue, float alpha) {
		blit(matrixStack, x, y, width, height, uOffset, vOffset, width, height, textureWidth, textureHeight, red, green, blue, alpha);
	}

	private static final void innerBlit(PoseStack matrixStack, int x1, int x2, int y1, int y2, int blitOffset, int uWidth, int vHeight, float uOffset, float vOffset, int textureWidth, int textureHeight, float red, float green, float blue, float alpha) {
		innerBlit(matrixStack.last().pose(), x1, x2, y1, y2, blitOffset, (uOffset + 0.0F) / (float)textureWidth, (uOffset + (float)uWidth) / (float)textureWidth, (vOffset + 0.0F) / (float)textureHeight, (vOffset + (float)vHeight) / (float)textureHeight, red, green, blue, alpha);
	}

	private static final void innerBlit(Matrix4f matrix, int x1, int x2, int y1, int y2, int blitOffset, float minU, float maxU, float minV, float maxV, float red, float green, float blue, float alpha) {
		BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
		bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
		bufferbuilder.vertex(matrix, (float)x1, (float)y2, (float)blitOffset).color(red, green, blue, alpha).uv(minU, maxV).endVertex();
		bufferbuilder.vertex(matrix, (float)x2, (float)y2, (float)blitOffset).color(red, green, blue, alpha).uv(maxU, maxV).endVertex();
		bufferbuilder.vertex(matrix, (float)x2, (float)y1, (float)blitOffset).color(red, green, blue, alpha).uv(maxU, minV).endVertex();
		bufferbuilder.vertex(matrix, (float)x1, (float)y1, (float)blitOffset).color(red, green, blue, alpha).uv(minU, minV).endVertex();
		bufferbuilder.end();
		//RenderSystem.enableAlphaTest();
		BufferUploader.end(bufferbuilder);
	}
	
	// Should be somewhere else?
	@OnlyIn(Dist.CLIENT)
	public static final ModelResourceLocation makeDefaultModelLocation(ResourceLocation loc) {
		return new ModelResourceLocation(loc, "");
	}

	public static final PoseStack makeNewMatrixStack(Camera renderInfo) {
		PoseStack stack = new PoseStack();
		
		// World renderer doesn't start with an identity stack; it applies some rotations based on
		// the camera. These are copied out of GameRenderer#RenderWorld right before calling "this.mc.worldRenderer.updateCameraAndRender"
		{
			stack.mulPose(Vector3f.ZP.rotationDegrees(0));
			stack.mulPose(Vector3f.XP.rotationDegrees(renderInfo.getXRot()));
			stack.mulPose(Vector3f.YP.rotationDegrees(renderInfo.getYRot() + 180.0F));
		}
		return stack;
	}

	public static void renderDiamond(PoseStack matrixStackIn, VertexConsumer bufferIn, float width, float height, int packedLightIn,
			int packedOverlayIn, float red, float green, float blue, float alpha) {
		final Matrix4f transform = matrixStackIn.last().pose();
		final Matrix3f normal = matrixStackIn.last().normal();
		final Vector3f[] normals = {new Vector3f(0.5774f, -0.5774f, 0.5774f), new Vector3f(-0.5774f, -0.5774f, 0.5774f), new Vector3f(-0.5774f, -0.5774f, -0.5774f), new Vector3f(0.5774f, -0.5774f, -0.5774f)};
		
		
		for (int i = 0; i < 4; i++) {
			double angle = (2*Math.PI) * ((double) i / (double) 4);
			
			final float vx1 = (float) (Math.cos(angle) * width);
			final float vy1 = (float) (Math.sin(angle) * height);
			final float u1 = (vx1 + (width)) / (width * 2);
			final float v1 = (vy1 + (height)) / (height * 2);
			final Vector3f n1 = normals[i];
			
			angle = (2*Math.PI) * ((double) ((i+1)%4) / (double) 4);
			
			final float vx2 = (float) (Math.cos(angle) * width);
			final float vy2 = (float) (Math.sin(angle) * height);
			final float u2 = (vx2 + (width)) / (width * 2);
			final float v2 = (vy2 + (height)) / (height * 2);
			final Vector3f n2 = normals[(i+1)%4];
			
			// For znegative, add in ZN, HIGH ANGLE, LOW ANGLE
			bufferIn.vertex(transform, 0, 0, -width).color(red, green, blue, alpha).uv(.5f, .5f).overlayCoords(packedOverlayIn).uv2(packedLightIn).normal(normal, n1.x(), n1.y(), n1.z()).endVertex();
			bufferIn.vertex(transform, vx2, vy2, 0).color(red, green, blue, alpha).uv(u2, v2).overlayCoords(packedOverlayIn).uv2(packedLightIn).normal(normal, n1.x(), n1.y(), n1.z()).endVertex();
			bufferIn.vertex(transform, vx1, vy1, 0).color(red, green, blue, alpha).uv(u1, v1).overlayCoords(packedOverlayIn).uv2(packedLightIn).normal(normal, n1.x(), n1.y(), n1.z()).endVertex();
			
			// for zpositive, add in ZP, LOW ANGLE, HIGH ANGLE
			bufferIn.vertex(transform, 0, 0, width).color(red, green, blue, alpha).uv(.5f, .5f).overlayCoords(packedOverlayIn).uv2(packedLightIn).normal(normal, n2.x(), n2.y(), n2.z()).endVertex();
			bufferIn.vertex(transform, vx1, vy1, 0).color(red, green, blue, alpha).uv(u1, v1).overlayCoords(packedOverlayIn).uv2(packedLightIn).normal(normal, n2.x(), n2.y(), n2.z()).endVertex();
			bufferIn.vertex(transform, vx2, vy2, 0).color(red, green, blue, alpha).uv(u2, v2).overlayCoords(packedOverlayIn).uv2(packedLightIn).normal(normal, n2.x(), n2.y(), n2.z()).endVertex();
		}
	}
	
}
