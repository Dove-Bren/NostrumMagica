package com.smanzana.nostrummagica.client.render.tile;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.smanzana.autodungeons.world.WorldKey;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.dungeon.LockedDoorBlock;
import com.smanzana.nostrummagica.client.render.NostrumRenderTypes;
import com.smanzana.nostrummagica.item.WorldKeyItem;
import com.smanzana.nostrummagica.tile.LockedDoorTileEntity;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;

public class TileEntityLockedDoorRenderer<E extends LockedDoorTileEntity> extends TileEntityRenderer<E> {

	public static final ResourceLocation TEX_GEM_LOC = new ResourceLocation(NostrumMagica.MODID, "textures/gui/brass.png");
	public static final ResourceLocation TEX_PLATE_LOC = new ResourceLocation(NostrumMagica.MODID, "textures/block/ceramic_generic.png");
	
	public TileEntityLockedDoorRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
		super(rendererDispatcherIn);
	}
	
	protected void renderChains(E tileEntityIn, double ticks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
		final MutableBoundingBox bounds = tileEntityIn.getDoorBounds();
		final float length = (float) Math.sqrt(
				Math.pow(bounds.x1 + 1 - bounds.x0, 2)
				+ Math.pow(bounds.y1 + 1 - bounds.y0, 2)
				+ Math.pow(bounds.z1 + 1 - bounds.z0, 2)
				);
		final float chainWidth = .25f;
		final float zOffset = -(chainWidth/2f);
		final IVertexBuilder buffer = bufferIn.getBuffer(NostrumRenderTypes.LOCKEDCHEST_CHAIN);
		final float yDiff = bounds.y1 + 1 - bounds.y0;
		final float hDiff;
		switch (tileEntityIn.getFace()) {
		case NORTH:
		default:
			hDiff = bounds.x1 + 1 - bounds.x0;
			break;
		case EAST:
			hDiff = bounds.z1 + 1 - bounds.z0;
			break;
		case SOUTH:
			hDiff = bounds.x1 + 1 - bounds.x0;
			break;
		case WEST:
			hDiff = bounds.z1 + 1 - bounds.z0;
			break;
		}
		
		final float angle = (float) Math.atan2(hDiff, yDiff);
		
		matrixStackIn.pushPose();
		matrixStackIn.translate(0, yDiff/2, zOffset);
		
		matrixStackIn.pushPose();
		matrixStackIn.mulPose(Vector3f.ZP.rotation(angle));
		matrixStackIn.translate(0, -length/2, 0);
		this.renderChain(ticks, length, chainWidth, 20, matrixStackIn, buffer, combinedLightIn, combinedOverlayIn);
		matrixStackIn.popPose();
		
		matrixStackIn.pushPose();
		matrixStackIn.mulPose(Vector3f.ZN.rotation(angle));
		matrixStackIn.translate(0, -length/2, 0);
		this.renderChain(ticks, length, chainWidth, 20, matrixStackIn, buffer, combinedLightIn, combinedOverlayIn);
		matrixStackIn.popPose();
		
		matrixStackIn.popPose();
	}
	
	protected void renderChain(double ticks, float length, float linkWidth, int points, MatrixStack matrixStackIn, IVertexBuilder buffer, int combinedLightIn, int combinedOverlayIn) {
		final float colorPeriod = 140;
		final float colorProg = (float) (1 - ((ticks % colorPeriod) / colorPeriod));
		final Matrix4f transform = matrixStackIn.last().pose();
		
		
		for (int i = 0; i < points; i++)
		{
			final float prog1 = ((float) i / (float)points);
			final float px1 = 0;
			final float py1 = length * prog1;
			final float pz1 = 0;
			final float colorDist1 = Math.min(Math.abs(colorProg - prog1), 1 - Math.abs(colorProg - prog1));
			final float alpha1 = Math.max(.2f, 1f - 4f * colorDist1);
			final float v1 = (i % 2 == 0 ? 0 : 1);
			final float offZ1 = linkWidth/2;
			final float offX1 = linkWidth/2;
			
			final float prog2 = ((float) (i+1) / (float)points);
			final float px2 = 0;
			final float py2 = length * prog2;
			final float pz2 = 0;
			final float colorDist2 = Math.min(Math.abs(colorProg - prog2), 1 - Math.abs(colorProg - prog2));
			final float alpha2 = Math.max(.2f, 1f - 4f * colorDist2);
			final float v2 = ((i+1) % 2 == 0 ? 0 : 1);
			final float offZ2 = linkWidth/2;
			final float offX2 = linkWidth/2;
			
			buffer.vertex(transform, px1 - offX1, py1, pz1).color(1f, 1f, 1f, alpha1).uv(0, v1).uv2(combinedLightIn).endVertex();
			buffer.vertex(transform, px1 + offX1, py1, pz1).color(1f, 1f, 1f, alpha1).uv(1, v1).uv2(combinedLightIn).endVertex();
			buffer.vertex(transform, px2 + offX2, py2, pz2).color(1f, 1f, 1f, alpha2).uv(1, v2).uv2(combinedLightIn).endVertex();
			buffer.vertex(transform, px2 - offX2, py2, pz2).color(1f, 1f, 1f, alpha2).uv(0, v2).uv2(combinedLightIn).endVertex();
			
			// Cross quad
			buffer.vertex(transform, px1, py1, pz1 - offZ1).color(1f, 1f, 1f, alpha1).uv(0, v1 + .5f).uv2(combinedLightIn).endVertex();
			buffer.vertex(transform, px1, py1, pz1 + offZ1).color(1f, 1f, 1f, alpha1).uv(1, v1 + .5f).uv2(combinedLightIn).endVertex();
			buffer.vertex(transform, px2, py2, pz2 + offZ2).color(1f, 1f, 1f, alpha2).uv(1, v2 + .5f).uv2(combinedLightIn).endVertex();
			buffer.vertex(transform, px2, py2, pz2 - offZ2).color(1f, 1f, 1f, alpha2).uv(0, v2 + .5f).uv2(combinedLightIn).endVertex();
		}
	}
	
	protected void renderLock(E tileEntityIn, double ticks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
		final MutableBoundingBox bounds = tileEntityIn.getDoorBounds();
		final float yDiff = bounds.y1 + 1 - bounds.y0;
		
		final float width = .75f;
		final float height = .75f;
		
		final float glow;
		if (tileEntityIn.getBlockState().getValue(LockedDoorBlock.UNLOCKABLE)) {
			final double glowPeriod = 20;
			final double glowProg = ((ticks % glowPeriod) / glowPeriod);
			glow = .15f * (float) Math.sin(glowProg * 2 * Math.PI);
		} else {
			final double glowPeriod = 60;
			final double glowProg = ((ticks % glowPeriod) / glowPeriod); 
			glow = .5f + (.15f * (float) Math.sin(glowProg * 2 * Math.PI));
		}
		
		final int colorRGB = tileEntityIn.getColor().getColorValue();
		final float red = ((float) ((colorRGB >> 16) & 0xFF) / 255f);
		final float green = ((float) ((colorRGB >> 8) & 0xFF) / 255f);
		final float blue = ((float) ((colorRGB >> 0) & 0xFF) / 255f);
		
		IVertexBuilder buffer = bufferIn.getBuffer(NostrumRenderTypes.LOCKEDCHEST_LOCK);
		
		matrixStackIn.pushPose();
		matrixStackIn.translate(0, yDiff/2, -.3f);
		matrixStackIn.translate(-width/2, -height/2, 0); // center
		
		// Wiggle a bit
		matrixStackIn.scale(width, height, 1f);
		RenderFuncs.drawScaledCustomSizeModalRect(matrixStackIn, buffer, 0, 0, 0, 0, 16, 16, 1, 1, 16, 16, red, green, blue, .25f + glow);
		
		buffer = bufferIn.getBuffer(RenderType.entityCutout(NostrumMagica.Loc("textures/models/crystal_blank.png")));
		matrixStackIn.translate(.5f, .5f, -.0255f);
		matrixStackIn.scale(.15f, .3f, .05f);
		RenderFuncs.drawUnitCube(matrixStackIn, buffer, combinedLightIn, combinedOverlayIn, .6f, .6f, .6f, .25f + glow);
		
		matrixStackIn.popPose();
	}
	
	@Override
	public void render(E tileEntityIn, float partialTicks, MatrixStack matrixStackIn,
			IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
		final Minecraft mc = Minecraft.getInstance();
		final double time = (double)tileEntityIn.getLevel().getGameTime() + partialTicks;
		
		matrixStackIn.pushPose();
		
		// Render centered on bottom-center of door, not TE (in case they're different)
		{
			BlockPos pos = tileEntityIn.getBlockPos();
			MutableBoundingBox bounds = tileEntityIn.getDoorBounds();
			Vector3d centerPos = new Vector3d(
					bounds.x0 + (float) (bounds.x1 + 1 - bounds.x0) / 2,
					bounds.y0,
					bounds.z0 + (float) (bounds.z1 + 1 - bounds.z0) / 2
					);
			matrixStackIn.translate(centerPos.x() - pos.getX(), centerPos.y() - pos.getY(), centerPos.z() - pos.getZ());
		}
		
		final float rotY = tileEntityIn.getFace().getOpposite().toYRot();
		matrixStackIn.mulPose(Vector3f.YN.rotationDegrees(rotY));
		
		this.renderChains(tileEntityIn, time, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);
		
		this.renderLock(tileEntityIn, time, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);
		
		// Draw lock info
		if (mc.player.isCreative())
		{
			final String lockStr;
			boolean matches = false;
			if (tileEntityIn.hasWorldKey()) {
				WorldKey key = tileEntityIn.getWorldKey();
				lockStr = mc.player.isShiftKeyDown() ? key.toString() : key.toString().substring(0, 8);
				
				final ItemStack held = mc.player.getMainHandItem();
				if ((held.getItem() instanceof WorldKeyItem && key.equals(((WorldKeyItem) held.getItem()).getKey(held)))) {
					matches = true;
				}
			} else {
				lockStr = "No lock info found";
			}
			
			final double drawZ = -.5;
			final float VANILLA_FONT_SCALE = 0.010416667f;
			final int color = (matches)
					? 0x50A0FFA0
					: 0xFFFFFFFF;
			
			matrixStackIn.pushPose();
			matrixStackIn.translate(0, 1, drawZ);
			matrixStackIn.scale(-VANILLA_FONT_SCALE * 2, -VANILLA_FONT_SCALE * 2, VANILLA_FONT_SCALE * 2);
			
			FontRenderer fonter = this.renderer.font;
			fonter.drawInBatch(lockStr, fonter.width(lockStr) / -2, 0, color, false, matrixStackIn.last().pose(), bufferIn, false, 0x0, combinedLightIn);
			matrixStackIn.popPose();
		}
		
		matrixStackIn.popPose();
	}
}
