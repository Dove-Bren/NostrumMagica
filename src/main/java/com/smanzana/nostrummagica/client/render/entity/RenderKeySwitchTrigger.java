package com.smanzana.nostrummagica.client.render.entity;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.entity.EntityKeySwitchTrigger;
import com.smanzana.nostrummagica.items.WorldKeyItem;
import com.smanzana.nostrummagica.tiles.KeySwitchBlockTileEntity;
import com.smanzana.nostrummagica.world.NostrumKeyRegistry.NostrumWorldKey;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class RenderKeySwitchTrigger extends LivingRenderer<EntityKeySwitchTrigger, ModelKeySwitchTrigger> {

	public RenderKeySwitchTrigger(EntityRendererManager renderManagerIn) {
		super(renderManagerIn, new ModelKeySwitchTrigger(), .1f);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityKeySwitchTrigger entity) {
		return new ResourceLocation(NostrumMagica.MODID,
				"textures/block/key_cage.png"
				);
	}
	
	@Override
	protected boolean canRenderName(EntityKeySwitchTrigger entity) {
		return entity.hasCustomName() || NostrumMagica.instance.proxy.getPlayer().isCreative();
	}
	
	@Override
	protected void renderEntityName(EntityKeySwitchTrigger entityIn, double x, double y, double z, String name, double distanceSq) {
		final Minecraft mc = Minecraft.getInstance();
		final double ticks = entityIn.world.getGameTime();
		final String info;
		boolean matches = false;
		KeySwitchBlockTileEntity te = (KeySwitchBlockTileEntity) entityIn.getLinkedTileEntity();
		if (te != null) {
			if (te.getOffset() != null) {
				NostrumWorldKey key = te.getWorldKey();
				info = mc.player.isSneaking() ? key.toString() : key.toString().substring(0, 8);
				
				final ItemStack held = mc.player.getHeldItemMainhand();
				if ((held.getItem() instanceof WorldKeyItem && key.equals(((WorldKeyItem) held.getItem()).getKey(held)))) {
					matches = true;
				}
			} else {
				info = "No lock info found";
			}
		} else {
			 info = "Missing TileEntity";
		}
		
		float yOffset = 0;
		if (matches) {
			final double matchWigglePeriod = 20;
			final double matchWiggleProg = 1 - ((ticks % matchWigglePeriod) / matchWigglePeriod);
			yOffset += (float) (.05 * Math.sin(2 * Math.PI * matchWiggleProg));
		}
		renderLivingLabel(entityIn, info, x, y + yOffset, z, 64);
	}
	
	@Override
	protected void renderModel(EntityKeySwitchTrigger entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor) {
		super.renderModel(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
	}
	
}
