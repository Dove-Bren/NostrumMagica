package com.smanzana.nostrummagica.client.render.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mojang.blaze3d.vertex.VertexBuilderUtils;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.render.NostrumRenderTypes;
import com.smanzana.nostrummagica.entity.EntitySwitchTrigger;
import com.smanzana.nostrummagica.tiles.SwitchBlockTileEntity;
import com.smanzana.nostrummagica.tiles.SwitchBlockTileEntity.SwitchHitType;
import com.smanzana.nostrummagica.tiles.SwitchBlockTileEntity.SwitchTriggerType;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;

public class RenderSwitchTrigger extends EntityRenderer<EntitySwitchTrigger> {

	private static final double spinIdle = 3.0; // seconds per turn
	private static final double spinActivated = 1.0;

	private ModelSwitchTrigger modelOneTime;
	private ModelSwitchTrigger modelTimed;
	private ModelSwitchTrigger modelRepeatable;
	
	public RenderSwitchTrigger(EntityRendererManager renderManagerIn) {
		super(renderManagerIn);
		this.modelOneTime = new ModelSwitchTrigger();
		this.modelTimed = new ModelTimedSwitchTrigger();
		this.modelRepeatable = new ModelRepeatSwitchTrigger();
	}

	@Override
	public ResourceLocation getEntityTexture(EntitySwitchTrigger entity) {
		return new ResourceLocation(NostrumMagica.MODID,
				"textures/block/spawner.png"
				);
	}
	
	@Override
	protected boolean canRenderName(EntitySwitchTrigger entity) {
		return entity.hasCustomName() || NostrumMagica.instance.proxy.getPlayer().isCreative();
	}
	
	@Override
	protected void renderName(EntitySwitchTrigger entityIn, ITextComponent displayNameIn, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
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
	
	protected void renderLivingLabel(Entity entityIn, String label, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, float yOffset) {
		final Minecraft mc = Minecraft.getInstance();
		boolean flag = !entityIn.isDiscrete();
		float y = entityIn.getHeight() + 0.5F;
		
		matrixStackIn.push();
		matrixStackIn.translate(0.0D, (double)y + yOffset, 0.0D);
		matrixStackIn.rotate(this.renderManager.getCameraOrientation());
		matrixStackIn.scale(-0.025F, -0.025F, 0.025F);
		
		Matrix4f matrix4f = matrixStackIn.getLast().getMatrix();
		float f1 = mc.gameSettings.getTextBackgroundOpacity(0.25F);
		int j = (int)(f1 * 255.0F) << 24;
		FontRenderer fontrenderer = this.getFontRendererFromRenderManager();
		float f2 = (float)(-fontrenderer.getStringWidth(label) / 2);
		fontrenderer.renderString(label, f2, 0, 553648127, false, matrix4f, bufferIn, flag, j, packedLightIn);
		if (flag) {
			fontrenderer.renderString(label, f2, 0, -1, false, matrix4f, bufferIn, false, 0, packedLightIn);
		}
		
		matrixStackIn.pop();
	}
	
	public ModelSwitchTrigger getEntityModel(EntitySwitchTrigger trigger) {
		
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
	
	@Override
	public void render(EntitySwitchTrigger entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
		// Note: This used to have all the LivingRenderer offsets and stuff baked in and now it doesn't. Likely have to
		// translate and rotate
		final ModelSwitchTrigger model = getEntityModel(entityIn);
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

		final float time = entityIn.world.getGameTime() + partialTicks;
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
		IVertexBuilder buffer = VertexBuilderUtils.newDelegate(bufferIn.getBuffer(NostrumRenderTypes.SWITCH_TRIGGER_BASE), bufferIn.getBuffer(NostrumRenderTypes.SWITCH_TRIGGER_CAGE));
		
		matrixStackIn.push();

		matrixStackIn.rotate(Vector3f.YP.rotationDegrees(spinAngle));
		
		// also bob up and down
		matrixStackIn.translate(0, verticalBob, 0);
		
		matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(turnAngle)); // GlStateManager.rotated(angle, 1, 0, 1); WAS x and z?
		
		matrixStackIn.translate(0, .6f, 0); // Should this be earlier?
		
		model.render(matrixStackIn, buffer, packedLightIn, OverlayTexture.NO_OVERLAY, red, green, blue, alpha);
		super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn); // Nameplate
		matrixStackIn.pop();
	}
	
}
