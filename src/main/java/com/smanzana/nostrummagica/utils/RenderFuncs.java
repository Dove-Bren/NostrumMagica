package com.smanzana.nostrummagica.utils;

import java.util.List;
import java.util.Random;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Quaternion;
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
	
	public static void RenderModelWithColorNoBatch(MatrixStack stack, IBakedModel model, int color, int combinedLight) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK); // quads hardcode this internally. If not, would need to convert when rendering quad?
		
		RenderModelWithColor(stack, buffer, model, color, combinedLight);
		tessellator.draw();
	}
	
//	private static final Vector3f Vec3fZero = new Vector3f();
	
	public static void RenderModelWithColor(MatrixStack stack, IVertexBuilder buffer, IBakedModel model, int color, int combinedLight) {
//		RenderModelWithColor(stack, buffer, model, color, combinedLight, Vec3fZero);
		final float colors[] = ColorUtil.ARGBToColor(color);
		RenderModel(stack, buffer, model, combinedLight, colors[0], colors[1], colors[2], colors[3]);
	}
	
	private static final Random RenderModelRandom = new Random();
	
//	public static void RenderModelWithColor(MatrixStack stack, IVertexBuilder buffer, IBakedModel model, int color, int combinedLight, Vector3f offset) {
//		final float colors[] = ColorUtil.ARGBToColor(color);
//		RenderModel(stack, buffer, model, combinedLight, colors[0], colors[1], colors[2], colors[3], offset);
//	}
	
	public static void RenderModel(MatrixStack stack, IVertexBuilder buffer, IBakedModel model, int combinedLight, float red, float green, float blue, float alpha) {
		RenderModel(stack.getLast(), buffer, model, combinedLight, red, green, blue, alpha);
	}
	
	public static void RenderModel(MatrixStack.Entry stackLast, IVertexBuilder buffer, IBakedModel model, int combinedLight, float red, float green, float blue, float alpha) {
		
		for(Direction side : Direction.values()) {
			List<BakedQuad> quads = model.getQuads(null, side, RenderRandom(RenderModelRandom), EmptyModelData.INSTANCE);
			if(!quads.isEmpty()) 
				for(BakedQuad quad : quads) {
					buffer.addVertexData(stackLast, quad, red, green, blue, alpha, combinedLight, OverlayTexture.NO_OVERLAY, true);
//					LightUtil.renderQuadColor(buffer, quad, color);
				}
		}
		List<BakedQuad> quads = model.getQuads(null, null, RenderRandom(RenderModelRandom), EmptyModelData.INSTANCE);
		if(!quads.isEmpty()) {
			for(BakedQuad quad : quads) 
				buffer.addVertexData(stackLast, quad, red, green, blue, alpha, combinedLight, OverlayTexture.NO_OVERLAY, true);
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
	
	/**
	 * Renders an item. Basically a wrapper for rendering classes.
	 * Making now because transform type is deprecated but required :P and I'd rather have one warning than a bunch.
	 * @param world
	 * @param stack
	 */
	public static void ItemRenderer(ItemStack stack, MatrixStack matrix, IRenderTypeBuffer typeBuffer, int combinedLight) {
		ItemRenderer(stack, matrix, typeBuffer, combinedLight, OverlayTexture.NO_OVERLAY);
	}
	
	public static void ItemRenderer(ItemStack stack, MatrixStack matrix, IRenderTypeBuffer typeBuffer, int combinedLight, int combinedOverlay) {
		Minecraft.getInstance().getItemRenderer()
			.renderItem(stack, TransformType.GROUND, combinedLight, combinedOverlay, matrix, typeBuffer);
	}
	
	/**
	 * Render an item with default blending and lighting.
	 * @param world
	 * @param stack
	 */
	public static void renderItemStandard(ItemStack stack, MatrixStack matrix, IRenderTypeBuffer typeBuffer, int combinedLight, int combinedOverlay) {
//		GlStateManager.enableBlend();
//		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
//		GlStateManager.disableLighting();
//		GlStateManager.enableAlphaTest();
//		GlStateManager.color4f(1.0f, 1.0f, 1.0f, 1.0f);
//
//		RenderHelper.enableStandardItemLighting();
		
		ItemRenderer(stack, matrix, typeBuffer, combinedLight, combinedOverlay);
	}
	
	// can use blit here: blit(x, y, 0, u, v, width, height, texWidth, texHeight)
	public static void drawModalRectWithCustomSizedTexture(MatrixStack stack, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight) {
		Screen.blit(stack, x, y, u, v, width, height, textureWidth, textureHeight);
//		float f = 1.0F / textureWidth;
//		float f1 = 1.0F / textureHeight;
//		Tessellator tessellator = Tessellator.getInstance();
//		BufferBuilder bufferbuilder = tessellator.getBuffer();
//		bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
//		bufferbuilder.pos((double)x, (double)(y + height), 0.0D).tex((double)(u * f), (double)((v + (float)height) * f1)).endVertex();
//		bufferbuilder.pos((double)(x + width), (double)(y + height), 0.0D).tex((double)((u + (float)width) * f), (double)((v + (float)height) * f1)).endVertex();
//		bufferbuilder.pos((double)(x + width), (double)y, 0.0D).tex((double)((u + (float)width) * f), (double)(v * f1)).endVertex();
//		bufferbuilder.pos((double)x, (double)y, 0.0D).tex((double)(u * f), (double)(v * f1)).endVertex();
//		tessellator.draw();
	}
	
	// Different from the above in that this includes scaling on what's drawn
	public static void drawScaledCustomSizeModalRect(MatrixStack stack, int x, int y, float u, float v, int uWidth, int vHeight, int width, int height, float tileWidth, float tileHeight) {
		float f = 1.0F / tileWidth;
		float f1 = 1.0F / tileHeight;
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
		bufferbuilder.pos((double)x, (double)(y + height), 0.0D).tex(u * f, (v + (float)vHeight) * f1).endVertex();
		bufferbuilder.pos((double)(x + width), (double)(y + height), 0.0D).tex((u + (float)uWidth) * f, (v + (float)vHeight) * f1).endVertex();
		bufferbuilder.pos((double)(x + width), (double)y, 0.0D).tex((u + (float)uWidth) * f, v * f1).endVertex();
		bufferbuilder.pos((double)x, (double)y, 0.0D).tex((u * f), (v * f1)).endVertex();
		tessellator.draw();
	}
	
	public static void drawRect(MatrixStack stack, int minX, int minY, int maxX, int maxY, int colorARGB) {
		AbstractGui.fill(stack, minX, minY, maxX, maxY, colorARGB);
	}

	// Called fillGradient
//	public static void drawGradientRect(int left, int top, int right, int bottom, int startColor, int endColor) {
//		float f = (float)(startColor >> 24 & 255) / 255.0F;
//		float f1 = (float)(startColor >> 16 & 255) / 255.0F;
//		float f2 = (float)(startColor >> 8 & 255) / 255.0F;
//		float f3 = (float)(startColor & 255) / 255.0F;
//		float f4 = (float)(endColor >> 24 & 255) / 255.0F;
//		float f5 = (float)(endColor >> 16 & 255) / 255.0F;
//		float f6 = (float)(endColor >> 8 & 255) / 255.0F;
//		float f7 = (float)(endColor & 255) / 255.0F;
//		GlStateManager.disableTexture2D();
//		GlStateManager.enableBlend();
//		GlStateManager.disableAlpha();
//		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
//		GlStateManager.shadeModel(7425);
//		Tessellator tessellator = Tessellator.getInstance();
//		BufferBuilder bufferbuilder = tessellator.getBuffer();
//		bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
//		bufferbuilder.pos((double)right, (double)top, (double)this.zLevel).color(f1, f2, f3, f).endVertex();
//		bufferbuilder.pos((double)left, (double)top, (double)this.zLevel).color(f1, f2, f3, f).endVertex();
//		bufferbuilder.pos((double)left, (double)bottom, (double)this.zLevel).color(f5, f6, f7, f4).endVertex();
//		bufferbuilder.pos((double)right, (double)bottom, (double)this.zLevel).color(f5, f6, f7, f4).endVertex();
//		tessellator.draw();
//		GlStateManager.shadeModel(7424);
//		GlStateManager.disableBlend();
//		GlStateManager.enableAlpha();
//		GlStateManager.enableTexture2D();
//    }
	
//	public static final void renderSpaceQuad(MatrixStack stack, IVertexBuilder buffer, double relX, double relY, double relZ,
//			double radius,
//			float red, float green, float blue, float alpha) {
//		// Billboard no rot
//		buffer.pos(relX - radius, relY - radius, relZ)
//			.tex(0, 0)
//			.color(red, green, blue, alpha)
//			.normal(0, 0, 1).endVertex();
//		buffer.pos(relX - radius, relY + radius, relZ)
//			.tex(0, 1)
//			.color(red, green, blue, alpha)
//			.normal(0, 0, 1).endVertex();
//		buffer.pos(relX + radius, relY + radius, relZ)
//			.tex(1, 1)
//			.color(red, green, blue, alpha)
//			.normal(0, 0, 1).endVertex();
//		buffer.pos(relX + radius, relY - radius, relZ)
//			.tex(1, 0)
//			.color(red, green, blue, alpha)
//			.normal(0, 0, 1).endVertex();
//	}
	
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
		buffer.pos(transform, avector3f[0].getX(), avector3f[0].getY(), avector3f[0].getZ()).normal(normal, 0, 0, 1).tex(uMax, vMax).color(red, green, blue, alpha).lightmap(combinedLightmapIn).overlay(combinedOverlayIn).endVertex();
		buffer.pos(transform, avector3f[1].getX(), avector3f[1].getY(), avector3f[1].getZ()).normal(normal, 0, 0, 1).tex(uMax, vMin).color(red, green, blue, alpha).lightmap(combinedLightmapIn).overlay(combinedOverlayIn).endVertex();
		buffer.pos(transform, avector3f[2].getX(), avector3f[2].getY(), avector3f[2].getZ()).normal(normal, 0, 0, 1).tex(uMin, vMin).color(red, green, blue, alpha).lightmap(combinedLightmapIn).overlay(combinedOverlayIn).endVertex();
		buffer.pos(transform, avector3f[3].getX(), avector3f[3].getY(), avector3f[3].getZ()).normal(normal, 0, 0, 1).tex(uMin, vMax).color(red, green, blue, alpha).lightmap(combinedLightmapIn).overlay(combinedOverlayIn).endVertex();
		
//		buffer.pos(relX - (rX * radius) - (rXY * radius), relY - (rZ * radius), relZ - (rYZ * radius) - (rXZ * radius))
//			.tex(0, 0)
//			.color(red, green, blue, alpha)
//			.normal(0, 0, 1).endVertex();
//		buffer.pos(relX - (rX * radius) + (rXY * radius), relY + (rZ * radius), relZ - (rYZ * radius) + (rXZ * radius))
//			.tex(0, 1)
//			.color(red, green, blue, alpha)
//			.normal(0, 0, 1).endVertex();
//		buffer.pos(relX + (rX * radius) + (rXY * radius), relY + (rZ * radius), relZ + (rYZ * radius) + (rXZ * radius))
//			.tex(1, 1)
//			.color(red, green, blue, alpha)
//			.normal(0, 0, 1).endVertex();
//		buffer.pos(relX + (rX * radius) - (rXY * radius), relY - (rZ * radius), relZ + (rYZ * radius) - (rXZ * radius))
//			.tex(1, 0)
//			.color(red, green, blue, alpha)
//			.normal(0, 0, 1).endVertex();
//		
//		{
//			buffer.pos(relX - (rX * radius) - (rXY * radius), relY - (rZ * radius), relZ - (rYZ * radius) - (rXZ * radius))
//				.tex(0, 0)
//				.color(red, green, blue, alpha)
//				.normal(0, 0, -1).endVertex();
//			buffer.pos(relX - (rX * radius) + (rXY * radius), relY + (rZ * radius), relZ - (rYZ * radius) + (rXZ * radius))
//				.tex(0, 1)
//				.color(red, green, blue, alpha)
//				.normal(0, 0, -1).endVertex();
//			buffer.pos(relX + (rX * radius) + (rXY * radius), relY + (rZ * radius), relZ + (rYZ * radius) + (rXZ * radius))
//				.tex(1, 1)
//				.color(red, green, blue, alpha)
//				.normal(0, 0, -1).endVertex();
//			buffer.pos(relX + (rX * radius) - (rXY * radius), relY - (rZ * radius), relZ + (rYZ * radius) - (rXZ * radius))
//				.tex(1, 0)
//				.color(red, green, blue, alpha)
//				.normal(0, 0, -1).endVertex();
//		}
	}
	
	public static final void renderSpaceQuadFacingCamera(MatrixStack stack, IVertexBuilder buffer, ActiveRenderInfo renderInfo,
			float radius,
			int lightmap, int overlay,
			float red, float green, float blue, float alpha) {
//		float rotationX = MathHelper.cos(renderInfo.getYaw() * ((float)Math.PI / 180F));
//		float rotationYZ = MathHelper.sin(renderInfo.getYaw() * ((float)Math.PI / 180F));
//		float rotationXY = -rotationYZ * MathHelper.sin(renderInfo.getPitch() * ((float)Math.PI / 180F));
//		float rotationXZ = rotationX * MathHelper.sin(renderInfo.getPitch() * ((float)Math.PI / 180F));
//		float rotationZ = MathHelper.cos(renderInfo.getPitch() * ((float)Math.PI / 180F));
		
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
	
	public static final void drawUnitCube(MatrixStack stack, IVertexBuilder buffer, int packedLightIn, float red, float green, float blue, float alpha) {
		drawUnitCube(stack, buffer, 0, 1, 0, 1, packedLightIn, red, green, blue, alpha);
	}
	
	public static final void drawUnitCube(MatrixStack stack, IVertexBuilder buffer, float minU, float maxU, float minV, float maxV, int packedLightIn,
			float red, float green, float blue, float alpha) {
		
		final float mind = -.5f;
		final float maxd = .5f;
		
		final float minn = -.5773f;
		final float maxn = .5773f;
		
		final Matrix4f transform = stack.getLast().getMatrix();
		final Matrix3f normal = stack.getLast().getNormal();
		
		// Top
		buffer.pos(transform, mind, maxd, mind).tex(minU,minV).normal(normal, minn, maxn, minn).color(red, green, blue, alpha).lightmap(packedLightIn).endVertex();
		buffer.pos(transform, mind, maxd, maxd).tex(minU,maxV).normal(normal, minn, maxn, maxn).color(red, green, blue, alpha).lightmap(packedLightIn).endVertex();
		buffer.pos(transform, maxd, maxd, maxd).tex(maxU,maxV).normal(normal, maxn, maxn, maxn).color(red, green, blue, alpha).lightmap(packedLightIn).endVertex();
		buffer.pos(transform, maxd, maxd, mind).tex(maxU,minV).normal(normal, maxn, maxn, minn).color(red, green, blue, alpha).lightmap(packedLightIn).endVertex();
		
		// North
		buffer.pos(transform, maxd, maxd, mind).tex(maxU,minV).normal(normal, maxn, maxn, minn).color(red, green, blue, alpha).lightmap(packedLightIn).endVertex();
		buffer.pos(transform, maxd, mind, mind).tex(maxU,maxV).normal(normal, maxn, minn, minn).color(red, green, blue, alpha).lightmap(packedLightIn).endVertex();
		buffer.pos(transform, mind, mind, mind).tex(minU,maxV).normal(normal, minn, minn, minn).color(red, green, blue, alpha).lightmap(packedLightIn).endVertex();
		buffer.pos(transform, mind, maxd, mind).tex(minU,minV).normal(normal, minn, maxn, minn).color(red, green, blue, alpha).lightmap(packedLightIn).endVertex();
		
		// East
		buffer.pos(transform, maxd, maxd, maxd).tex(maxU,maxV).normal(normal, maxn, maxn, maxn).color(red, green, blue, alpha).lightmap(packedLightIn).endVertex();
		buffer.pos(transform, maxd, mind, maxd).tex(minU,maxV).normal(normal, maxn, minn, maxn).color(red, green, blue, alpha).lightmap(packedLightIn).endVertex();
		buffer.pos(transform, maxd, mind, mind).tex(minU,minV).normal(normal, maxn, minn, minn).color(red, green, blue, alpha).lightmap(packedLightIn).endVertex();
		buffer.pos(transform, maxd, maxd, mind).tex(maxU,minV).normal(normal, maxn, maxn, minn).color(red, green, blue, alpha).lightmap(packedLightIn).endVertex();
		
		// South
		buffer.pos(transform, mind, maxd, maxd).tex(minU,maxV).normal(normal, minn, maxn, maxn).color(red, green, blue, alpha).lightmap(packedLightIn).endVertex();
		buffer.pos(transform, mind, mind, maxd).tex(minU,minV).normal(normal, minn, minn, maxn).color(red, green, blue, alpha).lightmap(packedLightIn).endVertex();
		buffer.pos(transform, maxd, mind, maxd).tex(maxU,minV).normal(normal, maxn, minn, maxn).color(red, green, blue, alpha).lightmap(packedLightIn).endVertex();
		buffer.pos(transform, maxd, maxd, maxd).tex(maxU,maxV).normal(normal, maxn, maxn, maxn).color(red, green, blue, alpha).lightmap(packedLightIn).endVertex();
		
		// West
		buffer.pos(transform, mind, maxd, mind).tex(minU,minV).normal(normal, minn, maxn, minn).color(red, green, blue, alpha).lightmap(packedLightIn).endVertex();
		buffer.pos(transform, mind, mind, mind).tex(maxU,minV).normal(normal, minn, minn, minn).color(red, green, blue, alpha).lightmap(packedLightIn).endVertex();
		buffer.pos(transform, mind, mind, maxd).tex(maxU,maxV).normal(normal, minn, minn, maxn).color(red, green, blue, alpha).lightmap(packedLightIn).endVertex();
		buffer.pos(transform, mind, maxd, maxd).tex(minU,maxV).normal(normal, minn, maxn, maxn).color(red, green, blue, alpha).lightmap(packedLightIn).endVertex();
		
		// Bottom
		buffer.pos(transform, mind, mind, mind).tex(maxU,minV).normal(normal, minn, minn, minn).color(red, green, blue, alpha).lightmap(packedLightIn).endVertex();
		buffer.pos(transform, maxd, mind, mind).tex(minU,minV).normal(normal, maxn, minn, minn).color(red, green, blue, alpha).lightmap(packedLightIn).endVertex();
		buffer.pos(transform, maxd, mind, maxd).tex(minU,maxV).normal(normal, maxn, minn, maxn).color(red, green, blue, alpha).lightmap(packedLightIn).endVertex();
		buffer.pos(transform, mind, mind, maxd).tex(maxU,maxV).normal(normal, minn, minn, maxn).color(red, green, blue, alpha).lightmap(packedLightIn).endVertex();
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
				
				buffer.pos(transform, vx, vy, 0f).tex(u, v).normal(normal, 0, 0, -1f).lightmap(packedLightIn).color(red, green, blue, alpha).endVertex();
				
			}
		}
	}
	
//	public static final float interpolateRotation(float prevYawOffset, float yawOffset, float partialTicks) {
//		return MathHelper.func_219805_h(partialTicks, prevYawOffset, yawOffset);
//	}
	
	// Should be somewhere else?
	@OnlyIn(Dist.CLIENT)
	public static final ModelResourceLocation makeDefaultModelLocation(ResourceLocation loc) {
		return new ModelResourceLocation(loc, "");
	}
	
}
