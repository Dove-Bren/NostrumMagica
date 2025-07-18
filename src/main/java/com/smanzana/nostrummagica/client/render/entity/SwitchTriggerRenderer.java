package com.smanzana.nostrummagica.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.model.RepeatSwitchTriggerModel;
import com.smanzana.nostrummagica.client.model.SwitchTriggerModel;
import com.smanzana.nostrummagica.client.model.TimedSwitchTriggerModel;
import com.smanzana.nostrummagica.client.render.NostrumRenderTypes;
import com.smanzana.nostrummagica.entity.SwitchTriggerEntity;
import com.smanzana.nostrummagica.tile.SwitchBlockTileEntity;
import com.smanzana.nostrummagica.tile.SwitchBlockTileEntity.SwitchHitType;
import com.smanzana.nostrummagica.tile.SwitchBlockTileEntity.SwitchTriggerType;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class SwitchTriggerRenderer extends LivingEntityRenderer<SwitchTriggerEntity, SwitchTriggerModel> {

	private static final double spinIdle = 3.0; // seconds per turn
	private static final double spinActivated = 1.0;

	private SwitchTriggerModel modelOneTime;
	private SwitchTriggerModel modelTimed;
	private SwitchTriggerModel modelRepeatable;
	
	public SwitchTriggerRenderer(EntityRendererProvider.Context renderManagerIn) {
		super(renderManagerIn, null, .1f);
		this.modelOneTime = new SwitchTriggerModel();
		this.modelTimed = new TimedSwitchTriggerModel();
		this.modelRepeatable = new RepeatSwitchTriggerModel();
	}

	@Override
	public ResourceLocation getTextureLocation(SwitchTriggerEntity entity) {
		return new ResourceLocation(NostrumMagica.MODID,
				"textures/block/spawner.png"
				);
	}
	
	@Override
	protected boolean shouldShowName(SwitchTriggerEntity entity) {
		return entity.hasCustomName() || NostrumMagica.Proxy.getPlayer().isCreative();
	}
	
	@Override
	protected void renderNameTag(SwitchTriggerEntity entityIn, Component displayNameIn, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
		final String triggerInfo;
		final String extraInfo;
		SwitchBlockTileEntity te = entityIn.getLinkedTileEntity();
		if (te != null) {
			if (te.getOffset() != null) {
				triggerInfo = te.getOffset().toString();
			} else {
				triggerInfo = "No Offset";
			}
			
			switch (te.getSwitchTriggerType()) {
			case ONE_TIME:
			case REPEATABLE:
			default:
				extraInfo = "";
				break;
			case TIMED:
				extraInfo = String.format("%.1f seconds", (float) ( (double) te.getTotalCooldownTicks() / 20.0 ));
				break;
			}
		} else {
			triggerInfo = "Missing TileEntity";
			extraInfo = "";
		}
		renderLivingLabel(entityIn, triggerInfo, matrixStackIn, bufferIn, packedLightIn, .2f);
		renderLivingLabel(entityIn, extraInfo, matrixStackIn, bufferIn, packedLightIn, 0);
	}
	
	protected void renderLivingLabel(Entity entityIn, String label, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, float yOffset) {
		RenderFuncs.drawNameplate(matrixStackIn, bufferIn, entityIn, label, this.getFont(), packedLightIn, yOffset, this.entityRenderDispatcher.camera);
	}
	
	public SwitchTriggerModel getEntityModel(SwitchTriggerEntity trigger) {
		
		if (trigger != null && trigger.getLinkedTileEntity() != null) // let null fall to fallback case after switch
		switch (trigger.getLinkedTileEntity().getSwitchTriggerType()) {
		case ONE_TIME:
			return this.modelOneTime;
		case REPEATABLE:
			return this.modelRepeatable;
		case TIMED:
			return this.modelTimed;
		}
		
		return this.modelOneTime;
	}
	
	protected float getAnimateTicks(SwitchTriggerEntity entityIn, float partialTicks) {
		return entityIn.level.getGameTime() + partialTicks;
	}
	
	protected boolean shouldRenderSwitch(SwitchTriggerEntity entityIn) {
		return true;
	}
	
	@Override
	public void render(SwitchTriggerEntity entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
		if (shouldRenderSwitch(entityIn)) {
			if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.RenderLivingEvent.Pre<>(entityIn, this, partialTicks, matrixStackIn, bufferIn, packedLightIn))) return;
			this.model = getEntityModel(entityIn);
			final SwitchBlockTileEntity te = entityIn.getLinkedTileEntity();
			final boolean magic = te == null ? false : te.getSwitchHitType() == SwitchHitType.MAGIC;
			final boolean triggered = te != null && te.isTriggered();
			final float saturation = !triggered ? 1f : .4f;
			final float red = magic ? saturation * .2f : saturation * 1f;
			final float green = magic ? saturation * .4f : saturation * 1f;
			final float blue = magic ?  saturation * 1f : saturation * 0f;
			final float alpha = .8f;
			
			final float spinAngle; // rotate around Y axis
			final float turnAngle; // rotate around Z axis
			final boolean isTimed = te != null && te.getSwitchTriggerType() == SwitchTriggerType.TIMED;
	
			final float time = getAnimateTicks(entityIn, partialTicks);
			if (isTimed) {
				// Timed flips on .5 second intervals
				final float period = 10f; // half a second
				
				// Want to make sure final .5 second interval ends with a flip such that the flip finishes when time is up.
				// Also want .5 second of just standing still between flips. Count from the back so that last one has a flip
				
				// for duration 30, we want
				// [0-9] FLIP
				// [10-19] no flip
				// [20-29] FLIP
				final long timeTillEnd = te.getCurrentCooldownTicks() - 1;
				if (timeTillEnd % (2 * period) < period) { // mod by 20 and see if it's in the bottom 10
					final float rotTicks = Math.max(0, timeTillEnd - partialTicks); // For the first tick, this is 0 - partial so negative
					
					// FLIP
					turnAngle = 180f * ((rotTicks % period) / period);
					
				} else {
					turnAngle = 0f;
				}
				spinAngle = 0f;
			} else {
				// Non-timed spin around y axis based on whether they're activated or not
				final float period = (float) (20 * (triggered ? spinActivated : spinIdle));
				spinAngle = 360f * ((time % period) / period);
				turnAngle = 0f;
			}
			
			final float bobAngle = (float) (2 * Math.PI * (time % 60 / 60));
			final float verticalBob = (float) (Math.sin(bobAngle) * .1f);
			
			// Render model to two render types so that cage overlays on the regular model
			//IVertexBuilder buffer = VertexBuilderUtils.newDelegate(bufferIn.getBuffer(NostrumRenderTypes.SWITCH_TRIGGER_BASE), bufferIn.getBuffer(NostrumRenderTypes.SWITCH_TRIGGER_CAGE));
			// This doesn't work because the buffers happen to be the same under the hood, and causes an exception
			// So isntead, render twice
			
			matrixStackIn.pushPose();
	
			
			// also bob up and down
			matrixStackIn.translate(0, verticalBob, 0);
			
			
			matrixStackIn.translate(0, 1f, 0); // Should this be earlier?
			matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(spinAngle));
			matrixStackIn.mulPose(Vector3f.ZP.rotationDegrees(turnAngle)); // GlStateManager.rotated(angle, 1, 0, 1); WAS x and z?
	
			VertexConsumer baseBuffer = bufferIn.getBuffer(NostrumRenderTypes.SWITCH_TRIGGER_BASE);
			model.renderToBuffer(matrixStackIn, baseBuffer, packedLightIn, OverlayTexture.NO_OVERLAY, red, green, blue, alpha);
			VertexConsumer cageBuffer = bufferIn.getBuffer(NostrumRenderTypes.SWITCH_TRIGGER_CAGE);
			model.renderToBuffer(matrixStackIn, cageBuffer, packedLightIn, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, 1f);
			matrixStackIn.popPose();
			if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.RenderLivingEvent.Post<>(entityIn, this, partialTicks, matrixStackIn, bufferIn, packedLightIn))) return;
		}
		
		//super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn); // Nameplate
		{
			net.minecraftforge.client.event.RenderNameplateEvent renderNameplateEvent = new net.minecraftforge.client.event.RenderNameplateEvent(entityIn, entityIn.getDisplayName(), this, matrixStackIn, bufferIn, packedLightIn, partialTicks);
			net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(renderNameplateEvent);
			if (renderNameplateEvent.getResult() != net.minecraftforge.eventbus.api.Event.Result.DENY && (renderNameplateEvent.getResult() == net.minecraftforge.eventbus.api.Event.Result.ALLOW || this.shouldShowName(entityIn))) {
				this.renderNameTag(entityIn, renderNameplateEvent.getContent(), matrixStackIn, bufferIn, packedLightIn);
			}
		}
	}
	
}
