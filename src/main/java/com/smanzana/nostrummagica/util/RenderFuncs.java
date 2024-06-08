package com.smanzana.nostrummagica.util;

import java.util.List;
import java.util.Random;
import java.util.function.Function;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.smanzana.nostrummagica.util.Curves.ICurve3d;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
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
	
	//private static final BlockPos.Mutable cursor = new BlockPos.Mutable(); // If there are ever threads at play, this will not work
	
	// Could redo these if needed
//	public static final void RenderBlockOutline(PlayerEntity player, World world, Vector3d pos, BlockState blockState, float partialTicks) {
//		GlStateManager.enableBlend();
//		GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
//		GlStateManager.lineWidth(2.0F);
//		GlStateManager.disableTexture();
//		GlStateManager.depthMask(false);
//
//		if (blockState.getMaterial() != Material.AIR) {
//			double d0 = player.lastTickPosX + (player.getPosX() - player.lastTickPosX) * (double)partialTicks;
//			double d1 = player.lastTickPosY + (player.getPosY() - player.lastTickPosY) * (double)partialTicks;
//			double d2 = player.lastTickPosZ + (player.getPosZ() - player.lastTickPosZ) * (double)partialTicks;
//			
//			WorldRenderer.drawVoxelShapeParts(blockState.getShape(world, new BlockPos(pos), ISelectionContext.forEntity(player)), -d0, -d1, -d2, 0.0F, 0.0F, 0.0F, 0.4F);
//			
//		}
//
//		GlStateManager.depthMask(true);
//		GlStateManager.enableTexture();
//		GlStateManager.disableBlend();
//	}
//	
//	public static final void RenderBlockOutline(PlayerEntity player, World world, BlockPos pos, BlockState blockState, float partialTicks) {
//		RenderBlockOutline(player, world, new Vector3d(pos), blockState, partialTicks);
//	}
	
	public static void RenderModelWithColorNoBatch(MatrixStack stack, IBakedModel model, int color, int combinedLight, int combinedOverlay) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK); // quads hardcode this internally. If not, would need to convert when rendering quad?
		RenderModelWithColor(stack, buffer, model, color, combinedLight, combinedOverlay);
		buffer.finishDrawing();
		WorldVertexBufferUploader.draw(buffer);
	}
	
	public static void RenderModelWithColor(MatrixStack stack, IVertexBuilder buffer, IBakedModel model, int color, int combinedLight, int combinedOverlay) {
		final float colors[] = ColorUtil.ARGBToColor(color);
		RenderModel(stack, buffer, model, combinedLight, combinedOverlay, colors[0], colors[1], colors[2], colors[3]);
	}
	
	private static final Random RenderModelRandom = new Random();
	
	public static void RenderModel(MatrixStack stack, IVertexBuilder buffer, IBakedModel model, int combinedLight, int combinedOverlay, float red, float green, float blue, float alpha) {
		RenderModel(stack.getLast(), buffer, model, combinedLight, combinedOverlay, red, green, blue, alpha);
	}
	
	public static void RenderModel(MatrixStack.Entry stackLast, IVertexBuilder buffer, IBakedModel model, int combinedLight, int combinedOverlay, float red, float green, float blue, float alpha) {
		
		for(Direction side : Direction.values()) {
			List<BakedQuad> quads = model.getQuads(null, side, RenderRandom(RenderModelRandom), EmptyModelData.INSTANCE);
			if(!quads.isEmpty()) 
				for(BakedQuad quad : quads) {
					buffer.addVertexData(stackLast, quad, red, green, blue, alpha, combinedLight, combinedOverlay, true);
//					LightUtil.renderQuadColor(buffer, quad, color);
				}
		}
		List<BakedQuad> quads = model.getQuads(null, null, RenderRandom(RenderModelRandom), EmptyModelData.INSTANCE);
		if(!quads.isEmpty()) {
			for(BakedQuad quad : quads) 
				buffer.addVertexData(stackLast, quad, red, green, blue, alpha, combinedLight, combinedOverlay, true);
				//LightUtil.renderQuadColor(buffer, quad, color);
		}

	}
	
	public static void RenderBlockState(BlockState state, MatrixStack stack, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
		// Could get model and turn around and call RenderModel() on it
		 Minecraft.getInstance().getBlockRendererDispatcher().renderBlock(state, stack, bufferIn, combinedLightIn, combinedOverlayIn, EmptyModelData.INSTANCE);
	}

	public static final void renderEntityOutline(Entity e, float partialTicks) {
		if (!e.world.isRemote) {
			return;
		}
		
		e.setGlowing(true); // Best we can do?
		
//		RenderGlobal global = getCachedRenderGlobal();
//		
//		if (global == null || cachedRenderGlobal_outlineFrameBuffer == null) {
//			e.setGlowing(true);
//			return; // Best we can do?
//		}
		
//		final Minecraft mc = Minecraft.getInstance();
//		
//		GlStateManager.depthFunc(519);
//		GlStateManager.disableFog();
//		GlStateManager.color4f(1f, 1f, 0f, 1f);
//		
//		try {
//			final RenderManager renderManager = ObfuscationReflectionHelper.getPrivateValue(RenderGlobal.class, global, "field_175010_j"); // "renderManager"
//			
//			//this.entityOutlineFramebuffer.bindFramebuffer(false);
//			((Framebuffer)(cachedRenderGlobal_outlineFrameBuffer.get(global))).bindFramebuffer(false);
//			RenderHelper.disableStandardItemLighting();
//			//this.renderManager.setRenderOutlines(true);
//			renderManager.setRenderOutlines(true);
//			
//			//this.renderManager.renderEntityStatic(list1.get(j), partialTicks, false);
//			renderManager.renderEntityStatic(e, partialTicks, false);
//			
//			//this.renderManager.setRenderOutlines(false);
//			renderManager.setRenderOutlines(false);
//			RenderHelper.enableStandardItemLighting();
//			GlStateManager.depthMask(false);
//			//this.entityOutlineShader.render(partialTicks);
//			((ShaderGroup)cachedRenderGlobal_outlineShader.get(global)).render(partialTicks);
//			
//			ObfuscationReflectionHelper.setPrivateValue(RenderGlobal.class, global, true, "field_184386_ad"); // entityOutlinesRendered
//			
//		} catch (Exception exception) {
//			System.out.print(".");
//		}
//		
//		GlStateManager.enableLighting();
//		GlStateManager.depthMask(true);
////		GlStateManager.enableFog();
//		GlStateManager.enableBlend();
//		GlStateManager.enableColorMaterial();
//		GlStateManager.depthFunc(515);
//		GlStateManager.enableDepth();
//		GlStateManager.enableAlphaTest();
//
//		mc.getFramebuffer().bindFramebuffer(false);
//		global.renderEntityOutlineFramebuffer();
//		mc.getFramebuffer().bindFramebuffer(false);
//		GlStateManager.enableBlend();
//		GlStateManager.blendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
	}
	
	public static void RenderWorldItem(ItemStack stack, MatrixStack matrix) {
		// light and overlay constants taken from ItemRenderer and GameRenderer
		final int combinedLight = 15728880;
		final int combinedOverlay = OverlayTexture.NO_OVERLAY;
		
		IRenderTypeBuffer.Impl typebuffer = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
		RenderWorldItem(stack, matrix, typebuffer, combinedLight, combinedOverlay);
		typebuffer.finish();
	}
	
	/**
	 * Renders an item. Basically a wrapper for rendering classes.
	 * Making now because transform type is deprecated but required :P and I'd rather have one warning than a bunch.
	 * @param world
	 * @param stack
	 */
	public static void RenderWorldItem(ItemStack stack, MatrixStack matrix, IRenderTypeBuffer typeBuffer, int combinedLight) {
		RenderWorldItem(stack, matrix, typeBuffer, combinedLight, OverlayTexture.NO_OVERLAY);
	}
	
	public static void RenderWorldItem(ItemStack stack, MatrixStack matrix, IRenderTypeBuffer typeBuffer, int combinedLight, int combinedOverlay) {
		Minecraft.getInstance().getItemRenderer()
			.renderItem(stack, TransformType.GROUND, combinedLight, combinedOverlay, matrix, typeBuffer);
	}
	
	public static void RenderGUIItem(ItemStack stack, MatrixStack matrixStackIn) {
		final Minecraft mc = Minecraft.getInstance();
		RenderSystem.pushMatrix();
		RenderSystem.multMatrix(matrixStackIn.getLast().getMatrix());
		mc.getItemRenderer().renderItemIntoGUI(stack, 0, 0);
		RenderSystem.popMatrix();
	}
	
	public static void RenderGUIItem(ItemStack stack, MatrixStack matrixStackIn, int x, int y, int z) {
		matrixStackIn.push();
		matrixStackIn.translate(x, y, z);
		RenderGUIItem(stack, matrixStackIn);
		matrixStackIn.pop();
	}
	
	public static void RenderGUIItem(ItemStack stack, MatrixStack matrixStackIn, int x, int y) {
		RenderGUIItem(stack, matrixStackIn, x, y, 0);
	}
	
	// can use blit here: blit(x, y, 0, u, v, width, height, texWidth, texHeight)
	public static void drawModalRectWithCustomSizedTextureImmediate(MatrixStack matrixStackIn, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight) {
		Screen.blit(matrixStackIn, x, y, u, v, width, height, textureWidth, textureHeight);
	}
	
	public static void drawModalRectWithCustomSizedTextureImmediate(MatrixStack matrixStackIn, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight, float red, float green, float blue, float alpha) {
		// hack for now
		RenderSystem.color4f(red, green, blue, alpha);
		drawModalRectWithCustomSizedTextureImmediate(matrixStackIn, x, y, u, v, width, height, textureWidth, textureHeight);
		RenderSystem.color4f(1f, 1f, 1f, 1f);
	}
	
	// Different from the above in that this includes scaling on what's drawn
	public static void drawScaledCustomSizeModalRectImmediate(MatrixStack matrixStackIn, int x, int y, float u, float v, int uWidth, int vHeight, int width, int height, float tileWidth, float tileHeight) {
		drawScaledCustomSizeModalRectImmediate(matrixStackIn, x, y, u, v, uWidth, vHeight, width, height, tileWidth, tileHeight, 1f, 1f, 1f, 1f);
	}
	
	public static void drawScaledCustomSizeModalRectImmediate(MatrixStack matrixStackIn, int x, int y, float u, float v, int uWidth, int vHeight, int width, int height, float tileWidth, float tileHeight, float red, float green, float blue, float alpha) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR_TEX);
		
		drawScaledCustomSizeModalRect(matrixStackIn, bufferbuilder, x, y, u, v, uWidth, vHeight, width, height, tileWidth, tileHeight,
				red, green, blue, alpha);

		bufferbuilder.finishDrawing();
		RenderSystem.enableAlphaTest();
		WorldVertexBufferUploader.draw(bufferbuilder);
	}
	
	public static void drawScaledCustomSizeModalRect(MatrixStack matrixStackIn, IVertexBuilder buffer, int x, int y, float u, float v, int uWidth, int vHeight, int width, int height, float tileWidth, float tileHeight, float red, float green, float blue, float alpha) {
		final int combinedLight = 15728880;
		final int combinedOverlay = OverlayTexture.NO_OVERLAY;
		drawScaledCustomSizeModalRect(matrixStackIn, buffer, x, y, u, v, uWidth, vHeight, width, height, tileWidth, tileHeight, combinedLight, combinedOverlay, red, green, blue, alpha);
	}
	
	public static void drawScaledCustomSizeModalRect(MatrixStack matrixStackIn, IVertexBuilder buffer, int x, int y, float u, float v, int uWidth, int vHeight, int width, int height, float tileWidth, float tileHeight, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
		final float f = 1.0F / tileWidth;
		final float f1 = 1.0F / tileHeight;
		final Matrix4f transform = matrixStackIn.getLast().getMatrix();
		final Matrix3f normal = matrixStackIn.getLast().getNormal();
		
		buffer.pos(transform, x, y + height, 0.0f).color(red, green, blue, alpha).tex(u * f, (v + vHeight) * f1).overlay(packedOverlayIn).lightmap(packedLightIn).normal(normal, 0, 0, 1).endVertex();
		buffer.pos(transform, x + width, y + height, 0.0f).color(red, green, blue, alpha).tex((u + uWidth) * f, (v + vHeight) * f1).overlay(packedOverlayIn).lightmap(packedLightIn).normal(normal, 0, 0, 1).endVertex();
		buffer.pos(transform, x + width, y, 0.0f).color(red, green, blue, alpha).tex((u + uWidth) * f, v * f1).overlay(packedOverlayIn).lightmap(packedLightIn).normal(normal, 0, 0, 1).endVertex();
		buffer.pos(transform, x, y, 0.0f).color(red, green, blue, alpha).tex((u * f), (v * f1)).overlay(packedOverlayIn).lightmap(packedLightIn).normal(normal, 0, 0, 1).endVertex();
	}
	
	public static void drawRect(MatrixStack stack, int minX, int minY, int maxX, int maxY, int colorARGB) {
		AbstractGui.fill(stack, minX, minY, maxX, maxY, colorARGB);
	}
	
	public static void drawGradientRect(MatrixStack stack, int minX, int minY, int maxX, int maxY, int colorTopLeft, int colorTopRight, int colorBottomLeft, int colorBottomRight) {
		final Matrix4f transform = stack.getLast().getMatrix();
		final float[] colorTR = ColorUtil.ARGBToColor(colorTopRight);
		final float[] colorTL = ColorUtil.ARGBToColor(colorTopLeft);
		final float[] colorBL = ColorUtil.ARGBToColor(colorBottomLeft);
		final float[] colorBR = ColorUtil.ARGBToColor(colorBottomRight);
		
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

		RenderSystem.disableTexture();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.disableAlphaTest();
		RenderSystem.shadeModel(GL11.GL_SMOOTH);
		{
			bufferbuilder.pos(transform, minX, minY, 0).color(colorTL[0], colorTL[1], colorTL[2], colorTL[3]).endVertex();
			bufferbuilder.pos(transform, minX, maxY, 0).color(colorBL[0], colorBL[1], colorBL[2], colorBL[3]).endVertex();
			bufferbuilder.pos(transform, maxX, maxY, 0).color(colorBR[0], colorBR[1], colorBR[2], colorBR[3]).endVertex();
			bufferbuilder.pos(transform, maxX, minY, 0).color(colorTR[0], colorTR[1], colorTR[2], colorTR[3]).endVertex();
		}

		bufferbuilder.finishDrawing();
		WorldVertexBufferUploader.draw(bufferbuilder);
		RenderSystem.disableBlend();
		RenderSystem.enableAlphaTest();
		RenderSystem.enableTexture();
		RenderSystem.shadeModel(GL11.GL_FLAT);
	}

	public static final void renderSpaceQuad(MatrixStack stack, IVertexBuilder buffer,
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
		
		final Matrix4f transform = stack.getLast().getMatrix();
		final Matrix3f normal = stack.getLast().getNormal();

		final float uMin = 0;
		final float uMax = 1;
		final float vMin = 0;
		final float vMax = 1;
		buffer.pos(transform, avector3f[0].getX(), avector3f[0].getY(), avector3f[0].getZ()).color(red, green, blue, alpha).tex(uMax, vMax).overlay(combinedOverlayIn).lightmap(combinedLightmapIn).normal(normal, 0, 0, 1).endVertex();
		buffer.pos(transform, avector3f[1].getX(), avector3f[1].getY(), avector3f[1].getZ()).color(red, green, blue, alpha).tex(uMax, vMin).overlay(combinedOverlayIn).lightmap(combinedLightmapIn).normal(normal, 0, 0, 1).endVertex();
		buffer.pos(transform, avector3f[2].getX(), avector3f[2].getY(), avector3f[2].getZ()).color(red, green, blue, alpha).tex(uMin, vMin).overlay(combinedOverlayIn).lightmap(combinedLightmapIn).normal(normal, 0, 0, 1).endVertex();
		buffer.pos(transform, avector3f[3].getX(), avector3f[3].getY(), avector3f[3].getZ()).color(red, green, blue, alpha).tex(uMin, vMax).overlay(combinedOverlayIn).lightmap(combinedLightmapIn).normal(normal, 0, 0, 1).endVertex();
	}
	
	public static final void renderSpaceQuadFacingCamera(MatrixStack stack, IVertexBuilder buffer, ActiveRenderInfo renderInfo,
			float radius,
			int lightmap, int overlay,
			float red, float green, float blue, float alpha) {
		Quaternion rotation = renderInfo.getRotation();
		
		stack.push();
		stack.rotate(rotation);
		
		renderSpaceQuad(stack, buffer,
				radius,
				lightmap, overlay,
				red, green, blue, alpha
				);
		stack.pop();
	}

	// Note: renders in ENTITY vertex formate
	public static final void drawUnitCube(MatrixStack stack, IVertexBuilder buffer, int packedLightIn, int combinedOverlayIn, float red, float green, float blue, float alpha) {
		drawUnitCube(stack, buffer, 0, 1, 0, 1, packedLightIn, combinedOverlayIn, red, green, blue, alpha);
	}
	
	public static final void drawUnitCube(MatrixStack stack, IVertexBuilder buffer, float minU, float maxU, float minV, float maxV, int packedLightIn, int combinedOverlayIn,
			float red, float green, float blue, float alpha) {
		
		final float mind = -.5f;
		final float maxd = .5f;
		
		final float minn = -.5773f;
		final float maxn = .5773f;
		
		final Matrix4f transform = stack.getLast().getMatrix();
		final Matrix3f normal = stack.getLast().getNormal();
		
		// Top
		buffer.pos(transform, mind, maxd, mind).color(red, green, blue, alpha).tex(minU,minV).overlay(combinedOverlayIn).lightmap(packedLightIn).normal(normal, minn, maxn, minn).endVertex();
		buffer.pos(transform, mind, maxd, maxd).color(red, green, blue, alpha).tex(minU,maxV).overlay(combinedOverlayIn).lightmap(packedLightIn).normal(normal, minn, maxn, maxn).endVertex();
		buffer.pos(transform, maxd, maxd, maxd).color(red, green, blue, alpha).tex(maxU,maxV).overlay(combinedOverlayIn).lightmap(packedLightIn).normal(normal, maxn, maxn, maxn).endVertex();
		buffer.pos(transform, maxd, maxd, mind).color(red, green, blue, alpha).tex(maxU,minV).overlay(combinedOverlayIn).lightmap(packedLightIn).normal(normal, maxn, maxn, minn).endVertex();
		
		// North
		buffer.pos(transform, maxd, maxd, mind).color(red, green, blue, alpha).tex(maxU,minV).overlay(combinedOverlayIn).lightmap(packedLightIn).normal(normal, maxn, maxn, minn).endVertex();
		buffer.pos(transform, maxd, mind, mind).color(red, green, blue, alpha).tex(maxU,maxV).overlay(combinedOverlayIn).lightmap(packedLightIn).normal(normal, maxn, minn, minn).endVertex();
		buffer.pos(transform, mind, mind, mind).color(red, green, blue, alpha).tex(minU,maxV).overlay(combinedOverlayIn).lightmap(packedLightIn).normal(normal, minn, minn, minn).endVertex();
		buffer.pos(transform, mind, maxd, mind).color(red, green, blue, alpha).tex(minU,minV).overlay(combinedOverlayIn).lightmap(packedLightIn).normal(normal, minn, maxn, minn).endVertex();
		
		// East
		buffer.pos(transform, maxd, maxd, maxd).color(red, green, blue, alpha).tex(maxU,maxV).overlay(combinedOverlayIn).lightmap(packedLightIn).normal(normal, maxn, maxn, maxn).endVertex();
		buffer.pos(transform, maxd, mind, maxd).color(red, green, blue, alpha).tex(minU,maxV).overlay(combinedOverlayIn).lightmap(packedLightIn).normal(normal, maxn, minn, maxn).endVertex();
		buffer.pos(transform, maxd, mind, mind).color(red, green, blue, alpha).tex(minU,minV).overlay(combinedOverlayIn).lightmap(packedLightIn).normal(normal, maxn, minn, minn).endVertex();
		buffer.pos(transform, maxd, maxd, mind).color(red, green, blue, alpha).tex(maxU,minV).overlay(combinedOverlayIn).lightmap(packedLightIn).normal(normal, maxn, maxn, minn).endVertex();
		
		// South
		buffer.pos(transform, mind, maxd, maxd).color(red, green, blue, alpha).tex(minU,maxV).overlay(combinedOverlayIn).lightmap(packedLightIn).normal(normal, minn, maxn, maxn).endVertex();
		buffer.pos(transform, mind, mind, maxd).color(red, green, blue, alpha).tex(minU,minV).overlay(combinedOverlayIn).lightmap(packedLightIn).normal(normal, minn, minn, maxn).endVertex();
		buffer.pos(transform, maxd, mind, maxd).color(red, green, blue, alpha).tex(maxU,minV).overlay(combinedOverlayIn).lightmap(packedLightIn).normal(normal, maxn, minn, maxn).endVertex();
		buffer.pos(transform, maxd, maxd, maxd).color(red, green, blue, alpha).tex(maxU,maxV).overlay(combinedOverlayIn).lightmap(packedLightIn).normal(normal, maxn, maxn, maxn).endVertex();
		
		// West
		buffer.pos(transform, mind, maxd, mind).color(red, green, blue, alpha).tex(minU,minV).overlay(combinedOverlayIn).lightmap(packedLightIn).normal(normal, minn, maxn, minn).endVertex();
		buffer.pos(transform, mind, mind, mind).color(red, green, blue, alpha).tex(maxU,minV).overlay(combinedOverlayIn).lightmap(packedLightIn).normal(normal, minn, minn, minn).endVertex();
		buffer.pos(transform, mind, mind, maxd).color(red, green, blue, alpha).tex(maxU,maxV).overlay(combinedOverlayIn).lightmap(packedLightIn).normal(normal, minn, minn, maxn).endVertex();
		buffer.pos(transform, mind, maxd, maxd).color(red, green, blue, alpha).tex(minU,maxV).overlay(combinedOverlayIn).lightmap(packedLightIn).normal(normal, minn, maxn, maxn).endVertex();
		
		// Bottom
		buffer.pos(transform, mind, mind, mind).color(red, green, blue, alpha).tex(maxU,minV).overlay(combinedOverlayIn).lightmap(packedLightIn).normal(normal, minn, minn, minn).endVertex();
		buffer.pos(transform, maxd, mind, mind).color(red, green, blue, alpha).tex(minU,minV).overlay(combinedOverlayIn).lightmap(packedLightIn).normal(normal, maxn, minn, minn).endVertex();
		buffer.pos(transform, maxd, mind, maxd).color(red, green, blue, alpha).tex(minU,maxV).overlay(combinedOverlayIn).lightmap(packedLightIn).normal(normal, maxn, minn, maxn).endVertex();
		buffer.pos(transform, mind, mind, maxd).color(red, green, blue, alpha).tex(maxU,maxV).overlay(combinedOverlayIn).lightmap(packedLightIn).normal(normal, minn, minn, maxn).endVertex();
	}
	
	// Assumes render type is LINES
	public static final void drawUnitCubeOutline(MatrixStack stack, IVertexBuilder buffer, int packedLightIn, int combinedOverlayIn,
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
		
		final Matrix4f transform = stack.getLast().getMatrix();
		final Matrix3f normal = stack.getLast().getNormal();
		
		// Top
		buffer.pos(transform, mind, maxd, mind).color(red, green, blue, alpha).tex(minU,minV).overlay(combinedOverlayIn).lightmap(packedLightIn).normal(normal, minn, maxn, minn).endVertex();
		buffer.pos(transform, mind, maxd, maxd).color(red, green, blue, alpha).tex(minU,maxV).overlay(combinedOverlayIn).lightmap(packedLightIn).normal(normal, minn, maxn, maxn).endVertex();
		// --
		buffer.pos(transform, mind, maxd, maxd).color(red, green, blue, alpha).tex(minU,maxV).overlay(combinedOverlayIn).lightmap(packedLightIn).normal(normal, minn, maxn, maxn).endVertex();
		buffer.pos(transform, maxd, maxd, maxd).color(red, green, blue, alpha).tex(maxU,maxV).overlay(combinedOverlayIn).lightmap(packedLightIn).normal(normal, maxn, maxn, maxn).endVertex();
		// --
		buffer.pos(transform, maxd, maxd, maxd).color(red, green, blue, alpha).tex(maxU,maxV).overlay(combinedOverlayIn).lightmap(packedLightIn).normal(normal, maxn, maxn, maxn).endVertex();
		buffer.pos(transform, maxd, maxd, mind).color(red, green, blue, alpha).tex(maxU,minV).overlay(combinedOverlayIn).lightmap(packedLightIn).normal(normal, maxn, maxn, minn).endVertex();
		// --
		buffer.pos(transform, maxd, maxd, mind).color(red, green, blue, alpha).tex(maxU,minV).overlay(combinedOverlayIn).lightmap(packedLightIn).normal(normal, maxn, maxn, minn).endVertex();
		buffer.pos(transform, mind, maxd, mind).color(red, green, blue, alpha).tex(minU,minV).overlay(combinedOverlayIn).lightmap(packedLightIn).normal(normal, minn, maxn, minn).endVertex();
		
		// North-West
		buffer.pos(transform, mind, mind, mind).color(red, green, blue, alpha).tex(minU,maxV).overlay(combinedOverlayIn).lightmap(packedLightIn).normal(normal, minn, minn, minn).endVertex();
		buffer.pos(transform, mind, maxd, mind).color(red, green, blue, alpha).tex(minU,minV).overlay(combinedOverlayIn).lightmap(packedLightIn).normal(normal, minn, maxn, minn).endVertex();
		
		// North-East
		buffer.pos(transform, maxd, mind, mind).color(red, green, blue, alpha).tex(minU,minV).overlay(combinedOverlayIn).lightmap(packedLightIn).normal(normal, maxn, minn, minn).endVertex();
		buffer.pos(transform, maxd, maxd, mind).color(red, green, blue, alpha).tex(maxU,minV).overlay(combinedOverlayIn).lightmap(packedLightIn).normal(normal, maxn, maxn, minn).endVertex();
		
		// South-West
		buffer.pos(transform, mind, maxd, maxd).color(red, green, blue, alpha).tex(minU,maxV).overlay(combinedOverlayIn).lightmap(packedLightIn).normal(normal, minn, maxn, maxn).endVertex();
		buffer.pos(transform, mind, mind, maxd).color(red, green, blue, alpha).tex(minU,minV).overlay(combinedOverlayIn).lightmap(packedLightIn).normal(normal, minn, minn, maxn).endVertex();
		
		// South-East
		buffer.pos(transform, maxd, mind, maxd).color(red, green, blue, alpha).tex(maxU,minV).overlay(combinedOverlayIn).lightmap(packedLightIn).normal(normal, maxn, minn, maxn).endVertex();
		buffer.pos(transform, maxd, maxd, maxd).color(red, green, blue, alpha).tex(maxU,maxV).overlay(combinedOverlayIn).lightmap(packedLightIn).normal(normal, maxn, maxn, maxn).endVertex();
		
		// Bottom
		buffer.pos(transform, mind, mind, mind).color(red, green, blue, alpha).tex(maxU,minV).overlay(combinedOverlayIn).lightmap(packedLightIn).normal(normal, minn, minn, minn).endVertex();
		buffer.pos(transform, maxd, mind, mind).color(red, green, blue, alpha).tex(minU,minV).overlay(combinedOverlayIn).lightmap(packedLightIn).normal(normal, maxn, minn, minn).endVertex();
		// --
		buffer.pos(transform, maxd, mind, mind).color(red, green, blue, alpha).tex(minU,minV).overlay(combinedOverlayIn).lightmap(packedLightIn).normal(normal, maxn, minn, minn).endVertex();
		buffer.pos(transform, maxd, mind, maxd).color(red, green, blue, alpha).tex(minU,maxV).overlay(combinedOverlayIn).lightmap(packedLightIn).normal(normal, maxn, minn, maxn).endVertex();
		// --
		buffer.pos(transform, maxd, mind, maxd).color(red, green, blue, alpha).tex(minU,maxV).overlay(combinedOverlayIn).lightmap(packedLightIn).normal(normal, maxn, minn, maxn).endVertex();
		buffer.pos(transform, mind, mind, maxd).color(red, green, blue, alpha).tex(maxU,maxV).overlay(combinedOverlayIn).lightmap(packedLightIn).normal(normal, minn, minn, maxn).endVertex();
		// -- 
		buffer.pos(transform, mind, mind, maxd).color(red, green, blue, alpha).tex(maxU,maxV).overlay(combinedOverlayIn).lightmap(packedLightIn).normal(normal, minn, minn, maxn).endVertex();
		buffer.pos(transform, mind, mind, mind).color(red, green, blue, alpha).tex(maxU,minV).overlay(combinedOverlayIn).lightmap(packedLightIn).normal(normal, minn, minn, minn).endVertex();
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
	public static final void drawEllipse(float horizontalRadius, float verticalRadius, int points, MatrixStack matrixStackIn, IVertexBuilder buffer, int packedLightIn, float red, float green, float blue, float alpha) {
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
	public static final void drawEllipse(float horizontalRadius, float verticalRadius, int points, float rotationPercent, MatrixStack matrixStackIn, IVertexBuilder buffer, int packedLightIn, float red, float green, float blue, float alpha) {
		
		final double angleOffset = rotationPercent * Math.PI;
		final Matrix4f transform = matrixStackIn.getLast().getMatrix();
		final Matrix3f normal = matrixStackIn.getLast().getNormal();
		
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
				
				buffer.pos(transform, vx, vy, 0f).color(red, green, blue, alpha).tex(u, v).overlay(OverlayTexture.NO_OVERLAY).lightmap(packedLightIn).normal(normal, 0, 0, -1f).endVertex();
				
			}
		}
	}
	
	public static final void drawOrb(MatrixStack matrixStackIn, IVertexBuilder buffer, int combinedLightIn, int combinedOverlayIn, float red, float green, float blue, float alpha,
			int rows, int columns, float xRadius, float yRadius, float zRadius) {
		final Matrix4f transform = matrixStackIn.getLast().getMatrix();
		final Matrix3f normal = matrixStackIn.getLast().getNormal();
		
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
					buffer.pos(transform, px1, py1, pz1).color(red, green, blue, alpha).tex(1, 1).overlay(combinedOverlayIn).lightmap(combinedLightIn).normal(normal, nx1, ny1, nz1).endVertex();
					buffer.pos(transform, px0, py0, pz0).color(red, green, blue, alpha).tex(1, 0).overlay(combinedOverlayIn).lightmap(combinedLightIn).normal(normal, nx0, ny0, nz0).endVertex();
				}

				if (j != columns) {
					buffer.pos(transform, px0, py0, pz0).color(red, green, blue, alpha).tex(0, 0).overlay(combinedOverlayIn).lightmap(combinedLightIn).normal(normal, nx0, ny0, nz0).endVertex();
					buffer.pos(transform, px1, py1, pz1).color(red, green, blue, alpha).tex(0, 1).overlay(combinedOverlayIn).lightmap(combinedLightIn).normal(normal, nx1, ny1, nz1).endVertex();
				}
			}
		}
	}
	
	public static final void renderLine(MatrixStack matrixStackIn, IVertexBuilder buffer, Vector3d start, Vector3d end,
			//float u1, float v1, float u2, float v2,
			int combinedOverlayIn, int combinedLightIn,
			float red, float green, float blue, float alpha) {
		renderLine(matrixStackIn, buffer, start, end, 1f,
				combinedOverlayIn, combinedLightIn,
				red, green, blue, alpha);
	}
	
	public static final void renderLine(MatrixStack matrixStackIn, IVertexBuilder buffer, Vector3d start, Vector3d end,
			//float u1, float v1, float u2, float v2,
			float width,
			int combinedOverlayIn, int combinedLightIn,
			float red, float green, float blue, float alpha) {
		
		final Vector3d diff = end.subtract(start);
		
		matrixStackIn.push();
		matrixStackIn.translate(start.getX(), start.getY(), start.getZ());
		final Matrix4f transform = matrixStackIn.getLast().getMatrix();
		final Matrix3f normal = matrixStackIn.getLast().getNormal();
		RenderSystem.lineWidth(width);
		buffer.pos(transform, 0, 0, 0).color(red, green, blue, alpha).tex(0, 0).overlay(combinedOverlayIn).lightmap(combinedLightIn).normal(normal, 0, 1, 0).endVertex();
		buffer.pos(transform, (float) diff.getX(), (float) diff.getY(), (float) diff.getZ()).color(red, green, blue, alpha).tex(1, 1).overlay(combinedOverlayIn).lightmap(combinedLightIn).normal(normal, 0, 1, 0).endVertex();
		//RenderSystem.lineWidth(1f);
		matrixStackIn.pop();
	}
	
	public static final void renderCurve(MatrixStack matrixStackIn, IVertexBuilder buffer, Vector3d start, ICurve3d curve, int segments,
			//float u1, float v1, float u2, float v2,
			int combinedOverlayIn, int combinedLightIn,
			float red, float green, float blue, float alpha) {
		Vector3d last = curve.getPosition(0f);

		matrixStackIn.push();
		matrixStackIn.translate(start.getX(), start.getY(), start.getZ());
		final Matrix4f transform = matrixStackIn.getLast().getMatrix();
		final Matrix3f normal = matrixStackIn.getLast().getNormal();
		for (int i = 1; i <= segments; i++) {
			final float nextProg = ((float) i / (float) segments);
			Vector3d next = curve.getPosition(nextProg);
			
			buffer.pos(transform, (float) last.getX(), (float) last.getY(), (float) last.getZ()).color(red, green, blue, alpha).tex(0, 0).overlay(combinedOverlayIn).lightmap(combinedLightIn).normal(normal, 0, 1, 0).endVertex();
			buffer.pos(transform, (float) next.getX(), (float) next.getY(), (float) next.getZ()).color(red, green, blue, alpha).tex(1, 1).overlay(combinedOverlayIn).lightmap(combinedLightIn).normal(normal, 0, 1, 0).endVertex();
			
			last = next;
		}
		matrixStackIn.pop();
	}
	
	public static final void renderHorizontalRibbon(MatrixStack matrixStackIn, IVertexBuilder buffer, Vector3d start, ICurve3d curve, int segments,
			float width, float texOffsetProg,
			int combinedOverlayIn, int combinedLightIn,
			float red, float green, float blue, float alpha) {
		
		Function<Vector3d, Vector3d> widthMapper = (segmentDiff) -> segmentDiff.mul(1, 0, 1).normalize().scale(width/2).rotateYaw(-90f);
		
		renderRibbon(matrixStackIn, buffer, start, curve, segments, widthMapper, texOffsetProg,
				combinedOverlayIn, combinedLightIn, red, green, blue, alpha);
	}
	
	public static final void renderVerticalRibbon(MatrixStack matrixStackIn, IVertexBuilder buffer, Vector3d start, ICurve3d curve, int segments,
			float width, float texOffsetProg,
			int combinedOverlayIn, int combinedLightIn,
			float red, float green, float blue, float alpha) {
		
		Function<Vector3d, Vector3d> widthMapper = (segmentDiff) -> new Vector3d(0, width/2, 0);
		
		renderRibbon(matrixStackIn, buffer, start, curve, segments, widthMapper, texOffsetProg,
				combinedOverlayIn, combinedLightIn, red, green, blue, alpha);
	}
	
	public static final void renderRibbon(MatrixStack matrixStackIn, IVertexBuilder buffer, Vector3d start, ICurve3d curve, int segments,
			Function<Vector3d, Vector3d> widthSupplier, float texOffsetProg,
			int combinedOverlayIn, int combinedLightIn,
			float red, float green, float blue, float alpha) {
		
		Vector3d last = curve.getPosition(0f);
		//float lastV = lowV; 

		matrixStackIn.push();
		matrixStackIn.translate(start.getX(), start.getY(), start.getZ());
		final Matrix4f transform = matrixStackIn.getLast().getMatrix();
		final Matrix3f normal = matrixStackIn.getLast().getNormal();
		for (int i = 1; i <= segments; i++) {
			final float nextProg = ((float) i / (float) segments);
			final Vector3d next = curve.getPosition(nextProg);
			final Vector3d segmentDiff = next.subtract(last);
			final Vector3d widthOffset = widthSupplier.apply(segmentDiff);
			//final float nextV = lastV == lowV ? highV : lowV;
			
			buffer.pos(transform, (float) (last.getX() - widthOffset.getX()), (float) (last.getY() - widthOffset.getY()), (float) (last.getZ() - widthOffset.getZ())).color(red, green, blue, alpha).tex(0, 1 + texOffsetProg).overlay(combinedOverlayIn).lightmap(combinedLightIn).normal(normal, 0, 1, 0).endVertex();
			buffer.pos(transform, (float) (last.getX() + widthOffset.getX()), (float) (last.getY() + widthOffset.getY()), (float) (last.getZ() + widthOffset.getZ())).color(red, green, blue, alpha).tex(1, 1 + texOffsetProg).overlay(combinedOverlayIn).lightmap(combinedLightIn).normal(normal, 0, 1, 0).endVertex();
			
			buffer.pos(transform, (float) (next.getX() + widthOffset.getX()), (float) (next.getY() + widthOffset.getY()), (float) (next.getZ() + widthOffset.getZ())).color(red, green, blue, alpha).tex(1, 0 + texOffsetProg).overlay(combinedOverlayIn).lightmap(combinedLightIn).normal(normal, 0, 1, 0).endVertex();
			buffer.pos(transform, (float) (next.getX() - widthOffset.getX()), (float) (next.getY() - widthOffset.getY()), (float) (next.getZ() - widthOffset.getZ())).color(red, green, blue, alpha).tex(0, 0 + texOffsetProg).overlay(combinedOverlayIn).lightmap(combinedLightIn).normal(normal, 0, 1, 0).endVertex();
			
			last = next;
			//lastV = nextV;
		}
		matrixStackIn.pop();
	}
	
	public static final void drawNameplate(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, Entity entityIn, String info, FontRenderer fonter, int packedLightIn, float yOffsetExtra, ActiveRenderInfo renderInfo) {
		final float offsetY = yOffsetExtra + (entityIn == null ? 0 : (entityIn.getHeight() + 0.5f));
		final boolean discrete = entityIn == null ? false : entityIn.isDiscrete();
		drawNameplate(matrixStackIn, bufferIn, info, fonter, packedLightIn, offsetY, discrete, renderInfo);
	}
	
	public static final void drawNameplate(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, String info, FontRenderer fonter, int packedLightIn, float yOffset, boolean discrete, ActiveRenderInfo renderInfo) {
		matrixStackIn.push();
		matrixStackIn.translate(0.0D, yOffset, 0.0D);
		matrixStackIn.rotate(renderInfo.getRotation());
		drawNameplate(matrixStackIn, bufferIn, info, fonter, packedLightIn, discrete);
		matrixStackIn.pop();
	}
	
	public static final void drawNameplate(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, String info, FontRenderer fontrenderer, int packedLightIn, boolean discrete) {
		matrixStackIn.push();
        matrixStackIn.scale(-0.025F, -0.025F, 0.025F);
		
		final Minecraft mc = Minecraft.getInstance();
		final Matrix4f matrix4f = matrixStackIn.getLast().getMatrix();
		float f1 = mc.gameSettings.getTextBackgroundOpacity(0.25F);
		int j = (int)(f1 * 255.0F) << 24;
		float f2 = (float)(-fontrenderer.getStringWidth(info) / 2);
		fontrenderer.renderString(info, f2, 0, 553648127, false, matrix4f, bufferIn, !discrete, j, packedLightIn);
		if (!discrete) {
			fontrenderer.renderString(info, f2, 0, -1, false, matrix4f, bufferIn, false, 0, packedLightIn);
		}
		matrixStackIn.pop();
	}

	public static int drawSplitString(MatrixStack matrixStackIn, FontRenderer fonter, String str, int x, int y, int width, int infoColor) {
		int offset = 0;
		int lineWidth = fonter.getStringWidth(str);
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
					
					subWidth = fonter.getStringWidth(lineBuffer.toString());
					hasWord = true;
				} else {
					wordBuffer.append(c);
					
					if (subWidth + fonter.getStringWidth(wordBuffer.toString()) >= width) {
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
			
			fonter.drawString(matrixStackIn, lineBuffer.toString(), x, y + offset, infoColor);
			str = str.substring(i);
			lineWidth = fonter.getStringWidth(str);
			offset += fonter.FONT_HEIGHT;
		}
		
		fonter.drawString(matrixStackIn, str, x, y + offset, infoColor);
		return offset;
	}
	
	// Copies of Vanilla's blit but with color support
	public static final void blit(MatrixStack matrixStack, int x, int y, int blitOffset, int width, int height, TextureAtlasSprite sprite, float red, float green, float blue, float alpha) {
		innerBlit(matrixStack.getLast().getMatrix(), x, x + width, y, y + height, blitOffset, sprite.getMinU(), sprite.getMaxU(), sprite.getMinV(), sprite.getMaxV(), red, green, blue, alpha);
	}

	public static final void blit(MatrixStack matrixStack, int x, int y, int uOffset, int vOffset, int uWidth, int vHeight, float red, float green, float blue, float alpha) {
		blit(matrixStack, x, y, 0, (float)uOffset, (float)vOffset, uWidth, vHeight, 256, 256, red, green, blue, alpha);
	}

	public static final void blit(MatrixStack matrixStack, int x, int y, int blitOffset, float uOffset, float vOffset, int uWidth, int vHeight, int textureHeight, int textureWidth, float red, float green, float blue, float alpha) {
		innerBlit(matrixStack, x, x + uWidth, y, y + vHeight, blitOffset, uWidth, vHeight, uOffset, vOffset, textureWidth, textureHeight, red, green, blue, alpha);
	}

	public static final void blit(MatrixStack matrixStack, int x, int y, int width, int height, float uOffset, float vOffset, int uWidth, int vHeight, int textureWidth, int textureHeight, float red, float green, float blue, float alpha) {
		innerBlit(matrixStack, x, x + width, y, y + height, 0, uWidth, vHeight, uOffset, vOffset, textureWidth, textureHeight, red, green, blue, alpha);
	}

	public static final void blit(MatrixStack matrixStack, int x, int y, float uOffset, float vOffset, int width, int height, int textureWidth, int textureHeight, float red, float green, float blue, float alpha) {
		blit(matrixStack, x, y, width, height, uOffset, vOffset, width, height, textureWidth, textureHeight, red, green, blue, alpha);
	}

	private static final void innerBlit(MatrixStack matrixStack, int x1, int x2, int y1, int y2, int blitOffset, int uWidth, int vHeight, float uOffset, float vOffset, int textureWidth, int textureHeight, float red, float green, float blue, float alpha) {
		innerBlit(matrixStack.getLast().getMatrix(), x1, x2, y1, y2, blitOffset, (uOffset + 0.0F) / (float)textureWidth, (uOffset + (float)uWidth) / (float)textureWidth, (vOffset + 0.0F) / (float)textureHeight, (vOffset + (float)vHeight) / (float)textureHeight, red, green, blue, alpha);
	}

	private static final void innerBlit(Matrix4f matrix, int x1, int x2, int y1, int y2, int blitOffset, float minU, float maxU, float minV, float maxV, float red, float green, float blue, float alpha) {
		BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();
		bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR_TEX);
		bufferbuilder.pos(matrix, (float)x1, (float)y2, (float)blitOffset).color(red, green, blue, alpha).tex(minU, maxV).endVertex();
		bufferbuilder.pos(matrix, (float)x2, (float)y2, (float)blitOffset).color(red, green, blue, alpha).tex(maxU, maxV).endVertex();
		bufferbuilder.pos(matrix, (float)x2, (float)y1, (float)blitOffset).color(red, green, blue, alpha).tex(maxU, minV).endVertex();
		bufferbuilder.pos(matrix, (float)x1, (float)y1, (float)blitOffset).color(red, green, blue, alpha).tex(minU, minV).endVertex();
		bufferbuilder.finishDrawing();
		RenderSystem.enableAlphaTest();
		WorldVertexBufferUploader.draw(bufferbuilder);
	}
	
	// Should be somewhere else?
	@OnlyIn(Dist.CLIENT)
	public static final ModelResourceLocation makeDefaultModelLocation(ResourceLocation loc) {
		return new ModelResourceLocation(loc, "");
	}

	public static final MatrixStack makeNewMatrixStack(ActiveRenderInfo renderInfo) {
		MatrixStack stack = new MatrixStack();
		
		// World renderer doesn't start with an identity stack; it applies some rotations based on
		// the camera. These are copied out of GameRenderer#RenderWorld right before calling "this.mc.worldRenderer.updateCameraAndRender"
		{
			stack.rotate(Vector3f.ZP.rotationDegrees(0));
			stack.rotate(Vector3f.XP.rotationDegrees(renderInfo.getPitch()));
			stack.rotate(Vector3f.YP.rotationDegrees(renderInfo.getYaw() + 180.0F));
		}
		return stack;
	}
	
}
