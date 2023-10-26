package com.smanzana.nostrummagica.client.render.entity;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.entity.EntitySwitchTrigger;
import com.smanzana.nostrummagica.tiles.SwitchBlockTileEntity;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.util.ResourceLocation;

public class RenderSwitchTrigger extends LivingRenderer<EntitySwitchTrigger, ModelSwitchTrigger> {

	public RenderSwitchTrigger(EntityRendererManager renderManagerIn) {
		super(renderManagerIn, new ModelSwitchTrigger(), .1f);
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
		final String info;
		SwitchBlockTileEntity te = entityIn.getLinkedTileEntity();
		if (te != null) {
			if (te.getOffset() != null) {
				info = te.getOffset().toString();
			} else {
				info = "No Offset";
			}
		} else {
			 info = "Missing TileEntity";
		}
		renderLivingLabel(entityIn, info, x, y, z, 64);
	}
	
}
