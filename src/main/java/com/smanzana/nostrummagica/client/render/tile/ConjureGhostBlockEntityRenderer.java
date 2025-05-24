package com.smanzana.nostrummagica.client.render.tile;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import com.smanzana.nostrummagica.block.dungeon.ConjureGhostBlock;
import com.smanzana.nostrummagica.client.gui.SpellComponentIcon;
import com.smanzana.nostrummagica.client.render.NostrumRenderTypes;
import com.smanzana.nostrummagica.spell.EAlteration;
import com.smanzana.nostrummagica.tile.ConjureGhostBlockEntity;
import com.smanzana.nostrummagica.util.Color;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class ConjureGhostBlockEntityRenderer extends BlockEntityRendererBase<ConjureGhostBlockEntity> {

	public ConjureGhostBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}
	
	@Override
	public void render(ConjureGhostBlockEntity tileEntityIn, float partialTicks, PoseStack matrixStackIn,
			MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
		
		final Level level = tileEntityIn.getLevel();
		final BlockPos pos = tileEntityIn.getBlockPos();
		final BlockState state = tileEntityIn.getGhostState();
		final int rawColor = ConjureGhostBlock.MakeBlockColor(tileEntityIn.getBlockState(), level, pos, 0);
		final Camera camera = this.context.getBlockEntityRenderDispatcher().camera;
		
		final float ticks = level.getGameTime() + partialTicks;
		
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		matrixStackIn.pushPose();
		
		if (tileEntityIn.shouldShowHint()) {
			final float distanceProg = 1f - (float) (camera.getPosition().distanceToSqr(pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5) / 9.0);
			final float alpha = distanceProg;
			
			matrixStackIn.pushPose();
			matrixStackIn.translate(.5, 0.5, .5);
			matrixStackIn.mulPose(camera.rotation());
			this.renderSymbol(tileEntityIn, partialTicks, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, alpha);
			matrixStackIn.popPose();
		}
		
		{
			// Fade color back and forth from darker to brighter
			final float glowPeriod = 40f;
			final float colorScale = .5f + Mth.sin(Mth.PI * 2 * (ticks % glowPeriod) / glowPeriod) * .5f;
			final float alpha = .4f;
			Color color = new Color(rawColor).multiply(colorScale, colorScale, colorScale, alpha);
			
			VertexConsumer buffer = bufferIn.getBuffer(NostrumRenderTypes.COLORED_GHOSTBLOCK);
			final BakedModel model = this.context.getBlockRenderDispatcher().getBlockModel(state);
			RenderFuncs.RenderModelWithColor(matrixStackIn, buffer, model, color.toARGB(), combinedLightIn, combinedOverlayIn);
		}
		matrixStackIn.popPose();
	}
	
	protected void renderSymbol(ConjureGhostBlockEntity tileEntityIn, float partialTicks, PoseStack matrixStackIn,
			MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn, float alpha) {
		final float scale = 1f;
		
		matrixStackIn.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
		matrixStackIn.scale(.75f, .75f, 1f);
		matrixStackIn.scale(scale, scale, 1f);
		matrixStackIn.translate(-.5, -.5, 0);
		SpellComponentIcon icon = SpellComponentIcon.get(((ConjureGhostBlock) tileEntityIn.getBlockState().getBlock()).getElement(tileEntityIn.getBlockState()));
		icon.draw(matrixStackIn, bufferIn, combinedLightIn, 1, 1, false, 1f, 1f, 1f, Math.max(.01f, alpha));
		
		icon = SpellComponentIcon.get(EAlteration.CONJURE);
		icon.draw(matrixStackIn, bufferIn, combinedLightIn, 1, 1, false, 0f, 0f, 0f, Math.max(.01f, alpha));
		icon.draw(matrixStackIn, bufferIn, combinedLightIn, 1, 1, false, 1f, 1f, 1f, Math.max(.01f, alpha));
		//RenderFuncs.RenderWorldItem(stack, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);
	}
}
