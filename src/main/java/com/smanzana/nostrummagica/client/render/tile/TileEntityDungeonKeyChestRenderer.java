package com.smanzana.nostrummagica.client.render.tile;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.render.NostrumRenderTypes;
import com.smanzana.nostrummagica.tile.DungeonKeyChestTileEntity;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;

public class TileEntityDungeonKeyChestRenderer extends TileEntityRenderer<DungeonKeyChestTileEntity> {

	public static final ResourceLocation ICON_COPPER_KEY = new ResourceLocation(NostrumMagica.MODID, "textures/models/copper_key.png");
	public static final ResourceLocation ICON_SILVER_KEY = new ResourceLocation(NostrumMagica.MODID, "textures/models/silver_key.png");
	private static final ResourceLocation ICON_GLOW = new ResourceLocation(NostrumMagica.MODID, "textures/effects/glow_orb.png");
	
	public TileEntityDungeonKeyChestRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
		super(rendererDispatcherIn);
	}
	
	@Override
	public void render(DungeonKeyChestTileEntity tileEntityIn, float partialTicks, MatrixStack matrixStackIn,
			IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
		
		if (tileEntityIn.getOpenTicks() != -1) {
			final boolean large = tileEntityIn.isLarge();
			final float animDuration = large ? 80f : 60f;
			final float elapsedTicks = (float) (int) (tileEntityIn.getWorld().getGameTime() - tileEntityIn.getOpenTicks()) + partialTicks;
			if (elapsedTicks < animDuration) {
				final float radius = .25f;
				final float yExtent = large ? 1.25f : .75f;
				final float animProg = (elapsedTicks / animDuration);
				
				final float yOffset = .25f + (yExtent * Math.min(1f, animProg * 2));
				final float glowMult = 2f + .25f * (float) Math.sin(Math.PI * 2 * animProg * 6);
				
				final float xOffset;
				final float zOffset;
				final Vector3d tileOffset = tileEntityIn.getCenterOffset();
				xOffset = (float) tileOffset.x;
				zOffset = (float) tileOffset.z;
				
				// Force bright light
				combinedLightIn = RenderFuncs.BrightPackedLight;
				
				final ResourceLocation icon = large
						? ICON_SILVER_KEY
						: ICON_COPPER_KEY;
				
				matrixStackIn.push();
				matrixStackIn.translate(xOffset, yOffset, zOffset);
				IVertexBuilder buffer = bufferIn.getBuffer(NostrumRenderTypes.GetBlendedEntity(ICON_GLOW));
				RenderFuncs.renderSpaceQuadFacingCamera(matrixStackIn, buffer, this.renderDispatcher.renderInfo, radius * glowMult, combinedLightIn, combinedOverlayIn, 1f, 1f, .5f, 1f);
				buffer = bufferIn.getBuffer(RenderType.getEntityCutout(icon));
				RenderFuncs.renderSpaceQuadFacingCamera(matrixStackIn, buffer, this.renderDispatcher.renderInfo, radius, combinedLightIn, combinedOverlayIn, 1f, 1f, 1f, 1f);
				matrixStackIn.pop();
			}
		}
	}
}
