package com.smanzana.nostrummagica.client.render.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.smanzana.nostrummagica.client.model.ModelCursedGlass;
import com.smanzana.nostrummagica.entity.SwitchTriggerEntity;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.tile.CursedGlassTileEntity;
import com.smanzana.nostrummagica.tile.SwitchBlockTileEntity;
import com.smanzana.nostrummagica.util.ColorUtil;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.block.Blocks;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class RenderCursedGlassTrigger extends RenderSwitchTrigger {
	
	private /*final*/ ModelCursedGlass model;
	
	public RenderCursedGlassTrigger(EntityRendererManager renderManagerIn) {
		super(renderManagerIn);
		this.model = new ModelCursedGlass();
	}

	@Override
	public ResourceLocation getEntityTexture(SwitchTriggerEntity entity) {
		return super.getEntityTexture(entity);
	}
	
	@Override
	protected boolean canRenderName(SwitchTriggerEntity entity) {
		return super.canRenderName(entity);
	}
	
	@Override
	protected void renderName(SwitchTriggerEntity entityIn, ITextComponent displayNameIn, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
		super.renderName(entityIn, displayNameIn, matrixStackIn, bufferIn, packedLightIn);
		
		final SwitchBlockTileEntity raw = entityIn.getLinkedTileEntity();
		if (raw == null || !(raw instanceof CursedGlassTileEntity)) {
			return;
		}
		
		final CursedGlassTileEntity te = (CursedGlassTileEntity) raw;
		renderLivingLabel(entityIn, String.format("Requires %.2f damage", te.getRequiredDamage()), matrixStackIn, bufferIn, packedLightIn, .4f);
		final EMagicElement elem = te.getRequiredElement();
		if (elem != null) {
			renderLivingLabel(entityIn, String.format("Requires %s element", te.getRequiredElement().getName()), matrixStackIn, bufferIn, packedLightIn, .6f);
		}
	}
	
	@Override
	protected float getAnimateTicks(SwitchTriggerEntity entityIn, float partialTicks) {
		final SwitchBlockTileEntity raw = entityIn.getLinkedTileEntity();
		if (raw == null || !(raw instanceof CursedGlassTileEntity)) {
			return 0f;
		}
		final CursedGlassTileEntity te = (CursedGlassTileEntity) raw;
		if (!te.isBroken()) {
			return 0f; // frozen
		} else {
			return super.getAnimateTicks(entityIn, partialTicks);
		}
	}
	
	@Override
	protected boolean shouldRenderSwitch(SwitchTriggerEntity entityIn) {
		final SwitchBlockTileEntity raw = entityIn.getLinkedTileEntity();
		if (raw == null || !(raw instanceof CursedGlassTileEntity)) {
			return false;
		}
		final CursedGlassTileEntity te = (CursedGlassTileEntity) raw;
		return !te.isNoSwitch();
	}
	
	@Override
	public void render(SwitchTriggerEntity entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
		final SwitchBlockTileEntity raw = entityIn.getLinkedTileEntity();
		if (raw == null || !(raw instanceof CursedGlassTileEntity)) {
			return;
		}
		final CursedGlassTileEntity te = (CursedGlassTileEntity) raw;
		
		super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn); // Trigger itself and nameplate
		
		// Render damage indicator
		if (!te.isBroken()) {
			model.render(matrixStackIn, bufferIn.getBuffer(RenderTypeLookup.func_239220_a_(Blocks.AIR.getDefaultState(), false)), packedLightIn, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, 1f);
			
			final float[] color = ColorUtil.ARGBToColor(te.getRequiredElement() == null ? 0xFFFFFFFF : te.getRequiredElement().getColor());
			final float glowPeriod = 20 * 3;
			final float glowProg = ((entityIn.ticksExisted + partialTicks) % glowPeriod) / glowPeriod;
			
			final float glow = .5f + (.25f * (float) Math.sin(glowProg * Math.PI * 2));
			
			model.renderDecal(matrixStackIn, bufferIn, packedLightIn, OverlayTexture.NO_OVERLAY, color[0], color[1], color[2], color[3] * glow);
			
			matrixStackIn.push();
			matrixStackIn.translate(0, 1.5, 0);
			matrixStackIn.scale(3f, 3f, 3f);
			matrixStackIn.scale(.99f, .99f, .99f);
			
			final float damageProg =  te.getDamageProgress(partialTicks);
			
			// I think vanilla supports 10 breaking progress indicators
			// ModelBakery.DESTROY_RENDER_TYPES.get(k3)
			if (damageProg > 0f) {
				final int renderIdx = (int) Math.max(0, Math.min(9, damageProg * 10));
				IVertexBuilder buffer = bufferIn.getBuffer(ModelBakery.DESTROY_RENDER_TYPES.get(renderIdx));
				RenderFuncs.drawUnitCube(matrixStackIn, buffer, packedLightIn, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, 1f);
			}
			
			matrixStackIn.pop();
		}
	}
	
}
