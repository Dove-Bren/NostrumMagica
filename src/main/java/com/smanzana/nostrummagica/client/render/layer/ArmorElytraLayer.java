package com.smanzana.nostrummagica.client.render.layer;

import javax.annotation.Nonnull;

import com.smanzana.nostrummagica.item.IElytraRenderer;
import com.smanzana.nostrummagica.item.armor.ICapeProvider;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class ArmorElytraLayer<T extends LivingEntity, M extends EntityModel<T>> extends ElytraLayer<T, M> {

	public ArmorElytraLayer(RenderLayerParent<T, M> renderer, EntityModelSet models) {
		super(renderer, models);
	}
	
	@Override
	public boolean shouldRender(ItemStack stackInChest, T entity) {
		// Cancel if a cape is specifically suppressing it
		final boolean flying = entity.isFallFlying();
		ItemStack cape = AetherCloakLayer.ShouldRender(entity);
		if (!flying && !cape.isEmpty() && ((ICapeProvider) cape.getItem()).shouldPreventOtherRenders(entity, cape)) {
			return false;
		}
		
		
		// Check if any equipment uses our interface for things that want to render an elytra
		for (@Nonnull ItemStack stack : entity.getAllSlots()) {
			if (!stack.isEmpty() && stack.getItem() instanceof IElytraRenderer) {
				if (((IElytraRenderer) stack.getItem()).shouldRenderElyta(entity, stack)) {
					return true;
				}
			}
		}
		
		return false;
	}
}
