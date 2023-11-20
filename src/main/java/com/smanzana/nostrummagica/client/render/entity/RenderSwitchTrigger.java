package com.smanzana.nostrummagica.client.render.entity;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.entity.EntitySwitchTrigger;
import com.smanzana.nostrummagica.tiles.SwitchBlockTileEntity;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.util.ResourceLocation;

public class RenderSwitchTrigger extends LivingRenderer<EntitySwitchTrigger, ModelSwitchTrigger> {

	private ModelSwitchTrigger modelOneTime;
	private ModelSwitchTrigger modelTimed;
	private ModelSwitchTrigger modelRepeatable;
	
	public RenderSwitchTrigger(EntityRendererManager renderManagerIn) {
		super(renderManagerIn, null, .1f);
		this.modelOneTime = new ModelSwitchTrigger();
		this.modelTimed = new ModelTimedSwitchTrigger();
		this.modelRepeatable = new ModelRepeatSwitchTrigger();
	}

	@Override
	protected ResourceLocation getEntityTexture(EntitySwitchTrigger entity) {
		return new ResourceLocation(NostrumMagica.MODID,
				"textures/block/spawner.png"
				);
	}
	
	@Override
	protected boolean canRenderName(EntitySwitchTrigger entity) {
		return entity.hasCustomName() || NostrumMagica.instance.proxy.getPlayer().isCreative();
	}
	
	@Override
	protected void renderEntityName(EntitySwitchTrigger entityIn, double x, double y, double z, String name, double distanceSq) {
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
		renderLivingLabel(entityIn, triggerInfo, x, y + .2, z, 64);
		renderLivingLabel(entityIn, extraInfo, x, y, z, 64);
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
	public ModelSwitchTrigger getEntityModel() {
		return getEntityModel(null);
	}
	
	@Override
	public void doRender(EntitySwitchTrigger entity, double x, double y, double z, float entityYaw, float partialTicks) {
		this.entityModel = getEntityModel(entity);
		super.doRender(entity, x, y, z, entityYaw, partialTicks);
	}
	
}
