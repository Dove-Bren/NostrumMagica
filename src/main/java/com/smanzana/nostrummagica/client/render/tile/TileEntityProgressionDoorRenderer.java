package com.smanzana.nostrummagica.client.render.tile;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.EMagicTier;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.gui.MagicTierIcon;
import com.smanzana.nostrummagica.client.gui.SpellComponentIcon;
import com.smanzana.nostrummagica.client.render.NostrumRenderTypes;
import com.smanzana.nostrummagica.spell.component.SpellComponentWrapper;
import com.smanzana.nostrummagica.tile.ProgressionDoorTileEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;

public class TileEntityProgressionDoorRenderer extends TileEntityRenderer<ProgressionDoorTileEntity> {

	public static final ResourceLocation TEX_GEM_LOC = new ResourceLocation(NostrumMagica.MODID, "textures/gui/brass.png");
	public static final ResourceLocation TEX_PLATE_LOC = new ResourceLocation(NostrumMagica.MODID, "textures/block/ceramic_generic.png");
	
	public TileEntityProgressionDoorRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
		super(rendererDispatcherIn);
	}
	
	@Override
	public void render(ProgressionDoorTileEntity tileEntityIn, float partialTicks, MatrixStack matrixStackIn,
			IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
		final Minecraft mc = Minecraft.getInstance();
		final double time = (double)tileEntityIn.getWorld().getGameTime() + partialTicks;
		final INostrumMagic attr = NostrumMagica.getMagicWrapper(mc.player);
		
		matrixStackIn.push();
		matrixStackIn.translate(.5, 1.2, .5);
		
		// Render centered on bottom-center of door, not TE (in case they're different)
		{
			BlockPos pos = tileEntityIn.getPos();
			BlockPos targ = tileEntityIn.getBottomCenterPos();
			matrixStackIn.translate(targ.getX() - pos.getX(), targ.getY() - pos.getY(), targ.getZ() - pos.getZ());
		}
		
		final float rotY = tileEntityIn.getFace().getOpposite().getHorizontalAngle();
		matrixStackIn.rotate(Vector3f.YN.rotationDegrees(rotY));
		
		// Draw lock symbol
		{
			final IVertexBuilder buffer = bufferIn.getBuffer(NostrumRenderTypes.PROGRESSION_DOOR_LOCK);
			
			matrixStackIn.push();
			
			
			final float horizontalRadius = .2f;
			final float verticalRadius = .4f;
			final int points = 4;
			final float depth = .2f;
			final double spinRate = 60.0;
			final float[] color;
			
			if (tileEntityIn.meetsRequirements(mc.player, null)) {
				color = new float[] {0f, 1f, 1f, .8f};
			} else {
				color = new float[] {1f, .3f, .6f, .8f};
			}

			matrixStackIn.translate(0, 0.25, -.3);
			matrixStackIn.rotate(Vector3f.YP.rotationDegrees((float) (360.0 * (time % spinRate) / spinRate)));
			
			final Matrix4f transform = matrixStackIn.getLast().getMatrix();
			for (int i = 0; i < points; i++) {
				double angle = (2*Math.PI) * ((double) i / (double) points);
				
				final float vx1 = (float) (Math.cos(angle) * horizontalRadius);
				final float vy1 = (float) (Math.sin(angle) * verticalRadius);
				final float u1 = (vx1 + (horizontalRadius)) / (horizontalRadius * 2);
				final float v1 = (vy1 + (verticalRadius)) / (verticalRadius * 2);
				
				angle = (2*Math.PI) * ((double) ((i+1)%points) / (double) points);
				
				final float vx2 = (float) (Math.cos(angle) * horizontalRadius);
				final float vy2 = (float) (Math.sin(angle) * verticalRadius);
				final float u2 = (vx2 + (horizontalRadius)) / (horizontalRadius * 2);
				final float v2 = (vy2 + (verticalRadius)) / (verticalRadius * 2);
				
				// For znegative, add in ZN, HIGH ANGLE, LOW ANGLE
				buffer.pos(transform, 0, 0, -depth).color(color[0], color[1], color[2], color[3]).tex(.5f, .5f).lightmap(combinedLightIn).endVertex();
				buffer.pos(transform, vx2, vy2, 0).color(color[0], color[1], color[2], color[3]).tex(u2, v2).lightmap(combinedLightIn).endVertex();
				buffer.pos(transform, vx1, vy1, 0).color(color[0], color[1], color[2], color[3]).tex(u1, v1).lightmap(combinedLightIn).endVertex();
				
				// for zpositive, add in ZP, LOW ANGLE, HIGH ANGLE
				buffer.pos(transform, 0, 0, depth).color(color[0], color[1], color[2], color[3]).tex(.5f, .5f).lightmap(combinedLightIn).endVertex();
				buffer.pos(transform, vx1, vy1, 0).color(color[0], color[1], color[2], color[3]).tex(u1, v1).lightmap(combinedLightIn).endVertex();
				buffer.pos(transform, vx2, vy2, 0).color(color[0], color[1], color[2], color[3]).tex(u2, v2).lightmap(combinedLightIn).endVertex();
			}
			matrixStackIn.pop();
		}
		

		// Draw requirement icons
		if (!tileEntityIn.getRequiredComponents().isEmpty()) {
			final float angleDiff = (float) (Math.PI/(float)tileEntityIn.getRequiredComponents().size());
			float angle = (float) (Math.PI + angleDiff/2);
			
			matrixStackIn.push();
			matrixStackIn.translate(0, 0, -.2);
			
			final Matrix4f transform = matrixStackIn.getLast().getMatrix();
			final Matrix3f normal = matrixStackIn.getLast().getNormal();
			
			for (SpellComponentWrapper comp : tileEntityIn.getRequiredComponents()) {
				boolean has = false;
				SpellComponentIcon icon;
				if (comp.isShape()) {
					icon = SpellComponentIcon.get(comp.getShape());
					has = attr != null && attr.getShapes().contains(comp.getShape());
				} else if (comp.isAlteration()) {
					icon = SpellComponentIcon.get(comp.getAlteration());
					Boolean known = attr == null ? null : attr.getAlterations().get(comp.getAlteration());
					has = known != null && known;
				} else {
					icon = SpellComponentIcon.get(comp.getElement());
					Boolean known = attr == null ? null : attr.getKnownElements().get(comp.getElement());
					has = known != null && known;
				}
				
				if (icon != null && icon.getModelLocation() != null) {
					// Draw background
					float[] color;
					if (has)
						color = new float[] {.4f, .4f, .4f, .4f};
					else
						color = new float[] {8f, .6f, .6f, .8f};
					
					final double wiggleTicks = 100;
					final float imageHalfLength = .15f;
					final float plateHalfLength = imageHalfLength * 2;
					final float radius = .75f;
					
					IVertexBuilder buffer = bufferIn.getBuffer(RenderType.getEntityCutoutNoCull(TEX_PLATE_LOC));
					
					double effAngle = angle + (.04f * Math.cos(2 * Math.PI * (time % wiggleTicks) / wiggleTicks));
					float vx = (float) (Math.cos(effAngle) * radius);
					float vy = (float) (Math.sin(effAngle) * radius);
					
					// +z
					buffer.pos(transform, vx, vy + plateHalfLength, 0.0005f).color(color[0], color[1], color[2], color[3]).tex(0.0f, 1.0f).overlay(combinedOverlayIn).lightmap(combinedLightIn).normal(normal, 0, 0, -1).endVertex();
					buffer.pos(transform, vx + plateHalfLength, vy, 0.0005f).color(color[0], color[1], color[2], color[3]).tex(1.0f, 1.0f).overlay(combinedOverlayIn).lightmap(combinedLightIn).normal(normal, 0, 0, -1).endVertex();
					buffer.pos(transform, vx, vy - plateHalfLength, 0.0005f).color(color[0], color[1], color[2], color[3]).tex(1.0f, 0.0f).overlay(combinedOverlayIn).lightmap(combinedLightIn).normal(normal, 0, 0, -1).endVertex();
					buffer.pos(transform, vx - plateHalfLength, vy, 0.0005f).color(color[0], color[1], color[2], color[3]).tex(0.0f, 0.0f).overlay(combinedOverlayIn).lightmap(combinedLightIn).normal(normal, 0, 0, -1).endVertex();
					
					
					// Draw icon
					buffer = bufferIn.getBuffer(RenderType.getEntityCutoutNoCull(icon.getModelLocation()));
					if (has)
						color = new float[] {1, 1, 1, .2f};
					else
						color = new float[] {1, 1, 1, .8f};
					
					// +z
					buffer.pos(transform, vx + imageHalfLength, vy - imageHalfLength, 0.0f).color(color[0], color[1], color[2], color[3]).tex(0.0f, 1.0f).overlay(combinedOverlayIn).lightmap(combinedLightIn).normal(normal, 0, 0, -1).endVertex();
					buffer.pos(transform, vx - imageHalfLength, vy - imageHalfLength, 0.0f).color(color[0], color[1], color[2], color[3]).tex(1.0f, 1.0f).overlay(combinedOverlayIn).lightmap(combinedLightIn).normal(normal, 0, 0, -1).endVertex();
					buffer.pos(transform, vx - imageHalfLength, vy + imageHalfLength, 0.0f).color(color[0], color[1], color[2], color[3]).tex(1.0f, 0.0f).overlay(combinedOverlayIn).lightmap(combinedLightIn).normal(normal, 0, 0, -1).endVertex();
					buffer.pos(transform, vx + imageHalfLength, vy + imageHalfLength, 0.0f).color(color[0], color[1], color[2], color[3]).tex(0.0f, 0.0f).overlay(combinedOverlayIn).lightmap(combinedLightIn).normal(normal, 0, 0, -1).endVertex();
					
				}
				angle += angleDiff;
			}
			matrixStackIn.pop();
		}
		
		// Draw required level
		if (tileEntityIn.getRequiredLevel() > 0) {
			final double drawZ = -.225;
			final float VANILLA_FONT_SCALE = 0.010416667f;
			String val = "Level: " + tileEntityIn.getRequiredLevel();
			final int color = (attr != null && attr.getLevel() >= tileEntityIn.getRequiredLevel())
					? 0x50A0FFA0
					: 0xFFFFFFFF;
			
			matrixStackIn.push();
			matrixStackIn.translate(0, 1.25, drawZ);
			matrixStackIn.scale(-VANILLA_FONT_SCALE * 2, -VANILLA_FONT_SCALE * 2, VANILLA_FONT_SCALE * 2);
			
			FontRenderer fonter = this.renderDispatcher.fontRenderer;
			fonter.renderString(val, fonter.getStringWidth(val) / -2, 0, color, false, matrixStackIn.getLast().getMatrix(), bufferIn, false, 0x0, combinedLightIn);
			matrixStackIn.pop();
		}
		
		// Draw required tier
		if (tileEntityIn.getRequiredTier() != EMagicTier.LOCKED) {
			matrixStackIn.push();
			matrixStackIn.translate(0, 0.25, -.15);

			final float radius = 1.75f;
			matrixStackIn.scale(radius, radius, 1f);
			
			final float outlineScale = 1.025f;
			matrixStackIn.push();
			matrixStackIn.scale(outlineScale, outlineScale, 1f);
			matrixStackIn.translate(-.5, -.5, 0);
			MagicTierIcon.get(tileEntityIn.getRequiredTier()).draw(matrixStackIn, bufferIn, combinedLightIn, 1, 1, false, 1f, 1f, 1f, 1f);
			matrixStackIn.pop();
			
			matrixStackIn.translate(-.5, -.5, 0);
			MagicTierIcon.get(tileEntityIn.getRequiredTier()).draw(matrixStackIn, bufferIn, combinedLightIn, 1, 1, false, .4f, .4f, .4f, 1f);
			
			matrixStackIn.pop();
		}

		matrixStackIn.pop();
		
	}
}
